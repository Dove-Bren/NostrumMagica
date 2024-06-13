package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntitySpellBubble extends EntitySpellProjectile {
	
	public static final String ID = "spell_bubble";
	
	protected float drag;
	protected int lifetime;
	
	public EntitySpellBubble(EntityType<? extends EntitySpellBubble> type, World world) {
		super(type, world);
		drag = 1f;
		this.lifetime = 20;
	}
	
	protected EntitySpellBubble(EntityType<? extends EntitySpellBubble> type,
			ISpellProjectileShape trigger, World world, LivingEntity shooter,
			Vector3d origin, Vector3d direction,
			float speedFactor, float dragFactor, int lifetime) {
		super(type, trigger, world, shooter, origin, direction, speedFactor, 50);
		this.drag = dragFactor;
		this.lifetime = lifetime;
	}
	
	protected EntitySpellBubble(EntityType<? extends EntitySpellBubble> type, ISpellProjectileShape trigger,
			LivingEntity shooter, float speedFactor, float dragFactor, int lifetime) {
		this(type,
				trigger,
				shooter.world,
				shooter,
				shooter.getPositionVec(),
				shooter.getLookVec(),
				speedFactor, dragFactor, lifetime
				);
	}
	
	public EntitySpellBubble(ISpellProjectileShape trigger,	LivingEntity shooter, float speedFactor, float dragFactor, int lifetime) {
		this(NostrumEntityTypes.spellBubble, trigger, shooter, speedFactor, dragFactor, lifetime);
	}

	public EntitySpellBubble(ISpellProjectileShape trigger,	LivingEntity shooter, Vector3d origin, Vector3d direction, float speedFactor, float dragFactor, int lifetime) {
		this(NostrumEntityTypes.spellBubble, trigger, shooter.world, shooter, origin, direction, speedFactor, dragFactor, lifetime);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
	}
	
	protected boolean dieOnImpact(BlockPos pos) {
		return true;
	}
	
	protected boolean dieOnImpact(Entity entity) {
		return true;
	}
	
	public boolean canImpact(BlockPos pos) {
		return true;
	}
	
	public boolean canImpact(Entity entity) {
		return true;
	}
	
	protected void doImpact(Entity entity) {
		trigger.onProjectileHit(entity);
	}
	
	protected void doImpact(BlockPos pos) {
		trigger.onProjectileHit(pos);
	}
	
	@Override
	public void tick() {
		if (!world.isRemote()) {
			// Apply drag if still moving
			if (this.accelerationX != 0 || this.accelerationY != 0 || this.accelerationZ != 0) {
				final float dragMod = 1f - (.2f * this.drag);
				final double precis = .0000025;
				this.accelerationX *= dragMod;
				this.accelerationY *= dragMod;
				this.accelerationZ *= dragMod;
				if (accelerationX != 0 && Math.abs(accelerationX) < precis) {
					accelerationX = 0;
				}
				if (accelerationY != 0 && Math.abs(accelerationY) < precis) {
					accelerationY = 0;
				}
				if (accelerationZ != 0 && Math.abs(accelerationZ) < precis) {
					accelerationZ = 0;
				}
			}
		}
		
		super.tick();
		
		if (!world.isRemote() && this.accelerationX == 0 && this.accelerationY == 0 && this.accelerationZ == 0) {
			// Done moving, so bob around a bit
			final int idx = this.entityUniqueID.hashCode() + this.ticksExisted;
			final int period = 3 * 20;
			final float prog = (float) (idx % period) / (float) period;
			final float floatAccel = (float) Math.sin(Math.PI * 2 * prog);
			this.setMotion(0, floatAccel * .01, 0);
			
			// DamagingProjectileEntity's collision detection is based on motion and doesn't work at these low speeds,
			// so do our own checking
			for (Entity e : this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), this::canImpact)) {
				this.onImpact(new EntityRayTraceResult(e, this.getPositionVec()));
			}
		}
		
		if (!world.isRemote() && this.ticksExisted >= lifetime) {
			this.onProjectileDeath();
			this.remove();
		}
	}

	@Override
	protected boolean isFireballFiery() {
		return false;
	}
	
	@Override
	protected IParticleData getParticle() {
		return new NostrumParticleData(NostrumParticles.WARD.getType(), new SpawnParams(1, 0, 0, 0, 0, 1, 0, Vector3d.ZERO));
	}
	
	@Override
	protected void doClientEffect() {
		if (this.rand.nextBoolean()) {
			super.doClientEffect();
		}
	}
	
	@Override
	protected void onProjectileDeath() {
		((ServerWorld) world).spawnParticle(ParticleTypes.BUBBLE_POP, this.getPosX(), this.getPosY(), this.getPosZ(), 3, 0, 0, 0, 0);
		NostrumMagicaSounds.BUBBLE_POP.play(this);
	}
}
