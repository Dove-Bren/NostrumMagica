package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SeekingBulletShape.SeekingBulletShapeInstance;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

// Like shulker bullets but have spells in them
public class SpellBulletEntity extends ShulkerBullet {
	
	public static final String ID = "spell_bullet";

	protected static final EntityDataAccessor<EMagicElement> ELEMENT = SynchedEntityData.<EMagicElement>defineId(SpellBulletEntity.class, MagicElementDataSerializer.instance);
	
	private SeekingBulletShapeInstance trigger;
	private @Nullable Predicate<Entity> filter;
	private LivingEntity target;
	private LivingEntity shooter;
	private double speed; // Vanilla shulkers use .15
	private ParticleOptions particle;
	
	private boolean blockyPath; // Shulker-style pathing? Else smooth curve style.
	
	// Mostly copied from vanilla for movement :(
	private Direction direction;
	private int steps;
	private double targetDeltaX;
	private double targetDeltaY;
	private double targetDeltaZ;

	public SpellBulletEntity(EntityType<? extends SpellBulletEntity> type, Level world) {
		super(type, world);
		this.speed = .15;
		this.particle = ParticleTypes.CRIT;
		this.steps = 1;
	}
	
	public SpellBulletEntity(EntityType<? extends SpellBulletEntity> type, 
			SeekingBulletShapeInstance self,
			LivingEntity shooter,
			LivingEntity target,
			Direction.Axis axis) {
		this(type, self, shooter, target, axis, .8, ParticleTypes.CRIT, false);
	}
	
