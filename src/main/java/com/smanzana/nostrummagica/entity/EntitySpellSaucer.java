package com.smanzana.nostrummagica.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.MysticAnchor;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class EntitySpellSaucer extends Entity implements IProjectile {
	
	public static interface ISpellSaucerTrigger {

		public void onProjectileHit(BlockPos pos);
		
		public void onProjectileHit(Entity entity);
		
	}
	
	protected LivingEntity shootingEntity;
	protected ISpellSaucerTrigger trigger;
	
	protected float speed;
	protected int ticksInAir;
	
	// TODO support not hitting blocks
	private Set<LivingEntity> hitEntities;
	private Set<Vector> hitBlocks;
	
	protected EntitySpellSaucer(EntityType<? extends EntitySpellSaucer> type, World world) {
		super(type, world);
		this.hitEntities = new HashSet<>();
		this.hitBlocks = new HashSet<>();
	}
	
	public EntitySpellSaucer(EntityType<? extends EntitySpellSaucer> type, World world, LivingEntity shooter, ISpellSaucerTrigger trigger, float speed) {
		this(type, world);
        this.speed = speed;
        this.shootingEntity = shooter;
        this.trigger = trigger;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return distance <= 64 * 64 * 64;
	}
	
	@Override
	public void tick() {
		super.tick();
		ticksInAir++;
		ticksExisted++;
		
		if (this.ticksExisted % 5 == 0 && world.isRemote) {
			this.world.addParticle(ParticleTypes.CRIT,
					posX - .5 + rand.nextFloat(), posY, posZ - .5 + rand.nextFloat(), 0, 0, 0);
		}
	}
	
	protected void addHit(LivingEntity entity) {
		this.hitEntities.add(entity);
	}
	
	protected boolean hasBeenHit(LivingEntity entity) {
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
		
		if (result.getType() == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos pos = ((BlockRayTraceResult) result).getPos();
			boolean dieOnImpact = this.dieOnImpact(pos);
			boolean canImpact = this.canImpact(pos);
			Vector vec = new Vector().set((int) result.getHitVec().x, (int) result.getHitVec().y, (int) result.getHitVec().z);
			if (canImpact && (dieOnImpact || !this.hasBeenHit(vec))) {
				trigger.onProjectileHit(pos);
				
				// Proc mystic anchors if we hit one
				if (world.isAirBlock(pos)) pos = pos.down();
				BlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof MysticAnchor) {
					state.onEntityCollision(world, pos, this);
				}
				
				if (dieOnImpact) {
					this.remove();
				} else {
					this.addHit(vec);
				}
			}
		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entityHit = ((EntityRayTraceResult) result).getEntity();
			if (entityHit instanceof EntitySpellSaucer || null == NostrumMagica.resolveLivingEntity(entityHit)) {
				
			} else if (!entityHit.equals(shootingEntity) && !shootingEntity.isRidingOrBeingRiddenBy(entityHit)) {
				LivingEntity living = NostrumMagica.resolveLivingEntity(entityHit);
				boolean dieOnImpact = this.dieOnImpact(living);
				boolean canImpact = this.canImpact(living);
				if (canImpact && (dieOnImpact || !this.hasBeenHit(living))) {
					trigger.onProjectileHit(entityHit);
					
					if (dieOnImpact) {
						this.remove();
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
	protected void registerData() {
		
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		return false; // This makes us not save and persist!!
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		this.speed = compound.getFloat("speed");
		UUID uuid = compound.getUniqueId("shooterID");
		this.shootingEntity = Entities.FindLiving(world, uuid);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.putFloat("speed", this.speed);
		compound.putUniqueId("shooterID", shootingEntity.getUniqueID());
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
	
	public boolean dieOnImpact(LivingEntity entity) {
		return true;
	}
	
	public boolean canImpact(BlockPos pos) {
		return true;
	}
	
	public boolean canImpact(LivingEntity entity) {
		return true;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
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
		
		public Vector set(Vector3d vec) {
			this.set(vec.x, vec.y, vec.z);
			
			return this;
		}
		
		public Vector subtract(Vector3d vec) {
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
	
	public @Nullable LivingEntity getShooter() {
		return this.shootingEntity;
	}
}
