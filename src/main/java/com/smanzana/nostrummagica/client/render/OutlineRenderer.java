package com.smanzana.nostrummagica.client.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class OutlineRenderer {
	
	public static final class Outline {
		public float red;
		public float green;
		public float blue;
		public float alpha;
		
		public Outline() {
			this(1f, 1f, 1f, 1f);
		}

		public Outline(float red, float green, float blue, float alpha) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
		}
	}
	
	// Per-frame tracking of whether vanilla is going to do any glow rendering
	private boolean renderEntityDoingGlow = false;

	private CustomOutlineTypeBuffer outlineBuffer;
	private boolean renderRecurseMarker = false;
	
	private final Map<Entity, Outline> outlineEntities;

	public OutlineRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
		this.outlineEntities = new HashMap<>();
	}
	
	public synchronized void add(Entity ent, Outline outline) {
		this.outlineEntities.put(ent, outline);
	}
	
	public synchronized void remove(Entity ent) {
		this.outlineEntities.remove(ent);
	}
	
	public synchronized void cleanup() {
		Iterator<Entity> it = this.outlineEntities.keySet().iterator();
		while (it.hasNext()) {
			Entity ent = it.next();
			if (ent == null || !ent.isAlive()) {
				it.remove();
			}
		}
	}
	
	private void setupOutlineBuffers(MultiBufferSource.BufferSource bufferIn) {
		outlineBuffer = new CustomOutlineTypeBuffer(bufferIn, 1f, 1f, 0f, 1f);
	}
	
	protected void renderEntityOutline(PoseStack matrixStackIn, Entity entity, Outline outline, float rotationYaw, float partialTicks) {
		if (outlineBuffer == null) {
			this.setupOutlineBuffers(Minecraft.getInstance().renderBuffers().bufferSource());
		}
		
		outlineBuffer.color(outline.red, outline.green, outline.blue, outline.alpha);
		Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0, 0, 0, rotationYaw, partialTicks, matrixStackIn, outlineBuffer, 0xFFFFFFFF);
	}
	
	private void forceRenderOutline(float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		final LevelRenderer worldRenderer = mc.levelRenderer;
		final PostChain outlineShader = ObfuscationReflectionHelper.getPrivateValue(LevelRenderer.class, worldRenderer, "entityEffect");
		
		outlineShader.process(partialTicks);
		mc.getMainRenderTarget().bindWrite(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableTexture(); // Rendering shaders disables texture but world renderer expects it to be enabled at this point for particles?
	}
	
	@SubscribeEvent
	public final void onEntityRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		if (!renderRecurseMarker) {
			final LivingEntity entity = event.getEntity();
			final PoseStack matrixStackIn = event.getMatrixStack();
			
			if (Minecraft.getInstance().shouldEntityAppearGlowing(entity)) {
				renderEntityDoingGlow = true; // Note that vanilla is doing glow rendering
			}
			
			@Nullable Outline outline;
			synchronized (this) {
				outline = this.outlineEntities.get(entity);
			}
			if (outline != null) {
				final float partialTicks = event.getPartialRenderTick();
				renderRecurseMarker = true;
				{
					renderEntityOutline(matrixStackIn, entity, outline, entity.getViewYRot(partialTicks), partialTicks);
					if (renderEntityDoingGlow) {
						// Flush our outlines to the framebuffer since vanilla's going to render it
						if (outlineBuffer != null) {
							outlineBuffer.finish();
						}
					}
				}
				renderRecurseMarker = false;
			}
		}
	}
	
	@SubscribeEvent
	public final void onRenderWorldBegin(CameraSetup event) {
		this.renderEntityDoingGlow = false; // Reset at beginning of render frame
	}
	
	@SubscribeEvent
	public final void onRenderLast(RenderWorldLastEvent event) {
		if (!renderEntityDoingGlow) {
			// Vanilla not doing normal outline rendering, so force it ourselves
			if (outlineBuffer != null) {
				outlineBuffer.finish();
			}
			forceRenderOutline(event.getPartialTicks());
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			this.cleanup();
		}
	}
	
}
