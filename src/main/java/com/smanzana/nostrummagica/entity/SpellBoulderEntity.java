package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SpellBoulderEntity extends SpellSaucerEntity {
	
	public static final String ID = "spell_boulder";
	
	protected boolean bounces;
	
	protected int bounceCount;
	
	public SpellBoulderEntity(EntityType<? extends SpellBoulderEntity> type, Level world) {
		super(type, world);
		this.setNoGravity(false);
	}
	
	protected SpellBoulderEntity(EntityType<? extends SpellBoulderEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance) {
		super(type, trigger, world, shooter, speed, maxDistance);
		this.setNoGravity(false);
	}
	
	protected SpellBoulderEntity(EntityType<? extends SpellBoulderEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 from, Vec3 direction,
			float speedFactor, boolean bounces) {
		super(type, trigger, shooter.level, shooter, from, direction, speedFactor, 100, 20);
		this.setNoGravity(false);
		this.bounces = bounces;
	}
	
	public SpellBoulderEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, boolean bounces) {
		super(NostrumEntityTypes.boulder, trigger, world, shooter, speed, 100, 20);
		this.setNoGravity(false);
		this.bounces = bounces;
	}
	
	public SpellBoulderEntity(ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 from, Vec3 direction,
			float speedFactor, boolean bounces) {
		super(NostrumEntityTypes.boulder, trigger, shooter.level, shooter, from, direction, speedFactor, 100, 20);
		this.setNoGravity(false);
		this.bounces = bounces;
	}

	@Override
	public void tick() {
		// gravity, which isn't in this class?
		if (!this.isNoGravity()) {
//			// Copied from AbstractArrow, although magnitude is increased
//			Vec3 vec34 = this.getDeltaMovement();
//			this.setDeltaMovement(vec34.x, vec34.y - (double)0.06F, vec34.z);
			this.yPower -= .01f;
		}
		
		super.tick();
		
		if (!level.isClientSide) {
			
			if (origin == null) {
				// We got loaded...
				this.discard();
				return;
			}
			
			// boulder is larger than most projectiles, so default projectile hit detection often misses entities (it checks with a range of .3 blocks...)
			// so redo hit detection here
			{
				for(Entity ent : level.getEntities(this.shootingEntity, this.getBoundingBox(), this::canHitEntity)) {
					this.onHit(new EntityHitResult(ent));
				}
			}
		} else {
			this.updateRotation();
		}
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean dieOnImpact(BlockPos pos) {
		return false;
	}
	
	@Override
	protected void doImpact(SpellLocation location) {
		// always bounce, and then maybe apply
		bounceAgainst(location);
		
		if (!this.hasBeenHit(location.selectedBlockPos, this.hitCooldown)) {
			super.doImpact(location);
		}
		
		if (this.bounceCount++ >= 5) {
			this.onProjectileDeath();
			this.discard();
			return;
		}
	}
	
	@Override
	public boolean canImpact(Entity entity) {
		return super.canImpact(entity);
	}
	
	@Override
	public boolean dieOnImpact(Entity entity) {
		return false;
	}
	
	@Override
	protected void doImpact(Entity entity) {
		super.doImpact(entity); // no bounce ever?
	}
	
	protected void bounceAgainst(SpellLocation location) {
		// Which axis did we hit?
		final Direction face = SpellLocation.InferFace(location);
		final Axis axis = face.getAxis();
		final Vec3 modVec;
		switch (axis) {
		case X:
			this.xPower = -xPower * .25;
			modVec = new Vec3(-1, 1, 1);
			break;
		case Y:
		default:
			this.yPower = -yPower * .125;
			modVec = new Vec3(1, -1, 1);
			break;
		case Z:
			this.zPower = -zPower * .25;
			modVec = new Vec3(1, 1, -1);
			break;
		}
		
		this.setDeltaMovement(this.getDeltaMovement().multiply(modVec).scale(.25));
		this.hasImpulse = true;
	}
}
