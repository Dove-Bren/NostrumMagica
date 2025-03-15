package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelBaked;
import com.smanzana.nostrummagica.client.model.ModelRenderShiv;
import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderKoid extends MobRenderer<KoidEntity, ModelRenderShiv<KoidEntity>> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/koid");
	
	protected ModelBaked<KoidEntity> modelBase;

	public RenderKoid(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelRenderShiv<>(), shadowSizeIn);
		this.modelBase = new ModelBaked<>(MODEL);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(KoidEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS;
	}
	
	@Override
	public void render(KoidEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		this.model.setPayload((deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha) -> {
			// Could pass through bufferIn to allow access to different buffer types, but only need the base one
			this.renderModel(entityIn, deferredStack, deferredBufferIn, deferredPackedLightIn, packedOverlayIn, red, green, blue, alpha, partialTicks);
		});
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	protected int getColor(KoidEntity koid) {
		int color = 0x00;
		
		switch (koid.getElement()) {
		case EARTH:
			color = 0xFF704113;
			break;
		case ENDER:
			color = 0xFF663099;
			break;
		case FIRE:
			color = 0xFFD10F00;
			break;
		case ICE:
			color = 0xFF23D9EA;
			break;
		case LIGHTNING:
			color = 0xFFEAEA2C;
			break;
		case PHYSICAL:
			color = 0xFFFFFFFF;
			break;
		case WIND:
			color = 0xFF18CC59;
			break;
		}
		
		//if (koid.getAttackTarget() != null) // doesn't work on the client :P
//			color += bright;
		
		
		return color;
	}
	
	protected void renderModel(KoidEntity entityIn, MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha, float partialTicks) {
		float color[] = ColorUtil.ARGBToColor(getColor(entityIn));
		
		red *= color[0];
		green *= color[1];
		blue *= color[2];
		alpha *= color[3];
		
		final float yOffset = entityIn.getBbHeight();
		final float rotY = 360f * ((entityIn.tickCount + partialTicks) / (20f * 3.0f));
		final float rotX = 360f * ((entityIn.tickCount + partialTicks) / (20f * 10f));
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, yOffset, 0);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(rotY));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(rotX));
		model.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	

}
