package com.smanzana.nostrummagica.entity;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Spell projectile that by default doesn't die on impact and instead keeps track of who it's
 * impacted already to avoid duplicates
 * @author Skyler
 *
 */
public abstract class SpellSaucerEntity extends SpellProjectileEntity {
	
	protected int hitCooldown;
	
	private final Map<Entity, Integer> hitEntities;
	private final Map<BlockPos, Integer> hitBlocks;
	
	protected SpellSaucerEntity(EntityType<? extends SpellSaucerEntity> type, Level world) {
		super(type, world);
		this.hitEntities = new HashMap<>();
		this.hitBlocks = new HashMap<>();
	}
	
	protected SpellSaucerEntity(EntityType<? extends SpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance, int hitCooldown) {
		super(type, trigger, shooter, speed, maxDistance);
		this.hitEntities = new HashMap<>();
		this.hitBlocks = new HashMap<>();
		this.hitCooldown = hitCooldown;
	}
	
	protected SpellSaucerEntity(EntityType<? extends SpellSaucerEntity> type, ISpellProjectileShape trigger,
			Level world, LivingEntity shooter,
			Vec3 origin, Vec3 direction,
			float speedFactor, double maxDistance, int hitCooldown) {
		super(type, trigger, world, shooter, origin, direction, speedFactor, maxDistance);
		this.hitEntities = new HashMap<>();
		this.hitBlocks = new HashMap<>();
		this.hitCooldown = hitCooldown;
	}
	
	protected SpellSaucerEntity(EntityType<? extends SpellSaucerEntity> type, ISpellProjectileShape trigger, Level world, LivingEntity shooter, float speed, double maxDistance) {
		this(type, trigger, world, shooter, speed, maxDistance, -1);
	}
	
	@Override
	public void tick() {
		super.tick();
		
//		if (this.ticksExisted % 5 == 0 && world.isRemote) {
//			this.world.addParticle(ParticleTypes.CRIT,
//					getPosX() - .5 + rand.nextFloat(), getPosY(), getPosZ() - .5 + rand.nextFloat(), 0, 0, 0);
//		}
	}
	
	protected void addHit(Entity entity) {
		this.hitEntities.put(entity, this.tickCount);
	}
	
	// -1 cooldown means EVER
	protected boolean hasBeenHit(Entity entity, int cooldown) {
		Integer hitTickCount = hitEntities.get(entity);
		return hitTickCount != null &&
				(cooldown == -1 || this.tickCount - hitTickCount < cooldown);
	}
	
	protected void addHit(BlockPos pos) {
		this.hitBlocks.put(pos.immutable(), this.tickCount);
	}
	
	// -1 cooldown means EVER
	protected boolean hasBeenHit(BlockPos pos, int cooldown) {
		Integer hitTickCount = hitBlocks.get(pos);
		return hitTickCount != null &&
				(cooldown == -1 || this.tickCount - hitTickCount < cooldown);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
	}
	
	@Override
	public boolean canImpact(BlockPos pos) {
		return !hasBeenHit(pos, hitCooldown);
	}
	
	@Override
	public boolean canImpact(Entity entity) {
		return super.canImpact(entity) && !hasBeenHit(entity, hitCooldown);
	}
	
	@Override
	protected void doImpact(SpellLocation location) {
		super.doImpact(location);
		this.addHit(location.selectedBlockPos);
	}
	
	@Override
	protected void doImpact(Entity entity) {
		super.doImpact(entity);
		this.addHit(entity);
	}
	
	@Override
	public boolean dieOnImpact(BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean dieOnImpact(Entity entity) {
		return false;
	}
	
//	protected void shoot(double xStart, double yStart, double zStart, double xTo, double yTo, double zTo, float velocity, float inaccuracy) {
//		this.ticksExisted = 0;
//	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity, inaccuracy);
		//this.shoot(getPosX(), getPosY(), getPosZ(), x, y, z, velocity, inaccuracy);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		//this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		return false; // This makes us not save and persist!!
	}

//	@Override
//	public void readAdditional(CompoundNBT compound) {
//		this.speed = compound.getFloat("speed");
//		if (compound.hasUniqueId("shooterID")) {
//			UUID uuid = compound.getUniqueId("shooterID");
//			this.shootingEntity = Entities.FindLiving(world, uuid);
//		} else {
//			this.shootingEntity = null;
//		}
//	}
//
//	@Override
//	public void writeAdditional(CompoundNBT compound) {
//		compound.putFloat("speed", this.speed);
//		compound.putUniqueId("shooterID", shootingEntity.getUniqueID());
//	}
	
	@Override
	public boolean isPickable() {
		return true;
	}
	
	@Override
	public float getPickRadius() {
		return 1f;
	}
}
