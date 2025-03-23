package com.smanzana.nostrummagica.client.effects;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientEffect {
	
	public static class ClientEffectRenderDetail {
		public float alpha;
		public float red;
		public float green;
		public float blue;
		
		public int getColor() {
			return ((int) (0xFF * blue) << 0)
				 | ((int) (0xFF * green) << 8)
				 | ((int) (0xFF * red) << 16)
				 | ((int) (0xFF * alpha) << 24);
		}
	}

	final protected Vec3 origin;
	final private ClientEffectForm form;

	protected long startTime;
	protected long existedMS;
	protected int existedTicks;
	protected long durationMS;
	protected int durationTicks;
	
	protected List<ClientEffectModifier> modifiers;
	
	private ClientEffect(Vec3 origin, ClientEffectForm form) {
		existedMS = 0;
		existedTicks = 0;
		startTime = 0;
		this.origin = origin;
		this.form = form;
		modifiers = new LinkedList<>();
	}
	
	public ClientEffect(Vec3 origin, ClientEffectForm form, int durationTicks) {
		this(origin, form);
		this.durationTicks = durationTicks;
	}
	
	public ClientEffect(Vec3 origin, ClientEffectForm form, long durationMS) {
		this(origin, form);
		this.durationMS = durationMS;
	}
	
	public ClientEffect modify(ClientEffectModifier mod) {
		modifiers.add(mod);
		return this;
	}
	
	public boolean displayTick(Minecraft mc, PoseStack matrixStackIn, float partialTicks) {
		long sysTime = System.currentTimeMillis();
		if (startTime == 0)
			startTime = sysTime;
		else {
			existedMS = sysTime - startTime;
			existedTicks = (int) existedMS / (1000 / 20);
		}
		
		final float progress = durationMS == 0
				? (durationTicks == 0 ? (1f) : (((float) existedTicks + partialTicks) / (float) durationTicks))
				: (float) (((double) existedMS + (partialTicks * (1000 / 20))) / (double) durationMS);
		
		
		matrixStackIn.pushPose();
		
		ClientEffectRenderDetail detail = new ClientEffectRenderDetail();
		detail.alpha = detail.red = detail.green = detail.blue = 1f;
		
		preModHook(matrixStackIn, detail, progress, partialTicks);
		
		setupRenderState();
		drawForm(matrixStackIn, detail, mc, progress, partialTicks);
		teardownRenderState();
		
		matrixStackIn.popPose();
		return progress < 1f;
	}
	
	protected void setupRenderState() {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//RenderSystem.disableLighting();
	}
	
	protected void teardownRenderState() {
		RenderSystem.disableBlend();
		//RenderSystem.defaultAlphaFunc();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(GL11.GL_LEQUAL); // ???
		
		// RenderManager has:
//		RenderSystem.depthMask(true);
//		RenderSystem.depthFunc(515);
//		RenderSystem.disableBlend();
//		RenderSystem.defaultAlphaFunc();
//		lightTextureIn.disableLightmap();
//		RenderSystem.disableFog();
	}
	
	protected void drawForm(PoseStack matrixStackIn, ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {

		if (!this.modifiers.isEmpty())
		for (ClientEffectModifier mod : modifiers) {
			mod.apply(matrixStackIn, detail, progress, partialTicks);
		}
		
		form.draw(matrixStackIn, mc, partialTicks, detail.getColor());
	}
	
	protected void preModHook(PoseStack matrixStackIn, ClientEffectRenderDetail detail, float progress, float partialTicks) {
		if (!this.modifiers.isEmpty())
		for (ClientEffectModifier mod : modifiers) {
			mod.earlyApply(matrixStackIn, detail, progress, partialTicks);
		}
		matrixStackIn.translate(origin.x, origin.y, origin.z);
	}
	
	public void onStart() {
		
	}
	
	public void onEnd() {
		
	}
	
}
