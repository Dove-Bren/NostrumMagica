package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class SubscreenInfoButton extends InfoButton {

	private IInfoSubScreen screen;
	private @Nonnull ItemStack icon;
	private String descKey;
	
	public SubscreenInfoButton(int buttonId, String key, IInfoSubScreen screen, @Nonnull ItemStack icon) {
		super(buttonId, 0, 0);
		this.screen = screen;
		this.icon = icon;
		this.descKey = key;
	}

	@Override
	public IInfoSubScreen getScreen(INostrumMagic attr) {
		return screen;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		float tint = 1f;
		if (mouseX >= this.x
			&& mouseY >= this.y 
			&& mouseX < this.x + width 
			&& mouseY < this.y + height) {
			tint = .75f;
		}
		
		GL11.glColor4f(tint, tint, tint, 1f);
		mc.getTextureManager().bindTexture(InfoScreen.background);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, 0, 0,
				width, height,
				InfoScreen.TEXT_WHOLE_WIDTH, InfoScreen.TEXT_WHOLE_HEIGHT);
		GlStateManager.disableBlend();
		
		final int itemLength = 16;
		
		if (!icon.isEmpty()) {
			int x = this.x + (width - itemLength) / 2;
			int y = this.y + (height - itemLength) / 2;
			mc.getRenderItem().renderItemIntoGUI(icon, x, y);
		}
	}
	
	private List<String> desc = new ArrayList<>(1);
	@Override
	public List<String> getDescription() {
		if (desc.isEmpty())
			desc.add(I18n.format("info." + descKey + ".name", new Object[0]));
		
		return desc;
	}
}
