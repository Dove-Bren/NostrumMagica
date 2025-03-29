package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelRenderShiv;
import com.smanzana.nostrummagica.client.model.ModelWillo;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.entity.WilloEntity;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderWillo extends MobRenderer<WilloEntity, ModelRenderShiv<WilloEntity>> {

	private static final ResourceLocation RES_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/willo.png");
	
	private ModelWillo mainModel;
	
	public RenderWillo(EntityRendererProvider.Context renderManagerIn, float scale) {
		super(renderManagerIn, new ModelRenderShiv<WilloEntity>(RenderType::entityCutoutNoCull), .33f);
		mainModel = new ModelWillo(renderManagerIn.bakeLayer(NostrumModelLayers.Willo));
	}
	
	protected void renderFace(WilloEntity entity, PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		
		// Choose face based on status
		final float umin;
		final float umax;
		final float vmin;
		final float vmax;
		
		switch (entity.getStatus()) {
		case NEUTRAL:
		default:
			umin = 0;
			umax = umin + (18f/64f);
			vmin = 0;
			vmax = vmin + (18f/64f);
			break;
		case PANIC:
			umin = (18f/64f);
			umax = umin + (18f/64f);
			vmin = 0;
			vmax = vmin + (18f/64f);
			break;
		case AGGRO:
			umin = (18f/64f) + (18f/64f);
			umax = umin + (18f/64f);
			vmin = 0;
			vmax = vmin + (18f/64f);
			break;
		}
		
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		
		// North
		buffer.vertex(transform, .5f, .5f, 0f).color(red, green, blue, alpha).uv(umax,vmax).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, .5773f, .5773f, -.5773f).endVertex();
		buffer.vertex(transform, .5f, -.5f, 0f).color(red, green, blue, alpha).uv(umax,vmin).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, .5773f, -.5773f, -.5773f).endVertex();
		buffer.vertex(transform, -.5f, -.5f, 0f).color(red, green, blue, alpha).uv(umin,vmin).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, -.5773f, -.5773f, -.5773f).endVertex();
		buffer.vertex(transform, -.5f, .5f, 0f).color(red, green, blue, alpha).uv(umin,vmax).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, -.5773f, .5773f, -.5773f).endVertex();
		
//		// South
//		buffer.pos(-.5, .5, .01).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(-.5, -.5, .01).tex(umin,vmin).normal(-.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, -.5, .01).tex(umax,vmin).normal(.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, .5, .01).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
	}
	
	protected void renderCube(WilloEntity entity, PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		final float umin = 0;
		final float umax = umin + (18f/64f);
		final float vmin = (36f/64f);
		final float vmax = vmin + (18f/64f);
		
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, umin, umax, vmin, vmax, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
//		// Top
//		buffer.pos(-.5, .5, -.5).tex(umin,vmin).normal(-.5773f, .5773f, -.5773f).endVertex();
//		buffer.pos(-.5, .5, .5).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(.5, .5, .5).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(.5, .5, -.5).tex(umax,vmin).normal(.5773f, .5773f, -.5773f).endVertex();
//		
//		// North
//		buffer.pos(.5, .5, -.5).tex(umax,vmin).normal(.5773f, .5773f, -.5773f).endVertex();
//		buffer.pos(.5, -.5, -.5).tex(umax,vmax).normal(.5773f, -.5773f, -.5773f).endVertex();
//		buffer.pos(-.5, -.5, -.5).tex(umin,vmax).normal(-.5773f, -.5773f, -.5773f).endVertex();
//		buffer.pos(-.5, .5, -.5).tex(umin,vmin).normal(-.5773f, .5773f, -.5773f).endVertex();
//		
//		// East
//		buffer.pos(.5, .5, .5).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(.5, -.5, .5).tex(umin,vmax).normal(.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, -.5, -.5).tex(umin,vmin).normal(.5773f, -.5773f, -.5773f).endVertex();
//		buffer.pos(.5, .5, -.5).tex(umax,vmin).normal(.5773f, .5773f, -.5773f).endVertex();
//		
//		// South
//		buffer.pos(-.5, .5, .5).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(-.5, -.5, .5).tex(umin,vmin).normal(-.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, -.5, .5).tex(umax,vmin).normal(.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, .5, .5).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
//		
//		// West
//		buffer.pos(-.5, .5, -.5).tex(umin,vmin).normal(-.5773f, .5773f, -.5773f).endVertex();
//		buffer.pos(-.5, -.5, -.5).tex(umax,vmin).normal(-.5773f, -.5773f, -.5773f).endVertex();
//		buffer.pos(-.5, -.5, .5).tex(umax,vmax).normal(-.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(-.5, .5, .5).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
//		
//		// Bottom
//		buffer.pos(-.5, -.5, -.5).tex(umax,vmin).normal(-.5773f, -.5773f, -.5773f).endVertex();
//		buffer.pos(.5, -.5, -.5).tex(umin,vmin).normal(.5773f, -.5773f, -.5773f).endVertex();
//		buffer.pos(.5, -.5, .5).tex(umin,vmax).normal(.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(-.5, -.5, .5).tex(umax,vmax).normal(-.5773f, -.5773f, .5773f).endVertex();
	}
	
	public void renderModels(WilloEntity entityIn, float partialTicks, PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		
		// GlStateManager.color4f(.65f, 1f, .7f, 1f);
		final float rotPeriod = 6f * 20f;
		final float time = partialTicks + entityIn.tickCount;
		final float rotX = 360f * (time % rotPeriod) / rotPeriod;
		final float[] color = ColorUtil.ARGBToColor(entityIn.getElement().getColor());
		
		this.mainModel.prepareMobModel(entityIn, 0, 0, partialTicks);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 1.5f, 0);
		matrixStackIn.translate(0, -entityIn.getBbHeight() / 2, 0);
		
		// Render main model body
		matrixStackIn.pushPose();
		matrixStackIn.scale(.25f, .25f, .25f);
		this.mainModel.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
		
		// Render face inside main body
		matrixStackIn.pushPose();
		matrixStackIn.scale(.4f, .4f, .4f);
		renderFace(entityIn, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red * .65f, green * 1f, blue * .7f, alpha);
		matrixStackIn.popPose();
		
		// Render face inside main body
		matrixStackIn.pushPose();
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(rotX));
		renderCube(entityIn, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red * color[0], green * color[1], blue * color[2], alpha);
		matrixStackIn.popPose();
		
		matrixStackIn.popPose();
	}
	
	@Override
	public void render(WilloEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		this.model.setPayload((deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha) -> {
			// Could pass through bufferIn to allow access to different buffer types, but only need the base one
			this.renderModels(entityIn, partialTicks, deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha);
		});
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
//		GlStateManager.color4f(.65f, 1f, .7f, 1f);
//		GlStateManager.disableBlend();
//		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//		GlStateManager.disableCull();
//		super.doRender(entity, x, y, z, entityYaw, partialTicks);
//		GlStateManager.enableCull();
//		GlStateManager.color3f(1f, 1f, 1f);
	}

	@Override
	public ResourceLocation getTextureLocation(WilloEntity entity) {
		return RES_TEXT;
	}
	
}
