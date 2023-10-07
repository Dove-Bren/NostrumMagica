package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger.ProjectileTriggerInstance;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpellProjectile extends FireballEntity {
	
	public static final String ID = "spell_projectile";
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntitySpellProjectile.class, MagicElementDataSerializer.instance);
	
	private ProjectileTriggerInstance trigger;
	private double maxDistance; // Squared distance so no sqrt
	private Vec3d origin;
	
	private @Nullable Predicate<Entity> filter;

	public EntitySpellProjectile(ProjectileTriggerInstance trigger,
			LivingEntity shooter, float speedFactor, double maxDistance) {
		this(trigger,
				shooter,
				shooter.world,
				shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ,
				shooter.getLookVec(),
				speedFactor, maxDistance
				);
	}
	
	public EntitySpellProjectile(EntityType<EntitySpellProjectile> type, World world) {
		super(type, world);
	}
	
	public EntitySpellProjectile(ProjectileTriggerInstance trigger, LivingEntity shooter,
			World world,
			double fromX, double fromY, double fromZ, Vec3d direction,
			float speedFactor, double maxDistance) {
		super(world, fromX, fromY, fromZ, 0, 0, 0);
		Vec3d accel = getAccel(direction, speedFactor);
		this.accelerationX = accel.x;
		this.accelerationY = accel.y;
		this.accelerationZ = accel.z;
		this.shootingEntity = shooter;
		
		this.trigger = trigger;
		this.maxDistance = Math.pow(maxDistance, 2);
		this.origin = new Vec3d(fromX, fromY, fromZ);
		
		this.setElement(trigger.getElement());
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	public void setFilter(@Nullable Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	private Vec3d getAccel(Vec3d direction, double scale) {
		Vec3d base = direction.normalize();
		final double tickScale = .05;
		
		return new Vec3d(base.x * scale * tickScale, base.y * scale * tickScale, base.z * scale * tickScale);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		// if client
//		if (this.ticksExisted % 5 == 0) {
//			this.world.addParticle(ParticleTypes.CRIT_MAGIC,
//					posX, posY, posZ, 0, 0, 0);
//		}
		
		if (!world.isRemote) {
			if (origin == null) {
				// We got loaded...
				this.remove();
				return;
			}
			// Can't avoid a SQR; tracking motion would require SQR, too to get path length
			if (this.getPositionVector().squareDistanceTo(origin) > maxDistance) {
				trigger.onFizzle(this.getPosition());
				this.remove();
			}
		} else {
			int color = getElement().getColor();
			color = (0x19000000) | (color & 0x00FFFFFF);
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					2,
					posX, posY + getHeight()/2f, posZ, 0, 40, 0,
					new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
				).color(color));
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (world.isRemote)
			return;
		
		if (result.getType() == RayTraceResult.Type.MISS) {
			; // Do nothing
		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
			trigger.onProjectileHit(new BlockPos(result.getHitVec()));
			this.remove();
		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entityHit = RayTrace.entFromRaytrace(result);
			if (filter == null || filter.apply(entityHit)) {
				if ((entityHit != shootingEntity && !shootingEntity.isRidingOrBeingRiddenBy(entityHit))
						|| this.ticksExisted > 20) {
					trigger.onProjectileHit(entityHit);
					this.remove();
				}
			}
		}
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
	
	public void setElement(EMagicElement element) {
		this.dataManager.set(ELEMENT, element);
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
}
