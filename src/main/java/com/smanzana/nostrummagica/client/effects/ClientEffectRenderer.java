package com.smanzana.nostrummagica.client.effects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Displays effects in the world
 * @author Skyler
 *
 */
@OnlyIn(Dist.CLIENT)
public class ClientEffectRenderer {
	
	public static interface ClientEffectFactory {
		
		public ClientEffect build(LivingEntity caster,
			Vector3d sourcePosition,
			LivingEntity target,
			Vector3d destPosition,
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

		Minecraft mc = Minecraft.getInstance();
		final ActiveRenderInfo renderInfo = mc.gameRenderer.getActiveRenderInfo();
		MatrixStack stack = event.getMatrixStack();//RenderFuncs.makeNewMatrixStack(renderInfo);
		
		stack.push();
		
//		 Set up render space. Effects want to render at absolute world positions,
//		 so don't actually offset at all
		Vector3d playerOffset = renderInfo.getProjectedView();
		//Vector3d playerOffset = mc.thePlayer.getPositionVec();
		stack.translate(-playerOffset.x, -playerOffset.y, -playerOffset.z);
		
		synchronized(activeEffects) {
			Iterator<ClientEffect> it = activeEffects.iterator();
			while (it.hasNext()) {
				ClientEffect ef = it.next();
				if (!ef.displayTick(mc, stack, event.getPartialTicks())) {
					ef.onEnd();
					it.remove();
				}
			}
		}
		
		stack.pop();
		
	}
	
	public void registerEffect(SpellComponentWrapper component, ClientEffectFactory factory) {
		registeredEffects.put(component, factory);
	}
	
	private static boolean DidWarned = false;
	
	public void spawnEffect(SpellComponentWrapper component,
			LivingEntity caster,
			Vector3d sourcePosition,
			LivingEntity target,
			Vector3d destPosition,
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
