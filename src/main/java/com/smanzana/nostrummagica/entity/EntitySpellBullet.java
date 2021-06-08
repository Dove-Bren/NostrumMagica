package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger.SeekingBulletTriggerInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

// Like shulker bullets but have spells in them
public class EntitySpellBullet extends EntityShulkerBullet {

	private SeekingBulletTriggerInstance trigger;
	private @Nullable Predicate<Entity> filter;
	private EntityLivingBase target;
	private EntityLivingBase shooter;
	private double speed; // Vanilla shulkers use .15
	private EnumParticleTypes particle;
	
	private boolean blockyPath; // Shulker-style pathing? Else smooth curve style.
	
	// Mostly copied from vanilla for movement :(
	private EnumFacing direction;
	private int steps;
	private double targetDeltaX;
	private double targetDeltaY;
	private double targetDeltaZ;

	public EntitySpellBullet(World world) {
		super(world);
		this.speed = .15;
		this.particle = EnumParticleTypes.CRIT;
		this.steps = 1;
	}
	
	public EntitySpellBullet(SeekingBulletTriggerInstance self,
			EntityLivingBase shooter,
			EntityLivingBase target,
			EnumFacing.Axis axis) {
		this(self, shooter, target, axis, .8, EnumParticleTypes.CRIT, false);
	}
	
