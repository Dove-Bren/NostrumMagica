package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.WhirlwindModel;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.WhirlwindEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class WhirlwindRenderer extends EntityRenderer<WhirlwindEntity> {
	
	private static final ResourceLocation TEX = new ResourceLocation(NostrumMagica.MODID, "textures/entity/whirlwind.png");

	protected WhirlwindModel model;
	
	public WhirlwindRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		this.model = new WhirlwindModel(renderManagerIn.bakeLayer(NostrumModelLayers.Whirlwind));
	}
	
	@Override
	public void render(WhirlwindEntity ent, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
//		matrixStackIn.translate(0, (-lux.getBbHeight() / 4), 0);
//		matrixStackIn.scale(.25f, .25f, .25f);
		
		matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
		matrixStackIn.translate(0.0D, (double)-1.501F, 0.0D);
		
		VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.XOffsetEnt(getTextureLocation(ent), xOffset(ent.tickCount + partialTicks) % 1f, false));
		this.model.renderToBuffer(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(WhirlwindEntity entity) {
		return TEX;
	}
	
	private float xOffset(float ticks) {
		return ticks * 0.02F;
	}
	
}
