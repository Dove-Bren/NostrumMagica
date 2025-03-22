package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spell.component.shapes.MortarShape.MortarShapeInstance;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpellMortarEntity extends SpellProjectileEntity {
	
	public static final String ID = "spell_mortar";
	
	private double gravity;
	
	public SpellMortarEntity(EntityType<? extends SpellMortarEntity> type, Level world) {
		super(type, world);
	}
	
	public SpellMortarEntity(EntityType<? extends SpellMortarEntity> type, MortarShapeInstance trigger, LivingEntity shooter,
			Level world, Vec3 start, Vec3 velocity,
			float speedFactor, double gravity) {
		super(type, trigger, world, shooter, start, velocity, speedFactor, 1000);
		this.xPower = 0; // Force custom acceleration
		this.yPower = 0;
		this.zPower = 0;
		this.setDeltaMovement(velocity); // Force our own velocity
		
		this.gravity = gravity;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (!level.isClientSide) {
			if (origin == null) {
				// We got loaded...
				this.discard();
				return;
			}
			
			// Gravity!
			this.setDeltaMovement(this.getDeltaMovement().add(0, -gravity, 0));
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
	protected float getInertia() {
		return 1f; // no friction
	}

	@Override
	protected void onHit(HitResult result) {
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
//			this.discard();
//		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
//			final Entity entityHit = RayTrace.entFromRaytrace(result);
//			if (filter == null || filter.apply(entityHit)) {
//				if ((entityHit != this.getOwner() && !this.getOwner().isRidingOrBeingRiddenBy(entityHit))
//						|| this.ticksExisted > 20) {
//					trigger.onProjectileHit(entityHit);
//					this.discard();
//				}
//			}
//		}
		super.onHit(result);
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	@Override
	protected boolean shouldBurn() {
		return false;
	}
	
	@Override
	protected ParticleOptions getTrailParticle() {
		return ParticleTypes.WITCH;
	}
}
