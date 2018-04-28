package com.smanzana.nostrummagica.client.effects;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;

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

	protected long startTime;
	protected long existedMS;
	protected int existedTicks;
	protected long durationMS;
	protected int durationTicks;
	protected Vec3d origin;
	private ClientEffectForm form;
	
	protected List<ClientEffectModifier> modifiers;
	
	private ClientEffect(Vec3d origin, ClientEffectForm form) {
		existedMS = 0;
		existedTicks = 0;
		startTime = 0;
		this.origin = origin;
		this.form = form;
		modifiers = new LinkedList<>();
	}
	
	public ClientEffect(Vec3d origin, ClientEffectForm form, int durationTicks) {
		this(origin, form);
		this.durationTicks = durationTicks;
	}
	
	public ClientEffect(Vec3d origin, ClientEffectForm form, long durationMS) {
		this(origin, form);
		this.durationMS = durationMS;
	}
	
	public ClientEffect modify(ClientEffectModifier mod) {
		modifiers.add(mod);
		return this;
	}
	
	public boolean displayTick(Minecraft mc, float partialTicks) {
		long sysTime = Minecraft.getSystemTime();
		if (startTime == 0)
			startTime = sysTime;
		else {
			existedMS = sysTime - startTime;
			existedTicks = (int) existedMS / (1000 / 20);
		}
		
		final float progress = durationMS == 0
				? (durationTicks == 0 ? (1f) : ((float) existedTicks / (float) durationTicks))
				: (float) ((double) existedMS / (double) durationMS);
		
		
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		
		ClientEffectRenderDetail detail = new ClientEffectRenderDetail();
		detail.alpha = detail.red = detail.green = detail.blue = 1f;
		
		preModHook(detail, progress);
		
		drawForm(detail, mc, progress, partialTicks);
		
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
		return progress < 1f;
	}
	
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {

		if (!this.modifiers.isEmpty())
		for (ClientEffectModifier mod : modifiers) {
			mod.apply(detail, progress);
		}
		
		form.draw(mc, partialTicks, detail.getColor());
	}
	
	protected void preModHook(ClientEffectRenderDetail detail, float progress) {
		if (!this.modifiers.isEmpty())
		for (ClientEffectModifier mod : modifiers) {
			mod.earlyApply(detail, progress);
		}
		GlStateManager.translate(origin.xCoord, origin.yCoord, origin.zCoord);
	}
	
}
