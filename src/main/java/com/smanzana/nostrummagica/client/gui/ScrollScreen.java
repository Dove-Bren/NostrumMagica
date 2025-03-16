package com.smanzana.nostrummagica.client.gui;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.SpellPlate;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;

public class ScrollScreen extends Screen {

	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/scrollback.png");
	
	protected static final int TEXT_BACK_WIDTH = 170;
	protected static final int TEXT_BACK_HEIGHT = 220;
	protected static final int TEXT_WHOLE_WIDTH = 170;
	protected static final int TEXT_WHOLE_HEIGHT = 220;
	
	private final Spell spell;
	private final List<String> components;
	private final String name;
	private final int color;
	private final SpellIcon icon;
	
	public ScrollScreen(@Nonnull ItemStack scroll) {
		this(SpellScroll.GetSpell(scroll));
	}
	
	public ScrollScreen(Spell spellIn) {
		super(new TextComponent("SpellScroll Info Screen"));
		this.spell = spellIn;
		this.components = new LinkedList<>();
		
		if (spell == null) {
			this.name = "Unknown Spell";
			this.components.add("This spell has vanished!");
			this.color = 0xFFFF0000;
			this.icon = null;
		} else {
			this.name = spell.getName();
			this.color = spell.getPrimaryElement().getColor();
			this.icon = SpellIcon.get(spell.getIconIndex());
			
			for (SpellShapePart part : spell.getSpellShapeParts()) {
				this.components.add(" - " + part.getShape().getDisplayName().getString());
			}
			
			for (SpellEffectPart part : spell.getSpellEffectParts()) {
				final String intensity = SpellPlate.toRoman(part.getElementCount());	
				this.components.add("   " + part.getElement().getName() + " " + intensity
						+ (part.getAlteration() == null ? "" : (" [" + part.getAlteration().getName() + "]")));
			}
		}
	}
	
	@Override	
	public void tick() {
		;
	}
	
	@Override
	public void render(PoseStack matrixStackIn, int parWidth, int parHeight, float p_73863_3_) {
		
		final int leftOffset = (this.width - TEXT_BACK_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - TEXT_BACK_HEIGHT) / 2;
		
		final int titleYOffset = 30;
		final int iconYOffset = 45;
		final int statXOffset = 15;
		final int statYOffset = 82;
		final int statXWidth = TEXT_BACK_WIDTH - (statXOffset);
		final int listYOffset = 95;
		final int listXOffset = 25;
		
		//RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getInstance().getTextureManager().bind(background);
		
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, leftOffset, topOffset, 0, 0, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT,
				1f, 1f, 1f, 1f);
		
		final int nameWidth = this.font.width(this.name);
		this.font.draw(matrixStackIn, this.name, leftOffset + (TEXT_BACK_WIDTH / 2) - (nameWidth / 2), topOffset + titleYOffset, color);
		
		if (this.icon != null) {
			final int iconLen = 32;
			final int left = leftOffset + ((TEXT_BACK_WIDTH - iconLen) / 2);
			RenderFuncs.drawRect(matrixStackIn, left - 2, topOffset + iconYOffset - 2, left + iconLen + 2, topOffset + iconYOffset + iconLen + 2, 0xFF000000);
			RenderFuncs.drawRect(matrixStackIn, left, topOffset + iconYOffset, left + iconLen, topOffset + iconYOffset + iconLen, 0xFFE2DDCC);
			//GlStateManager.color4f(1f, 1f, 1f, 1f);
			icon.render(Minecraft.getInstance(), matrixStackIn, left, topOffset + iconYOffset, iconLen, iconLen);
		}
		
		final String weightStr = "Weight: " + spell.getWeight();
		final int weightWidth = this.font.width(weightStr);
		this.font.draw(matrixStackIn, "Cost: " + spell.getManaCost(), leftOffset + statXOffset, topOffset + statYOffset, 0xFF000000);
		this.font.draw(matrixStackIn, weightStr, leftOffset + statXWidth - (weightWidth), topOffset + statYOffset, 0xFF000000);
		
		RenderFuncs.drawRect(matrixStackIn, leftOffset + statXOffset - 2, topOffset + statYOffset + font.lineHeight,
				leftOffset + statXWidth + 2, topOffset + statYOffset + font.lineHeight + 2, 0xFF000000);
		
		int i = 0;
		for (String line : this.components) {
			this.font.draw(matrixStackIn, line, leftOffset + listXOffset, topOffset + listYOffset + (i * this.font.lineHeight + 2), 0xFF000000);
			i++;
		}
		
		super.render(matrixStackIn, parWidth, parHeight, p_73863_3_);
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
