package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.block.MysticAnchor;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.ProjectileShape.ProjectileShapeInstance;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntitySpellProjectile extends DamagingProjectileEntity {
	
	public static final String ID = "spell_projectile";
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntitySpellProjectile.class, MagicElementDataSerializer.instance);
	
	private ProjectileShapeInstance trigger;
	private double maxDistance; // Squared distance so no sqrt
	private Vector3d origin;
	
	private @Nullable Predicate<Entity> filter;

	public EntitySpellProjectile(ProjectileShapeInstance trigger,
			LivingEntity shooter, float speedFactor, double maxDistance) {
		this(trigger,
				shooter,
				shooter.world,
				shooter.getPosX(), shooter.getPosY() + shooter.getEyeHeight(), shooter.getPosZ(),
				shooter.getLookVec(),
				speedFactor, maxDistance
				);
	}
	
	public EntitySpellProjectile(EntityType<EntitySpellProjectile> type, World world) {
		super(type, world);
	}
	
	public EntitySpellProjectile(ProjectileShapeInstance trigger, LivingEntity shooter,
			World world,
			double fromX, double fromY, double fromZ, Vector3d direction,
			float speedFactor, double maxDistance) {
		super(NostrumEntityTypes.spellProjectile, fromX, fromY, fromZ, 0, 0, 0, world);
		Vector3d accel = getAccel(direction, speedFactor);
		this.accelerationX = accel.x;
		this.accelerationY = accel.y;
		this.accelerationZ = accel.z;
		this.setShooter(shooter);
		
		this.trigger = trigger;
		this.maxDistance = Math.pow(maxDistance, 2);
		this.origin = new Vector3d(fromX, fromY, fromZ);
		
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
	
	private Vector3d getAccel(Vector3d direction, double scale) {
		Vector3d base = direction.normalize();
		final double tickScale = .05;
		
		return new Vector3d(base.x * scale * tickScale, base.y * scale * tickScale, base.z * scale * tickScale);
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
			if (this.getPositionVec().squareDistanceTo(origin) > maxDistance) {
				trigger.onFizzle(this.getPosition());
				this.remove();
			}
		} else {
			int color = getElement().getColor();
			color = (0x19000000) | (color & 0x00FFFFFF);
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					2,
					getPosX(), getPosY() + getHeight()/2f, getPosZ(), 0, 40, 0,
					new Vector3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
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
			BlockPos pos = RayTrace.blockPosFromResult(result);
			trigger.onProjectileHit(pos);
			
			// Proc mystic anchors if we hit one
			if (world.isAirBlock(pos)) pos = pos.down();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof MysticAnchor) {
				state.onEntityCollision(world, pos, this);
			}
			
			this.remove();
		} else if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entityHit = RayTrace.entFromRaytrace(result);
			if (filter == null || filter.apply(entityHit)) {
				if ((entityHit != this.func_234616_v_() && !this.func_234616_v_().isRidingOrBeingRiddenBy(entityHit))
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

	@Override
	public IPacket<?> createSpawnPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	public @Nullable Entity getShooter() {
		return super.func_234616_v_();
	}
}
