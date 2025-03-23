package com.smanzana.nostrummagica.client.particles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ParticleBatchRenderer {

	private static ParticleBatchRenderer instance = null;
	
	public static ParticleBatchRenderer instance() {
		if (instance == null) {
			instance = new ParticleBatchRenderer();
		}
		
		return instance;
	}
	
	private List<BatchRenderParticle> batch;
	
	private ParticleBatchRenderer() {
		batch = new ArrayList<>(); // TODO faster to use heap?
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		this.renderBatch(event.getMatrixStack(), event.getPartialTicks());
	}
	
	public void queueParticle(BatchRenderParticle particle) {
		batch.add(particle);
	}
	
	public void renderBatch(PoseStack matrixStackIn, float partialTicks) {
		if (!batch.isEmpty()) {
			Collections.sort(batch);
			BatchRenderParticle last = null;
			final Minecraft mc = Minecraft.getInstance();
			final Camera renderInfo = mc.gameRenderer.getMainCamera();
			
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder buffer = tessellator.getBuilder();
			
			for (BatchRenderParticle next : batch) {
				if (last == null || next.compareTo(last) != 0) {
					
					if (last != null) {
						tessellator.end();
						last.teardownBatchedRender();
					}
					
					RenderSystem.setShaderTexture(0, next.getTexture());
					next.setupBatchedRender();
					buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
				}

				matrixStackIn.pushPose();
				next.renderBatched(matrixStackIn, buffer, renderInfo, partialTicks);
				matrixStackIn.popPose();
				last = next;
			}
			
			tessellator.end();
			if (last != null) {
				last.teardownBatchedRender();
			}
			
			batch.clear();
		}
	}
	
}
