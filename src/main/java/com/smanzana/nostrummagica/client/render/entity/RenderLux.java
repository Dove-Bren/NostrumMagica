package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityLux;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderLux extends MobRenderer<EntityLux, ModelLux> {

	public RenderLux(EntityRendererManager renderManagerIn, float scale) {
		super(renderManagerIn, new ModelLux(), .33f);
	}
	

	
	private float getSwingRot(float swingProgress) {
		return (float) (Math.sin(Math.PI * 2 * swingProgress) * 30.0);
	}
	
	@Override
	public void render(EntityLux lux, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		
		
		matrixStackIn.push();
		matrixStackIn.translate(0, 1.5 - (-lux.getHeight() / 2), 0);
		if (!lux.isRoosting()) {
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(getSwingRot(lux.getSwingProgress(partialTicks))));
		}
		matrixStackIn.scale(.25f, .25f, .25f);
		
		super.render(lux, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.pop();
		
		
//		Was in model:
//		GlStateManager.pushMatrix();
//		GlStateManager.translated(0, 1.5, 0);
//		GlStateManager.translatef(0, -entity.getHeight() / 2, 0);
//		if (!lux.isRoosting()) {
//			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(lux.getSwingProgress(partialTicks)));
//		}
//		GlStateManager.scaled(.25, .25, .25);
//		main.render(scale);
//		GlStateManager.popMatrix();
//		GlStateManager.color4f(1f, 1f, 1f, 1f);
//		
//		Was in render:
//		GlStateManager.color3f(.65f, 1f, .7f);
//		super.doRender(entity, x, y, z, entityYaw, partialTicks);
//		GlStateManager.color3f(1f, 1f, 1f);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityLux entity) {
		return new ResourceLocation(NostrumMagica.MODID, "textures/entity/sprite_core.png");
	}
	
}
