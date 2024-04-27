package com.smanzana.nostrummagica.client.gui;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

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
		this(SpellScroll.getSpell(scroll));
	}
	
	public ScrollScreen(Spell spellIn) {
		super(new StringTextComponent("SpellScroll Info Screen"));
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
			
			for (SpellPart part : spell.getSpellParts()) {
				if (part.isTrigger()) {
					this.components.add(part.getTrigger().getDisplayName());
				} else {
					final String intensity = SpellPlate.toRoman(part.getElementCount());	
					this.components.add(" - " + part.getShape().getDisplayName());
					this.components.add("   " + part.getElement().getName() + " " + intensity
							+ (part.getAlteration() == null ? "" : (" [" + part.getAlteration().getName() + "]")));
				}
			}
		}
	}
	
	@Override	
	public void tick() {
		;
	}
	
	@Override
	public void render(int parWidth, int parHeight, float p_73863_3_) {
		
		final int leftOffset = (this.width - TEXT_BACK_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - TEXT_BACK_HEIGHT) / 2;
		
		final int titleYOffset = 30;
		final int iconYOffset = 45;
		final int listYOffset = 80;
		final int listXOffset = 25;
		
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getInstance().getTextureManager().bindTexture(background);
		
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, leftOffset, topOffset, 0, 0, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
		
		final int nameWidth = this.font.getStringWidth(this.name);
		this.font.drawString(this.name, leftOffset + (TEXT_BACK_WIDTH / 2) - (nameWidth / 2), topOffset + titleYOffset, color);
		
		if (this.icon != null) {
			final int iconLen = 32;
			final int left = leftOffset + ((TEXT_BACK_WIDTH - iconLen) / 2);
			RenderFuncs.drawRect(left - 2, topOffset + iconYOffset - 2, left + iconLen + 2, topOffset + iconYOffset + iconLen + 2, 0xFF000000);
			RenderFuncs.drawRect(left, topOffset + iconYOffset, left + iconLen, topOffset + iconYOffset + iconLen, 0xFFE2DDCC);
			GlStateManager.color4f(1f, 1f, 1f, 1f);
			icon.render(Minecraft.getInstance(), matrixStackIn, left, topOffset + iconYOffset, iconLen, iconLen);
		}
		
		int i = 0;
		for (String line : this.components) {
			this.font.drawString(line, leftOffset + listXOffset, 10 + topOffset + listYOffset + (i * this.font.FONT_HEIGHT + 2), 0xFF000000);
			i++;
		}
		
		super.render(parWidth, parHeight, p_73863_3_);
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
