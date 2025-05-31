package com.smanzana.nostrummagica.client.render.entity;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.equipment.SpellTome.TomeStyle;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.ChargeType;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SpelltomeRenderer {

	// Based on EnchantTableRenderer
	
	private final BookModel bookModel;
	
	public SpelltomeRenderer() {
		bookModel = new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK));
	}
	
	protected void translateIfSneaking(PoseStack matrixStack, LivingEntity entity) {
		// copied from curios :)
		if (entity.isCrouching()) {
			matrixStack.translate(0.0F, 0.1875F, 0.0F);
		}
	}
	
	protected void rotateIfSneaking(PoseStack matrixStack, LivingEntity entity) {
		if (entity.isCrouching()) {
			matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F / (float) Math.PI));
		}
	}
	
	@SuppressWarnings("deprecation")
	protected Material getMaterialForStyle(@Nullable TomeStyle style) {
		if (style == null) {
			return EnchantTableRenderer.BOOK_LOCATION;
		}
		
		return new Material(TextureAtlas.LOCATION_BLOCKS, NostrumMagica.Loc("entity/spelltome_render_" + style.name().toLowerCase()));
	}
	
	public void render(PoseStack matrixStack, LivingEntity entity, MultiBufferSource renderTypeBuffer, float partialTicks, float ageInTicks, int light, @Nullable TomeStyle style) {
		final SpellCharge charge = NostrumMagica.spellChargeTracker.getCharge(entity); 
		final boolean isCharging = charge != null && charge.type() == ChargeType.TOME_CAST;
		final float animTicks = getAnimTicks(entity) + partialTicks;
		
		// update animation 
		this.updateCharging(entity, isCharging);
		
		
		
		matrixStack.pushPose();
		
		if (!isCharging) translateIfSneaking(matrixStack, entity);
		if (!isCharging) rotateIfSneaking(matrixStack, entity);
		
		final Vec3 offset = this.getAnimPos(isCharging, animTicks);
		matrixStack.translate(offset.x, offset.y, offset.z);
		matrixStack.mulPose(this.getAnimRot(isCharging, animTicks));
		matrixStack.scale(.75f, .75f, .75f);
		
		final float flip1 = this.getPage1Flip(isCharging, animTicks);
		final float flip2 = this.getPage2Flip(isCharging, animTicks);
		
		final float openProg = this.getOpenProg(isCharging, animTicks);
		
		bookModel.setupAnim(ageInTicks, Mth.clamp(flip1, 0.0F, 1.0F), Mth.clamp(flip2, 0.0F, 1.0F), openProg);
		VertexConsumer vertexconsumer = getMaterialForStyle(style).buffer(renderTypeBuffer, RenderType::entitySolid);
		this.bookModel.render(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		
		matrixStack.popPose();
	}
	
	protected void updateCharging(LivingEntity entity, boolean isCharging) {
		castTracker.setCharging(entity, isCharging);
	}
	
	// Animation calculators
	protected Vec3 getLerpedAnimPos(float prog) {
		final Vec3 full = new Vec3(.45, .2, -.75); 
		final Vec3 start = new Vec3(.35, .6, .05);
		
		if (prog >= 1f) {
			return full;
		} else {
			return start.add(full.subtract(start).scale(prog));
		}
	}
	
	protected Vec3 getCastingAnimPos(float ticksElapsed) {
		final float bobPeriod = 20;
		final float bobProg = (ticksElapsed % bobPeriod) / bobPeriod;
		final float bobAmt = .05f * Mth.sin(Mth.PI * 2 * bobProg);
		return getLerpedAnimPos(ticksElapsed / 10f).add(0, bobAmt, 0);
	}
	
	protected Vec3 getClosingAnimPos(float ticksElapsed) {
		return getLerpedAnimPos(1f - Math.min(1f, ticksElapsed / 20f));
	}
	
	protected Vec3 getAnimPos(boolean charging, float ticksElapsed) {
		if (charging) {
			return getCastingAnimPos(ticksElapsed);
		} else {
			return getClosingAnimPos(ticksElapsed);
		}
	}
	
	protected Quaternion getLerpedAnimRot(float prog) {
		final float fullX = 60f;
		final float fullY = 255f;
		final float fullZ = 0f;

		final float startX = 90f;
		final float startY = 85f;
		final float startZ = 0f;
		
		final Vector3f rot;
		
		if (prog >= 1f) {
			rot = new Vector3f(fullX, fullY, fullZ);
		} else if (prog <= 0f) {
			rot = new Vector3f(startX, startY, startZ);
		} else {
			rot = new Vector3f(Mth.lerp(prog, startX, fullX),
					Mth.lerp(prog, startY, fullY),
					Mth.lerp(prog, startZ, fullZ)
					);
		}
		
		return Quaternion.fromXYZDegrees(rot);
	}
	
	protected Quaternion getCastingAnimRot(float ticksElapsed) {
		return getLerpedAnimRot(ticksElapsed / 10f);
	}
	
	protected Quaternion getClosingAnimRot(float ticksElapsed) {
		return getLerpedAnimRot(1f - Math.min(1f, ticksElapsed / 20f));
	}
	
	protected Quaternion getAnimRot(boolean charging, float ticksElapsed) {
		if (charging) {
			return getCastingAnimRot(ticksElapsed);
		} else {
			return getClosingAnimRot(ticksElapsed);
		}
	}
	
	protected float getCastingOpenProg(float ticksElapsed) {
		return Math.min(ticksElapsed, 10f) / 10f;
	}
	
	protected float getClosingOpenProg(float ticksElapsed) {
		return 1f - (Math.min(ticksElapsed, 10f) / 10f);
	}
	
	protected float getOpenProg(boolean charging, float ticksElapsed) {
		if (charging) {
			return getCastingOpenProg(ticksElapsed);
		} else {
			return getClosingOpenProg(ticksElapsed);
		}
	}
	
	protected float getPage1Flip(boolean charging, float ticksElapsed) {
		if (!charging) {
			return 0;
		}
		final float flipProg = (ticksElapsed % 30f) / 30f;
		return Mth.frac(flipProg + 0.25F) * 1.6F - 0.3F;
	}
	
	protected float getPage2Flip(boolean charging, float ticksElapsed) {
		if (!charging) {
			return 0;
		}
		
		final float flipProg = (ticksElapsed % 30f) / 30f;
		return  Mth.frac(flipProg + 0.75F) * 1.6F - 0.3F;
	}
	
	protected int getAnimTicks(LivingEntity ent) {
		return castTracker.chargeTicksElapsed(ent);
	}
	
	private TomeCastTracker castTracker = new TomeCastTracker();
	
	/**
	 * Client-side (but mostly third-party) tracking of when players start casting and how far into their charge they are.
	 * There's no good way to communicate this data to the server when you start charging, and no good way to servers to capture
	 * what time they _think_ someone started charging.
	 * So instead of providing a REALLY inaccurate set of info to base animations on in the client<->server charge manager, just
	 * track it lazily here and use it to inform animation without it looking official.
	 */
	private static class TomeCastTracker {
		private static final class Tracker {
			public int startTicks; // Ticks last marked. Could be started charging, or stopped charging
			public boolean charging;
		}
		
		private final Map<LivingEntity, Tracker> trackers;
		
		public TomeCastTracker() {
			trackers = new HashMap<>();
		}
		
		private final @Nonnull Tracker getTracker(LivingEntity entity) {
			return trackers.computeIfAbsent(entity, (e) -> new Tracker());
		}
		
		public final void setCharging(LivingEntity entity, boolean charging) {
			Tracker tracker = getTracker(entity);
			if (tracker.charging != charging) {
				tracker.startTicks = entity.tickCount;
				tracker.charging = charging;
			}
		}
		
//		public final boolean isCharging(LivingEntity entity) {
//			Tracker tracker = getTracker(entity);
//			return tracker.charging;
//		}
		
		public final int chargeTicksElapsed(LivingEntity entity) {
			Tracker tracker = getTracker(entity);
			return entity.tickCount - tracker.startTicks;
		}
	}
	
}
