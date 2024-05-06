package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityWillo;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderWillo extends MobRenderer<EntityWillo, ModelRenderShiv<EntityWillo>> {

	private static final ResourceLocation RES_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/willo.png");
	
	private ModelWillo mainModel;
	
	public RenderWillo(EntityRendererManager renderManagerIn, float scale) {
		super(renderManagerIn, new ModelRenderShiv<EntityWillo>(RenderType::getEntityTranslucent), .33f);
		mainModel = new ModelWillo();
	}
	
	protected void renderFace(EntityWillo entity, MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn,
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
		
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		
		// North
		buffer.pos(transform, .5f, .5f, 0f).color(red, green, blue, alpha).tex(umax,vmax).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, .5773f, .5773f, -.5773f).endVertex();
		buffer.pos(transform, .5f, -.5f, 0f).color(red, green, blue, alpha).tex(umax,vmin).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, .5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(transform, -.5f, -.5f, 0f).color(red, green, blue, alpha).tex(umin,vmin).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, -.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(transform, -.5f, .5f, 0f).color(red, green, blue, alpha).tex(umin,vmax).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, -.5773f, .5773f, -.5773f).endVertex();
		
//		// South
//		buffer.pos(-.5, .5, .01).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(-.5, -.5, .01).tex(umin,vmin).normal(-.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, -.5, .01).tex(umax,vmin).normal(.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, .5, .01).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
	}
	
	protected void renderCube(EntityWillo entity, MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		final float umin = 0;
		final float umax = umin + (18f/64f);
		final float vmin = (36f/64f);
		final float vmax = vmin + (18f/64f);
		
		int unused; // need to pass overlay in to get red flash when hurt
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, umin, umax, vmin, vmax, packedLightIn, red, green, blue, alpha);
		
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
	
	public void renderModels(EntityWillo entityIn, float partialTicks, MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		
		// GlStateManager.color4f(.65f, 1f, .7f, 1f);
		red *= .65f;
		green *= 1f;
		blue *= .7f;
		alpha *= 1f;
		
		final float rotPeriod = 6f * 20f;
		final float time = partialTicks + entityIn.ticksExisted;
		final float rotX = 360f * (time % rotPeriod) / rotPeriod;
		
		this.mainModel.setLivingAnimations(entityIn, 0, 0, partialTicks);
		
		matrixStackIn.push();
		matrixStackIn.translate(0, 1.5f, 0);
		matrixStackIn.translate(0, -entityIn.getHeight() / 2, 0);
		
		// Render main model body
		matrixStackIn.push();
		matrixStackIn.scale(.25f, .25f, .25f);
		this.mainModel.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
		
		// Render face inside main body
		matrixStackIn.push();
		matrixStackIn.scale(.4f, .4f, .4f);
		renderFace(entityIn, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
		
		// Render face inside main body
		matrixStackIn.push();
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(rotX));
		renderCube(entityIn, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
		
		matrixStackIn.pop();
	}
	
	@Override
	public void render(EntityWillo entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		this.entityModel.setPayload((deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha) -> {
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
	public ResourceLocation getEntityTexture(EntityWillo entity) {
		return RES_TEXT;
	}
	
}
