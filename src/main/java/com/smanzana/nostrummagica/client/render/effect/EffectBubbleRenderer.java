package com.smanzana.nostrummagica.client.render.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class EffectBubbleRenderer implements IEffectRenderer {
	
	private static final ResourceLocation TEX_BUBBLE = NostrumMagica.Loc("textures/entity/effect_bubble.png");
	private static final float OFFSET_BAD = .3f;
	private static final float ORBIT_BAD = 1.3f;
	private static final float OFFSET_NEUTRAL = .2f;
	private static final float ORBIT_NEUTRAL = .8f;
	private static final float OFFSET_GOOD = .1f;
	private static final float ORBIT_GOOD = 1f;
	
	protected static final float GetDefaultOffset(EffectType type) {
		switch (type) {
		case BENEFICIAL:
			return OFFSET_GOOD;
		case HARMFUL:
		default:
			return OFFSET_BAD;
		case NEUTRAL:
			return OFFSET_NEUTRAL;
		}
	}
	
	protected static final float GetDefaultOrbit(EffectType type) {
		switch (type) {
		case BENEFICIAL:
			return ORBIT_GOOD;
		case HARMFUL:
		default:
			return ORBIT_BAD;
		case NEUTRAL:
			return ORBIT_NEUTRAL;
		}
	}
	
	protected static final float GetDefaultOrbitOffset(Effect effect) {
		return (float) (effect.getRegistryName().hashCode() % 60) / 60f;
	}
	
	protected final float yOffset;
	protected final float orbitSpeed;
	protected final float orbitOffset;
	protected final float red;
	protected final float green;
	protected final float blue;
	protected final float alpha;
	
	public EffectBubbleRenderer(float yOffset, float orbitSpeed, float orbitOffset, int color) {
		this.yOffset = yOffset;
		this.orbitSpeed = orbitSpeed;
		this.orbitOffset = orbitOffset;
		final float[] colors = ColorUtil.ARGBToColor(color);
		this.red = colors[0];
		this.green = colors[1];
		this.blue = colors[2];
		this.alpha = colors[3];
	}
	
	public EffectBubbleRenderer(float yExtraOffset, Effect effect) {
		this(yExtraOffset + GetDefaultOffset(effect.getEffectType()), GetDefaultOrbit(effect.getEffectType()), GetDefaultOrbitOffset(effect), effect.getLiquidColor());
	}
	
	public EffectBubbleRenderer(Effect effect) {
		this(0f, effect);
	}
	
	protected float getYOffset() {
		return yOffset;
	}
	
	protected float getOrbitSpeed() {
		return this.orbitSpeed;
	}
	
	protected float getOrbitOffset() {
		return this.orbitOffset;
	}
	
	protected float getWidth() {
		return .1f;
	}
	
	@Override
	public void renderEffectOnEntity(EffectInstance effect, MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		final Minecraft mc = Minecraft.getInstance();
		final ActiveRenderInfo activeInfo = mc.gameRenderer.getActiveRenderInfo();
		
		final float entYaw = MathHelper.interpolateAngle(partialTicks, entity.prevRenderYawOffset, entity.renderYawOffset);
		
		stack.push();
		
		// Stack is offset to up 1.5. Undo, and then offset to just above the entity's height.
		stack.translate(0, (double)+1.501F, 0);
		stack.translate(0, -entity.getHeight() * 1.15, 0);
		stack.translate(0, -getYOffset(), 0);
		
		// Undo entity rotation
		stack.rotate(Vector3f.YP.rotationDegrees(-entYaw));
		
		// Orbit
		final float periodBaseTicks = 5 * 20; // move to static
		final float periodTicks = periodBaseTicks / this.getOrbitSpeed();
		final float orbitProg = (((entity.ticksExisted + partialTicks) % periodTicks) / periodTicks) + this.getOrbitOffset();
		
		stack.rotate(Vector3f.YP.rotationDegrees(orbitProg * 360f));
		
		stack.translate(0, 0, -entity.getWidth()*.5f);
		
		// Rotate to camera
		//stack.rotate(Vector3f.YP.rotationDegrees(180f + activeInfo.getYaw()));
		stack.rotate(Vector3f.YP.rotationDegrees(-(orbitProg * 360f)));
		stack.scale(1f, -1f, -1f);
		stack.rotate(activeInfo.getRotation());
		
		renderOrb(stack, typeBuffer, packedLight, getWidth(), red, green, blue, alpha);
		stack.pop();
	}
	
	protected void renderOrb(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, float width, float red, float green, float blue, float alpha) {
		final IVertexBuilder buffer = typeBuffer.getBuffer(RenderType.getEntityTranslucent(TEX_BUBBLE));
		final Matrix4f transform = stack.getLast().getMatrix();
		final Matrix3f normal = stack.getLast().getNormal();
		final int packedOverlay = OverlayTexture.NO_OVERLAY;
		final float xMin = -width/2f;
		final float xMax = width/2f;
		final float yMin = -width/2f;
		final float yMax = width/2f;
		final float uMin = 0;
		final float uMax = 1;
		final float vMin = 0;
		final float vMax = 1;
		buffer.pos(transform, xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMax).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMax).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMin).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMin).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
	}
}
