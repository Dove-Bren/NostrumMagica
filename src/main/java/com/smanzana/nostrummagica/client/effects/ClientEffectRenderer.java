package com.smanzana.nostrummagica.client.effects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Displays effects in the world
 * @author Skyler
 *
 */
@OnlyIn(Dist.CLIENT)
public class ClientEffectRenderer {
	
	public static interface ClientShapeEffectFactory {
		
		public ClientEffect build(LivingEntity caster,
			Vec3 sourcePosition,
			LivingEntity target,
			Vec3 destPosition,
			SpellShapeProperties properties,
			SpellCharacteristics characteristics);
	}
	
	public static interface ClientActionEffectFactory {
		
		public ClientEffect build(LivingEntity caster,
			Vec3 sourcePosition,
			LivingEntity target,
			Vec3 destPosition,
			SpellEffectPart effect);
	}
	
	private static record EffectPair(EMagicElement element, EAlteration alteration) {}

	private final Map<SpellShape, ClientShapeEffectFactory> registeredShapeEffects;
	private final Map<EffectPair, ClientActionEffectFactory> registeredActionEffects;
	private final Map<EAlteration, ClientActionEffectFactory> registeredAlterationEffects;
	private final List<ClientEffect> activeEffects;
	
	private static ClientEffectRenderer instance = null;
	public static ClientEffectRenderer instance() {
		if (instance == null)
			instance = new ClientEffectRenderer();
		
		return instance;
	}
	
	private ClientEffectRenderer() {
		activeEffects = Collections.synchronizedList(new LinkedList<>());
		registeredShapeEffects = new HashMap<>();
		registeredAlterationEffects = new HashMap<>();
		registeredActionEffects = new HashMap<>();
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
	public void onRender(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) {
			return;
		}
		
		if (!ModConfig.config.displayEffects() || activeEffects.isEmpty()) {
			clearEffects();
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		final Camera renderInfo = mc.gameRenderer.getMainCamera();
		PoseStack stack = event.getPoseStack();//RenderFuncs.makeNewMatrixStack(renderInfo);
		final MultiBufferSource buffers = mc.renderBuffers().bufferSource();;
		
		stack.pushPose();
		
//		 Set up render space. Effects want to render at absolute world positions,
//		 so don't actually offset at all
		Vec3 playerOffset = renderInfo.getPosition();
		//Vector3d playerOffset = mc.thePlayer.getPositionVec();
		stack.translate(-playerOffset.x, -playerOffset.y, -playerOffset.z);
		
		synchronized(activeEffects) {
			Iterator<ClientEffect> it = activeEffects.iterator();
			while (it.hasNext()) {
				ClientEffect ef = it.next();
				if (!ef.displayTick(mc, stack, event.getPartialTick(), buffers)) {
					ef.onEnd();
					it.remove();
				}
			}
		}
		
		mc.renderBuffers().bufferSource().endBatch();
		
		stack.popPose();
		
	}
	
	public void registerEffect(SpellShape shape, ClientShapeEffectFactory factory) {
		this.registeredShapeEffects.put(shape, factory);
	}
	
	public void registerEffect(@Nullable EAlteration alteration, ClientActionEffectFactory factory) {
		this.registeredAlterationEffects.put(alteration, factory);
	}
	
	public void registerEffect(EMagicElement element, @Nullable EAlteration alteration, ClientActionEffectFactory factory) {
		this.registeredActionEffects.put(new EffectPair(element, alteration), factory);
	}
	
	private static boolean DidWarned = false;
	
	public void spawnEffect(SpellShape shape,
			LivingEntity caster,
			Vec3 sourcePosition,
			LivingEntity target,
			Vec3 destPosition,
			SpellShapeProperties properties,
			SpellCharacteristics characteristics) {
		ClientShapeEffectFactory factory = registeredShapeEffects.get(shape);
		if (factory == null) {
			if (!DidWarned) {
				NostrumMagica.logger.warn("Trying to spawn effect for unmapped shape. Create a mapping for the component " + shape.getShapeKey());
				DidWarned = true;
			}
			return;
		}
		
		ClientEffect effect = factory.build(caster, sourcePosition, target, destPosition, properties, characteristics);
		if (effect == null)
			return;
		
		this.addEffect(effect);
	}
	
	public void spawnEffect(SpellEffectPart effect,
			LivingEntity caster,
			Vec3 sourcePosition,
			LivingEntity target,
			Vec3 destPosition) {
		final var effectParts = new EffectPair(effect.getElement(), effect.getAlteration());
		ClientActionEffectFactory factory = registeredActionEffects.get(effectParts);
		
		if (factory == null) {
			factory = registeredAlterationEffects.get(effect.getAlteration());
		}
		
		if (factory == null) {
			if (!DidWarned) {
				NostrumMagica.logger.warn("Trying to spawn effect for unmapped alteration. Create a mapping for the component " + effectParts);
				DidWarned = true;
			}
			return;
		}
		
		ClientEffect vfx = factory.build(caster, sourcePosition, target, destPosition, effect);
		if (vfx == null)
			return;
		
		this.addEffect(vfx);
	}
}
