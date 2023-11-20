package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity.SwitchHitType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ModelTimedSwitchTrigger extends ModelSwitchTrigger {
	
	private static final double width = .45;
	private static final double height = .7;
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/golem_ice.png");
	private static final ResourceLocation CAGE_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/block/spawner.png");

	public ModelTimedSwitchTrigger() {
		;
	}
	
	@Override
	public void render(EntitySwitchTrigger entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		EntitySwitchTrigger trigger = (EntitySwitchTrigger) entity;
		SwitchBlockTileEntity te = trigger.getLinkedTileEntity();
		
		GlStateManager.pushMatrix();
		
		//GlStateManager.translatef(0, .6f, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		//GlStateManager.disableCull();
		
		boolean magic = (te != null && te.getSwitchHitType() == SwitchHitType.MAGIC);
		float sat = 1.0f;
		if (te != null && te.isTriggered()) {
			sat = 0.4f;
		}
		
		if (magic) {
			GlStateManager.color4f(sat * .2f, sat * .4f, sat * 1f, .8f);
		} else {
			GlStateManager.color4f(sat * 1f, sat * 1f, sat * 0f, .8f);
		}
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, (height/4), 0).tex(.5, .5).endVertex();
		for (int i = 4; i >= 0; i--) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, -height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 4; i >= 0; i--) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, -height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, (-height/4), 0).tex(.5, .5).endVertex();
		for (int i = 0; i <= 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 0; i <= 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		
		
		
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		Minecraft.getInstance().getTextureManager().bindTexture(CAGE_TEXT);
		
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, (height/4), 0).tex(.5, .5).endVertex();
		for (int i = 4; i >= 0; i--) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, -height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 4; i >= 0; i--) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, -height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, (-height/4), 0).tex(.5, .5).endVertex();
		for (int i = 0; i <= 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 0; i <= 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			double vx = Math.cos(angle) * width;
			double vz = Math.sin(angle) * width;
			
			double u = (vx + (width)) / (width * 2);
			double v = (vz + (width)) / (width * 2);
			wr.pos(vx, height, vz).tex(u, v).endVertex();
		}
		Tessellator.getInstance().draw();
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntitySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
		SwitchBlockTileEntity te = trigger.getLinkedTileEntity();
		
		GlStateManager.translated(0, .6, 0);
		
		final double time = trigger.world.getGameTime() + partialTickTime;
		double angle;
		
		// bob up and down
		angle = (2 * Math.PI * (time % 60 / 60));
		GlStateManager.translated(0, Math.sin(angle) * .1, 0);
		
		if (te.isTriggered()) {
			final double period = 10f; // half a second
			
			// Want to make sure final .5 second interval ends with a flip such that the flip finishes when time is up.
			// Also want .5 second of just standing still between flips. Count from the back so that last one has a flip
			
			// for duration 30, we want
			// [0-9] FLIP
			// [10-19] no flip
			// [20-29] FLIP
			final long timeTillEnd = te.getCurrentCooldownTicks() - 1;
			if (timeTillEnd % (2 * period) < period) { // mod by 20 and see if it's in the bottom 10
				final double rotTicks = Math.max(0, timeTillEnd - partialTickTime); // For the first tick, this is 0 - partial so negative
				
				// FLIP
				angle = 180 * ((rotTicks % period) / period);
				GlStateManager.rotated(angle, 1, 0, 1);
			}
			
		}
	}
}
