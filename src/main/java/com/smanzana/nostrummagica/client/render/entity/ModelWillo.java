package com.smanzana.nostrummagica.client.render.entity;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.entity.EntityWillo;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class ModelWillo extends ModelBase {
	
	private static final int SEGMENTS = 8;
	private static final float PERIOD = 20f * 2;
	
	private ModelRenderer main;
	private List<ModelRenderer> armLeft;
	private List<ModelRenderer> armRight;
	
	public ModelWillo() {
		this.textureHeight = 64;
		this.textureWidth = 64;
		main = new ModelRenderer(this, 0, 0);
		
		//main.addBox(-8f, -8f, -8f, 16, 16, 16);
		
		armLeft = new ArrayList<>();
		armRight = new ArrayList<>();
		
		final float offset = .75f;
		final float spacing = .75f;
		for (int i = 0; i < SEGMENTS; i++) {
			ModelRenderer render = new ModelRenderer(this, 0, 0);
			render.setTextureOffset(0, 18);
			render.addBox(-4.5f, -4.5f, -4.5f, 9, 9, 9);
			render.offsetX = offset + (i+1) * spacing;
			main.addChild(render);
			armLeft.add(render);
		}
		
		for (int i = 0; i < SEGMENTS; i++) {
			ModelRenderer render = new ModelRenderer(this, 0, 0);
			render.setTextureOffset(0, 18);
			render.addBox(-4.5f, -4.5f, -4.5f, 9, 9, 9);
			render.offsetX = -offset + (i+1) * -spacing;
			main.addChild(render);
			armRight.add(render);
		}
	}
	
	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
		
		float progress = ((float) entity.ticksExisted + partialTickTime) / PERIOD;
		for (int i = 0; i < SEGMENTS; i++) {
			float progressAdj = (progress + i * .1f) % 1f;
			armLeft.get(i).offsetY = (float) (Math.sin(2 * Math.PI * progressAdj) * .5);
			progressAdj = ((progress + .5f) + i * .1f) % 1f;
			armRight.get(i).offsetY = (float) (Math.sin(2 * Math.PI * progressAdj) * .5);
		}
	}
	
	protected void renderFace(BufferBuilder buffer, EntityWillo entity, float partialTicks) {
		
		// Choose face based on status
		final float umin;
		final float umax;
		final float vmin;
		final float vmax;
		
		switch (entity.getStatus()) {
		case NEUTRAL:
		default:
			umin = 0;
			umax = umin + (18f/64f);
			vmin = 0;
			vmax = vmin + (18f/64f);
			break;
		case PANIC:
			umin = (18f/64f);
			umax = umin + (18f/64f);
			vmin = 0;
			vmax = vmin + (18f/64f);
			break;
		case AGGRO:
			umin = (18f/64f) + (18f/64f);
			umax = umin + (18f/64f);
			vmin = 0;
			vmax = vmin + (18f/64f);
			break;
		}
		
		// North
		buffer.pos(.5, .5, 0).tex(umax,vmax).normal(.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(.5, -.5, 0).tex(umax,vmin).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, -.5, 0).tex(umin,vmin).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, .5, 0).tex(umin,vmax).normal(-.5773f, .5773f, -.5773f).endVertex();
		
//		// South
//		buffer.pos(-.5, .5, .01).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
//		buffer.pos(-.5, -.5, .01).tex(umin,vmin).normal(-.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, -.5, .01).tex(umax,vmin).normal(.5773f, -.5773f, .5773f).endVertex();
//		buffer.pos(.5, .5, .01).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
	}
	
	protected void renderCube(BufferBuilder buffer, EntityWillo entity, float partialTicks) {
		final float umin = 0;
		final float umax = umin + (18f/64f);
		final float vmin = (36f/64f);
		final float vmax = vmin + (18f/64f);
		
		// Top
		buffer.pos(-.5, .5, -.5).tex(umin,vmin).normal(-.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(-.5, .5, .5).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, .5, .5).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, .5, -.5).tex(umax,vmin).normal(.5773f, .5773f, -.5773f).endVertex();
		
		// North
		buffer.pos(.5, .5, -.5).tex(umax,vmin).normal(.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(.5, -.5, -.5).tex(umax,vmax).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, -.5, -.5).tex(umin,vmax).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, .5, -.5).tex(umin,vmin).normal(-.5773f, .5773f, -.5773f).endVertex();
		
		// East
		buffer.pos(.5, .5, .5).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
		buffer.pos(.5, -.5, .5).tex(umin,vmax).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, -.5, -.5).tex(umin,vmin).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, .5, -.5).tex(umax,vmin).normal(.5773f, .5773f, -.5773f).endVertex();
		
		// South
		buffer.pos(-.5, .5, .5).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
		buffer.pos(-.5, -.5, .5).tex(umin,vmin).normal(-.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, -.5, .5).tex(umax,vmin).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(.5, .5, .5).tex(umax,vmax).normal(.5773f, .5773f, .5773f).endVertex();
		
		// West
		buffer.pos(-.5, .5, -.5).tex(umin,vmin).normal(-.5773f, .5773f, -.5773f).endVertex();
		buffer.pos(-.5, -.5, -.5).tex(umax,vmin).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(-.5, -.5, .5).tex(umax,vmax).normal(-.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(-.5, .5, .5).tex(umin,vmax).normal(-.5773f, .5773f, .5773f).endVertex();
		
		// Bottom
		buffer.pos(-.5, -.5, -.5).tex(umax,vmin).normal(-.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, -.5, -.5).tex(umin,vmin).normal(.5773f, -.5773f, -.5773f).endVertex();
		buffer.pos(.5, -.5, .5).tex(umin,vmax).normal(.5773f, -.5773f, .5773f).endVertex();
		buffer.pos(-.5, -.5, .5).tex(umax,vmax).normal(-.5773f, -.5773f, .5773f).endVertex();
	}
	
	
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		final EntityWillo willo = (EntityWillo) entity;
		final float partialTicks = time % 1f;
		final float rotPeriod = 6f;
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1.5, 0);
		GlStateManager.translate(0, -entity.height / 2, 0);
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(.25, .25, .25);
		main.render(scale);
		GlStateManager.popMatrix();
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		
		GlStateManager.pushMatrix();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		GlStateManager.scale(.4, .4, .4);
		renderFace(buffer, willo, partialTicks);
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();

		final float rotY = 360f * (time % rotPeriod) / rotPeriod;
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		GlStateManager.pushMatrix();
		GlStateManager.scale(.5, .5, .5);
		GlStateManager.rotate(rotY, 1, 0, 0);
		renderCube(buffer, willo, partialTicks);
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
		
		GlStateManager.color(1f, 1f, 1f, 1f);
	}
	
}
