package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.KeySwitchTriggerEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.entity.model.EntityModel;

public class ModelKeySwitchTrigger extends EntityModel<KeySwitchTriggerEntity> {
	
	public ModelKeySwitchTrigger() {
		;
	}
	
	@Override
	public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		// Cage is just a textured box
		RenderFuncs.drawUnitCube(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		
//		GlStateManager.disableBlend();
//			GlStateManager.enableBlend();
//			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//			GlStateManager.enableNormalize();
//			GlStateManager.disableAlphaTest();
//			GlStateManager.enableAlphaTest();
//			GlStateManager.color4f(1f, 1f, 1f, 1f);
//			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
//		GlStateManager.disableLighting();
//			GlStateManager.enableColorMaterial();
	}
	
	@Override
	public void prepareMobModel(KeySwitchTriggerEntity entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
	}

	@Override
	public void setupAnim(KeySwitchTriggerEntity entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		;
		
	}
}
