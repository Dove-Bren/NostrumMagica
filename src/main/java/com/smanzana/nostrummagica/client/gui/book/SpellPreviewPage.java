package com.smanzana.nostrummagica.client.gui.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;

public class SpellPreviewPage implements IBookPage {

	private Spell spell;
	private List<String> tooltip;
	
	public SpellPreviewPage(Spell spell) {
		this.spell = spell;
		tooltip = new ArrayList<>();
		Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
		for (ReagentType type : reagents.keySet()) {
			if (type == null)
				continue;
			Integer count = reagents.get(type);
			if (count == null || count == 0)
				continue;
			
			tooltip.add(count + " " + type.prettyName());
		}
	}
	
	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {

		yoffset += 5;
		height -= 5;
		
		GL11.glPushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Gui.drawRect(xoffset, yoffset, xoffset + width, yoffset + height, 0x40000000);
		
		// Draw element icon
		SpellIcon elementIcon = SpellIcon.get(spell.getPrimaryElement());
		int icony = yoffset + (height / 2) + (-12);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		elementIcon.draw(parent, fonter, xoffset + 4, icony, 24, 24);
		fonter.drawString(spell.getName(), xoffset + 32, yoffset + 2, spell.getPrimaryElement().getColor());
		yoffset += fonter.FONT_HEIGHT + 3;
		fonter.drawString("Mana: " + spell.getManaCost(), xoffset + 32, yoffset, 0xFF354AA8);
		yoffset += fonter.FONT_HEIGHT + 3;
		fonter.drawSplitString(spell.getDescription(), xoffset + 32,
				yoffset, Math.max(32, width - 32), 0xFF000000);
		
		GL11.glPopMatrix();
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		GuiUtils.drawHoveringText(tooltip, trueX, trueY, res.getScaledWidth(), res.getScaledHeight(), 200, fonter);
	}
}