	public SpellBulletEntity(EntityType<? extends SpellBulletEntity> type, 
			SeekingBulletShapeInstance self,
			LivingEntity shooter,
			LivingEntity target,
			Direction.Axis axis,
			double speed,
			ParticleOptions particle,
			boolean blockyPath) {
		//super(shooter.world, shooter, target, axis);
		this(type, shooter.level);
		{ // copied out from super since it hardcodes type now
			//this.owner = ownerIn;
			this.setOwner(shooter);
			BlockPos blockpos = shooter.blockPosition();
			double d0 = (double)blockpos.getX() + 0.5D;
			double d1 = (double)blockpos.getY() + 0.5D;
			double d2 = (double)blockpos.getZ() + 0.5D;
			this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
			this.target = target;
			this.direction = Direction.UP;
			this.selectNextMoveDirection(axis);
		}
		this.steps = 1;
		this.trigger = self;
		this.shooter = shooter;
		this.target = target;
		this.speed = speed;
		this.particle = particle;
		this.blockyPath = blockyPath;
		
		// shulker shells move them to center of block. We want shooter pos + eye height
		if (shooter != null) {
			this.moveTo(shooter.getX(), shooter.getY() + shooter.getEyeHeight(), shooter.getZ(), this.getYRot(), this.getXRot());
		}
		
		this.setElement(self.getElement());
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	public void setElement(EMagicElement element) {
		this.entityData.set(ELEMENT, element);
	}
	
	public EMagicElement getElement() {
		return this.entityData.get(ELEMENT);
	}
	
	@Override
	protected void onHit(HitResult result) {
		Entity entityHit = RayTrace.entFromRaytrace(result);
		if (entityHit == null) {
			//trigger.onProjectileHit(result.getBlockPos());
			return; // ?
		} else {
			trigger.onProjectileHit(entityHit);
		}
		this.discard();
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!this.level.isClientSide) {
			trigger.onProjectileHit(this.blockPosition());
		}
		this.discard();
		return true;
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	private void setDirection(@Nullable Direction directionIn)
	{
		this.direction = directionIn;
	}

	// Sets up targetDelta[] to figure out ideal motion vector
	protected void selectNextMoveDirection(@Nullable Direction.Axis currentAxis) {
		double targetHeight = 0.5D;
		BlockPos targetPos;

		if (this.target == null)
		{
			targetPos = this.blockPosition().below();
		}
		else
		{
			targetHeight = (double)this.target.getBbHeight() * 0.5D;
			targetPos = new BlockPos(this.target.getX(), this.target.getY(), this.target.getZ());
		}

		double targetX = (double)targetPos.getX() + 0.5D;
		double targetY = (double)targetPos.getY() + targetHeight;
		double targetZ = (double)targetPos.getZ() + 0.5D;
		Direction enumfacing = null;

		// Blocky movement looks for next block and tries to move to that one, using random if multiple spots still
		// move in the right direction.
		if (!targetPos.closerToCenterThan(this.position(), 2)) {
			BlockPos blockpos1 = this.blockPosition();
			List<Direction> list = Lists.<Direction>newArrayList();

			if (currentAxis != Direction.Axis.X)
			{
				if (blockpos1.getX() < targetPos.getX() && this.level.isEmptyBlock(blockpos1.east()))
				{
					list.add(Direction.EAST);
				}
				else if (blockpos1.getX() > targetPos.getX() && this.level.isEmptyBlock(blockpos1.west()))
				{
					list.add(Direction.WEST);
				}
			}

			if (currentAxis != Direction.Axis.Y)
			{
				if (blockpos1.getY() < targetPos.getY() && this.level.isEmptyBlock(blockpos1.above()))
				{
					list.add(Direction.UP);
				}
				else if (blockpos1.getY() > targetPos.getY() && this.level.isEmptyBlock(blockpos1.below()))
				{
					list.add(Direction.DOWN);
				}
			}

			if (currentAxis != Direction.Axis.Z)
			{
				if (blockpos1.getZ() < targetPos.getZ() && this.level.isEmptyBlock(blockpos1.south()))
				{
					list.add(Direction.SOUTH);
				}
				else if (blockpos1.getZ() > targetPos.getZ() && this.level.isEmptyBlock(blockpos1.north()))
				{
					list.add(Direction.NORTH);
				}
			}

			enumfacing = Direction.getRandom(this.random);

			if (list.isEmpty())
			{
				for (int i = 5; !this.level.isEmptyBlock(blockpos1.relative(enumfacing)) && i > 0; --i)
				{
					enumfacing = Direction.getRandom(this.random);
				}
			}
			else
			{
				enumfacing = (Direction)list.get(this.random.nextInt(list.size()));
			}

			targetX = this.getX() + (double)enumfacing.getStepX();
			targetY = this.getY() + (double)enumfacing.getStepY();
			targetZ = this.getZ() + (double)enumfacing.getStepZ();
		}

		this.setDirection(enumfacing);
		double deltaX = targetX - this.getX();
		double deltaY = targetY - this.getY();
		double deltaZ = targetZ - this.getZ();
		double dist = (double)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

		if (dist == 0.0D)
		{
			this.targetDeltaX = 0.0D;
			this.targetDeltaY = 0.0D;
			this.targetDeltaZ = 0.0D;
		}
		else
		{
			this.targetDeltaX = deltaX / dist * speed;
			this.targetDeltaY = deltaY / dist * speed;
			this.targetDeltaZ = deltaZ / dist * speed;
		}

		this.hasImpulse = true;
		this.steps = (this.blockyPath ? 10 + this.random.nextInt(5) * 10 : 1);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void tick() {
		if (!this.level.isClientSide && this.level.getDifficulty() == Difficulty.PEACEFUL) {
			this.discard();
		} else {
			//super.tick();
			this.baseTick();

			if (!this.level.isClientSide) {
				double dist = Double.MAX_VALUE;
				if (this.target == null || !this.target.isAlive() || this.target instanceof Player && ((Player)this.target).isSpectator()) {
					if (!this.isNoGravity()) {
						this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
					}
				} else {
					dist = target.position().add(0, target.getBbHeight() / 2, 0).distanceTo(this.position());
					this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
					this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
					this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
					final double adj = (this.blockyPath ? .2 : (dist < 2 ? .3 : .05));
					final Vec3 oldMot = this.getDeltaMovement();
					this.setDeltaMovement(oldMot.add(
							(this.targetDeltaX - oldMot.x) * adj,
							(this.targetDeltaY - oldMot.y) * adj,
							(this.targetDeltaZ - oldMot.z) * adj
							));
				}
				
				if (dist < .5) {
					this.onHit(new EntityHitResult(target));
				} else {
					HitResult raytraceresult = ProjectileUtil.getHitResult(this, (ent) -> ent != this.shooter);
	
					if (raytraceresult != null) {
						this.onHit(raytraceresult);
					}
				}
			}

			this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
			ProjectileUtil.rotateTowardsMovement(this, 0.5F);

			if (this.level.isClientSide) {
				this.level.addParticle(particle, this.getX() - this.getDeltaMovement().x, this.getY() - this.getDeltaMovement().y + 0.15D, this.getZ() - this.getDeltaMovement().z, 0.0D, 0.0D, 0.0D);
			} else if (this.target != null && this.target.isAlive()) {
				if (this.steps > 0) {
					--this.steps;

					if (this.steps == 0) {
						this.selectNextMoveDirection(this.direction == null ? null : this.direction.getAxis());
					}
				}

				if (this.direction != null) {
					BlockPos blockpos = this.blockPosition();
					Direction.Axis enumfacing$axis = this.direction.getAxis();

					if (this.level.loadedAndEntityCanStandOn(blockpos.relative(this.direction), this)) {
						this.selectNextMoveDirection(enumfacing$axis);
					} else {
						BlockPos blockpos1 = this.target.blockPosition();

						if (enumfacing$axis == Direction.Axis.X && blockpos.getX() == blockpos1.getX() || enumfacing$axis == Direction.Axis.Z && blockpos.getZ() == blockpos1.getZ() || enumfacing$axis == Direction.Axis.Y && blockpos.getY() == blockpos1.getY()) {
							this.selectNextMoveDirection(enumfacing$axis);
						}
					}
				}
			}
		}
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
}
