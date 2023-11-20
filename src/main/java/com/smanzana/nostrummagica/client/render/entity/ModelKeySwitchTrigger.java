package com.smanzana.nostrummagica.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityKeySwitchTrigger;
import com.smanzana.nostrummagica.tiles.KeySwitchBlockTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ModelKeySwitchTrigger extends EntityModel<EntityKeySwitchTrigger> {
	
	private static final double spinIdle = 3.0; // seconds per turn
	private static final double spinActivated = 3.0;
	
	//private static final ResourceLocation CAGE_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/item/key.png");
	private static final ResourceLocation KEY_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/item/key.png");

	public ModelKeySwitchTrigger() {
		;
	}
	
	protected void renderWholeOrb(EntityKeySwitchTrigger trigger, double ticks) {
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		{
			final float umin = 0;
			final float umax = 1;
			final float vmin = 0;
			final float vmax = 1;
			
			
			
			GlStateManager.disableBlend();
//			GlStateManager.enableBlend();
//			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//			GlStateManager.enableNormalize();
//			GlStateManager.disableAlphaTest();
//			GlStateManager.enableAlphaTest();
//			GlStateManager.color4f(1f, 1f, 1f, 1f);
//			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
			GlStateManager.disableLighting();
//			GlStateManager.enableColorMaterial();
			
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
		tessellator.draw();
	}
	
	protected void renderKey(EntityKeySwitchTrigger trigger, double ticks) {
		final Minecraft mc = Minecraft.getInstance();
		
		mc.getTextureManager().bindTexture(KEY_TEXT);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		RenderFuncs.renderSpaceQuadFacingCamera(buffer, mc.gameRenderer.getActiveRenderInfo(),
				0, 0, 0,
				.25,
				1f, 1f, 1f, 1f);
		tessellator.draw();
	}
	
	@Override
	public void render(EntityKeySwitchTrigger entity, float swingProgress,
			float swing, float partialTicks, float headAngleY, float headAngleX, float scale) {
		final double ticks = (partialTicks % 1) + entity.world.getGameTime();
		
		GlStateManager.pushMatrix();
		
		GlStateManager.pushMatrix();
		KeySwitchBlockTileEntity te = (KeySwitchBlockTileEntity) entity.getLinkedTileEntity();
		
		boolean fast = false;
		if (te == null) {
			;
		} else {
			if (te.isTriggered()) {
				fast = true;
			}
		}
		
		final double period = (float) (20 * (fast ? spinActivated * 2 : spinIdle));
		float angle = 360f * (float)((ticks % period) / period);
		GlStateManager.rotatef(angle, 0, 1, 0);
		
		renderWholeOrb(entity, ticks);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scalef(1f, -1f, -1f);
		renderKey(entity, ticks);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setLivingAnimations(EntityKeySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
		final float time = trigger.world.getGameTime() + partialTickTime;
		float angle = (float) (2 * Math.PI * (time % 60 / 60));
		GlStateManager.translated(0, Math.sin(angle) * .1, 0);
	}
}
