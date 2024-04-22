package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.EntityHookShot;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelHookShot extends EntityModel<EntityHookShot> {
	
	private static final float width = .2f;
	private static final float height = .2f;
	
	private final ModelRenderer main;

	public ModelHookShot() {
		main = new ModelRenderer(this);
		main.addBox(-width/2, -height/2, -width/2, width/2, height/2, width/2);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
		
		
//	public void oldRender() {
//		BufferBuilder wr = Tessellator.getInstance().getBuffer();
//		
//		GlStateManager.pushMatrix();
//		
//		GlStateManager.translated(0, .6, 0);
//		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GlStateManager.enableBlend();
//		GlStateManager.disableLighting();
//		GlStateManager.enableAlphaTest();
//		
//		Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID,
//				"textures/block/dungeon_dark.png"
//				));
//		
//		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
//		wr.pos(0, 0, -width).tex(.5, .5).endVertex();
//		for (int i = 4; i >= 0; i--) {
//			double angle = (2*Math.PI) * ((double) i / (double) 4);
//			double vx = Math.cos(angle) * width;
//			double vy = Math.sin(angle) * height;
//			
//			double u = (vx + (width)) / (width * 2);
//			double v = (vy + (height)) / (height * 2);
//			wr.pos(vx, vy, 0).tex(u, v).endVertex();
//		}
//		Tessellator.getInstance().draw();
//		
//		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
//		wr.pos(0, 0, width).tex(.5, .5).endVertex();
//		for (int i = 0; i <= 4; i++) {
//			double angle = (2*Math.PI) * ((double) i / (double) 4);
//			double vx = Math.cos(angle) * width;
//			double vy = Math.sin(angle) * height;
//			
//			double u = (vx + (width)) / (width * 2);
//			double v = (vy + (height)) / (height * 2);
//			wr.pos(vx, vy, 0).tex(u, v).endVertex();
//		}
//		Tessellator.getInstance().draw();
//		
//		GlStateManager.enableLighting();
//		GlStateManager.popMatrix();
//	}
	
	@Override
	public void setRotationAngles(EntityHookShot entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
}
