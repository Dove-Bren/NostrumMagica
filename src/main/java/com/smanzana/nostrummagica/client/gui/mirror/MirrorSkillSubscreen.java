package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.widget.MoveableObscurableWidget;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientPurchaseSkillMessage;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.progression.skill.SkillCategory;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.Rect2i;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix4f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MirrorSkillSubscreen extends PanningMirrorSubscreen {
	
	private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_skills.png");
	private static final ResourceLocation RES_BACK = NostrumMagica.Loc("textures/gui/mirror_back_skills.png");
	
	private final Component name;
	private final ItemStack icon;
	private final Map<SkillCategory, SkillCategoryButton> skillTabs;
	private final Map<Skill, SkillButton> buttons;
	private SkillCategory activeCategory;
	
	private Player player;
	private INostrumMagic attr;
	private int width;
	private int height;
	
	public MirrorSkillSubscreen() {
		name = new TranslatableComponent("mirror.tab.skill.name");
		icon = new ItemStack(NostrumItems.spellTomeCombat, 1);
		buttons = new HashMap<>();
		this.skillTabs = new HashMap<>();
	}
	
	@Override
	public Component getName() {
		return name;
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
	
	@Override
	public boolean isVisible(IMirrorScreen parent, Player player) {
		return true;
	}
	
	@Override
	public void show(IMirrorScreen parent, Player player, int width, int height, int guiLeft, int guiTop) {
		this.player = player;
		attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		this.width = width;
		this.height = height;
		
		buttons.clear();
		skillTabs.clear();
		Map<SkillCategory, List<Skill>> categories = new HashMap<>();
		
		final Rect2i bounds = new Rect2i(guiLeft, guiTop, width, height);
		for (Skill skill : Skill.allSkills()) {
			final int buttonWidth = POS_BUTTON_WIDTH;
			final int buttonHeight = POS_BUTTON_HEIGHT;
			final int buttonX = guiLeft + (width/2) + (skill.getPlotX() * GRID_SCALE) - (buttonWidth / 2);
			final int buttonY = guiTop + (height/2) + (-skill.getPlotY() * GRID_SCALE) - (buttonHeight / 2);
			
			SkillButton button = new SkillButton(this, skill,
					buttonX, buttonY,
					buttonWidth, buttonHeight);
			button.setBounds(bounds);
			parent.addWidget(button);
			buttons.put(skill, button);
			
			categories.computeIfAbsent(skill.getCategory(), (s) -> new ArrayList<>()).add(skill);
		}
		
		// Discover reverse-parent links
		for (SkillButton button : buttons.values()) {
			button.linkParent();
		}
		
		// Create category buttons
		List<SkillCategory> categoryTypes = Lists.newArrayList(categories.keySet());
		Collections.sort(categoryTypes, (l, r) -> {
			// Make magica be first, like research
			if (l == NostrumSkills.Category_Magica) {
				return -1;
			}
			if (r == NostrumSkills.Category_Magica) {
				return 1;
			}
			return l.getID().getPath().compareTo(r.getID().getPath());
		});
		for (SkillCategory category : categoryTypes) {
			SkillCategoryButton categoryButton = new SkillCategoryButton(this, category);
			categoryButton.setChildren(categories.get(category));
			parent.addMinorTab(categoryButton);
			skillTabs.put(category, categoryButton);
		}
		
		
		setButtonPositions();
		
		// Add parent last so it's below everything
		super.show(parent, player, width, height, guiLeft, guiTop);
	}

	@Override
	public void hide(IMirrorScreen parent, Player player) {
		; // Not sure there's much to do. Parent will clean things up for us
	}

	@Override
	public void drawBackground(IMirrorScreen parent, PoseStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		float extra = .1f * (float) Math.sin((double) System.currentTimeMillis() / 1500.0);
		float inv = .1f - extra;
		
		Minecraft.getInstance().getTextureManager().bind(RES_BACK);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
				0, 0, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				width, height, TEX_BACK_WIDTH, TEX_BACK_HEIGHT,
				.9f + extra, 1f, .8f + inv, 1f);
	}

	@Override
	public void drawForeground(IMirrorScreen parent, PoseStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final Font font = mc.font;
		matrixStackIn.pushPose();
		
		matrixStackIn.translate(width/2, 20, 0);
		RenderFuncs.drawGradientRect(matrixStackIn, -40, 0, 40, 20,
				0xFF000000, 0xFF000000,
				0x40000000, 0x40000000);
		
		
		final EMagicElement activeElement = this.activeCategory.getSkillpointType();
		if (activeElement == null) {
			matrixStackIn.translate(0, 8, 0);
			String str = "Skillpoints: " + attr.getSkillPoints();
			int strWidth = font.width(str);
			matrixStackIn.pushPose();
			matrixStackIn.scale(.75f, .75f, 1f);
			font.draw(matrixStackIn, str, -strWidth/2, 0, 0xFFFFFFFF);
			matrixStackIn.popPose();
		} else {
			matrixStackIn.translate(0, 4, 0);
			String str = "Skillpoints: " + attr.getElementalSkillPoints(activeElement);
			int strWidth = font.width(str);
			matrixStackIn.pushPose();
			matrixStackIn.scale(.75f, .75f, 1f);
			font.draw(matrixStackIn, str, -strWidth/2, 0, 0xFFFFFFFF);
			matrixStackIn.popPose();
			matrixStackIn.translate(0, font.lineHeight, 0);
			
			{
				final int xp = attr.getElementXP(activeElement);
				final int maxXP = attr.getElementMaxXP(activeElement);
				
				final int barWidth = 64;
				final int barHeight = 3;
				final int x = Math.round(((float) xp / (float) maxXP) * (float) barWidth);
				RenderFuncs.drawRect(matrixStackIn, - (barWidth/2), 0, + (barWidth / 2), 2 + barHeight, 0xFF555555);
				RenderFuncs.drawRect(matrixStackIn, - (barWidth/2) + 1, 0 + 1, (barWidth / 2) - 1, 1 + barHeight, 0xFF000000);
				RenderFuncs.drawRect(matrixStackIn, - (barWidth/2) + 1, 0 + 1, - (barWidth/2) + 1 + x, 1 + barHeight, 0xFFFFFF00);
			}
		}
		
		
		matrixStackIn.popPose();
	}
	
	@Override
	protected void onPan(int panX, int panY, float scale) {
		setButtonPositions();
	}
	
	@Override
	protected void onZoom(int panX, int panY, float scale) {
		setButtonPositions();
	}
	
	protected void setButtonPositions() {
		for (SkillButton button : buttons.values()) {
			final int offsetX = button.getStartingX() - ((width) / 2);
			final int offsetY = button.getStartingY() - ((height) / 2);
			
			
			final int scaledX = (width/2) + (int) ((offsetX + this.getPanX()) * this.getPanScale());
			final int scaledY = (height/2) + (int) ((offsetY + this.getPanY()) * this.getPanScale());
			button.setPosition(scaledX, scaledY);
		}
	}
	
	protected boolean hasSkillPointsFor(Skill skill) {
		if (skill.getCategory().getSkillpointType() == null) {
			return attr.getSkillPoints() > 0;
		} else {
			return attr.getElementalSkillPoints(skill.getCategory().getSkillpointType()) > 0;
		}
	}
	
	protected void onSkillButton(SkillButton button) {
		//if (ignoreButton(button)) return;
		
		final Skill skill = button.skill;
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		
		if (button.state == SkillState.AVAILABLE && attr != null && hasSkillPointsFor(skill)) {
			// do on client too for instant feedback
			attr.addSkill(skill);
			
			NetworkHandler.sendToServer(
				new ClientPurchaseSkillMessage(skill)	
				);
			
			button.updateState();
		}
	}
	
	protected void onCategoryButton(SkillCategory category) {
		this.activeCategory = category;
		this.resetPan();
	}
	
	protected static enum SkillState {
		UNAVAILABLE,
		AVAILABLE,
		OWNED
	}
	
	private static class SkillButton extends MoveableObscurableWidget {
		
		private final MirrorSkillSubscreen subscreen;
		private final Skill skill;
		
		private final List<Component> tooltip;
		private final float fontScale = 0.75f;
		
		private SkillState state;
		
		private List<SkillButton> childButtons;
		private @Nullable SkillButton parentButton;
		
		public SkillButton(MirrorSkillSubscreen subscreen, Skill skill, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY);
			this.subscreen = subscreen;
			this.skill = skill;
			this.tooltip = genTooltip();
			this.childButtons = new ArrayList<>();
			updateState();
		}

		protected void updateState() {
			if (subscreen.attr.hasSkill(skill)) {
				this.state = SkillState.OWNED;
			} else if (!skill.isHidden(subscreen.player) && subscreen.hasSkillPointsFor(skill)) {
				this.state = SkillState.AVAILABLE;
			} else {
				this.state = SkillState.UNAVAILABLE;
			}
		}
		
		public void addChild(SkillButton button) {
			this.childButtons.add(button);
		}
		
		public void linkParent() {
			assert(parentButton == null); // Else need to clear our parents
			if (skill.getParentKey() != null) {
				Skill parent = Skill.lookup(this.skill.getParentKey());
				if (parent != null) {
					parentButton = subscreen.buttons.get(parent);
					
					if (parentButton != null) {
						parentButton.addChild(this);
					}
				}
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!this.isHidden()) {
				return super.mouseClicked(mouseX, mouseY, button);
			}
			
			return false;
		}
		
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
			return false;
		}
		
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return false;
		}
		
		@Override
		public void onClick(double mouseX, double mouseY) {
			subscreen.onSkillButton(this);
		}
		
		public void drawTreeLines(PoseStack matrixStackIn, Minecraft mc) {
//			if (skill.getParentKey() != null) {
//				Skill parent = Skill.lookup(this.skill.getParentKey());
//				if (parent == null)
//					return;
//				
//				SkillButton other = subscreen.buttons.get(parent);
//				if (other != null && !other.isHidden())
//					renderLine(matrixStackIn, other, false);
//			}
			
			// Used to have children draw to parent but now will draw to chldren, possible half-faded if child is hidden
			for (SkillButton child : this.childButtons) {
				renderLine(matrixStackIn, child, child.isHidden());
			}
		}
		
		@SuppressWarnings("deprecation")
		private void renderLine(PoseStack matrixStackIn, SkillButton other, boolean faded) {
			matrixStackIn.pushPose();
//			GlStateManager.pushLightingAttributes();
			matrixStackIn.translate(width / 2, height / 2, 0);
			
			final Matrix4f transform = matrixStackIn.last().pose();
			
			BufferBuilder buf = Tesselator.getInstance().getBuilder();
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.lineWidth(3f);

			RenderSystem.defaultBlendFunc();
			RenderSystem.disableAlphaTest();
			RenderSystem.shadeModel(GL11.GL_SMOOTH);
	        //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//	        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 0.6f);
	        buf.begin(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR);
	        buf.vertex(transform, x, y, 0).color(1f, 1f, 1f, faded ? .2f : .6f).endVertex();
	        buf.vertex(transform, other.x, other.y, 0).color(1f, 1f, 1f, faded ? 0f : .6f).endVertex();
	        Tesselator.getInstance().end();
	        RenderSystem.enableTexture();
//	        GlStateManager.enableTexture();
	        RenderSystem.disableBlend();
			RenderSystem.lineWidth(1f);
			
//	        GlStateManager.popAttributes();
	        matrixStackIn.popPose();
		}
		
		@Override
		public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			// Render tree lines even if we're out of bounds
			if (!this.isHidden()) {
				updateState();
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, 0);
				drawTreeLines(matrixStackIn, Minecraft.getInstance());
				matrixStackIn.translate(0, 0, 1);
				super.render(matrixStackIn, mouseX, mouseY, partialTicks);
				matrixStackIn.popPose();
			}
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			int u = TEX_ICON_BUTTON_HOFFSET;
			int v = TEX_ICON_BUTTON_VOFFSET;
			float[] color = ColorUtil.ARGBToColor(skill.getCategory().getColor());
			if (this.state == SkillState.UNAVAILABLE) {
				color[0] *= .2f;
				color[1] *= .2f;
				color[2] *= .2f;
			} else if (this.state == SkillState.OWNED) {
				u = TEX_ICON_BUTTON_OWNED_HOFFSET;
				v = TEX_ICON_BUTTON_OWNED_VOFFSET;
			}
			
			if (this.isHovered()) {
				color[0] *= .8f;
				color[1] *= .8f;
				color[2] *= .8f;
			}
			
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bind(RES_ICONS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
					u, v, TEX_ICON_BUTTON_WIDTH, TEX_ICON_BUTTON_HEIGHT,
					this.width, this.height, TEX_ICON_WIDTH, TEX_ICON_HEIGHT,
					color[0], color[1], color[2], color[3]);

			// Icon
			matrixStackIn.pushPose();
			Lighting.turnBackOn();
			// RenderGuiItem moves 100 forward. Blocks render several z deep.
			// Squish to 8 deep, and shift back
			matrixStackIn.scale(1f, 1f, .4f);
			matrixStackIn.translate(x + (width/2), y + (height / 2), -90);
			matrixStackIn.scale(.75f, .75f, 1f);
			RenderFuncs.RenderGUIItem(skill.getIcon(), matrixStackIn, (- 16) / 2, (- 16) / 2);
			RenderSystem.enableDepthTest();
			Lighting.turnOff();
			
			matrixStackIn.popPose();
		}
		
		@Override
		public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHovered()) { 
				final Minecraft mc = Minecraft.getInstance();
				final Font font = mc.font;
				matrixStackIn.pushPose();
				matrixStackIn.scale(fontScale, fontScale, 1f);
				matrixStackIn.translate((int) (mouseX / fontScale) - mouseX, (int) (mouseY / fontScale) - mouseY, 0);
				GuiUtils.drawHoveringText(matrixStackIn, tooltip, mouseX, mouseY, subscreen.width, subscreen.height, 400, font);
				matrixStackIn.popPose();
			}
		}
		
		private List<Component> genTooltip() {
			List<Component> tooltip = new LinkedList<>();
			tooltip.add(((BaseComponent) skill.getName()).withStyle(ChatFormatting.BLUE));
			tooltip.addAll(skill.getDescription());
			
			if (this.subscreen.attr.hasSkill(skill)) {
				tooltip.add(new TextComponent(" "));
				tooltip.add(new TextComponent("Owned").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN));
			} else {
				tooltip.add(new TextComponent(" "));
				tooltip.add(new TranslatableComponent("info.research.purchase").withStyle(ChatFormatting.GREEN));
			}
			
			return tooltip;
		}
		
		public boolean isHidden() {
			return skill.getCategory() != subscreen.activeCategory
					|| skill.isHidden(subscreen.player)
					|| !skill.meetsRequirements(subscreen.player);
		}
	}
	
	private static final class SkillCategoryButton implements IMirrorMinorTab {
		
		protected final SkillCategory category;
		private final Component name;
		private final List<Skill> children;
		
		public SkillCategoryButton(MirrorSkillSubscreen subscreen, SkillCategory category) {
			this.category = category;
			this.name = category.getName();
			this.children = new ArrayList<>(32);
		}

		@Override
		public void onClick(IMirrorScreen parent, IMirrorSubscreen subscreenIn) {
			MirrorSkillSubscreen subscreen = ((MirrorSkillSubscreen) subscreenIn);
			subscreen.onCategoryButton(this.category);
		}

		@Override
		public Component getName() {
			return name;
		}

		@Override
		public void renderTab(IMirrorScreen parent, IMirrorSubscreen subscreen, PoseStack matrixStackIn, int width, int height) {
			RenderFuncs.RenderGUIItem(category.getIcon(), matrixStackIn, (width - 16) / 2, (height - 16) / 2);
		}

		@Override
		public boolean isVisible(IMirrorScreen parent, IMirrorSubscreen subscreenIn) {
			MirrorSkillSubscreen subscreen = (MirrorSkillSubscreen) subscreenIn;
			for (Skill child : children) {
				if (!child.isHidden(subscreen.player) && child.meetsRequirements(subscreen.player)) {
					return true;
				}
			}
			return false;
		}
		
		public void setChildren(Collection<Skill> children) {
			this.children.clear();
			this.children.addAll(children);
		}

		@Override
		public boolean hasNewEntry(IMirrorScreen parent, IMirrorSubscreen subscreen) {
			return false;
		}
	}
	
	private static final int TEX_BACK_WIDTH = 256;
	private static final int TEX_BACK_HEIGHT = 256;
	
	private static final int TEX_ICON_WIDTH = 64;
	private static final int TEX_ICON_HEIGHT = 64;
	
	private static final int TEX_ICON_BUTTON_HOFFSET = 0;
	private static final int TEX_ICON_BUTTON_VOFFSET = 0;
	private static final int TEX_ICON_BUTTON_WIDTH = 32;
	private static final int TEX_ICON_BUTTON_HEIGHT = 32;
	
	private static final int TEX_ICON_BUTTON_OWNED_HOFFSET = TEX_ICON_BUTTON_HOFFSET + TEX_ICON_BUTTON_WIDTH;
	private static final int TEX_ICON_BUTTON_OWNED_VOFFSET = TEX_ICON_BUTTON_VOFFSET;
	
	private static final int POS_BUTTON_WIDTH = 32;
	private static final int POS_BUTTON_HEIGHT = 32;
	
	private static final int GRID_SCALE = POS_BUTTON_WIDTH + 8;

}
