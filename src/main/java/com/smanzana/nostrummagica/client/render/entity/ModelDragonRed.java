package com.smanzana.nostrummagica.client.render.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRedBase;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.utils.ColorUtil;
import com.smanzana.nostrummagica.utils.MemoryPool;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ModelDragonRed<T extends EntityDragonRedBase> extends EntityModel<T> {
	
	public static enum EDragonOverlayMaterial {
		NONE(""),
		SCALES("scales"),
		GOLD("gold"),
		DIAMOND("diamond");
		
		public final String locSuffix;
		
		private EDragonOverlayMaterial(String locSuffix) {
			this.locSuffix = locSuffix;
		}
		
		public String getSuffix() {
			return locSuffix;
		}
	}
	
	protected static class ModelRendererBakedWithOffset extends ModelRendererBaked {
		
		protected float offsetX;
		protected float offsetY;
		protected float offsetZ;
		
		public ModelRendererBakedWithOffset(Model base, ResourceLocation model) {
			super(base, model);
		}
		
		// Made public with AT :)
		@Override
		public void translateRotate(MatrixStack matrixStackIn) {
			// Apply offset
			matrixStackIn.translate(offsetX, offsetY, offsetZ);
			super.translateRotate(matrixStackIn);

		}
	}
	
	protected static class DragonArmorKey {
		public EDragonArmorPart part;
		public EDragonOverlayMaterial material;
		
		public DragonArmorKey() {
			;
		}
		
		public DragonArmorKey set(EDragonArmorPart part, EDragonOverlayMaterial material) {
			this.part = part;
			this.material = material;
			return this;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DragonArmorKey) {
				DragonArmorKey other = (DragonArmorKey) o;
				return other.part == this.part && other.material == this.material;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return (part.ordinal() << 16) | material.ordinal(); 
		}
	}
	
	protected static final MemoryPool<DragonArmorKey> KeyPool = new MemoryPool<>(() -> { return new DragonArmorKey(); });

	private Map<EDragonPart, ModelRendererBakedWithOffset> renderers;
	private Map<DragonArmorKey, ModelRendererBakedWithOffset> overlays;
	private Map<EDragonArmorPart, EDragonOverlayMaterial> overlayMaterial;
	private ModelRendererBakedWithOffset baseRenderer;
	
	private float[] color;
	
	public ModelDragonRed(int color) {
		super(RenderType::getEntityTranslucent);
		
		this.color = ColorUtil.ARGBToColor(color);
		
		renderers = new EnumMap<>(EDragonPart.class);
		
		for (EDragonPart part : EDragonPart.values()) {
			ModelRendererBakedWithOffset render = new ModelRendererBakedWithOffset(this, part.getLoc());
			render.setTextureOffset(0, 0); // TODO add texture offsets?
			// Set offset?
			render.offsetX = (float) part.getX();
			render.offsetY = (float) part.getY();
			render.offsetZ = (float) part.getZ();
			//render.setRotationPoint((float) part.getX(), (float) part.getY(), (float) part.getZ());
			
			if (part.parent == null) {
				if (part != EDragonPart.BODY) {
					NostrumMagica.logger.error("What part is " + part.name() + "   and why isn't it parented?");
				} else {
					baseRenderer = render;
				}
			} else {
				ModelRendererBakedWithOffset parent = renderers.get(part.parent);
				if (null == parent) {
					NostrumMagica.logger.error("Dragon part iteration did not set up parents right!");
				} else {
					parent.addChild(render);
				}
			}
			
			renderers.put(part, render);
		}
		
		overlays = new HashMap<>();
		overlayMaterial = new EnumMap<>(EDragonArmorPart.class);
		
		for (EDragonArmorPart part : EDragonArmorPart.values()) {
			for (EDragonOverlayMaterial material : EDragonOverlayMaterial.values()) {
				if (material == EDragonOverlayMaterial.NONE) {
					continue;
				}
				
				ResourceLocation loc = new ResourceLocation(NostrumMagica.MODID, part.getLocPrefix() + material.getSuffix() + "");
				ModelRendererBakedWithOffset render = new ModelRendererBakedWithOffset(this, loc);
				render.setTextureOffset(0, 0); // TODO add texture offsets?
				render.offsetX = (float) part.getX();
				render.offsetY = (float) part.getY();
				render.offsetZ = (float) part.getZ();
				
				if (part.parent == null) {
					NostrumMagica.logger.error("What part is " + part.name() + "   and why isn't it parented?");
				} else {
					ModelRendererBakedWithOffset parent = renderers.get(part.parent);
					if (null == parent) {
						NostrumMagica.logger.error("Dragon part iteration did not set up parents right!");
					} else {
						parent.addChild(render);
					}
				}
				
				DragonArmorKey key = new DragonArmorKey().set(part, material); // NOT using pool for actual map keys
				overlays.put(key, render);
			}
			
			overlayMaterial.put(part, EDragonOverlayMaterial.NONE);
		}
	}
	
	public ModelDragonRed() {
		this(-1);
	}

	protected static enum EDragonPart {
		BODY("entity/red_dragon/body", 0, -.889, 0),
		NECK("entity/red_dragon/neck", .019, -.955, -1.585, EDragonPart.BODY),
		HEAD("entity/red_dragon/head", 0, -1.90, -1.575, EDragonPart.NECK),
		//TAIL1("entity/red_dragon/tail1", 0, -.95, 2.9, EDragonPart.BODY),
		//TAIL2("entity/red_dragon/tail2", 0, .2245, 6.67, EDragonPart.TAIL1),
		//TAIL3("entity/red_dragon/tail3", 0, .85, 9.9, EDragonPart.TAIL2),
		TAIL("entity/red_dragon/tail", 0, -.975, 2.81, EDragonPart.BODY),
		WING_LEFT("entity/red_dragon/wing_left", .275, -1.7, -.95, EDragonPart.BODY),
		WING_RIGHT("entity/red_dragon/wing_right", -.275, -1.7, -.95, EDragonPart.BODY),
		LEG_FRONT_LEFT("entity/red_dragon/leg_fl", .75, -.45, -.76, EDragonPart.BODY),
		LEG_FRONT_RIGHT("entity/red_dragon/leg_fr", -.725, -.45, -.82, EDragonPart.BODY),
		LEG_BACK_LEFT("entity/red_dragon/leg_bl", .54, -1, 1.78, EDragonPart.BODY),
		LEG_BACK_RIGHT("entity/red_dragon/leg_br", -.593, -1, 1.64, EDragonPart.BODY);
		
		private ResourceLocation loc;
		public double offsetX;
		public double offsetY;
		public double offsetZ;
		public EDragonPart parent;
		
		private EDragonPart(String path, double x, double y, double z) {
			this(path, x, y, z, null);
		}
		
		private EDragonPart(String path, double x, double y, double z, EDragonPart parent){
			loc = new ResourceLocation(NostrumMagica.MODID, path);
			offsetX = x;
			offsetY = y;
			offsetZ = z;
			this.parent = parent;
		}
		
		public static ResourceLocation[] allLocs() {
			EDragonPart[] parts = EDragonPart.values();
			ResourceLocation[] locs = new ResourceLocation[parts.length];
			
			for (int i = 0; i < parts.length; i++) {
				locs[i] = parts[i].getLoc();
			}
			
			return locs;
		}
		
		public ResourceLocation getLoc() {
			return loc;
		}
		
		public double getX() {
			double parX = (this.parent == null ? 0 : this.parent.offsetX);
			
			return offsetX - parX;
		}
		
		public double getY() {
			double parY = (this.parent == null ? 0 : this.parent.offsetY);
			
			return offsetY - parY;
		}
		
		public double getZ() {
			double parZ = (this.parent == null ? 0 : this.parent.offsetZ);
			
			return offsetZ - parZ;
		}
	}
	
	public static enum EDragonArmorPart {
		BODY("entity/red_dragon/body_overlay_", 0, 0, 0, EDragonPart.BODY),
		HEAD("entity/red_dragon/head_overlay_", 0, -1.90, -1.575, EDragonPart.NECK); // have to copy insteadof parenting :(
		
		private final String locPrefix;
		private final EDragonPart parent;
		public final double offsetX;
		public final double offsetY;
		public final double offsetZ;
		
		private EDragonArmorPart(String pathPrefix, double offsetX, double offsetY, double offsetZ, EDragonPart parent) {
			locPrefix = pathPrefix;
			this.parent = parent;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
		}
		
		public static ResourceLocation[] allLocs() {
			EDragonPart[] parts = EDragonPart.values();
			ResourceLocation[] locs = new ResourceLocation[parts.length];
			
			for (int i = 0; i < parts.length; i++) {
				locs[i] = parts[i].getLoc();
			}
			
			return locs;
		}
		
		public String getLocPrefix() {
			return locPrefix;
		}
		
		public double getX() {
//			// Special case for 0
//			if (this.offsetX == 0) {
//				return 0;
//			}
			
			double parX = (this.parent == null ? 0 : this.parent.offsetX);
			
			return offsetX - parX;
		}
		
		public double getY() {
			// Special case for 0
			if (this.offsetY == 0) {
				return 0;
			}
			double parY = (this.parent == null ? 0 : this.parent.offsetY);
			
			return offsetY - parY;
		}
		
		public double getZ() {
			// Special case for 0
			if (this.offsetZ == 0) {
				return 0;
			}
			double parZ = (this.parent == null ? 0 : this.parent.offsetZ);
			
			return offsetZ - parZ;
		}
		
		public EDragonPart getParent() {
			return parent;
		}
	}
	
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		// Update overlay visibility
		for (DragonArmorKey key : overlays.keySet()) {
			final ModelRendererBakedWithOffset renderer = overlays.get(key);
			final EDragonOverlayMaterial material = overlayMaterial.get(key.part);
			renderer.showModel = (material == key.material);
		}
		
		// Apply color supplied in constructor
		red *= this.color[0];
		green *= this.color[1];
		blue *= this.color[2];
		alpha *= this.color[3];
		
//		final float modelScale = 1.0f;// / 20.0f; // 16 pixels wide model to .8 blocks
//		matrixStackIn.push();
//		matrixStackIn.scale(modelScale, modelScale, modelScale);
		matrixStackIn.push();
		this.baseRenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
//		matrixStackIn.pop();
	}
	
	@Override
	public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks) {
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
		float frac;
		float weight;
		
		EntityDragonRedBase dragon = (EntityDragonRedBase) entityIn;
		
		limbSwing *= .4;
		limbSwingAmount *= .4;
		
		// Reset all rotations to 0
		for (EDragonPart part : EDragonPart.values()) {
			ModelRendererBakedWithOffset render = renderers.get(part);
			render.rotateAngleX = render.rotateAngleY = render.rotateAngleZ = 0f;
		}
		
		ModelRendererBakedWithOffset wing_left = renderers.get(EDragonPart.WING_LEFT);
		ModelRendererBakedWithOffset wing_right = renderers.get(EDragonPart.WING_RIGHT);
		ModelRendererBakedWithOffset body = renderers.get(EDragonPart.BODY);
		
		final long now = System.currentTimeMillis();
		long stateTime = now - dragon.getFlyStateTime();
		
		boolean flying = dragon.isFlying();
		
		boolean sitting = false;
		if (dragon instanceof EntityTameDragonRed) {
			sitting = ((EntityTameDragonRed) dragon).isEntitySitting();
		}
		
		boolean casting = dragon.isCasting();
		
		if (flying) {
			frac = dragon.getWingFlag(partialTicks);
			weight = (float) Math.sin(frac * Math.PI * 2);
			
			wing_left.rotateAngleX = wing_left.rotateAngleY = 0f;
			wing_left.rotateAngleZ = (weight * .8f);
			wing_right.rotateAngleX = wing_right.rotateAngleY = 0f;
			wing_right.rotateAngleZ = -(weight * .8f);
			body.rotateAngleX = 0f;
		} else if (casting) {
			// Done in setRotationAngles because it's animated
			//body.rotateAngleX = -.5f;
		} else if (!dragon.isOnGround() && dragon.getMotion().y < -.62f) {
			// Falling
			float rotX = (float) (2 * Math.PI * 0.14);
			float rotY = (float) (2 * Math.PI * -0.12);
			float rotZ = (float) (2 * Math.PI * -0.05);
			wing_left.rotateAngleX = rotX;
			wing_left.rotateAngleY = rotY;
			wing_left.rotateAngleZ = rotZ;
			wing_right.rotateAngleX = rotX;
			wing_right.rotateAngleY = -rotY;
			wing_right.rotateAngleZ = -rotZ;
			body.rotateAngleX = -.1f;
		} else {
			float rotX = (float) (2 * Math.PI * 0.168);
			float rotY = (float) (2 * Math.PI * -0.2);
			float rotZ = (float) (2 * Math.PI * -0.05);
			
			if (dragon.isFlightTransitioning()) {
				float scale = (float) (stateTime)
						/ (float) EntityDragonRed.ANIM_UNFURL_DUR;
				
				if (!dragon.isLanding()) {
					scale = (1f - scale);
				}
				
				rotX *= scale;
				rotY *= scale;
				rotZ *= scale;
			}
			
			wing_left.rotateAngleX = rotX;
			wing_left.rotateAngleY = rotY;
			wing_left.rotateAngleZ = rotZ;
			wing_right.rotateAngleX = rotX;
			wing_right.rotateAngleY = -rotY;
			wing_right.rotateAngleZ = -rotZ;
			body.rotateAngleX = 0f;
			
			if (sitting) {
				body.rotateAngleX = -0.3f;
			}
		}
	}
	
	@Override
	public void setRotationAngles(T dragon, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		
		float period;
		float frac;
		float weight;
		
		limbSwing *= .4;
		limbSwingAmount *= .4;
		
		// Reset all rotations to 0
//		for (EDragonPart part : EDragonPart.values()) {
//			RenderObj render = renderers.get(part);
//			render.rotateAngleX = render.rotateAngleY = render.rotateAngleZ = 0f;
//		}
		
//		RenderObj wing_left = renderers.get(EDragonPart.WING_LEFT);
//		RenderObj wing_right = renderers.get(EDragonPart.WING_RIGHT);
//		RenderObj body = renderers.get(EDragonPart.BODY);
		
		final long now = System.currentTimeMillis();
		long stateTime = now - dragon.getFlyStateTime();
		
		boolean flying = dragon.isFlying();
		
		boolean sitting = false;
		if (dragon instanceof EntityTameDragonRed) {
			sitting = ((EntityTameDragonRed) dragon).isEntitySitting();
		}
		
		boolean casting = dragon.isCasting();
//		
//		if (flying) {
//			if (dragon instanceof EntityTameDragonRed) {
//				frac = dragon.getWingFlag();
//			} else {
//				period = 1200.0f;
//				frac = (float) (stateTime % period) / period;
//			}
//			weight = (float) Math.sin(frac * Math.PI * 2);
//			
//			wing_left.rotateAngleX = wing_left.rotateAngleY = 0f;
//			wing_left.rotateAngleZ = (weight * .8f);
//			wing_right.rotateAngleX = wing_right.rotateAngleY = 0f;
//			wing_right.rotateAngleZ = -(weight * .8f);
//			body.rotateAngleX = 0f;
//		} else {
//			float rotX = (float) (2 * Math.PI * 0.14);
//			float rotY = (float) (2 * Math.PI * 0.22);
//			float rotZ = (float) (2 * Math.PI * 0.05);
//			
//			if (dragon.isFlightTransitioning()) {
//				float scale = (float) (stateTime)
//						/ (float) EntityDragonRed.ANIM_UNFURL_DUR;
//				
//				if (!dragon.isLanding()) {
//					scale = (1f - scale);
//				}
//				
//				rotX *= scale;
//				rotY *= scale;
//				rotZ *= scale;
//			}
//			
//			wing_left.rotateAngleX = rotX;
//			wing_left.rotateAngleY = rotY;
//			wing_left.rotateAngleZ = rotZ;
//			wing_right.rotateAngleX = rotX;
//			wing_right.rotateAngleY = -rotY;
//			wing_right.rotateAngleZ = -rotZ;
//			body.rotateAngleX = 0f;
//			
//			if (sitting) {
//				body.rotateAngleX = -0.3f;
//			}
//		}
		
		
		
		ModelRendererBakedWithOffset head = renderers.get(EDragonPart.HEAD);
		head.rotateAngleY = (float) (netHeadYaw / -360f * 2 * Math.PI);
		head.rotateAngleX = (float) (headPitch / 360f * 2 * Math.PI);
		
		ModelRendererBakedWithOffset frontleg_left = renderers.get(EDragonPart.LEG_FRONT_LEFT);
		ModelRendererBakedWithOffset frontleg_right = renderers.get(EDragonPart.LEG_FRONT_RIGHT);
		ModelRendererBakedWithOffset backleg_left = renderers.get(EDragonPart.LEG_BACK_LEFT);
		ModelRendererBakedWithOffset backleg_right = renderers.get(EDragonPart.LEG_BACK_RIGHT);
		
		ModelRendererBakedWithOffset body = renderers.get(EDragonPart.BODY);
		ModelRendererBakedWithOffset wing_left = renderers.get(EDragonPart.WING_LEFT);
		ModelRendererBakedWithOffset wing_right = renderers.get(EDragonPart.WING_RIGHT);

		backleg_left.offsetY = (float) EDragonPart.BODY.offsetY - (float) EDragonPart.LEG_BACK_LEFT.offsetY; 
		backleg_right.offsetY = (float) EDragonPart.BODY.offsetY - (float) EDragonPart.LEG_BACK_RIGHT.offsetY;
		if (casting) {
			stateTime = now - dragon.getLastCastTime();
			final float progress;
			final long castDurationMS = 1000;
			if (stateTime < castDurationMS) {
				progress = (float) stateTime / (float) castDurationMS;
			} else {
				progress = 1f;
			}
			
			head.rotateAngleX = progress * .2f;
			frontleg_left.rotateAngleX = progress * .80f;
			frontleg_right.rotateAngleX = progress * .8125f;
			backleg_left.rotateAngleX = backleg_right.rotateAngleX = 0f;
			backleg_left.offsetY = backleg_right.offsetY = progress * -.5f;
			body.rotateAngleX = progress * -.5f;
			
			if (!flying) {
				float invProgress = 1 - progress;
				float rotX = (float) (2 * Math.PI * 0.168);
				float rotY = (float) (2 * Math.PI * 0.2);
				float rotZ = (float) (2 * Math.PI * 0.05);
				wing_left.rotateAngleX = invProgress * rotX;
				wing_left.rotateAngleY = invProgress * rotY;
				wing_left.rotateAngleZ = invProgress * rotZ;
				wing_right.rotateAngleX = invProgress * rotX;
				wing_right.rotateAngleY = invProgress * -rotY;
				wing_right.rotateAngleZ = invProgress * -rotZ;
			}
		} else if (sitting) {
			frontleg_left.rotateAngleX = .45f;
			frontleg_right.rotateAngleX = .45f;
			backleg_left.rotateAngleX = backleg_right.rotateAngleX = 0f;
			backleg_left.offsetY = backleg_right.offsetY = -.5f;
		} else if (!flying && dragon.isOnGround()) {
			frontleg_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			frontleg_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
			
			limbSwingAmount *= .5;
			backleg_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
			backleg_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		} else {
			float ang = (float) (Math.PI * 2 * 0.12);
			frontleg_left.rotateAngleX = frontleg_right.rotateAngleX = ang;
			
			ang = (float) (Math.PI * 2 * 0.15);
			
			backleg_left.rotateAngleX = backleg_right.rotateAngleX = ang;
		}
		
		stateTime = now - dragon.getLastSlashTime();
		if (stateTime < EntityDragonRed.ANIM_SLASH_DUR) {
			float progress = (float) stateTime / (float) EntityDragonRed.ANIM_SLASH_DUR;
			float ang = (float) Math.sin(progress * (float) (Math.PI * 2));
			ang *= Math.PI * 2 * -.15;
			frontleg_right.rotateAngleX += ang;
		}
		
		stateTime = now - dragon.getLastBiteTime();
		if (stateTime < EntityDragonRed.ANIM_BITE_DUR) {
			float progress = (float) stateTime / (float) EntityDragonRed.ANIM_SLASH_DUR;
			float ang = (float) Math.sin(progress * (float) (Math.PI * 2));
			ang *= Math.PI * 2 * .1;
			head.rotateAngleX += ang;
		}
		
		ModelRendererBakedWithOffset tail = renderers.get(EDragonPart.TAIL);
		
		period = 80.0f;
		frac = (float) (ageInTicks % period) / period;
		weight = (float) Math.sin(frac * Math.PI * 2);
		
		float amt = (float) (weight * (.785398163397)) / 6; // 45degrees in radians
		tail.rotateAngleY = amt;
		
		for (EDragonOverlayMaterial mat : EDragonOverlayMaterial.values()) {
			if (mat == EDragonOverlayMaterial.NONE) {
				continue;
			}
			DragonArmorKey key = KeyPool.claim().set(EDragonArmorPart.HEAD, mat);
			ModelRendererBakedWithOffset headArmor = overlays.get(key); //TODO why is this needed? It avoids allocating a key each time we access the map
			
			headArmor.rotateAngleY = head.rotateAngleY;
			headArmor.rotateAngleX = head.rotateAngleX;
			KeyPool.release(key);
		}
		
	}
	
	public void setOverlayMaterial(EDragonArmorPart part, EDragonOverlayMaterial material) {
		this.overlayMaterial.put(part, material);
	}
	
	public void hideAllOverlays() {
		for (EDragonArmorPart part : EDragonArmorPart.values()) {
			overlayMaterial.put(part, EDragonOverlayMaterial.NONE);
		}
	}
	
	public static List<ResourceLocation> getModelParts() {
		List<ResourceLocation> list = new ArrayList<>(EDragonPart.values().length + (EDragonArmorPart.values().length * EDragonOverlayMaterial.values().length));
		for (EDragonPart part : EDragonPart.values()) {
			// Body pieces
			list.add(part.getLoc());
		}
		
		// Armor overlays
		for (EDragonArmorPart part : EDragonArmorPart.values()) {
			for (EDragonOverlayMaterial material : EDragonOverlayMaterial.values()) {
				if (material == EDragonOverlayMaterial.NONE) {
					continue;
				}
				
				ResourceLocation loc = new ResourceLocation(NostrumMagica.MODID, part.getLocPrefix() + material.getSuffix() + "");
				list.add(loc);
			}
		}
		return list;
	}

}
