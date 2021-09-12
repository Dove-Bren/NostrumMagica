package com.smanzana.nostrummagica.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntitySpellSaucer extends Entity implements IProjectile {
	
	public static interface ISpellSaucerTrigger {

		public void onProjectileHit(BlockPos pos);
		
		public void onProjectileHit(Entity entity);
		
	}
	
	protected EntityLivingBase shootingEntity;
	protected ISpellSaucerTrigger trigger;
	
	protected float speed;
	protected int ticksInAir;
	
	// TODO support not hitting blocks
	private Set<EntityLivingBase> hitEntities;
	private Set<Vector> hitBlocks;
	
	protected EntitySpellSaucer(World world) {
		super(world);
		this.setSize(1F, .2F);
		this.hitEntities = new HashSet<>();
		this.hitBlocks = new HashSet<>();
	}
	
	public EntitySpellSaucer(World world, EntityLivingBase shooter, ISpellSaucerTrigger trigger, float speed) {
		this(world);
        this.speed = speed;
        this.shootingEntity = shooter;
        this.trigger = trigger;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance <= 64 * 64 * 64;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		ticksInAir++;
		ticksExisted++;
		
		if (this.ticksExisted % 5 == 0 && world.isRemote) {
			this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
					posX - .5 + rand.nextFloat(), posY, posZ - .5 + rand.nextFloat(), 0, 0, 0);
		}
	}
	
	protected void addHit(EntityLivingBase entity) {
		this.hitEntities.add(entity);
	}
	
	protected boolean hasBeenHit(EntityLivingBase entity) {
		return this.hitEntities.contains(entity);
	}
	
	protected void addHit(Vector pos) {
		this.hitBlocks.add(pos.copy());
	}
	
	protected boolean hasBeenHit(Vector pos) {
		return this.hitBlocks.contains(pos);
	}

	protected void onImpact(RayTraceResult result) {
		if (world.isRemote)
			return;
		
		if (result.typeOfHit == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = result.getBlockPos();
			boolean dieOnImpact = this.dieOnImpact(pos);
			boolean canImpact = this.canImpact(pos);
			Vector vec = new Vector().set((int) result.hitVec.x, (int) result.hitVec.y, (int) result.hitVec.z);
			if (canImpact && (dieOnImpact || !this.hasBeenHit(vec))) {
				trigger.onProjectileHit(new BlockPos(result.hitVec));
				
				if (dieOnImpact) {
					this.setDead();
				} else {
					this.addHit(vec);
				}
			}
		} else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
			if (result.entityHit instanceof EntitySpellSaucer || !(result.entityHit instanceof EntityLivingBase)) {
				
			} else if (result.entityHit != shootingEntity && !shootingEntity.isRidingOrBeingRiddenBy(result.entityHit)) {
				EntityLivingBase living = (EntityLivingBase) result.entityHit;
				boolean dieOnImpact = this.dieOnImpact(living);
				boolean canImpact = this.canImpact(living);
				if (canImpact && (dieOnImpact || !this.hasBeenHit(living))) {
					trigger.onProjectileHit(result.entityHit);
					
					if (dieOnImpact) {
						this.setDead();
					} else {
						this.addHit(living);
					}
				}
			}
		}
	}
	
	protected void shoot(double xStart, double yStart, double zStart, double xTo, double yTo, double zTo, float velocity, float inaccuracy) {
		this.ticksExisted = 0;
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		this.shoot(posX, posY, posZ, x, y, z, velocity, inaccuracy);
	}

	@Override
	protected void entityInit() {
		
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return false; // This makes us not save and persist!!
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.speed = compound.getFloat("speed");
		UUID uuid = compound.getUniqueId("shooterID");
		this.shootingEntity = (EntityLivingBase) world.loadedEntityList.stream().filter((ent) -> { return ent.getUniqueID().equals(uuid);}).findFirst().orElse(null);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setFloat("speed", this.speed);
		compound.setUniqueId("shooterID", shootingEntity.getUniqueID());
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	@Override
	public float getCollisionBorderSize() {
		return 1f;
	}
	
	public boolean dieOnImpact(BlockPos pos) {
		return true;
	}
	
	public boolean dieOnImpact(EntityLivingBase entity) {
		return true;
	}
	
	public boolean canImpact(BlockPos pos) {
		return true;
	}
	
	public boolean canImpact(EntityLivingBase entity) {
		return true;
	}
	
	public static final class Vector {
		public double x;
		public double y;
		public double z;
		
		public Vector() {
			
		}
		
		public Vector set(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			
			return this;
		}
		
		public Vector set(Vec3d vec) {
			this.set(vec.x, vec.y, vec.z);
			
			return this;
		}
		
		public Vector subtract(Vec3d vec) {
			this.x -= vec.x;
			this.y -= vec.y;
			this.z -= vec.z;
			
			return this;
		}
		
		public Vector subtract(Vector vec) {
			this.x -= vec.x;
			this.y -= vec.y;
			this.z -= vec.z;
			
			return this;
		}
		
		public double getMagnitude() {
			double length = Math.sqrt(x*x + y*y);
			length = Math.sqrt(length*length + z*z);
			
			return length;
		}
		
		public Vector normalize() {
			double length = this.getMagnitude();
			this.x /= length;
			this.y /= length;
			this.z /= length;
			
			return this;
		}
		
		public Vector scale(double amount) {
			this.x *= amount;
			this.y *= amount;
			this.z *= amount;
			
			return this;
		}
		
		public Vector setMagnitude(double magnitude) {
			this.normalize();
			this.scale(magnitude);
			
			return this;
		}
		
		public Vector copy() {
			return new Vector().set(x, y, z);
		}
		
		@Override
		public boolean equals(Object other) {
			return other.hashCode() == this.hashCode();
		}
		
		@Override
		public int hashCode() {
			return (int) (Math.round(x * 1000)
					+ Math.round(y * 1000) * 13397
					+ Math.round(z * 1000) * 68329);
		}
	}
}
