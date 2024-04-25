package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss.PlantBossLeafLimb;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;

public class ModelPlantBossLeaf extends EntityModel<EntityPlantBoss.PlantBossLeafLimb> {
	
	private ModelRenderer main;
	
	public ModelPlantBossLeaf() {
		
		this.textureHeight = 256;
		this.textureWidth = 256;
		main = new ModelRenderer(this, 92, 250);
		
		main.setTextureOffset(92, 250);
		main.addBox(-16, -4, 0, 32, 2, 4);
		
		main.setTextureOffset(19, 199);
		main.addBox(-30, -4, 4, 60, 2, 49);

		// bottom
		main.setTextureOffset(38, 199);
		main.addBox(-4, -2, 4, 8, 2, 49).mirror = true;
		
		main.setTextureOffset(71, 190);
		main.addBox(-26, -4, 53, 52, 2, 5);
		
		main.setTextureOffset(81, 183);
		main.addBox(-22, -4, 58, 44, 2, 3);
		
		main.setTextureOffset(100, 177);
		main.addBox(-12, -4, 61, 24, 2, 4);
		
		main.setTextureOffset(194, 2254);
		main.addBox(-32, -4, 8, 2, 2, 29);
		
		main.setTextureOffset(0, 225);
		main.addBox(30, -4, 8, 2, 2, 29);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		matrixStackIn.push();
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90f));
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
	}
	
	@Override
	public void setLivingAnimations(EntityPlantBoss.PlantBossLeafLimb entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
	}

	@Override
	public void setRotationAngles(PlantBossLeafLimb entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
