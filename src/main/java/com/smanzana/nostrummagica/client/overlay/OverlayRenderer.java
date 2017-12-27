package com.smanzana.nostrummagica.client.overlay;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
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
	
	private int wiggleIndex; // set to multiples of 12 for each wiggle
	private static final int wiggleOffsets[] = {0, 1, 1, 2, 1, 1, 0, -1, -1, -2, -1, -1};
	
	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		wiggleIndex = 0;
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		// if config says one way or the other, do that
		// like fill in these floats
		// TODO
		if (event.getType() != ElementType.ALL)
			return;
		
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		
		ScaledResolution scaledRes = event.getResolution();
		
		int hudXAnchor = scaledRes.getScaledWidth() / 2 + 89;
		int hudYAnchor = scaledRes.getScaledHeight() - 49;
		
		if (player.isInsideOfMaterial(Material.WATER)) {
			hudYAnchor -= 10;
		}
		
		int wiggleOffset = 0;
		if (wiggleIndex > 0)
			wiggleOffset = OverlayRenderer.wiggleOffsets[wiggleIndex-- % 12];
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null || !attr.isUnlocked())
			return;
		
		int mana = attr.getMana(),
				maxMana = attr.getMaxMana();
		float ratio = (float) mana / (float) maxMana;
		
		// Orbs
		if (!Minecraft.getMinecraft().thePlayer.isCreative())
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
				this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)) + wiggleOffset,
						hudYAnchor,	36, 16, 9, 9);
			}
			
			if (pieces != 0) {
				// Draw a single partle orb
				this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)) + wiggleOffset,
						hudYAnchor, 9 * pieces, 16, 9, 9);
				i++;
			}
			
			for (; i < 10; i++) {
				this.drawTexturedModalRect(hudXAnchor - (8 * (i + 1)) + wiggleOffset,
						hudYAnchor,	0, 16, 9, 9);
			}
		}
		
		
		// Spell name
		Spell current = NostrumMagica.getCurrentSpell(Minecraft.getMinecraft().thePlayer);
		if (current != null) {
			FontRenderer fonter = Minecraft.getMinecraft().fontRendererObj;
			fonter.drawString(current.getName(), 5, scaledRes.getScaledHeight() - (fonter.FONT_HEIGHT + 5), 0xFF000000);
		}
		
	}
	
	public void startManaWiggle(int wiggleCount) {
		this.wiggleIndex = 12 * wiggleCount;
	}
	
}
