package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.MagicTierIcon;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.ShrineTriggerEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public abstract class RenderShrineTrigger<E extends ShrineTriggerEntity<?>> extends EntityRenderer<E> {

	private static final ResourceLocation TEX_BUBBLE = NostrumMagica.Loc("textures/block/shrine_bubble.png");
	private static final ResourceLocation TEX_BUBBLE_DAM1 = NostrumMagica.Loc("textures/block/shrine_bubble_1.png");
	private static final ResourceLocation TEX_BUBBLE_DAM2 = NostrumMagica.Loc("textures/block/shrine_bubble_2.png");
	private static final ResourceLocation TEX_BUBBLE_DAM3 = NostrumMagica.Loc("textures/block/shrine_bubble_3.png");
	
	public RenderShrineTrigger(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(E entity) {
		return TEX_BUBBLE;
	}
	
	protected abstract void renderSymbol(E entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, float scale, float glow, float[] color, int packedLightIn);
	
	@Override
	public void render(E entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final Minecraft mc = Minecraft.getInstance();
		if (entityIn.isInvisibleTo(mc.player)) {
			return;
		}
		
		final ResourceLocation bubbleTex;
		if (entityIn.getHitCount() <= 0) {
			bubbleTex = TEX_BUBBLE;
		} else if (entityIn.getHitCount() <= 1) {
			bubbleTex = TEX_BUBBLE_DAM1;
		} else if (entityIn.getHitCount() <= 2) {
			bubbleTex = TEX_BUBBLE_DAM2;
		} else {
			bubbleTex = TEX_BUBBLE_DAM3;
		}
		
		final float[] color = {1f, 1f, 1f, 1f};
		final float ticks = entityIn.tickCount + partialTicks;
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, .5, 0);
		
		final float hoverPeriod = 80f;
		final float hoverOffset = .05f * (float) Math.sin(((ticks % hoverPeriod) / hoverPeriod) * Math.PI * 2);
		matrixStackIn.translate(0, hoverOffset, 0);
		
		matrixStackIn.mulPose(this.entityRenderDispatcher.cameraOrientation());
		//matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
		
		// Render symbol
		matrixStackIn.pushPose();
		{
			final float glowPeriod = 40f;
			float prog = (ticks%glowPeriod) / glowPeriod;
			final float glow = .7f + ((float) Math.sin(prog * Math.PI * 2) * .15f);
			
			final float scalePeriod = 90f;
			prog = (ticks % scalePeriod) / scalePeriod;
			final float scale = .95f + .05f * (float) Math.sin(prog * Math.PI * 2);

			this.renderSymbol(entityIn, matrixStackIn, bufferIn, scale, glow, color, packedLightIn);
		}
		matrixStackIn.popPose();
		
		// Render bubble
		matrixStackIn.pushPose();
		{
			Matrix4f transform = matrixStackIn.last().pose();
			Matrix3f normal = matrixStackIn.last().normal();
			VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(bubbleTex, true));
			buffer.vertex(transform, -0.5f, -.5f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(0, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
			buffer.vertex(transform, 0.5f, -.5f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
			buffer.vertex(transform, 0.5f, .5f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
			buffer.vertex(transform, -0.5f, .5f, 0.0f).color(color[0], color[1], color[2], color[3]).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		}
		matrixStackIn.popPose();
		//super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.popPose();
	}
	
	protected static abstract class SpellComponentRender<E extends ShrineTriggerEntity<?>> extends RenderShrineTrigger<E> {
		public SpellComponentRender(EntityRenderDispatcher renderManagerIn) {
			super(renderManagerIn);
		}
		
		protected abstract SpellComponentIcon getIcon(E entity);
		
		protected void renderSymbol(E entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, float scale, float glow, float[] color, int packedLightIn) {
			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			matrixStackIn.scale(.75f, .75f, 1f);
			matrixStackIn.scale(scale, scale, 1f);
			matrixStackIn.translate(-.5, -.5, 0);
			SpellComponentIcon icon = this.getIcon(entityIn);
			icon.draw(matrixStackIn, bufferIn, packedLightIn, 1, 1, false, color[0] * glow, color[1] * glow, color[2] * glow, color[3]);
		}
	}
	
	public static class Element extends SpellComponentRender<ShrineTriggerEntity.Element> {
		
		public Element(EntityRenderDispatcher renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected SpellComponentIcon getIcon(ShrineTriggerEntity.Element entity) {
			return SpellComponentIcon.get(entity.getElement());
		}
		
	}
	
	public static class Shape extends SpellComponentRender<ShrineTriggerEntity.Shape> {
		
		public Shape(EntityRenderDispatcher renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected SpellComponentIcon getIcon(ShrineTriggerEntity.Shape entity) {
			return SpellComponentIcon.get(entity.getShape());
		}
		
	}
	
	public static class Alteration extends SpellComponentRender<ShrineTriggerEntity.Alteration> {
		
		public Alteration(EntityRenderDispatcher renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected SpellComponentIcon getIcon(ShrineTriggerEntity.Alteration entity) {
			return SpellComponentIcon.get(entity.getAlteration());
		}
		
	}
	
	public static class Tier extends RenderShrineTrigger<ShrineTriggerEntity.Tier> {
		
		public Tier(EntityRenderDispatcher renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected void renderSymbol(ShrineTriggerEntity.Tier entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, float scale, float glow, float[] color, int packedLightIn) {
			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
			matrixStackIn.scale(.75f, .75f, 1f);
			matrixStackIn.scale(scale, scale, 1f);
			matrixStackIn.translate(-.5, -.5, 0);
			MagicTierIcon icon = MagicTierIcon.get(entityIn.getTier());
			icon.draw(matrixStackIn, bufferIn, packedLightIn, 1, 1, false, .01f * glow, .01f * glow, .01f * glow, color[3]);
		}
		
	}
	
}
