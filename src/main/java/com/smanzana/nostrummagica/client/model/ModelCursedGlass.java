package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.CursedGlassTriggerEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ModelCursedGlass extends EntityModel<CursedGlassTriggerEntity> {

	private static final ResourceLocation TEX = NostrumMagica.Loc("textures/entity/cursed_glass.png");
	
	public ModelCursedGlass() {
		;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(-1.5, 0, -1.5);
		matrixStackIn.scale(3f, 3f, 3f);
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.GLASS.defaultBlockState());
		RenderFuncs.RenderModel(matrixStackIn, buffer, model, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	
	public void renderDecal(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 1.5, 0);
		matrixStackIn.scale(3f, 3f, 3f);
		matrixStackIn.scale(.99f, .99f, .99f);
		
		VertexConsumer buffer = bufferIn.getBuffer(RenderType.entityTranslucent(TEX));
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
		
		matrixStackIn.popPose();
	}
	
	@Override
	public void prepareMobModel(CursedGlassTriggerEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
	}

	@Override
	public void setupAnim(CursedGlassTriggerEntity entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		;
		
	}
}
