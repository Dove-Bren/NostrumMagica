package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityHookShot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ModelHookShot extends EntityModel<EntityHookShot> {
	
	private static final double width = .2;
	private static final double height = .2;

	public ModelHookShot() {
		;
	}
	
	@Override
	public void render(EntityHookShot entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translated(0, .6, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		
		Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID,
				"textures/block/dungeon_dark.png"
				));
		
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, 0, -width).tex(.5, .5).endVertex();
		for (int i = 4; i >= 0; i--) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vy = Math.sin(angle) * height;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vy + (height)) / (height * 2);
			wr.pos(vx, vy, 0).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, 0, width).tex(.5, .5).endVertex();
		for (int i = 0; i <= 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vy = Math.sin(angle) * height;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vy + (height)) / (height * 2);
			wr.pos(vx, vy, 0).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntityHookShot entitylivingbaseIn, float p_78086_2_, float age, float partialTickTime) {
		
	}
}
