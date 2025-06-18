package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.entity.WhirlwindEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * Copied trimmed version of 1.21's breeze model
 */
public class WhirlwindModel extends EntityModel<WhirlwindEntity> {
	
	public static final LayerDefinition createLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		
		// < TRIM head rods and eyes >
		
		PartDefinition partdefinition4 = partdefinition.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition partdefinition5 = partdefinition4.addOrReplaceChild(
			"wind_bottom",
			CubeListBuilder.create().texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		PartDefinition partdefinition6 = partdefinition5.addOrReplaceChild(
			"wind_mid",
			CubeListBuilder.create()
				.texOffs(74, 28)
				.addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(78, 32)
				.addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(49, 71)
				.addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -7.0F, 0.0F)
		);
		partdefinition6.addOrReplaceChild(
			"wind_top",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(6, 6)
				.addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(105, 57)
				.addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, -6.0F, 0.0F)
		);
		return LayerDefinition.create(meshdefinition, 128, 128);
	}
	
	protected final ModelPart wind;
	protected final ModelPart windTop;
	protected final ModelPart windMid;
	protected final ModelPart windBottom;
	
	public WhirlwindModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		
		this.wind = root.getChild("wind_body");
		this.windBottom = this.wind.getChild("wind_bottom");
		this.windMid = this.windBottom.getChild("wind_mid");
		this.windTop = this.windMid.getChild("wind_top");
	}

	@Override
	public void setupAnim(WhirlwindEntity p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_,
			float p_102623_) {
		;
		
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		this.wind.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

}
