package com.smanzana.nostrummagica.client.effects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
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
	
	public static interface ClientEffectFactory {
		
		public ClientEffect build(EntityLivingBase caster,
			Vec3d sourcePosition,
			EntityLivingBase target,
			Vec3d destPosition,
			SpellComponentWrapper flavor);
	}

	private Map<SpellComponentWrapper, ClientEffectFactory> registeredEffects;
	private List<ClientEffect> activeEffects;
	
	private static ClientEffectRenderer instance = null;
	public static ClientEffectRenderer instance() {
		if (instance == null)
			instance = new ClientEffectRenderer();
		
		return instance;
	}
	
	private ClientEffectRenderer() {
		activeEffects = Collections.synchronizedList(new LinkedList<>());
		registeredEffects = new HashMap<>();
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
		synchronized(activeEffects) {
		Iterator<ClientEffect> it = activeEffects.iterator();
			while (it.hasNext()) {
				ClientEffect ef = it.next();
				if (!ef.displayTick(mc, event.getPartialTicks()))
					it.remove();
			}
		}
		GlStateManager.popMatrix();
	}
	
	public void registerEffect(SpellComponentWrapper component, ClientEffectFactory factory) {
		registeredEffects.put(component, factory);
	}
	
	public void spawnEffect(SpellComponentWrapper component,
			EntityLivingBase caster,
			Vec3d sourcePosition,
			EntityLivingBase target,
			Vec3d destPosition,
			SpellComponentWrapper flavor) {
		ClientEffectFactory factory = registeredEffects.get(component);
		if (factory == null) {
			NostrumMagica.logger.warn("Trying to spawn effect for unmapped component. Create a mapping for the component " + component);
			return;
		}
		
		ClientEffect effect = factory.build(caster, sourcePosition, target, destPosition, flavor);
		if (effect == null)
			return;
		
		this.addEffect(effect);
	}
}
