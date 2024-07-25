package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.widget.LabeledWidget;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MirrorCharacterSubscreen implements IMirrorSubscreen {
	
	//private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_character.png");
	
	private final ITextComponent name;
	private final ItemStack icon;
	
	private @Nullable INostrumMagic attr;
	
	public MirrorCharacterSubscreen() {
		name = new TranslationTextComponent("mirror.tab.character.name");
		icon = new ItemStack(Items.PLAYER_HEAD, 1);
	}
	
	@Override
	public ITextComponent getName() {
		return name;
	}
	
	@Override
	public ItemStack getIcon() {
		return icon;
	}
	
	@Override
	public boolean isVisible(IMirrorScreen parent, PlayerEntity player) {
		return true;
	}
	
	@Override
	public void show(IMirrorScreen parent, PlayerEntity player, int width, int height, int guiLeft, int guiTop) {
		this.attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return; // Just do nothing. Shouldn't exist and I don't want to program a message for if it happens.
		}
		
		// Set up sections
		// Basic stats section
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer font = mc.fontRenderer;
		final Screen helper = parent.getGuiHelper();
		final int leftMargin = guiLeft + 16;

		int y = guiTop + 56;
		parent.addWidget(new SectionLabel("Characteristics", font, leftMargin, y, width - 30, 20));
		y += font.FONT_HEIGHT + 4;
		{
			final float scale = .5f;
			final int yPer = (int) (font.FONT_HEIGHT * scale) + 1;
			final int yTop = y;
			int x = leftMargin + 4;
			
			// First column: Mana
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Mana: "), new LabeledWidget.TextValue(() -> attr.getMana() + "/" + attr.getMaxMana()), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Mana Regen: "), new LabeledWidget.TextValue(() -> String.format("%+.1f%% (%.02f/s)", attr.getManaRegenModifier() * 100f, 2 * (1 + attr.getManaRegenModifier()))), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Mana Cost: "), new LabeledWidget.TextValue(() -> String.format("%+.1f%%", attr.getManaCostModifier() * 100f)), x, y, width/2, yPer).scale(scale));
			y += yPer;
			
			// Second column: Mana modifiers
			y = yTop;
			x = leftMargin + width/2;
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Bonus Mana: "), new LabeledWidget.TextValue(() -> String.format("%+.1f%%", attr.getManaModifier() * 100f)), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Bonus Mana (Flat): "), new LabeledWidget.TextValue(() -> "" + attr.getManaBonus()), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Reserved Mana: "), new LabeledWidget.TextValue(() -> "" + attr.getReservedMana()), x, y, width/2, yPer).scale(scale).tooltip(getMiscDesc("info.reserved_mana.desc")));
			y += yPer;
		}
		
		
		y = guiTop + 90;
		parent.addWidget(new SectionLabel("Attributes", font, leftMargin, y, width - 30, 20));
		y += font.FONT_HEIGHT + 4;
		{
			final float scale = .5f;
			final int yPer = (int) (font.FONT_HEIGHT * scale) + 1;
			final int yTop = y;
			int x = leftMargin + 4;
			
			// First row: primary attributes
			Attribute[] list = {
					NostrumAttributes.magicPotency,
					NostrumAttributes.manaRegen,
					NostrumAttributes.manaCost,
					NostrumAttributes.magicResist,
					NostrumAttributes.magicDamage,
					NostrumAttributes.xpBonus,
			};
			
			for (Attribute attribute : list) {
				parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel(I18n.format(attribute.getAttributeName()) + ": "), new LabeledWidget.TextValue(() -> String.format("%.1f%%", player.getAttribute(attribute).getValue())), x, y, width/2, yPer).scale(scale).tooltip(getAttribDesc(attribute)	));
				y += yPer;
			}
			
			// Second row: reduction amounts
			y = yTop;
			x = leftMargin + width/2;
			list = new Attribute[] {
					NostrumAttributes.GetReduceAttribute(EMagicElement.PHYSICAL),
					NostrumAttributes.GetReduceAttribute(EMagicElement.EARTH),
					NostrumAttributes.GetReduceAttribute(EMagicElement.ENDER),
					NostrumAttributes.GetReduceAttribute(EMagicElement.FIRE),
					NostrumAttributes.GetReduceAttribute(EMagicElement.ICE),
					NostrumAttributes.GetReduceAttribute(EMagicElement.LIGHTNING),
					NostrumAttributes.GetReduceAttribute(EMagicElement.WIND),
			};
			
			for (Attribute attribute : list) {
				parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel(I18n.format(attribute.getAttributeName()) + ": "), new LabeledWidget.TextValue(() -> String.format("%.1f", (float) player.getAttribute(attribute).getValue())), x, y, width/2, yPer).scale(scale).tooltip(getAttribDesc(attribute)));
				y += yPer;
			}
		}
		
		y = guiTop + 150;
		parent.addWidget(new SectionLabel("Skills", font, leftMargin, y, width - 30, 20));
		y += font.FONT_HEIGHT + 4;
		{
			final float scale = .5f;
			final int yPer = (int) (font.FONT_HEIGHT * scale) + 1;
			int x = leftMargin + 4;
			
			// First row: skillpoints
			parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel("Skill Points: "), new LabeledWidget.TextValue(() -> "" + attr.getSkillPoints()), x, y, width/2, yPer).scale(scale));
			y += yPer;
			
			// Second row: elemental skillpoints
			int elemCount = 0;
			for (EMagicElement element : EMagicElement.values()) {
				if (attr.getElementalMastery(element).isGreaterOrEqual(EElementalMastery.NOVICE)) {
					parent.addWidget(new LabeledWidget(helper, new LabeledWidget.StringLabel(element.getName() + " Points: "), new LabeledWidget.TextValue(() -> "" + attr.getElementalSkillPoints(element)), x, y, width / 5, yPer).scale(scale));
					x += (width/5);
					if (++elemCount >= 4) {
						x = leftMargin + 4;
						y += yPer;
						elemCount = 0;
					}
				}
			}
		}
		
		refreshButtons(); // Set visibility on buttons based on attributes
	}

	@Override
	public void hide(IMirrorScreen parent, PlayerEntity player) {
		; // Not sure there's much to do. Parent will clean things up for us
	}

	@Override
	public void drawBackground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		RenderFuncs.drawGradientRect(matrixStackIn, 0, 0, width, height,
				0xFF332266, 0xFF221155, 0xFF443388, 0xFF443377
				);
	}

	@Override
	public void drawForeground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks) {
		final Minecraft mc = Minecraft.getInstance();
		final FontRenderer font = mc.fontRenderer;
		
		// DRAW STATS
		int y = 22;
		int len;
		int colorKey = 0xFFAAAAAA;
		String str;
		
		str = "Level " + attr.getLevel();
		len = font.getStringWidth(str);
		font.drawStringWithShadow(matrixStackIn, str, (width - len) / 2, y, 0xFFFFFFFF);
		y += font.FONT_HEIGHT + 2;
		
		matrixStackIn.push();
		matrixStackIn.translate(width/2, y, 0);
		matrixStackIn.scale(.75f, .75f, 1f);
		{
			final String xp = String.format("%.02f%%", 100f * attr.getXP() / attr.getMaxXP());
			int strWidth = font.getStringWidth(xp) / 2;
			font.drawStringWithShadow(matrixStackIn, xp, -strWidth, 0, colorKey);
			matrixStackIn.translate(0, font.FONT_HEIGHT + 2, 0);
			
			final String tier = attr.getTier().getRawName() + " Tier";
			strWidth = font.getStringWidth(tier) / 2;
			font.drawStringWithShadow(matrixStackIn, tier, -strWidth, 0, colorKey);
		}
		matrixStackIn.pop();
	}
	
	protected List<ITextComponent> getAttribDesc(Attribute attrib) {
		return TextUtils.GetTranslatedList(attrib.getAttributeName() + ".desc");
	}
	
	protected List<ITextComponent> getMiscDesc(String key) {
		return TextUtils.GetTranslatedList(key);
	}
	
	protected void refreshButtons() {
		// Add buttons that show up on the character screen
	}
	
	private static class SectionLabel extends Widget {
		
		private final String label;
		private final FontRenderer font;
		
		public SectionLabel(String label, FontRenderer font, int x, int y, int width, int height) {
			super(x, y, width, height, new StringTextComponent(label));
			this.label = label;
			this.font = font;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final float scale = .75f;
			
			matrixStackIn.push();
			matrixStackIn.translate(x, y, 0);
			RenderFuncs.drawRect(matrixStackIn, 0, (int) (font.FONT_HEIGHT * scale) + 2, this.width - 4, (int) (font.FONT_HEIGHT * scale) + 2 + 2, 0xFF888888);
			matrixStackIn.scale(scale, scale, 1f);
			font.drawString(matrixStackIn, label, 0, 0, 0xFFAAAAAA);
			matrixStackIn.pop();
		}
	}
	
//	private static final int TEX_WIDTH = 64;
//	private static final int TEX_HEIGHT = 64;
//	
//	private static final int TEX_BUTTON_HOFFSET = 0;
//	private static final int TEX_BUTTON_VOFFSET = 0;
//	private static final int TEX_BUTTON_WIDTH = 32;
//	private static final int TEX_BUTTON_HEIGHT = 32;

}
