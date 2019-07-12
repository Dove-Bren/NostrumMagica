package com.smanzana.nostrummagica.client.gui;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ScrollScreen extends GuiScreen {

	protected static final ResourceLocation background = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/scrollback.png");
	
	protected static final int TEXT_BACK_WIDTH = 170;
	protected static final int TEXT_BACK_HEIGHT = 220;
	protected static final int TEXT_WHOLE_WIDTH = 170;
	protected static final int TEXT_WHOLE_HEIGHT = 220;
	
	protected static final int TEXT_TITLE_VOFFSET = 30;
	protected static final int TEXT_LIST_VOFFSET = 50;
	protected static final int TEXT_LIST_HOFFSET = 30;
	
	//30 height for name
	//50 height for desc
	
	
	private final Spell spell;
	private final List<String> components;
	private final String name;
	private final int color;
	
	public ScrollScreen(ItemStack scroll) {
		this.components = new LinkedList<>();
		this.spell = SpellScroll.getSpell(scroll);
		
		if (spell == null) {
			this.name = "Unknown Spell";
			this.components.add("This spell has vanished!");
			this.color = 0xFFFF0000;
		} else {
			this.name = spell.getName();
			this.color = spell.getPrimaryElement().getColor();
			
			for (SpellPart part : spell.getSpellParts()) {
				if (part.isTrigger()) {
					this.components.add(part.getTrigger().getDisplayName());
				} else {
					this.components.add(" - " + part.getShape().getDisplayName());
					this.components.add("   " + part.getElement().getName()
							+ (part.getAlteration() == null ? "" : (" [" + part.getAlteration().getName() + "]")));
				}
			}
		}
	}
	
	@Override	
	public void updateScreen() {
		;
	}
	
	@Override
	public void drawScreen(int parWidth, int parHeight, float p_73863_3_) {
		
		final int leftOffset = (this.width - TEXT_BACK_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - TEXT_BACK_HEIGHT) / 2;
		
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(background);
		
		Gui.drawScaledCustomSizeModalRect(leftOffset, topOffset, 0, 0, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, TEXT_BACK_WIDTH, TEXT_BACK_HEIGHT, TEXT_WHOLE_WIDTH, TEXT_WHOLE_HEIGHT);
		
		final int nameWidth = this.fontRendererObj.getStringWidth(this.name);
		this.fontRendererObj.drawString(this.name, leftOffset + (TEXT_BACK_WIDTH / 2) - (nameWidth / 2), topOffset + TEXT_TITLE_VOFFSET, color);
		
		int i = 0;
		for (String line : this.components) {
			this.fontRendererObj.drawString(line, leftOffset + TEXT_LIST_HOFFSET, topOffset + TEXT_LIST_VOFFSET + (i * this.fontRendererObj.FONT_HEIGHT + 2), 0xFF000000);
			i++;
		}
		
		super.drawScreen(parWidth, parHeight, p_73863_3_);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
}
