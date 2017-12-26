package com.smanzana.nostrummagica.client.overlay;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OverlayRenderer extends Gui {

	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int GUI_ORB_WIDTH = 16;
	private static final int GUI_ORB_HEIGHT = 16;
	private static final int GUI_BAR_WIDTH = 17;
	private static final int GUI_BAR_HEIGHT = 62;
	private static final int GUI_BAR_OFFSETX = 96;
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		// if config says one way or the other, do that
		// like fill in these floats
		// TODO
		if (event.getType() != ElementType.ALL)
			return;
		
		
		ScaledResolution scaledRes = event.getResolution();
		
		int hudXAnchor = scaledRes.getScaledWidth() / 2 + 91;
		int hudYAnchor = scaledRes.getScaledHeight() - 49;
		
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(Minecraft.getMinecraft().thePlayer);
		if (attr == null || !attr.isUnlocked())
			return;
		
		int mana = attr.getMana(),
				maxMana = attr.getMaxMana();
		float ratio = (float) mana / (float) maxMana;
		
		// Orbs
		{
			int parts = Math.round(40 * ratio);
			int whole = parts / 4;
			int pieces = parts % 4;
			
			int i = 0;
			Minecraft.getMinecraft().getTextureManager().bindTexture(GUI_ICONS);
			for (; i < whole; i++) {
				//this.drawTexturedModalRect(hudXAnchor - (), y, textureX, textureY, width, height);
//				Gui.drawModalRectWithCustomSizedTexture(hudXAnchor - (9 * i), hudYAnchor,
//						64, 0, 16, 16, 256, 256);
				// Draw a single partle orb
				this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)), hudYAnchor,
						36, 16, 9, 9);
			}
			
			if (pieces != 0) {
				// Draw a single partle orb
				this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)), hudYAnchor,
						9 * pieces, 16, 9, 9);
				i++;
			}
			
			for (; i < 10; i++) {
				this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)), hudYAnchor,
						0, 16, 9, 9);
			}
		}
		
	}
	
}
