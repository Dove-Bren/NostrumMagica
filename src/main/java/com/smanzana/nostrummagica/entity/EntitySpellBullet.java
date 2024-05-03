package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger.SeekingBulletTriggerInstance;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

// Like shulker bullets but have spells in them
public class EntitySpellBullet extends ShulkerBulletEntity {
	
	public static final String ID = "spell_bullet";

	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntitySpellBullet.class, MagicElementDataSerializer.instance);
	
	private SeekingBulletTriggerInstance trigger;
	private @Nullable Predicate<Entity> filter;
	private LivingEntity target;
	private LivingEntity shooter;
	private double speed; // Vanilla shulkers use .15
	private IParticleData particle;
	
	private boolean blockyPath; // Shulker-style pathing? Else smooth curve style.
	
	// Mostly copied from vanilla for movement :(
	private Direction direction;
	private int steps;
	private double targetDeltaX;
	private double targetDeltaY;
	private double targetDeltaZ;

	public EntitySpellBullet(EntityType<? extends EntitySpellBullet> type, World world) {
		super(type, world);
		this.speed = .15;
		this.particle = ParticleTypes.CRIT;
		this.steps = 1;
	}
	
	public EntitySpellBullet(EntityType<? extends EntitySpellBullet> type, 
			SeekingBulletTriggerInstance self,
			LivingEntity shooter,
			LivingEntity target,
			Direction.Axis axis) {
		this(type, self, shooter, target, axis, .8, ParticleTypes.CRIT, false);
	}
	
	public EntitySpellBullet(EntityType<? extends EntitySpellBullet> type, 
			SeekingBulletTriggerInstance self,
			LivingEntity shooter,
			LivingEntity target,
			Direction.Axis axis,
			double speed,
			IParticleData particle,
			boolean blockyPath) {
		//super(shooter.world, shooter, target, axis);
		this(type, shooter.world);
		{ // copied out from super since it hardcodes type now
			//this.owner = ownerIn;
			this.setShooter(shooter);
			BlockPos blockpos = shooter.getPosition();
			double d0 = (double)blockpos.getX() + 0.5D;
			double d1 = (double)blockpos.getY() + 0.5D;
			double d2 = (double)blockpos.getZ() + 0.5D;
			this.setLocationAndAngles(d0, d1, d2, this.rotationYaw, this.rotationPitch);
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
			this.setLocationAndAngles(shooter.getPosX(), shooter.getPosY() + shooter.getEyeHeight(), shooter.getPosZ(), this.rotationYaw, this.rotationPitch);
		}
		
		this.setElement(self.getElement());
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	public void setElement(EMagicElement element) {
		this.dataManager.set(ELEMENT, element);
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
	
	@Override
	protected void onImpact(RayTraceResult result) {
		Entity entityHit = RayTrace.entFromRaytrace(result);
		if (entityHit == null) {
			//trigger.onProjectileHit(result.getBlockPos());
			return; // ?
		} else {
			trigger.onProjectileHit(entityHit);
		}
		this.remove();
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!this.world.isRemote) {
			trigger.onProjectileHit(this.getPosition());
		}
		this.remove();
		return true;
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
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
			targetPos = this.getPosition().down();
		}
		else
		{
			targetHeight = (double)this.target.getHeight() * 0.5D;
			targetPos = new BlockPos(this.target.getPosX(), this.target.getPosY(), this.target.getPosZ());
		}

		double targetX = (double)targetPos.getX() + 0.5D;
		double targetY = (double)targetPos.getY() + targetHeight;
		double targetZ = (double)targetPos.getZ() + 0.5D;
		Direction enumfacing = null;

		// Blocky movement looks for next block and tries to move to that one, using random if multiple spots still
		// move in the right direction.
		if (blockyPath && targetPos.distanceSq(this.getPosX(), this.getPosY(), this.getPosZ(), true) >= 4.0D) {
			BlockPos blockpos1 = this.getPosition();
			List<Direction> list = Lists.<Direction>newArrayList();

			if (currentAxis != Direction.Axis.X)
			{
				if (blockpos1.getX() < targetPos.getX() && this.world.isAirBlock(blockpos1.east()))
				{
					list.add(Direction.EAST);
				}
				else if (blockpos1.getX() > targetPos.getX() && this.world.isAirBlock(blockpos1.west()))
				{
					list.add(Direction.WEST);
				}
			}

			if (currentAxis != Direction.Axis.Y)
			{
				if (blockpos1.getY() < targetPos.getY() && this.world.isAirBlock(blockpos1.up()))
				{
					list.add(Direction.UP);
				}
				else if (blockpos1.getY() > targetPos.getY() && this.world.isAirBlock(blockpos1.down()))
				{
					list.add(Direction.DOWN);
				}
			}

			if (currentAxis != Direction.Axis.Z)
			{
				if (blockpos1.getZ() < targetPos.getZ() && this.world.isAirBlock(blockpos1.south()))
				{
					list.add(Direction.SOUTH);
				}
				else if (blockpos1.getZ() > targetPos.getZ() && this.world.isAirBlock(blockpos1.north()))
				{
					list.add(Direction.NORTH);
				}
			}

			enumfacing = Direction.getRandomDirection(this.rand);

			if (list.isEmpty())
			{
				for (int i = 5; !this.world.isAirBlock(blockpos1.offset(enumfacing)) && i > 0; --i)
				{
					enumfacing = Direction.getRandomDirection(this.rand);
				}
			}
			else
			{
				enumfacing = (Direction)list.get(this.rand.nextInt(list.size()));
			}

			targetX = this.getPosX() + (double)enumfacing.getXOffset();
			targetY = this.getPosY() + (double)enumfacing.getYOffset();
			targetZ = this.getPosZ() + (double)enumfacing.getZOffset();
		}

		this.setDirection(enumfacing);
		double deltaX = targetX - this.getPosX();
		double deltaY = targetY - this.getPosY();
		double deltaZ = targetZ - this.getPosZ();
		double dist = (double)MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

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

		this.isAirBorne = true;
		this.steps = (this.blockyPath ? 10 + this.rand.nextInt(5) * 10 : 1);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void tick() {
		if (!this.world.isRemote && this.world.getDifficulty() == Difficulty.PEACEFUL) {
			this.remove();
		} else {
			//super.tick();
			this.baseTick();

			if (!this.world.isRemote) {
				double dist = Double.MAX_VALUE;
				if (this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && ((PlayerEntity)this.target).isSpectator()) {
					if (!this.hasNoGravity()) {
						this.setMotion(this.getMotion().add(0, -0.04, 0));
					}
				} else {
					dist = target.getPositionVec().add(0, target.getHeight() / 2, 0).distanceTo(this.getPositionVec());
					this.targetDeltaX = MathHelper.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
					this.targetDeltaY = MathHelper.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
					this.targetDeltaZ = MathHelper.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
					final double adj = (this.blockyPath ? .2 : (dist < 2 ? .3 : .05));
					final Vector3d oldMot = this.getMotion();
					this.setMotion(oldMot.add(
							(this.targetDeltaX - oldMot.x) * adj,
							(this.targetDeltaY - oldMot.y) * adj,
							(this.targetDeltaZ - oldMot.z) * adj
							));
				}
				
				if (dist < .5) {
					this.onImpact(new EntityRayTraceResult(target));
				} else {
					RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, (ent) -> ent != this.shooter);
	
					if (raytraceresult != null) {
						this.onImpact(raytraceresult);
					}
				}
			}

			this.setPosition(this.getPosX() + this.getMotion().x, this.getPosY() + this.getMotion().y, this.getPosZ() + this.getMotion().z);
			ProjectileHelper.rotateTowardsMovement(this, 0.5F);

			if (this.world.isRemote) {
				this.world.addParticle(particle, this.getPosX() - this.getMotion().x, this.getPosY() - this.getMotion().y + 0.15D, this.getPosZ() - this.getMotion().z, 0.0D, 0.0D, 0.0D);
			} else if (this.target != null && this.target.isAlive()) {
				if (this.steps > 0) {
					--this.steps;

					if (this.steps == 0) {
						this.selectNextMoveDirection(this.direction == null ? null : this.direction.getAxis());
					}
				}

				if (this.direction != null) {
					BlockPos blockpos = this.getPosition();
					Direction.Axis enumfacing$axis = this.direction.getAxis();

					if (this.world.isTopSolid(blockpos.offset(this.direction), this)) {
						this.selectNextMoveDirection(enumfacing$axis);
					} else {
						BlockPos blockpos1 = this.target.getPosition();

						if (enumfacing$axis == Direction.Axis.X && blockpos.getX() == blockpos1.getX() || enumfacing$axis == Direction.Axis.Z && blockpos.getZ() == blockpos1.getZ() || enumfacing$axis == Direction.Axis.Y && blockpos.getY() == blockpos1.getY()) {
							this.selectNextMoveDirection(enumfacing$axis);
						}
					}
				}
			}
		}
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
}
