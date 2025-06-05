package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SwitchTriggerEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;

public class SwitchTriggerModel extends EntityModel<SwitchTriggerEntity> {
	
	public static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/golem_ice.png");
	public static final ResourceLocation CAGE_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/block/spawner.png");
	
	protected final float width;
	protected final float height;

	public SwitchTriggerModel() {
		this(.45f, .8f);
	}
	
	protected SwitchTriggerModel(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		RenderFuncs.renderDiamond(matrixStackIn, bufferIn, width, height, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public void prepareMobModel(SwitchTriggerEntity trigger, float p_78086_2_, float age, float partialTickTime) {
		
	}

	@Override
	public void setupAnim(SwitchTriggerEntity entityIn, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
