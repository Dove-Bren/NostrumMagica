package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.widget.LabeledWidget;
import com.smanzana.nostrummagica.client.gui.widget.ObscurableWidget;
import com.smanzana.nostrummagica.client.gui.widget.ParentWidget;
import com.smanzana.nostrummagica.client.gui.widget.ScrollbarWidget;
import com.smanzana.nostrummagica.client.gui.widget.TextWidget;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.log.SpellLogEffectLine;
import com.smanzana.nostrummagica.spell.log.SpellLogEffectSummary;
import com.smanzana.nostrummagica.spell.log.SpellLogEntry;
import com.smanzana.nostrummagica.spell.log.SpellLogModifier;
import com.smanzana.nostrummagica.spell.log.SpellLogStageSummary;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class SpellLogSubScreen implements IInfoSubScreen {

	private SpellLogEntry log;
	
	private final List<StageButton> stageButtons;
	private final List<List<ObscurableWidget>> stageSubWidgets;
	
	public SpellLogSubScreen(SpellLogEntry log) {
		this.log = log;
		
		this.stageButtons = new ArrayList<>(log.getStageIndexCount());
		this.stageSubWidgets = new ArrayList<>(log.getStageIndexCount());
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		
		matrixStackIn.push();
		matrixStackIn.translate(x, y, 0);
		
		String title = this.log.getSpell().getName();
		int len = mc.fontRenderer.getStringWidth(title);
		mc.fontRenderer.drawStringWithShadow(matrixStackIn, title, width / 2 + (-len / 2), 0, 0xFFFFFFFF);
		
		matrixStackIn.pop();
	}
	
	@Override
	public void drawForeground(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, 100);
		for (List<ObscurableWidget> widgets : stageSubWidgets) {
			for (Widget widget : widgets) {
				widget.renderToolTip(matrixStackIn, mouseX, mouseY);
			}
		}
		matrixStackIn.pop();
	}
	
	protected List<ObscurableWidget> generateStageWidgets(int idx, SpellLogStageSummary summary, int x, int y, int width, int height) {
		final List<ObscurableWidget> widgets = new ArrayList<>();
		
		final Minecraft mc = Minecraft.getInstance();
		final ITextComponent title = (summary.getShape() == null ? new StringTextComponent("Start") : summary.getShape().getDisplayName());
		widgets.add(new TextWidget(mc.currentScreen, title,
				x + (width / 2), y + GUI.STAGE_SECTION_VOFFSET, 1, 1)
				.center());
		
		
		final String entCount = " " + summary.getAffectedEntCounts().size();
		List<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(new StringTextComponent("Entities:"));
		summary.getAffectedEntCounts().entrySet().stream().map(e -> e.getKey().getName().copyRaw().appendString(" x" + e.getValue())).forEachOrdered(tooltip::add);
		final LabeledWidget entIcon = new LabeledWidget(mc.currentScreen, new AffectedEntsLabel(GUI.AFFECTED_LEN, GUI.AFFECTED_LEN), new LabeledWidget.TextValue(() -> entCount),
				x + (width / 2) - (GUI.AFFECTED_LEN + 14), y + GUI.STAGE_SECTION_VOFFSET + 12,
				10, 10)
				.tooltip(tooltip);
		
		final String locCount = " " + summary.getAffectedLocCounts().size();
		tooltip = new ArrayList<>();
		tooltip.add(new StringTextComponent("Locations:"));
		summary.getAffectedLocCounts().entrySet().stream().map(e -> new StringTextComponent(String.format("(%d, %d, %d) x%d", e.getKey().hitBlockPos.getX(), e.getKey().hitBlockPos.getY(), e.getKey().hitBlockPos.getZ(), e.getValue()))).forEachOrdered(tooltip::add);
		final LabeledWidget locIcon = new LabeledWidget(mc.currentScreen, new AffectedLocsLabel(GUI.AFFECTED_LEN, GUI.AFFECTED_LEN), new LabeledWidget.TextValue(() -> locCount),
				x + (width / 2) + 10, y + GUI.STAGE_SECTION_VOFFSET + 12,
				10, 10)
				.tooltip(tooltip);
		
		if (summary.getAffectedLocCounts().isEmpty() && !summary.getAffectedEntCounts().isEmpty()) {
			entIcon.x = x + ((width-GUI.AFFECTED_LEN) / 2); 
			widgets.add(entIcon);
		} else if (!summary.getAffectedLocCounts().isEmpty() && summary.getAffectedEntCounts().isEmpty()) {
			locIcon.x = x + ((width-GUI.AFFECTED_LEN) / 2); 
			widgets.add(locIcon);
		} else {
			widgets.add(entIcon);
			widgets.add(locIcon);
		}
		
		if (summary.hasEffects()) {
			boolean shift = false;
			if (summary.getTotalDamage() > 0 || summary.getTotalHeal() == 0f) {
				final String damageText = String.format(" %.1f", summary.getTotalDamage());
				widgets.add(new LabeledWidget(mc.currentScreen, new TotalDamageLabel(16, 16), new LabeledWidget.TextValue(() -> damageText), x + 10, y + GUI.STAGE_SECTION_VOFFSET + 20, 10, 10)
						.tooltip(new StringTextComponent("Total Damage")));
				shift = true;
			}
			if (summary.getTotalHeal() > 0f) {
				final String healText = String.format(" +%.1f", summary.getTotalHeal());
				widgets.add(new LabeledWidget(mc.currentScreen, new TotalHealLabel(16, 16), new LabeledWidget.TextValue(() -> healText), x + 10 + (shift ? 50 : 0), y + GUI.STAGE_SECTION_VOFFSET + 20, 10, 10)
						.tooltip(new StringTextComponent("Total Healing")));
			}
			
			// For now, just find first entity stage effect and display it?
			Optional<Entry<LivingEntity, SpellLogEffectSummary>> first = summary.getStages().stream().map(stage -> stage.getAffectedEnts()).flatMap(m -> m.entrySet().stream()).filter(e -> e.getValue() != null).findFirst();
			if (first.isPresent()) {
				//first.get().getValue().
				final EffectSummaryWidget summaryWidget = new EffectSummaryWidget(first.get().getKey(), first.get().getValue(),
						x + GUI.EFFECT_HOFFSET, y + GUI.EFFECT_VOFFSET, width - (GUI.EFFECT_HOFFSET * 2), height - (GUI.EFFECT_VOFFSET + 5)
						);
				widgets.add(summaryWidget);
				
				if (summaryWidget.getHeightRealms() > height - (GUI.EFFECT_VOFFSET + 5)) {
					summaryWidget.setHeight(height - (GUI.EFFECT_VOFFSET + 5));
					summaryWidget.setHasScrollbar();
				}
			}
		} else {
			
		}
		return widgets;
	}

	@Override
	public Collection<Widget> getWidgets(int x, int y, int width, int height) {
		List<Widget> widgets = new ArrayList<>();
		
		this.stageButtons.clear();
		
		final int stageButtonLen = GUI.STAGE_BUTTON_SIZE;
		final int stageButtonMargin = 8;
		
		final int stageRowLen = (stageButtonLen * log.getStageIndexCount()) + (stageButtonMargin * (log.getStageIndexCount() - 1));
		final int stageButtonStartX = x + ((width - stageRowLen) / 2);
		for (int i = 0; i < log.getStageIndexCount(); i++) {
			stageButtons.add(new StageButton(this, i, log.getStages(i),
					stageButtonStartX + i * stageButtonLen + i * stageButtonMargin, y + GUI.STAGE_BUTTON_YOFFSET,
					stageButtonLen, stageButtonLen));
		}
		widgets.addAll(this.stageButtons);
		
		this.stageSubWidgets.clear();
		for (int i = 0; i < log.getStageIndexCount(); i++) {
			stageSubWidgets.add(generateStageWidgets(i, log.getStages(i), x, y, width, height));
		}
		this.stageSubWidgets.forEach(l -> widgets.addAll(l));

		setStage(0);
		return widgets;
	}
	
	protected void setStage(int idx) {
		for (int i = 0; i < this.stageButtons.size(); i++) {
			final boolean active = (i == idx);
			this.stageButtons.get(i).setActive(active);
			this.stageSubWidgets.get(i).forEach(w -> w.setHidden(!active));
		}
	}
	
	private static final class Texture {
		private Texture() {}
		
		public static final ResourceLocation TEXT = NostrumMagica.Loc("textures/gui/spelllog.png");
		
		private static final int WIDTH = 128;
		private static final int HEIGHT = 128;
		
		private static final int SHAPE_ICON_HOFFSET = 64;
		private static final int SHAPE_ICON_VOFFSET = 0;
		private static final int SHAPE_ICON_WIDTH = 32;
		private static final int SHAPE_ICON_HEIGHT = 32;
		
		private static final int START_ICON_HOFFSET = 96;
		private static final int START_ICON_VOFFSET = 0;
		private static final int START_ICON_WIDTH = 32;
		private static final int START_ICON_HEIGHT = 32;
		
		private static final int AFFECT_ICON_HOFFSET = 64;
		private static final int AFFECT_ICON_VOFFSET = 32;
		private static final int AFFECT_ICON_WIDTH = 32;
		private static final int AFFECT_ICON_HEIGHT = 32;
		
		private static final int DAMAGE_ICON_HOFFSET = 0;
		private static final int DAMAGE_ICON_VOFFSET = 0;
		private static final int DAMAGE_ICON_WIDTH = 32;
		private static final int DAMAGE_ICON_HEIGHT = 32;
		
		private static final int DAMAGE_TOT_ICON_HOFFSET = 32;
		private static final int DAMAGE_TOT_ICON_VOFFSET = 0;
		private static final int DAMAGE_TOT_ICON_WIDTH = 32;
		private static final int DAMAGE_TOT_ICON_HEIGHT = 32;
		
		private static final int HEAL_ICON_HOFFSET = 0;
		private static final int HEAL_ICON_VOFFSET = 32;
		private static final int HEAL_ICON_WIDTH = 32;
		private static final int HEAL_ICON_HEIGHT = 32;
		
		private static final int HEAL_TOT_ICON_HOFFSET = 32;
		private static final int HEAL_TOT_ICON_VOFFSET = 32;
		private static final int HEAL_TOT_ICON_WIDTH = 32;
		private static final int HEAL_TOT_ICON_HEIGHT = 32;
	}
	
	private static final class GUI {
		private GUI() {}
		
		private static final int AFFECTED_LEN = 16;
		
		private static final int STAGE_BUTTON_YOFFSET = 12;
		private static final int STAGE_BUTTON_SIZE = 24;
		
		private static final int STAGE_SECTION_VOFFSET = STAGE_BUTTON_YOFFSET + STAGE_BUTTON_SIZE + 4;

		private static final int EFFECT_HOFFSET = 24;
		private static final int EFFECT_VOFFSET = STAGE_SECTION_VOFFSET + 40;
	}
	
	private static class StageButton extends AbstractButton {
		
		private final SpellLogSubScreen screen;
		private final int idx;
		private final SpellLogStageSummary stageSummary;
		private boolean active;

		public StageButton(SpellLogSubScreen screen, int idx, SpellLogStageSummary stageSummary, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.screen = screen;
			this.idx = idx;
			this.stageSummary = stageSummary;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final float tint = (this.isHovered() ? .8f : 1f) * (active ? .8f : 1f);
			
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.SHAPE_ICON_HOFFSET, Texture.SHAPE_ICON_VOFFSET, Texture.SHAPE_ICON_WIDTH, Texture.SHAPE_ICON_HEIGHT,
					width, height, Texture.WIDTH, Texture.HEIGHT,
					tint, tint, tint, 1f);
			
			if (this.stageSummary.getShape() != null) {
				SpellComponentIcon.get(this.stageSummary.getShape()).draw(matrixStackIn, x + 1, y + 1, this.width - 2, this.height - 2, tint, tint, tint, 1f);
			} else {
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x + 1, y + 1,
						Texture.START_ICON_HOFFSET, Texture.START_ICON_VOFFSET, Texture.START_ICON_WIDTH, Texture.START_ICON_HEIGHT,
						width - 2, height - 2, Texture.WIDTH, Texture.HEIGHT,
						tint, tint, tint, 1f);
			}
			
			if (this.isHovered()) {
				final ITextComponent title = (stageSummary.getShape() == null ? new StringTextComponent("Start") : stageSummary.getShape().getDisplayName());
				final Minecraft mc = Minecraft.getInstance();
				mc.currentScreen.renderTooltip(matrixStackIn, title, mouseX, mouseY);
			}
		}

		@Override
		public void onPress() {
			screen.setStage(this.idx);
		}
		
		public void setActive(boolean active) {
			this.active = active;
		}
	}
	
	private static class AffectedEntsLabel extends LabeledWidget.ComponentIconLabel {
		
		public AffectedEntsLabel(int width, int height) {
			super(SpellComponentIcon.get(NostrumSpellShapes.AtFeet), width-2, height-2);
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.AFFECT_ICON_HOFFSET, Texture.AFFECT_ICON_VOFFSET, Texture.AFFECT_ICON_WIDTH, Texture.AFFECT_ICON_HEIGHT,
					this.width + 2, height + 2, Texture.WIDTH, Texture.HEIGHT);
			
			return super.render(matrixStackIn, x + 1, y + 1, partialTicks, color);
		}
	}
	
	private static class AffectedLocsLabel extends LabeledWidget.ComponentIconLabel {
		
		public AffectedLocsLabel(int width, int height) {
			super(SpellComponentIcon.get(NostrumSpellShapes.Proximity), width-2, height-2);
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.AFFECT_ICON_HOFFSET, Texture.AFFECT_ICON_VOFFSET, Texture.AFFECT_ICON_WIDTH, Texture.AFFECT_ICON_HEIGHT,
					this.width + 2, height + 2, Texture.WIDTH, Texture.HEIGHT);
			
			return super.render(matrixStackIn, x + 1, y + 1, partialTicks, color);
		}
	}
	
	private static class TotalDamageLabel implements LabeledWidget.ILabel {
		
		private final int width;
		private final int height;
		
		public TotalDamageLabel(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.DAMAGE_TOT_ICON_HOFFSET, Texture.DAMAGE_TOT_ICON_VOFFSET, Texture.DAMAGE_TOT_ICON_WIDTH, Texture.DAMAGE_TOT_ICON_HEIGHT,
					this.width, height, Texture.WIDTH, Texture.HEIGHT);
			
			return new Rectangle2d(x, y, width, height);
		}
	}
	
	private static class DamageLabel implements LabeledWidget.ILabel {
		
		private final int width;
		private final int height;
		private final @Nullable EMagicElement element;
		
		public DamageLabel(@Nullable EMagicElement element, int width, int height) {
			this.element = element;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final float colors[] = ColorUtil.ARGBToColor(element.getColor()); // ignore color argument
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.DAMAGE_ICON_HOFFSET, Texture.DAMAGE_ICON_VOFFSET, Texture.DAMAGE_ICON_WIDTH, Texture.DAMAGE_ICON_HEIGHT,
					this.width, height, Texture.WIDTH, Texture.HEIGHT,
					colors[0], colors[1], colors[2], colors[3]);
			
			return new Rectangle2d(x, y, width, height);
		}
	}
	
	private static class TotalHealLabel implements LabeledWidget.ILabel {
		
		private final int width;
		private final int height;
		
		public TotalHealLabel(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.HEAL_TOT_ICON_HOFFSET, Texture.HEAL_TOT_ICON_VOFFSET, Texture.HEAL_TOT_ICON_WIDTH, Texture.HEAL_TOT_ICON_HEIGHT,
					this.width, height, Texture.WIDTH, Texture.HEIGHT);
			
			return new Rectangle2d(x, y, width, height);
		}
	}
	
	private static class HealLabel implements LabeledWidget.ILabel {
		
		private final int width;
		private final int height;
		private final @Nullable EMagicElement element;
		
		public HealLabel(@Nullable EMagicElement element, int width, int height) {
			this.width = width;
			this.height = height;
			this.element = element;
		}
		
		@Override
		public Rectangle2d render(MatrixStack matrixStackIn, int x, int y, float partialTicks, int color) {
			final float colors[] = ColorUtil.ARGBToColor(element.getColor()); // ignore color argument
			
			Minecraft.getInstance().getTextureManager().bindTexture(Texture.TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					Texture.HEAL_ICON_HOFFSET, Texture.HEAL_ICON_VOFFSET, Texture.HEAL_ICON_WIDTH, Texture.HEAL_ICON_HEIGHT,
					this.width, height, Texture.WIDTH, Texture.HEIGHT,
					colors[0], colors[1], colors[2], colors[3]);
			
			return new Rectangle2d(x, y, width, height);
		}
	}
	
	private static class EffectSummaryWidget extends ParentWidget {
		
		private final ScrollbarWidget scrollbar;
		private final List<EffectLineWidget> lineWidgets;
		
		public EffectSummaryWidget(LivingEntity entity, SpellLogEffectSummary summary, int x, int y, int width, int heightIn) {
			super(x, y, width, heightIn, StringTextComponent.EMPTY);
			
			// Reset height and built it as we go
			this.height = 0;
			height += 4; // top margin 
			
			final Minecraft mc = Minecraft.getInstance();
			boolean shift = false;
			if (summary.getTotalDamage() > 0 || summary.getTotalHeal() == 0f) {
				final String damageText = String.format(" %.1f", summary.getTotalDamage());
				this.addChild(new LabeledWidget(mc.currentScreen, new TotalDamageLabel(16, 16), new LabeledWidget.TextValue(() -> damageText), x + 10, y + height, 10, 10)
						.tooltip(new StringTextComponent("Damaged")));
				shift = true;
			}
			if (summary.getTotalHeal() > 0f) {
				final String healText = String.format(" %.1f", summary.getTotalHeal());
				this.addChild(new LabeledWidget(mc.currentScreen, new TotalHealLabel(16, 16), new LabeledWidget.TextValue(() -> healText), x + 10 + (shift ? 40 : 0), y + height, 10, 10)
						.tooltip(new StringTextComponent("Healed")));
			}
			
			final ITextComponent title = entity.getName();
			addChild(new TextWidget(mc.currentScreen, title,
					x + (width / 2), y + height, 1, 1)
					.center());
			height += mc.fontRenderer.FONT_HEIGHT + 12;
			
			lineWidgets = new ArrayList<>(summary.getElements().size());
			for (SpellLogEffectLine line : summary.getElements()) {
				EffectLineWidget child = new EffectLineWidget(line, x + 10, y + height, width - 20, 70);
				child.setBounds(x, y, width, heightIn + 100); // so it can just scroll off end of page
				addChild(child);
				lineWidgets.add(child);
				height += child.getHeightRealms() + 5;
			}
			
			height += 4; // bottom margin
			

			final int scrollWidth = 10;
			this.scrollbar = new ScrollbarWidget(this::setToScroll, x + width - (scrollWidth + 2), y + 2, scrollWidth, height - 4);
			scrollbar.setScrollRate(1f / (summary.getElements().size()-1));
		}
		
		public void setHasScrollbar() {
			this.scrollbar.setHeight(this.height - 4);
			addChild(scrollbar);
			
			for (EffectLineWidget line : lineWidgets) {
				line.setWidth(this.width - 20 - (this.scrollbar.getWidth() + 4));
			}
		}
		
		protected void setToScroll(float scroll) {
			final int idx = Math.round(scroll / (1f / (lineWidgets.size()-1)));
			int heightOffset = 0;
			for (int i = 0; i < lineWidgets.size(); i++) {
				if (i < idx) {
					final EffectLineWidget line = lineWidgets.get(i);
					line.offsetFromStart(-500, -100);
					heightOffset += line.getHeightRealms() + 5;
				} else {
					final EffectLineWidget line = lineWidgets.get(i);
					line.offsetFromStart(0, -heightOffset);
				}
			}
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			RenderFuncs.drawRect(matrixStackIn, x, y, x + width, y + height, 0xFF404040);
		}
		
		@Override
		protected void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			super.renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 100);
			for (Widget widget : this.children) {
				widget.renderToolTip(matrixStackIn, mouseX, mouseY);
			}
			matrixStackIn.pop();
		}
	}
	
	private static class EffectLineWidget extends ParentWidget {
		
		private SpellLogEffectLine line;
		
		public EffectLineWidget(SpellLogEffectLine line, int x, int y, int width, int heightIn) {
			super(x, y, width, heightIn, StringTextComponent.EMPTY);
			this.line = line;
			
			// Reset height and built it as we go
			this.height = 0;
			height += 2; // top margin 

			final Minecraft mc = Minecraft.getInstance();
			this.addChild(new TextWidget(mc.currentScreen, line.getName(), x + 2, y + height, 100, 10)
					.tooltip(line.getDescription()));
			
			if (line instanceof SpellLogEffectLine.Damage) {
				final String damageText = String.format(" %.1f", line.getTotalDamage());
				final @Nullable EMagicElement element = ((SpellLogEffectLine.Damage)line).getElement();
				this.addChild(new LabeledWidget(mc.currentScreen, new DamageLabel(element, 12, 12), new LabeledWidget.TextValue(() -> damageText), x + width - (12 + 40), y + height, 10, 10)
						.tooltip(new StringTextComponent((element == null ? "Raw" : element.getName()) + " Damage")));
			} else if (line instanceof SpellLogEffectLine.Heal) {
				final String healText = String.format(" %.1f", line.getTotalHeal());
				final @Nullable EMagicElement element = ((SpellLogEffectLine.Heal)line).getElement();
				this.addChild(new LabeledWidget(mc.currentScreen, new HealLabel(element, 12, 12), new LabeledWidget.TextValue(() -> healText), x + width - (12 + 40), y + height, 10, 10)
						.tooltip(new StringTextComponent((element == null ? "Raw" : element.getName()) + " Healing")));
			}
			
			height += 12 + 4;
			
			if (line.getModifiers().isEmpty()) {
				this.addChild(new TextWidget(mc.currentScreen, new StringTextComponent("No modifiers"), x + 8, y + height, 100, 10));
			} else {
				for (SpellLogModifier mod : line.getModifiers()) {
					final ModifierWidget modWidget = new ModifierWidget(mod, x + 8, y + height, 100, 10);
					this.addChild(modWidget);
					height += modWidget.getHeightRealms();
				}
			}

			height += 2; // bottom margin
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			RenderFuncs.drawRect(matrixStackIn, x, y, x + width, y + height, line.isHarmful() ? 0xFF804040 : 0xFF404080);
		}
		
		@Override
		protected void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			super.renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 100);
			for (Widget widget : this.children) {
				widget.renderToolTip(matrixStackIn, mouseX, mouseY);
			}
			matrixStackIn.pop();
		}
	}
	
	private static class ModifierWidget extends ParentWidget {
		
		public ModifierWidget(SpellLogModifier modifier, int x, int y, int width, int heightIn) {
			super(x, y, width, heightIn, StringTextComponent.EMPTY);
			
			final Minecraft mc = Minecraft.getInstance();
			
			// Reset height and built it as we go
			this.height = 0;
			height += 1; // top margin 

			this.addChild(new TextWidget(mc.currentScreen, modifier.getDescription(), x, y + height, 100, 10));
			height += mc.fontRenderer.FONT_HEIGHT;

			height += 1; // bottom margin
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			;
		}
		
		@Override
		protected void renderForeground(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			super.renderForeground(matrixStackIn, mouseX, mouseY, partialTicks);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 100);
			for (Widget widget : this.children) {
				widget.renderToolTip(matrixStackIn, mouseX, mouseY);
			}
			matrixStackIn.pop();
		}
	}
}
