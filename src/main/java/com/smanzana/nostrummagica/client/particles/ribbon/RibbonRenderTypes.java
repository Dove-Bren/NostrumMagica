package com.smanzana.nostrummagica.client.particles.ribbon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public final class RibbonRenderTypes {
	
	protected static void SetupRibbonRender(BufferBuilder buffer, TextureManager textureManager) {
		RenderSystem.disableCull();
		//RenderSystem.depthMask(true);
		//RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}
	
	protected static void TeardownRibbonRender(Tesselator tesselator) {
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();
	}
	
	public static class ColorRibbonRenderType implements ParticleRenderType {
		public static final ColorRibbonRenderType INSTANCE = new ColorRibbonRenderType();
		
		protected ColorRibbonRenderType() {
			
		}
	
		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			SetupRibbonRender(buffer, textureManager);
			RenderSystem.disableTexture();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		}
	
		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
			RenderSystem.enableTexture();
			TeardownRibbonRender(tesselator);
		}
		
		@Override
		public String toString() {
			return "NostrumMagica::ColorRibbon";
		}
	}
	
	public static class LitColorRibbonRenderType extends ColorRibbonRenderType {
		public static final LitColorRibbonRenderType INSTANCE = new LitColorRibbonRenderType();
		
		protected LitColorRibbonRenderType() {
			super();
		}
	
		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			//super.begin(buffer, textureManager);
			SetupRibbonRender(buffer, textureManager);
			RenderSystem.disableTexture();
			RenderSystem.setShader(GameRenderer::getPositionColorLightmapShader); // Leash?
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP);
		}
	
		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
			RenderSystem.enableTexture();
			TeardownRibbonRender(tesselator);
		}
		
		@Override
		public String toString() {
			return "NostrumMagica::LitColorRibbon";
		}
	}
	
	public static class TexturedRibbonRenderType implements ParticleRenderType {
		protected ResourceLocation texture;
		
		public TexturedRibbonRenderType(ResourceLocation texture) {
			this.texture = texture;
		}

		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			SetupRibbonRender(buffer, textureManager);
			RenderSystem.enableTexture();
			RenderSystem.setShaderTexture(0, texture);
			RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		}

		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
			TeardownRibbonRender(tesselator);
		}
		
		@Override
		public String toString() {
			return "NostrumMagica::TexturedRibbon";
		}
	}
	
	public static class LitTexturedRibbonRenderType extends TexturedRibbonRenderType {
		public LitTexturedRibbonRenderType(ResourceLocation texture) {
			super(texture);
		}

		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			SetupRibbonRender(buffer, textureManager);
			RenderSystem.enableTexture();
			RenderSystem.setShaderTexture(0, texture);
			RenderSystem.setShader(GameRenderer::getRendertypeTextShader); // pos_color_text_lightmap shader doesn't use lightmap! But text happens to line up!
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
		}
		
		@Override
		public String toString() {
			return "NostrumMagica::LitTexturedRibbon";
		}
	}
}