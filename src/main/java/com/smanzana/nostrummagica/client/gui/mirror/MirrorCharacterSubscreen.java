package com.smanzana.nostrummagica.client.gui.mirror;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.widget.LabeledTextWidget;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MirrorCharacterSubscreen implements IMirrorSubscreen {
	
	private static final ResourceLocation RES_ICONS = NostrumMagica.Loc("textures/gui/mirror_character.png");
	
	private final ITextComponent name;
	private final ItemStack icon;
	
	private @Nullable INostrumMagic attr;
	
	private ImproveButton buttonControl;
	private ImproveButton buttonTechnique;
	private ImproveButton buttonFinesse;
	
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

		int y = guiTop + 50;
		parent.addWidget(new SectionLabel("Characteristics", font, leftMargin, y, width - 30, 20));
		y += font.FONT_HEIGHT + 4;
		{
			final float scale = .5f;
			final int yPer = (int) (font.FONT_HEIGHT * scale) + 1;
			final int yTop = y;
			int x = leftMargin + 4;
			
			// First column: Mana
			parent.addWidget(new LabeledTextWidget(helper, "Mana: ", () -> attr.getMana() + "/" + attr.getMaxMana(), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledTextWidget(helper, "Mana Regen: ", () -> String.format("%+.1f%% (%.02f/s)", attr.getManaRegenModifier() * 100f, 2 * (1 + attr.getManaRegenModifier())), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledTextWidget(helper, "Mana Cost: ", () -> String.format("%+.1f%%", attr.getManaCostModifier() * 100f), x, y, width/2, yPer).scale(scale));
			y += yPer;
			
			// Second column: Mana modifiers
			y = yTop;
			x = leftMargin + width/2;
			parent.addWidget(new LabeledTextWidget(helper, "Bonus Mana: ", () -> String.format("%+.1f%%", attr.getManaModifier() * 100f), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledTextWidget(helper, "Bonus Mana (Flat): ", () -> "" + attr.getManaBonus(), x, y, width/2, yPer).scale(scale));
			y += yPer;
			parent.addWidget(new LabeledTextWidget(helper, "Reserved Mana: ", () -> "" + attr.getReservedMana(), x, y, width/2, yPer).scale(scale).tooltip(getMiscDesc("info.reserved_mana.desc")));
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
					NostrumAttributes.magicResist
			};
			
			for (Attribute attribute : list) {
				parent.addWidget(new LabeledTextWidget(helper, I18n.format(attribute.getAttributeName()) + ": ", () -> player.getAttribute(attribute).getValue() + "%", x, y, width/2, yPer).scale(scale).tooltip(getAttribDesc(attribute)	));
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
				parent.addWidget(new LabeledTextWidget(helper, I18n.format(attribute.getAttributeName()) + ": ", () -> "" + player.getAttribute(attribute).getValue(), x, y, width/2, yPer).scale(scale).tooltip(getAttribDesc(attribute)));
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
			
			// First row: resources
			parent.addWidget(new LabeledTextWidget(helper, "Available Points: ", () -> "" + attr.getSkillPoints(), x, y, width/2, yPer).scale(scale));
			y += yPer;
			
			// Second row: stats
//			parent.addWidget(new LabeledTextWidget(helper, "Control: ", () -> "" + attr.getControl(), x, y, width/3, yPer).scale(scale).tooltip(getMiscDesc("control.desc")));
//			buttonControl = new ImproveButton(this, x + 20, y + yPer + 2, POS_IMPROVE_BUTTON_WIDTH, POS_IMPROVE_BUTTON_HEIGHT);
//			x += xPer;
//			parent.addWidget(new LabeledTextWidget(helper, "Technique: ", () -> "" + attr.getTech(), x, y, width/3, yPer).scale(scale).tooltip(getMiscDesc("technique.desc")));
//			buttonTechnique = new ImproveButton(this, x + 20, y + yPer + 2, POS_IMPROVE_BUTTON_WIDTH, POS_IMPROVE_BUTTON_HEIGHT);
//			x += xPer;
//			parent.addWidget(new LabeledTextWidget(helper, "Finesse: ", () -> "" + attr.getFinesse(), x, y, width/3, yPer).scale(scale).tooltip(getMiscDesc("finesse.desc")));
//			buttonFinesse = new ImproveButton(this, x + 20, y + yPer + 2, POS_IMPROVE_BUTTON_WIDTH, POS_IMPROVE_BUTTON_HEIGHT);
//			x += xPer;
		}
		
		refreshButtons(); // Set visibility on buttons based on attributes
		
		parent.addWidget(buttonControl);
		parent.addWidget(buttonTechnique);
		parent.addWidget(buttonFinesse);
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
			final int strWidth = font.getStringWidth(xp) / 2;
			font.drawStringWithShadow(matrixStackIn, xp, -strWidth, 0, colorKey);
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
		if (attr.getSkillPoints() == 0) {
			buttonControl.visible
				= buttonTechnique.visible
				= buttonFinesse.visible
				= false;
		} else {
			buttonControl.visible
			= buttonTechnique.visible
			= buttonFinesse.visible
			= true;
		}
	}
	
	protected void onImproveButton(Button button) {
		//if (ignoreButton(button)) return;
		
//		final Type type;
//		if (button == this.buttonControl) {
//			type = Type.CONTROL;
//		} else if (button == this.buttonFinesse) {
//			type = Type.FINESSE;
//		} else {
//			type = Type.TECHNIQUE;
//		}
//		attr.takeSkillPoint(); // take a local point so our update makes sense
//		NetworkHandler.sendToServer(
//				new ClientSkillUpMessage(type)
//				);
		
		refreshButtons();
	}
	
	private static class ImproveButton extends Button {
		
		public ImproveButton(MirrorCharacterSubscreen screen, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY, (b) -> {
				screen.onImproveButton(b);
			});
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int parX, int parY, float partialTicks) {
			if (visible) {
				RenderSystem.enableDepthTest();
				Minecraft.getInstance().getTextureManager().bindTexture(RES_ICONS);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y,
						TEX_BUTTON_HOFFSET, TEX_BUTTON_VOFFSET, TEX_BUTTON_WIDTH, TEX_BUTTON_HEIGHT,
						this.width, this.height, TEX_WIDTH, TEX_HEIGHT);
				
				if (this.isHovered()) {
					RenderFuncs.drawRect(matrixStackIn, x, y, x + width, y + height, 0x40FFFFFF);
				}
			}
		}
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
	
	private static final int TEX_WIDTH = 64;
	private static final int TEX_HEIGHT = 64;
	
	private static final int TEX_BUTTON_HOFFSET = 0;
	private static final int TEX_BUTTON_VOFFSET = 0;
	private static final int TEX_BUTTON_WIDTH = 32;
	private static final int TEX_BUTTON_HEIGHT = 32;

}
