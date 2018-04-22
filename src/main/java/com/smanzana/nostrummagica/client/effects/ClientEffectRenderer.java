package com.smanzana.nostrummagica.client.effects;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Displays effects in the world
 * @author Skyler
 *
 */
@SideOnly(Side.CLIENT)
public class ClientEffectRenderer {

	private List<ClientEffect> activeEffects;
	
	private static ClientEffectRenderer instance = null;
	public static ClientEffectRenderer instance() {
		if (instance == null)
			instance = new ClientEffectRenderer();
		
		return instance;
	}
	
	private ClientEffectRenderer() {
		activeEffects = new LinkedList<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void addEffect(ClientEffect effect) {
		this.activeEffects.add(effect);
	}
	
	public void clearEffects() {
		this.activeEffects.clear();
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		if (!ModConfig.config.displayEffects() || activeEffects.isEmpty()) {
			activeEffects.clear();
			return;
		}
		
		GlStateManager.pushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d playerOffset = mc.thePlayer.getPositionVector();
		GlStateManager.translate(-playerOffset.xCoord, -playerOffset.yCoord, -playerOffset.zCoord);
		Iterator<ClientEffect> it = activeEffects.iterator();
		while (it.hasNext()) {
			ClientEffect ef = it.next();
			if (!ef.displayTick(mc, event.getPartialTicks()))
				it.remove();
		}
		GlStateManager.popMatrix();
	}
}
