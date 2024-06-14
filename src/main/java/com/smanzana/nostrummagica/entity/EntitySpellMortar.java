package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spell.component.shapes.MortarShape.MortarShapeInstance;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntitySpellMortar extends EntitySpellProjectile {
	
	public static final String ID = "spell_mortar";
	
	private double gravity;
	
	public EntitySpellMortar(EntityType<? extends EntitySpellMortar> type, World world) {
		super(type, world);
	}
	
	public EntitySpellMortar(EntityType<? extends EntitySpellMortar> type, MortarShapeInstance trigger, LivingEntity shooter,
			World world, Vector3d start, Vector3d velocity,
			float speedFactor, double gravity) {
		super(type, trigger, world, shooter, start, velocity, speedFactor, 1000);
		this.accelerationX = 0; // Force custom acceleration
		this.accelerationY = 0;
		this.accelerationZ = 0;
		this.setMotion(velocity); // Force our own velocity
		
		this.gravity = gravity;
	}
	
	@Override
	protected void registerData() {
		super.registerData();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!world.isRemote) {
			if (origin == null) {
				// We got loaded...
				this.remove();
				return;
			}
			
			// Gravity!
			this.setMotion(this.getMotion().add(0, -gravity, 0));
		} 
//		else {
//			int color = getElement().getColor();
//			color = (0x19000000) | (color & 0x00FFFFFF);
//			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//					2,
//					getPosX(), getPosY() + getHeight()/2f, getPosZ(), 0, 40, 0,
//					new Vector3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
//				).color(color));
//		}
	}
	
	@Override
	protected float getMotionFactor() {
		return 1f; // no friction
	}

	@Override
	protected void onImpact(RayTraceResult result) {
//		if (world.isRemote)
//			return;
//		
//		if (result.getType() == RayTraceResult.Type.MISS) {
//			; // Do nothing
//		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
//			BlockPos pos = RayTrace.blockPosFromResult(result);
//			trigger.onProjectileHit(pos);
//			
//			// Proc mystic anchors if we hit one
//			if (world.isAirBlock(pos)) pos = pos.down();
//			BlockState state = world.getBlockState(pos);
//			if (state.getBlock() instanceof MysticAnchor) {
//				state.onEntityCollision(world, pos, this);
//			}
//			
//			this.remove();
//		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
//			final Entity entityHit = RayTrace.entFromRaytrace(result);
//			if (filter == null || filter.apply(entityHit)) {
//				if ((entityHit != this.func_234616_v_() && !this.func_234616_v_().isRidingOrBeingRiddenBy(entityHit))
//						|| this.ticksExisted > 20) {
//					trigger.onProjectileHit(entityHit);
//					this.remove();
//				}
//			}
//		}
		super.onImpact(result);
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	@Override
	protected boolean isFireballFiery() {
		return false;
	}
	
	@Override
	protected IParticleData getParticle() {
		return ParticleTypes.WITCH;
	}
}
