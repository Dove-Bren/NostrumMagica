package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.MagicTierIcon;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.ShrineTriggerEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public abstract class RenderShrineTrigger<E extends ShrineTriggerEntity<?>> extends EntityRenderer<E> {

	private static final ResourceLocation TEX_BUBBLE = NostrumMagica.Loc("textures/block/shrine_bubble.png");
	private static final ResourceLocation TEX_BUBBLE_DAM1 = NostrumMagica.Loc("textures/block/shrine_bubble_1.png");
	private static final ResourceLocation TEX_BUBBLE_DAM2 = NostrumMagica.Loc("textures/block/shrine_bubble_2.png");
	private static final ResourceLocation TEX_BUBBLE_DAM3 = NostrumMagica.Loc("textures/block/shrine_bubble_3.png");
	
	public RenderShrineTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getEntityTexture(E entity) {
		return TEX_BUBBLE;
	}
	
	protected abstract void renderSymbol(E entityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float scale, float glow, float[] color, int packedLightIn);
	
	@Override
	public void render(E entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final Minecraft mc = Minecraft.getInstance();
		if (entityIn.isInvisibleToPlayer(mc.player)) {
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
		final float ticks = entityIn.ticksExisted + partialTicks;
		
		matrixStackIn.push();
		matrixStackIn.translate(0, .5, 0);
		
		final float hoverPeriod = 80f;
		final float hoverOffset = .05f * (float) Math.sin(((ticks % hoverPeriod) / hoverPeriod) * Math.PI * 2);
		matrixStackIn.translate(0, hoverOffset, 0);
		
		matrixStackIn.rotate(this.renderManager.getCameraOrientation());
		//matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
		
		// Render symbol
		matrixStackIn.push();
		{
			final float glowPeriod = 40f;
			float prog = (ticks%glowPeriod) / glowPeriod;
			final float glow = .7f + ((float) Math.sin(prog * Math.PI * 2) * .15f);
			
			final float scalePeriod = 90f;
			prog = (ticks % scalePeriod) / scalePeriod;
			final float scale = .95f + .05f * (float) Math.sin(prog * Math.PI * 2);

			this.renderSymbol(entityIn, matrixStackIn, bufferIn, scale, glow, color, packedLightIn);
		}
		matrixStackIn.pop();
		
		// Render bubble
		matrixStackIn.push();
		{
			Matrix4f transform = matrixStackIn.getLast().getMatrix();
			Matrix3f normal = matrixStackIn.getLast().getNormal();
			IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(bubbleTex, true));
			buffer.pos(transform, -0.5f, -.5f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(0, 1f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(transform, 0.5f, -.5f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(1f, 1f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(transform, 0.5f, .5f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(1f, 0f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(transform, -0.5f, .5f, 0.0f).color(color[0], color[1], color[2], color[3]).tex(0f, 0f).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		}
		matrixStackIn.pop();
		//super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.pop();
	}
	
	protected static abstract class SpellComponentRender<E extends ShrineTriggerEntity<?>> extends RenderShrineTrigger<E> {
		public SpellComponentRender(EntityRendererManager renderManagerIn) {
			super(renderManagerIn);
		}
		
		protected abstract SpellComponentIcon getIcon(E entity);
		
		protected void renderSymbol(E entityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float scale, float glow, float[] color, int packedLightIn) {
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0F));
			matrixStackIn.scale(.75f, .75f, 1f);
			matrixStackIn.scale(scale, scale, 1f);
			matrixStackIn.translate(-.5, -.5, 0);
			SpellComponentIcon icon = this.getIcon(entityIn);
			icon.draw(matrixStackIn, bufferIn, packedLightIn, 1, 1, false, color[0] * glow, color[1] * glow, color[2] * glow, color[3]);
		}
	}
	
	public static class Element extends SpellComponentRender<ShrineTriggerEntity.Element> {
		
		public Element(EntityRendererManager renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected SpellComponentIcon getIcon(ShrineTriggerEntity.Element entity) {
			return SpellComponentIcon.get(entity.getElement());
		}
		
	}
	
	public static class Shape extends SpellComponentRender<ShrineTriggerEntity.Shape> {
		
		public Shape(EntityRendererManager renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected SpellComponentIcon getIcon(ShrineTriggerEntity.Shape entity) {
			return SpellComponentIcon.get(entity.getShape());
		}
		
	}
	
	public static class Alteration extends SpellComponentRender<ShrineTriggerEntity.Alteration> {
		
		public Alteration(EntityRendererManager renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected SpellComponentIcon getIcon(ShrineTriggerEntity.Alteration entity) {
			return SpellComponentIcon.get(entity.getAlteration());
		}
		
	}
	
	public static class Tier extends RenderShrineTrigger<ShrineTriggerEntity.Tier> {
		
		public Tier(EntityRendererManager renderManagerIn) {
			super(renderManagerIn);
		}
		
		@Override
		protected void renderSymbol(ShrineTriggerEntity.Tier entityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, float scale, float glow, float[] color, int packedLightIn) {
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0F));
			matrixStackIn.scale(.75f, .75f, 1f);
			matrixStackIn.scale(scale, scale, 1f);
			matrixStackIn.translate(-.5, -.5, 0);
			MagicTierIcon icon = MagicTierIcon.get(entityIn.getTier());
			icon.draw(matrixStackIn, bufferIn, packedLightIn, 1, 1, false, .01f * glow, .01f * glow, .01f * glow, color[3]);
		}
		
	}
	
}
