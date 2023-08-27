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
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Displays effects in the world
 * @author Skyler
 *
 */
@OnlyIn(Dist.CLIENT)
public class ClientEffectRenderer {
	
	public static interface ClientEffectFactory {
		
		public ClientEffect build(LivingEntity caster,
			Vec3d sourcePosition,
			LivingEntity target,
			Vec3d destPosition,
			SpellComponentWrapper flavor,
			boolean isNegative,
			float compParam);
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
		effect.onStart();
	}
	
	public void clearEffects() {
		for (ClientEffect effect : activeEffects) {
			effect.onEnd();
		}
		this.activeEffects.clear();
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		if (!ModConfig.config.displayEffects() || activeEffects.isEmpty()) {
			clearEffects();
			return;
		}
		
		GlStateManager.pushMatrix();
		Minecraft mc = Minecraft.getInstance();
		
		Vec3d playerOffset = mc.player.getEyePosition(event.getPartialTicks()).add(0, -mc.player.getEyeHeight(), 0);
		//Vec3d playerOffset = mc.thePlayer.getPositionVector();
		GlStateManager.translated(-playerOffset.x, -playerOffset.y, -playerOffset.z);
		
		synchronized(activeEffects) {
			Iterator<ClientEffect> it = activeEffects.iterator();
			while (it.hasNext()) {
				ClientEffect ef = it.next();
				if (!ef.displayTick(mc, event.getPartialTicks())) {
					ef.onEnd();
					it.remove();
				}
			}
		}
		
		GlStateManager.popMatrix();
	}
	
	public void registerEffect(SpellComponentWrapper component, ClientEffectFactory factory) {
		registeredEffects.put(component, factory);
	}
	
	private static boolean DidWarned = false;
	
	public void spawnEffect(SpellComponentWrapper component,
			LivingEntity caster,
			Vec3d sourcePosition,
			LivingEntity target,
			Vec3d destPosition,
			SpellComponentWrapper flavor,
			boolean isNegative,
			float compParam) {
		ClientEffectFactory factory = registeredEffects.get(component);
		if (factory == null) {
			if (!DidWarned) {
				NostrumMagica.logger.warn("Trying to spawn effect for unmapped component. Create a mapping for the component " + component);
				DidWarned = true;
			}
			return;
		}
		
		ClientEffect effect = factory.build(caster, sourcePosition, target, destPosition, flavor, isNegative, compParam);
		if (effect == null)
			return;
		
		this.addEffect(effect);
	}
}
