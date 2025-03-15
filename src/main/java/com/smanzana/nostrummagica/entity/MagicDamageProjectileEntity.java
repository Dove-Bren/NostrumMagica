package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.serializer.MagicElementDataSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellDamage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * A projectile that does magic damage when it impacts an entity.
 * Not a full blown SpellProjectile.
 * @author Skyler
 *
 */
public class MagicDamageProjectileEntity extends DamagingProjectileEntity {
	
	public static final String ID = "magic_projectile";
	
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>defineId(MagicDamageProjectileEntity.class, MagicElementDataSerializer.instance);
	protected float damage;

	public MagicDamageProjectileEntity(EntityType<? extends DamagingProjectileEntity> type, World world) {
		super(type, world);
		damage = 2f;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(ELEMENT, EMagicElement.PHYSICAL);
	}

	public EMagicElement getElement() {
		return entityData.get(ELEMENT);
	}

	public void setElement(EMagicElement element) {
		this.entityData.set(ELEMENT, element);
	}

	public float getDamage() {
		return damage;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}
	
	@Override
	public boolean saveAsPassenger(CompoundNBT compound) {
		// Returning false means we won't be saved. That's what we want.
		return false;
    }
	
	@Override
	protected boolean shouldBurn() {
		return false;
	}
	
	@Override
	protected IParticleData getTrailParticle() {
		return new NostrumParticleData(NostrumParticles.WARD.getType(), new SpawnParams(1, 0, 0, 0, 0, 1, 0, Vector3d.ZERO));
	}
	
	@Override
	public IPacket<?> getAddEntityPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	public @Nullable Entity getShooter() {
		return super.getOwner();
	}
	
	protected void doClientEffect() {
		int color = getElement().getColor();
		color = (0x19000000) | (color & 0x00FFFFFF);
		NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
				2,
				getX(), getY() + getBbHeight()/2f, getZ(), 0, 40, 0,
				new Vector3d(random.nextFloat() * .05 - .025, random.nextFloat() * .05, random.nextFloat() * .05 - .025), null
			).color(color));
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (level.isClientSide()) {
			doClientEffect();
		}
	}
	
	@Override
	protected boolean canHitEntity(Entity entity) {
		return this.canImpact(entity);
	}
	
	public boolean canImpact(Entity entity) {
		return this.getShooter() == null || ((!entity.equals(getShooter()) && !getShooter().isPassengerOfSameVehicle(entity)));
	}
	
	@Override
	protected void onHitEntity(EntityRayTraceResult result) {
		if (!level.isClientSide()) {
			Entity entityHit = result.getEntity();
			boolean canImpact = this.canImpact(entityHit);
			if (canImpact && entityHit instanceof LivingEntity) {
				this.damageEntity((LivingEntity) entityHit);
				this.remove();
			}
		}
	}
	
	@Override
	protected void onHitBlock(BlockRayTraceResult result) {
		if (!level.isClientSide()) {
			NostrumMagicaSounds.CAST_FAIL.play(this);
			this.remove();
		}
	}
	
	protected void damageEntity(LivingEntity entity) {
		@Nullable LivingEntity shooter = (this.getShooter() != null && this.getShooter() instanceof LivingEntity) ? (LivingEntity) this.getShooter() : null;
		SpellDamage.DamageEntity(entity, getElement(), damage, shooter);
		
		NostrumMagicaSounds sound;
		switch (getElement()) {
		case EARTH:
			sound = NostrumMagicaSounds.DAMAGE_EARTH;
			break;
		case ENDER:
			sound = NostrumMagicaSounds.DAMAGE_ENDER;
			break;
		case FIRE:
			sound = NostrumMagicaSounds.DAMAGE_FIRE;
			break;
		case ICE:
			sound = NostrumMagicaSounds.DAMAGE_ICE;
			break;
		case LIGHTNING:
			sound = NostrumMagicaSounds.DAMAGE_LIGHTNING;
			break;
		case PHYSICAL:
		default:
			sound = NostrumMagicaSounds.DAMAGE_PHYSICAL;
			break;
		case WIND:
			sound = NostrumMagicaSounds.DAMAGE_WIND;
			break;
		
		}
		
		sound.play(entity);
	}

}
