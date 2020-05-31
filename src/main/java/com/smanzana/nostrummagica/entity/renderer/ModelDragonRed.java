package com.smanzana.nostrummagica.entity.renderer;

import java.util.EnumMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRedBase;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ModelDragonRed extends ModelBase {

	private Map<EDragonPart, RenderObj> renderers;
	private RenderObj baseRenderer;
	
	public ModelDragonRed(int color) {
		super();
		
		renderers = new EnumMap<>(EDragonPart.class);
		
		for (EDragonPart part : EDragonPart.values()) {
			RenderObj render = new RenderObj(this, part.getLoc()) {
				@Override
				protected int getColor() {
					return color;
				}
			};
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
				RenderObj parent = renderers.get(part.parent);
				if (null == parent) {
					NostrumMagica.logger.error("Dragon part iteration did not set up parents right!");
				} else {
					parent.addChild(render);
				}
			}
			
			renderers.put(part, render);
		}
	}
	
	public ModelDragonRed() {
		this(-1);
	}

	
	
	/*
	 * if (model == EDragonPart.LEG_BACK_LEFT.ordinal()) {
			GlStateManager.translate(.54, -1, 1.78);
		} else if (model == EDragonPart.LEG_BACK_RIGHT.ordinal()) {
			GlStateManager.translate(-.593, -1, 1.64);
		} else if (model == EDragonPart.BODY.ordinal()) {
			GlStateManager.translate(0, -.889, 0);
		} else if (model == EDragonPart.LEG_FRONT_LEFT.ordinal()) {
			GlStateManager.translate(.75, -.45, -.76);
		} else if (model == EDragonPart.LEG_FRONT_RIGHT.ordinal()) {
			GlStateManager.translate(-.725, -.45, -.82);
		} else if (model == EDragonPart.NECK.ordinal()) {
			GlStateManager.translate(0, -.975, -1.6);
		} else if (model == EDragonPart.HEAD.ordinal()) {
			GlStateManager.translate(0, -2.05, -1.575);
		} else if (model == EDragonPart.WING_LEFT.ordinal()) {
			GlStateManager.translate(.35, -1.4, -.75);
		} else if (model == EDragonPart.WING_RIGHT.ordinal()) {
			GlStateManager.translate(-.35, -1.4, -.75);
		} else if (model == EDragonPart.TAIL1.ordinal()) {
			GlStateManager.translate(0, -.95, 2.9);
		} else if (model == EDragonPart.TAIL2.ordinal()) {
			GlStateManager.translate(0, .2245, 6.67);
		} else if (model == EDragonPart.TAIL3.ordinal()) {
			GlStateManager.translate(0, .85, 9.9);
		}
	 * 
	 * 
	 */
	
	protected static enum EDragonPart {
		BODY("entity/red_dragon/body.obj", 0, -.889, 0),
		NECK("entity/red_dragon/neck.obj", 0, -.975, -1.6, EDragonPart.BODY),
		HEAD("entity/red_dragon/head.obj", 0, -1.90, -1.575, EDragonPart.NECK),
		//TAIL1("entity/red_dragon/tail1.obj", 0, -.95, 2.9, EDragonPart.BODY),
		//TAIL2("entity/red_dragon/tail2.obj", 0, .2245, 6.67, EDragonPart.TAIL1),
		//TAIL3("entity/red_dragon/tail3.obj", 0, .85, 9.9, EDragonPart.TAIL2),
		TAIL("entity/red_dragon/tail.obj", 0, -.95, 2.5, EDragonPart.BODY),
		WING_LEFT("entity/red_dragon/wing_left.obj", .35, -1.4, -.75, EDragonPart.BODY),
		WING_RIGHT("entity/red_dragon/wing_right.obj", -.35, -1.4, -.75, EDragonPart.BODY),
		LEG_FRONT_LEFT("entity/red_dragon/leg_fl.obj", .75, -.45, -.76, EDragonPart.BODY),
		LEG_FRONT_RIGHT("entity/red_dragon/leg_fr.obj", -.725, -.45, -.82, EDragonPart.BODY),
		LEG_BACK_LEFT("entity/red_dragon/leg_bl.obj", .54, -1, 1.78, EDragonPart.BODY),
		LEG_BACK_RIGHT("entity/red_dragon/leg_br.obj", -.593, -1, 1.64, EDragonPart.BODY);
		
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
	
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		setRotationAngles(time, swingProgress, swing, headAngleY, headAngleX, scale, entity);
		
		GL11.glPushMatrix();
		
		float modelScale = 1.0f;// / 20.0f; // 16 pixels wide model to .8 blocks
		GL11.glScalef(modelScale, modelScale, modelScale);
		
		this.baseRenderer.render(scale);
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		
		float period;
		float frac;
		float weight;
		
		EntityDragonRedBase dragon = (EntityDragonRedBase) entityIn;
		
		limbSwing *= .4;
		limbSwingAmount *= .4;
		
		// Reset all rotations to 0
		for (EDragonPart part : EDragonPart.values()) {
			RenderObj render = renderers.get(part);
			render.rotateAngleX = render.rotateAngleY = render.rotateAngleZ = 0f;
		}
		
		RenderObj wing_left = renderers.get(EDragonPart.WING_LEFT);
		RenderObj wing_right = renderers.get(EDragonPart.WING_RIGHT);
		RenderObj body = renderers.get(EDragonPart.BODY);
		
		final long now = System.currentTimeMillis();
		long stateTime = now - dragon.getFlyStateTime();
		
		boolean flying = dragon.isFlying();
		
		boolean sitting = false;
		if (dragon instanceof EntityTameDragonRed) {
			sitting = ((EntityTameDragonRed) dragon).isSitting();
		}
		
		if (flying) {
			period = 1200.0f;
			frac = (float) (stateTime % period) / period;
			weight = (float) Math.sin(frac * Math.PI * 2);
			
			wing_left.rotateAngleX = wing_left.rotateAngleY = 0f;
			wing_left.rotateAngleZ = (weight * .8f);
			wing_right.rotateAngleX = wing_right.rotateAngleY = 0f;
			wing_right.rotateAngleZ = -(weight * .8f);
			body.rotateAngleX = 0f;
		} else {
			float rotX = (float) (2 * Math.PI * 0.14);
			float rotY = (float) (2 * Math.PI * 0.22);
			float rotZ = (float) (2 * Math.PI * 0.05);
			
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
		
		
		
		RenderObj head = renderers.get(EDragonPart.HEAD);
		head.rotateAngleY = (float) (netHeadYaw / -360f * 2 * Math.PI);
		head.rotateAngleX = (float) (headPitch / 360f * 2 * Math.PI);
		
		RenderObj frontleg_left = renderers.get(EDragonPart.LEG_FRONT_LEFT);
		RenderObj frontleg_right = renderers.get(EDragonPart.LEG_FRONT_RIGHT);
		RenderObj backleg_left = renderers.get(EDragonPart.LEG_BACK_LEFT);
		RenderObj backleg_right = renderers.get(EDragonPart.LEG_BACK_RIGHT);

		backleg_left.offsetY = (float) EDragonPart.BODY.offsetY - (float) EDragonPart.LEG_BACK_LEFT.offsetY; 
		backleg_right.offsetY = (float) EDragonPart.BODY.offsetY - (float) EDragonPart.LEG_BACK_RIGHT.offsetY;
		if (sitting) {
			frontleg_left.rotateAngleX = .45f;
			frontleg_right.rotateAngleX = .45f;
			backleg_left.rotateAngleX = backleg_right.rotateAngleX = 0f;
			backleg_left.offsetY = backleg_right.offsetY = -.5f;
		} else if (!flying && dragon.onGround) {
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
		
		RenderObj tail = renderers.get(EDragonPart.TAIL);
		
		period = 80.0f;
		frac = (float) (ageInTicks % period) / period;
		weight = (float) Math.sin(frac * Math.PI * 2);
		
		float amt = (float) (weight * (.785398163397)) / 6; // 45degrees in radians
		tail.rotateAngleY = amt;
		
	}

}
