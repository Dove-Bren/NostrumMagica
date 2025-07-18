package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SpellBubbleEntity extends SpellProjectileEntity {
	
	public static final String ID = "spell_bubble";
	
	protected float drag;
	protected int lifetime;
	
	public SpellBubbleEntity(EntityType<? extends SpellBubbleEntity> type, Level world) {
		super(type, world);
		drag = 1f;
		this.lifetime = 20;
	}
	
	protected SpellBubbleEntity(EntityType<? extends SpellBubbleEntity> type,
			ISpellProjectileShape trigger, Level world, LivingEntity shooter,
			Vec3 origin, Vec3 direction,
			float speedFactor, float dragFactor, int lifetime) {
		super(type, trigger, world, shooter, origin, direction, speedFactor, 50);
		this.drag = dragFactor;
		this.lifetime = lifetime;
	}
	
	protected SpellBubbleEntity(EntityType<? extends SpellBubbleEntity> type, ISpellProjectileShape trigger,
			LivingEntity shooter, float speedFactor, float dragFactor, int lifetime) {
		this(type,
				trigger,
				shooter.level,
				shooter,
				shooter.position(),
				shooter.getLookAngle(),
				speedFactor, dragFactor, lifetime
				);
	}
	
	public SpellBubbleEntity(ISpellProjectileShape trigger,	LivingEntity shooter, float speedFactor, float dragFactor, int lifetime) {
		this(NostrumEntityTypes.spellBubble, trigger, shooter, speedFactor, dragFactor, lifetime);
	}

	public SpellBubbleEntity(ISpellProjectileShape trigger,	LivingEntity shooter, Vec3 origin, Vec3 direction, float speedFactor, float dragFactor, int lifetime) {
		this(NostrumEntityTypes.spellBubble, trigger, shooter.level, shooter, origin, direction, speedFactor, dragFactor, lifetime);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	protected boolean dieOnImpact(BlockPos pos) {
		return true;
	}

	@Override
	protected boolean dieOnImpact(Entity entity) {
		return true;
	}

	@Override
	public boolean canImpact(BlockPos pos) {
		return true;
	}

	@Override
	public boolean canImpact(Entity entity) {
		return true;
	}

	@Override
	protected void doImpact(Entity entity) {
		trigger.onProjectileHit(entity);
	}
	
	@Override
	protected void doImpact(SpellLocation location) {
		trigger.onProjectileHit(location);
	}
	
	@Override
	protected void spawnTrailParticle() {
		// no trail
	}
	
	@Override
	public void tick() {
		if (!level.isClientSide()) {
			// Apply drag if still moving
			if (this.xPower != 0 || this.yPower != 0 || this.zPower != 0) {
				final float dragMod = 1f - (.2f * this.drag);
				final double precis = .0000025;
				this.xPower *= dragMod;
				this.yPower *= dragMod;
				this.zPower *= dragMod;
				if (xPower != 0 && Math.abs(xPower) < precis) {
					xPower = 0;
				}
				if (yPower != 0 && Math.abs(yPower) < precis) {
					yPower = 0;
				}
				if (zPower != 0 && Math.abs(zPower) < precis) {
					zPower = 0;
				}
			}
		}
		
		super.tick();
		
		if (!level.isClientSide() && this.xPower == 0 && this.yPower == 0 && this.zPower == 0) {
			// Done moving, so bob around a bit
			final int idx = this.uuid.hashCode() + this.tickCount;
			final int period = 3 * 20;
			final float prog = (float) (idx % period) / (float) period;
			final float floatAccel = (float) Math.sin(Math.PI * 2 * prog);
			this.setDeltaMovement(0, floatAccel * .01, 0);
			
			// DamagingProjectileEntity's collision detection is based on motion and doesn't work at these low speeds,
			// so do our own checking
			for (Entity e : this.level.getEntities(this, this.getBoundingBox(), this::canImpact)) {
				this.onHit(new EntityHitResult(e, this.position()));
			}
		}
		
		if (!level.isClientSide() && this.tickCount >= lifetime) {
			this.onProjectileDeath();
			this.discard();
		}
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}
	
	@Override
	protected ParticleOptions getTrailParticle() {
		return new NostrumParticleData(NostrumParticles.WARD.getType(), new SpawnParams(1, 0, 0, 0, 0, 1, 0, new TargetLocation(Vec3.ZERO)));
	}
	
	@Override
	protected void doClientEffect() {
		if (this.random.nextBoolean()) {
			super.doClientEffect();
		}
	}
	
	@Override
	protected void onProjectileDeath() {
		((ServerLevel) level).sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY(), this.getZ(), 3, 0, 0, 0, 0);
		NostrumMagicaSounds.BUBBLE_POP.play(this);
	}
}
