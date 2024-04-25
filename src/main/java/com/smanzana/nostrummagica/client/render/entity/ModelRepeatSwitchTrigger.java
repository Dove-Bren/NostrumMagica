package com.smanzana.nostrummagica.client.render.entity;

public class ModelRepeatSwitchTrigger extends ModelSwitchTrigger {
	
	public ModelRepeatSwitchTrigger() {
		super(.45f, .45f);
	}
	
//	@Override
//	public void render(EntitySwitchTrigger entity, float time, float swingProgress,
//			float swing, float headAngleY, float headAngleX, float scale) {
//		BufferBuilder wr = Tessellator.getInstance().getBuffer();
//		EntitySwitchTrigger trigger = (EntitySwitchTrigger) entity;
//		SwitchBlockTileEntity te = trigger.getLinkedTileEntity();
//		
//		GlStateManager.pushMatrix();
//		
//		GlStateManager.translatef(0, .6f, 0);
//		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GlStateManager.enableBlend();
//		GlStateManager.disableLighting();
//		GlStateManager.enableAlphaTest();
//		//GlStateManager.disableCull();
//		
//		boolean magic = (te != null && te.getSwitchHitType() == SwitchHitType.MAGIC);
//		float sat = 1.0f;
//		if (te != null && te.isTriggered()) {
//			sat = 0.4f;
//		}
//		
//		if (magic) {
//			GlStateManager.color4f(sat * .2f, sat * .4f, sat * 1f, .8f);
//		} else {
//			GlStateManager.color4f(sat * 1f, sat * 1f, sat * 0f, .8f);
//		}
//		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
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
//		
//		
//		
//		GlStateManager.color4f(1f, 1f, 1f, 1f);
//		Minecraft.getInstance().getTextureManager().bindTexture(CAGE_TEXT);
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
//		GlStateManager.popMatrix();
//	}
	
//	@Override
//	public void setLivingAnimations(EntitySwitchTrigger trigger, float p_78086_2_, float age, float partialTickTime) {
//		SwitchBlockTileEntity te = trigger.getLinkedTileEntity();
//		
//		boolean fast = false;
//		if (te == null) {
//			;
//		} else {
//			if (te.isTriggered()) {
//				fast = true;
//			}
//		}
//		
//		final float time = trigger.world.getGameTime() + partialTickTime;
//		final float period = (float) (20 * (fast ? spinActivated : spinIdle));
//		float angle = 360f * ((time % period) / period);
//		GlStateManager.rotatef(angle, 0, 1, 0);
//		
//		// also bob up and down
//		angle = (float) (2 * Math.PI * (time % 60 / 60));
//		GlStateManager.translated(0, Math.sin(angle) * .1, 0);
//	}
}
