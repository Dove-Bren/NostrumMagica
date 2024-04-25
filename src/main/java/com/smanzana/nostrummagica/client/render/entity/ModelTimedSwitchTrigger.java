package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;

import net.minecraft.util.math.vector.Matrix4f;

public class ModelTimedSwitchTrigger extends ModelSwitchTrigger {
	
	private static final float width = .45f;
	private static final float height = .7f;
	
	public ModelTimedSwitchTrigger() {
		;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		
		matrixStackIn.push();
		
		for (int i = 0; i < 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			
			final float vx1 = (float) (Math.cos(angle) * width);
			final float vz1 = (float) (Math.sin(angle) * width);
			final float u1 = (vx1 + (width)) / (width * 2);
			final float v1 = (vz1 + (width)) / (width * 2);
			
			angle = (2*Math.PI) * ((double) ((i+1)%4) / (double) 4);
			
			final float vx2 = (float) (Math.cos(angle) * width);
			final float vz2 = (float) (Math.sin(angle) * width);
			final float u2 = (vx2 + (width)) / (width * 2);
			final float v2 = (vz2 + (width)) / (width * 2);
			
			// For ypositive, add in YP, HIGH ANGLE, LOW ANGLE
			bufferIn.pos(transform, 0, (height/4f), 0).tex(.5f, .5f).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(transform, vx2, -height, vz2).tex(u2, v2).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(transform, vx1, -height, vz1).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			
			// for ynegative, add in YN, LOW ANGLE, HIGH ANGLE
			bufferIn.pos(transform, 0, -(height/4f), 0).tex(.5f, .5f).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(transform, vx1, height, vz1).tex(u1, v1).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(transform, vx2, height, vz2).tex(u2, v2).color(red, green, blue, alpha).endVertex();
		}
		
		// Top and bottom 'quads'
		bufferIn.pos(transform, -width, (height/4f), -width).tex(0f, 0f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, width, (height/4f), -width).tex(1f, 0f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, width, (height/4f), width).tex(1f, 1f).color(red, green, blue, alpha).endVertex();
		//
		bufferIn.pos(transform, width, (height/4f), width).tex(1f, 1f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, -width, (height/4f), width).tex(0f, 1f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, -width, (height/4f), -width).tex(0f, 0f).color(red, green, blue, alpha).endVertex();
		
		bufferIn.pos(transform, -width, -(height/4f), -width).tex(1f, 0f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, -width, -(height/4f), width).tex(1f, 1f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, width, -(height/4f), width).tex(0f, 1f).color(red, green, blue, alpha).endVertex();
		//
		bufferIn.pos(transform, width, -(height/4f), width).tex(0f, 1f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, width, -(height/4f), -width).tex(0f, 0f).color(red, green, blue, alpha).endVertex();
		bufferIn.pos(transform, -width, -(height/4f), -width).tex(1f, 0f).color(red, green, blue, alpha).endVertex();
		
		
//		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
//		wr.pos(0, (height/4), 0).tex(.5, .5).endVertex();
//		for (int i = 4; i >= 0; i--) {
//			double angle = (2*Math.PI) * ((double) i / (double) 4);
//			double vx = Math.cos(angle) * width;
//			double vz = Math.sin(angle) * width;
//			
//			double u = (vx + (width)) / (width * 2);
//			double v = (vz + (width)) / (width * 2);
//			wr.pos(vx, -height, vz).tex(u, v).endVertex();
//		}
//		Tessellator.getInstance().draw();
//		
//		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//		for (int i = 4; i >= 0; i--) {
//			double angle = (2*Math.PI) * ((double) i / (double) 4);
//			double vx = Math.cos(angle) * width;
//			double vz = Math.sin(angle) * width;
//			
//			double u = (vx + (width)) / (width * 2);
//			double v = (vz + (width)) / (width * 2);
//			wr.pos(vx, -height, vz).tex(u, v).endVertex();
//		}
//		Tessellator.getInstance().draw();
//		
//		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
//		wr.pos(0, (-height/4), 0).tex(.5, .5).endVertex();
//		for (int i = 0; i <= 4; i++) {
//			double angle = (2*Math.PI) * ((double) i / (double) 4);
//			double vx = Math.cos(angle) * width;
//			double vz = Math.sin(angle) * width;
//			
//			double u = (vx + (width)) / (width * 2);
//			double v = (vz + (width)) / (width * 2);
//			wr.pos(vx, height, vz).tex(u, v).endVertex();
//		}
//		Tessellator.getInstance().draw();
//		
//		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//		for (int i = 0; i <= 4; i++) {
//			double angle = (2*Math.PI) * ((double) i / (double) 4);
//			double vx = Math.cos(angle) * width;
//			double vz = Math.sin(angle) * width;
//			
//			double u = (vx + (width)) / (width * 2);
//			double v = (vz + (width)) / (width * 2);
//			wr.pos(vx, height, vz).tex(u, v).endVertex();
//		}
//		Tessellator.getInstance().draw();
		
		matrixStackIn.pop();
	}
	
	@Override
	public void setLivingAnimations(EntitySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
//			
	}
}
