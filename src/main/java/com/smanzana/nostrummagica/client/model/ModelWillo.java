package com.smanzana.nostrummagica.client.model;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.WilloEntity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelWillo extends EntityModel<WilloEntity> {
	
	private static final int SEGMENTS = 8;
	private static final float PERIOD = 20f * 2;
	
	private List<ModelRenderer> armLeft;
	private List<ModelRenderer> armRight;
	
	protected float waveProg;
	
	public ModelWillo() {
		super(RenderType::getEntityCutoutNoCull);
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		armLeft = new ArrayList<>();
		armRight = new ArrayList<>();
		
		final float offset = 16f * .75f;
		final float spacing = 16f * .75f;
		for (int i = 0; i < SEGMENTS; i++) {
			ModelRenderer render = new ModelRenderer(this, 0, 0);
			render.setTextureOffset(0, 18);
			render.addBox(-4.5f + (offset + (i+1) * spacing), -4.5f, -4.5f, 9, 9, 9);
			//render.offsetX = offset + (i+1) * spacing;
			//main.addChild(render);
			armLeft.add(render);
		}
		
		for (int i = 0; i < SEGMENTS; i++) {
			ModelRenderer render = new ModelRenderer(this, 0, 0);
			render.setTextureOffset(0, 18);
			render.addBox(-4.5f + (-offset + (i+1) * -spacing), -4.5f, -4.5f, 9, 9, 9);
//			render.offsetX = -offset + (i+1) * -spacing;
			//main.addChild(render);
			armRight.add(render);
		}
	}
	
	@Override
	public void setLivingAnimations(WilloEntity entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
		
		// Wave timing information for rendering arms
		this.waveProg = ((float) entity.ticksExisted + partialTickTime) / PERIOD;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		// Used to have all parented to a main modelrender, and adjusted the yoffset to make it do the wave with its arms.
		// There is no adjustable offset on model renderers OR on their boxes. So isntead we iterate the lists ourselves
		// and render with different offsets.
		//main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
		for (int i = 0; i < SEGMENTS; i++) {
			final float progressAdjLeft = (waveProg + i * .1f) % 1f;
			final float progressAdjRight = ((waveProg + .5f) + i * .1f) % 1f;
			final float offsetLeft = (float) (Math.sin(2 * Math.PI * progressAdjLeft) * .5);
			final float offsetRight = (float) (Math.sin(2 * Math.PI * progressAdjRight) * .5);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, offsetLeft, 0);
			armLeft.get(i).render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
			
			matrixStackIn.push();
			matrixStackIn.translate(0, offsetRight, 0);
			armRight.get(i).render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
		}
	}

	@Override
	public void setRotationAngles(WilloEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
	
}
