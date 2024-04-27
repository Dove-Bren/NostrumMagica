package com.smanzana.nostrummagica.client.particles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
		this.renderBatch(event.getPartialTicks());
	}
	
	public void queueParticle(BatchRenderParticle particle) {
		batch.add(particle);
	}
	
	public void renderBatch(float partialTicks) {
		if (!batch.isEmpty()) {
			Collections.sort(batch);
			BatchRenderParticle last = null;
			final Minecraft mc = Minecraft.getInstance();
			final ActiveRenderInfo renderInfo = mc.gameRenderer.getActiveRenderInfo();
			
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			
			for (BatchRenderParticle next : batch) {
				if (last == null || next.compareTo(last) != 0) {
					
					if (last != null) {
						tessellator.draw();
						last.teardownBatchedRender();
					}
					
					mc.getTextureManager().bindTexture(next.getTexture());
					next.setupBatchedRender();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
				}
				
				next.renderBatched(buffer, renderInfo, partialTicks);
				last = next;
			}
			
			tessellator.draw();
			if (last != null) {
				last.teardownBatchedRender();
			}

//			GlStateManager.popAttributes();
//			GlStateManager.depthMask(true);
//			GlStateManager.enableLighting();
//			GlStateManager.disableBlend();
//			GlStateManager.enableAlphaTest();
//			GlStateManager.alphaFunc(516, .1f);
//			GlStateManager.color4f(1f, 1f, 1f, 1f);
//			GlStateManager.enableTexture();
//			GlStateManager.enableColorMaterial();
			
			batch.clear();
		}
	}
	
}