	public EntitySpellBullet(
			SeekingBulletTriggerInstance self,
			EntityLivingBase shooter,
			EntityLivingBase target,
			EnumFacing.Axis axis,
			double speed,
			EnumParticleTypes particle,
			boolean blockyPath) {
		super(shooter.worldObj, shooter, target, axis);
		this.steps = 1;
		this.trigger = self;
		this.shooter = shooter;
		this.target = target;
		this.speed = speed;
		this.particle = particle;
		this.blockyPath = blockyPath;
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	@Override
	protected void bulletHit(RayTraceResult result) {
		if (result.entityHit == null) {
			//trigger.onProjectileHit(result.getBlockPos());
			return; // ?
		} else {
			trigger.onProjectileHit(result.entityHit);
		}
		this.setDead();
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!this.worldObj.isRemote) {
			trigger.onProjectileHit(this.getPosition());
		}
		this.setDead();
		return true;
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	private void setDirection(@Nullable EnumFacing directionIn)
	{
		this.direction = directionIn;
	}

	// Sets up targetDelta[] to figure out ideal motion vector
	protected void selectNextMoveDirection(@Nullable EnumFacing.Axis currentAxis) {
		double targetHeight = 0.5D;
		BlockPos targetPos;

		if (this.target == null)
		{
			targetPos = (new BlockPos(this)).down();
		}
		else
		{
			targetHeight = (double)this.target.height * 0.5D;
			targetPos = new BlockPos(this.target.posX, this.target.posY, this.target.posZ);
		}

		double targetX = (double)targetPos.getX() + 0.5D;
		double targetY = (double)targetPos.getY() + targetHeight;
		double targetZ = (double)targetPos.getZ() + 0.5D;
		EnumFacing enumfacing = null;

		// Blocky movement looks for next block and tries to move to that one, using random if multiple spots still
		// move in the right direction.
		if (blockyPath && targetPos.distanceSqToCenter(this.posX, this.posY, this.posZ) >= 4.0D) {
			BlockPos blockpos1 = new BlockPos(this);
			List<EnumFacing> list = Lists.<EnumFacing>newArrayList();

			if (currentAxis != EnumFacing.Axis.X)
			{
				if (blockpos1.getX() < targetPos.getX() && this.worldObj.isAirBlock(blockpos1.east()))
				{
					list.add(EnumFacing.EAST);
				}
				else if (blockpos1.getX() > targetPos.getX() && this.worldObj.isAirBlock(blockpos1.west()))
				{
					list.add(EnumFacing.WEST);
				}
			}

			if (currentAxis != EnumFacing.Axis.Y)
			{
				if (blockpos1.getY() < targetPos.getY() && this.worldObj.isAirBlock(blockpos1.up()))
				{
					list.add(EnumFacing.UP);
				}
				else if (blockpos1.getY() > targetPos.getY() && this.worldObj.isAirBlock(blockpos1.down()))
				{
					list.add(EnumFacing.DOWN);
				}
			}

			if (currentAxis != EnumFacing.Axis.Z)
			{
				if (blockpos1.getZ() < targetPos.getZ() && this.worldObj.isAirBlock(blockpos1.south()))
				{
					list.add(EnumFacing.SOUTH);
				}
				else if (blockpos1.getZ() > targetPos.getZ() && this.worldObj.isAirBlock(blockpos1.north()))
				{
					list.add(EnumFacing.NORTH);
				}
			}

			enumfacing = EnumFacing.random(this.rand);

			if (list.isEmpty())
			{
				for (int i = 5; !this.worldObj.isAirBlock(blockpos1.offset(enumfacing)) && i > 0; --i)
				{
					enumfacing = EnumFacing.random(this.rand);
				}
			}
			else
			{
				enumfacing = (EnumFacing)list.get(this.rand.nextInt(list.size()));
			}

			targetX = this.posX + (double)enumfacing.getFrontOffsetX();
			targetY = this.posY + (double)enumfacing.getFrontOffsetY();
			targetZ = this.posZ + (double)enumfacing.getFrontOffsetZ();
		}

		this.setDirection(enumfacing);
		double deltaX = targetX - this.posX;
		double deltaY = targetY - this.posY;
		double deltaZ = targetZ - this.posZ;
		double dist = (double)MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

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
	public void onUpdate()
	{
		if (!this.worldObj.isRemote && this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL)
		{
			this.setDead();
		}
		else
		{
			//super.onUpdate();
			this.onEntityUpdate();

			if (!this.worldObj.isRemote)
			{
				double dist = Double.MAX_VALUE;
				if (this.target == null || !this.target.isEntityAlive() || this.target instanceof EntityPlayer && ((EntityPlayer)this.target).isSpectator())
				{
					if (!this.hasNoGravity())
					{
						this.motionY -= 0.04D;
					}
				}
				else
				{
					dist = target.getPositionVector().addVector(0, target.height / 2, 0).distanceTo(this.getPositionVector());
					this.targetDeltaX = MathHelper.clamp_double(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
					this.targetDeltaY = MathHelper.clamp_double(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
					this.targetDeltaZ = MathHelper.clamp_double(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
					final double adj = (this.blockyPath ? .2 : (dist < 2 ? .3 : .05));
					this.motionX += (this.targetDeltaX - this.motionX) * adj;
					this.motionY += (this.targetDeltaY - this.motionY) * adj;
					this.motionZ += (this.targetDeltaZ - this.motionZ) * adj;
				}
				
				if (dist < .5) {
					this.bulletHit(new RayTraceResult(target));
				} else {
					RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, false, this.shooter);
	
					if (raytraceresult != null) {
						this.bulletHit(raytraceresult);
					}
				}
			}

			this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			ProjectileHelper.rotateTowardsMovement(this, 0.5F);

			if (this.worldObj.isRemote)
			{
				this.worldObj.spawnParticle(particle, this.posX - this.motionX, this.posY - this.motionY + 0.15D, this.posZ - this.motionZ, 0.0D, 0.0D, 0.0D, new int[0]);
			}
			else if (this.target != null && !this.target.isDead)
			{
				if (this.steps > 0)
				{
					--this.steps;

					if (this.steps == 0)
					{
						this.selectNextMoveDirection(this.direction == null ? null : this.direction.getAxis());
					}
				}

				if (this.direction != null)
				{
					BlockPos blockpos = new BlockPos(this);
					EnumFacing.Axis enumfacing$axis = this.direction.getAxis();

					if (this.worldObj.isBlockNormalCube(blockpos.offset(this.direction), false))
					{
						this.selectNextMoveDirection(enumfacing$axis);
					}
					else
					{
						BlockPos blockpos1 = new BlockPos(this.target);

						if (enumfacing$axis == EnumFacing.Axis.X && blockpos.getX() == blockpos1.getX() || enumfacing$axis == EnumFacing.Axis.Z && blockpos.getZ() == blockpos1.getZ() || enumfacing$axis == EnumFacing.Axis.Y && blockpos.getY() == blockpos1.getY())
						{
							this.selectNextMoveDirection(enumfacing$axis);
						}
					}
				}
			}
		}
	}
	
}