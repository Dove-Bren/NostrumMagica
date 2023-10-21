package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity.SwitchType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ModelSwitchTrigger extends EntityModel<EntitySwitchTrigger> {
	
	private static final double width = .45;
	private static final double height = .8;
	private static final double spinIdle = 3.0; // seconds per turn
	private static final double spinActivated = 1.0;

	public ModelSwitchTrigger() {
		;
	}
	
	@Override
	public void render(EntitySwitchTrigger entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		BufferBuilder wr = Tessellator.getInstance().getBuffer();
		EntitySwitchTrigger trigger = (EntitySwitchTrigger) entity;
		SwitchBlockTileEntity te = trigger.getLinkedTileEntity();
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translatef(0, .6f, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		//GlStateManager.disableCull();
		
		boolean magic = (te != null && te.getSwitchType() == SwitchType.MAGIC);
		float sat = 1.0f;
		if (te != null && te.isTriggered()) {
			sat = 0.4f;
		}
		
		if (magic) {
			GlStateManager.color4f(sat * .2f, sat * .4f, sat * 1f, .8f);
		} else {
			GlStateManager.color4f(sat * 1f, sat * 1f, sat * 0f, .8f);
		}
		Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/golem_ice.png"
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
		
		
		
		
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(NostrumMagica.MODID,
				"textures/block/spawner.png"
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
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntitySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
		SwitchBlockTileEntity te = trigger.getLinkedTileEntity();
		
		boolean fast = false;
		if (te == null) {
			;
		} else {
			if (te.isTriggered()) {
				fast = true;
			}
		}
		
		final float time = trigger.world.getGameTime() + partialTickTime;
		final float period = (float) (20 * (fast ? spinActivated : spinIdle));
		float angle = 360f * ((time % period) / period);
		GlStateManager.rotatef(angle, 0, 1, 0);
		
		// also bob up and down
		angle = (float) (2 * Math.PI * (time % 60 / 60));
		GlStateManager.translated(0, Math.sin(angle) * .1, 0);
	}
}
