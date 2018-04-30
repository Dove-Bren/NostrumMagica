package com.smanzana.nostrummagica.client.gui.infoscreen;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class RitualInfoButton extends InfoButton {

	private RitualRecipe ritual;
	
	public RitualInfoButton(int buttonId, RitualRecipe ritual) {
		super(buttonId, 0, 0);
		this.ritual = ritual;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return new RitualInfoSubScreen(ritual);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		float tint = 1f;
		if (mouseX >= xPosition 
			&& mouseY >= yPosition 
			&& mouseX < xPosition + width 
			&& mouseY < yPosition + height) {
			tint = .75f;
		}
		
		GL11.glColor4f(tint, tint, tint, 1f);
		mc.getTextureManager().bindTexture(InfoScreen.background);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0,
				width, height,
				InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT);
		GlStateManager.disableBlend();
		
		final int itemLength = 16;
		
		ItemStack iconStack = ritual.getIcon();
		if (iconStack != null) {
			int x = xPosition + (width - itemLength) / 2;
			int y = yPosition + (height - itemLength) / 2;
			mc.getRenderItem().renderItemIntoGUI(iconStack, x, y);
		}
	}
}
