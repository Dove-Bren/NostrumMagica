package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.BakedModel;
import com.smanzana.nostrummagica.client.model.RenderShivModel;
import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class KoidRenderer extends MobRenderer<KoidEntity, RenderShivModel<KoidEntity>> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/koid");
	
	protected BakedModel<KoidEntity> modelBase;

	public KoidRenderer(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new RenderShivModel<>(), shadowSizeIn);
		this.modelBase = new BakedModel<>(MODEL);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(KoidEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
	
	@Override
	public void render(KoidEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
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
		case NEUTRAL:
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
	
	protected void renderModel(KoidEntity entityIn, PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn,
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
		modelBase.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	

}
