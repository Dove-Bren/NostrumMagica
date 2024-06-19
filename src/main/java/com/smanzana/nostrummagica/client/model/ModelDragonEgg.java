package com.smanzana.nostrummagica.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelDragonEgg extends EntityModel<DragonEggEntity> {

	private ModelRenderer main;
	private float coldScale;
	
	private static final int textureHeight = 32;
	private static final int textureWidth = 32;
	
	private void addCyl(int y, int height, int radius) {
		main.addBox(-radius, y, -radius, radius * 2, -height, radius * 2);
	}
	
	public ModelDragonEgg() {
		main = new ModelRenderer(this, 0, 0);
		int y = 28;
		
		main.setTextureSize(textureWidth, textureHeight);
		main.setRotationPoint(0, 20, 0);
		addCyl(y--, 1, 2);
		addCyl(y--, 1, 4);
		addCyl(y--, 1, 5);
		addCyl(y, 2, 6); y -= 2;
		addCyl(y, 4, 7); y -= 4;
		addCyl(y, 2, 6); y -= 2;
		addCyl(y, 2, 5); y -= 2;
		addCyl(y--, 1, 4);
		addCyl(y--, 1, 3);
		addCyl(y--, 1, 2);
	}
	
	public void setColdScale(float scale) {
		this.coldScale = scale; // Wish renderer could just pass color to render...
	}
	
	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer,
            int light, int overlay, float red, float green, float blue, float alpha) {
		
		// Tint based on how cold it is
		red *= 1f - (coldScale * .4f);
		green *= 1f - (coldScale * .1f);
		blue *= 1f - (coldScale * .1f);
		
		final float modelScale = 0.5f;
		matrixStack.push();
		matrixStack.scale(modelScale, modelScale, modelScale);
		main.render(matrixStack, buffer, light, overlay, red, green, blue, alpha);
		matrixStack.pop();
	}

	@Override
	public void setRotationAngles(DragonEggEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
