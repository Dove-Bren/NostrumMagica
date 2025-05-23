package com.smanzana.nostrummagica.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.serializer.OptionalParticleDataSerializer;
import com.smanzana.nostrummagica.util.ParticleHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * More generic version of EntityAreaEffectCloud
 * @author Skyler
 *
 */
public class AreaEffectEntity extends AreaEffectCloud {
	
	public static interface IAreaEntityEffect {
		public void apply(Level world, Entity ent);
	}
	
	public static interface IAreaLocationEffect {
		public void apply(Level world, BlockPos pos);
	}
	
	public static interface IAreaVFX {
		public void apply(Level world, int ticksExisted, AreaEffectEntity cloud);
	}
	
	public static final String ID = "entity_effect_cloud";
	
	private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.<Float>defineId(AreaEffectEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Optional<ParticleOptions>> EXTRA_PARTICLE = SynchedEntityData.<Optional<ParticleOptions>>defineId(AreaEffectEntity.class, OptionalParticleDataSerializer.instance);
	private static final EntityDataAccessor<Float> EXTRA_PARTICLE_OFFSET_Y = SynchedEntityData.<Float>defineId(AreaEffectEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> EXTRA_PARTICLE_FREQUENCY = SynchedEntityData.<Float>defineId(AreaEffectEntity.class, EntityDataSerializers.FLOAT);
	
	protected final List<IAreaEntityEffect> entityEffects;
	protected final List<IAreaLocationEffect> locationEffects;
	protected final Map<Entity, Integer> effectDelays;
	
	// Same as parent's reapplicationDelay
	protected int effectDelay;
	protected int waitTime = 20; // Copy of parent's, captured for time checks
	
	protected boolean verticalSteps;
	protected boolean gravity;
	protected double gravitySpeed;
	protected float radiusPerFall;
	protected boolean walksOnLiquid;
	protected boolean ignoreOwner;
	
	//private float prevHeight;
	private Vec3 waddleDir;
	private double waddleMagnitude;
	
	protected final List<IAreaVFX> manualVFX;

	public AreaEffectEntity(EntityType<? extends AreaEffectEntity> type, Level worldIn) {
		super(type, worldIn);
		entityEffects = new LinkedList<>();
		locationEffects = new LinkedList<>();
		effectDelays = new HashMap<>();
		effectDelay = 20;
		verticalSteps = false;
		gravity = false;
		gravitySpeed = 0;
		manualVFX = new LinkedList<>();
	}
	
	public AreaEffectEntity(EntityType<? extends AreaEffectEntity> type, Level worldIn, double x, double y, double z) {
		super(type, worldIn);
		this.setPos(x, y, z);
		entityEffects = new LinkedList<>();
		locationEffects = new LinkedList<>();
		effectDelays = new HashMap<>();
		effectDelay = 20;
		manualVFX = new LinkedList<>();
	}
	
	public int getEffectDelay() {
		return effectDelay;
	}
	
	/**
	 * Set how often to apply any effects.
	 * This is the number of ticks before effects can be applied again to the same entity.
	 * Note this does not include potion effects, which is hardcoded to 20 >:(
	 * @param delay
	 */
	public void setEffectDelay(int delay) {
		effectDelay = delay;
	}
	
	public boolean doesVerticalSteps() {
		return verticalSteps;
	}
	
	/**
	 * If true, will automatically raise or lower the cloud if movement has made it go into a solid block (or off a cliff!)
	 * @param shouldStep
	 */
	public void setVerticleStepping(boolean shouldStep) {
		this.verticalSteps = shouldStep;
	}
	
	public boolean hasGravity() {
		return gravity;
	}
	
	public double getGravitySpeed() {
		return gravitySpeed;
	}
	
	public void setGravity(boolean hasGravity, double gravitySpeed) {
		this.gravity = hasGravity;
		this.gravitySpeed = gravitySpeed;
	}
	
	public void addEffect(IAreaEntityEffect effect) {
		this.entityEffects.add(effect);
	}
	
	public void addEffect(IAreaLocationEffect effect) {
		this.locationEffects.add(effect);
	}
	
	public void setWaddle(Vec3 direction, double waddle) {
		this.waddleDir = direction;
		this.waddleMagnitude = waddle;
	}
	
	public void setRadiusPerFall(float radiusPerBlock) {
		this.radiusPerFall = radiusPerBlock;
	}
	
	public void setWalksWater() {
		this.walksOnLiquid = true;
	}
	
	public boolean getWalksWater() {
		return this.walksOnLiquid;
	}
	
	public void setIgnoreOwner(boolean ignore) {
		this.ignoreOwner = ignore;
	}
	
	public boolean getIgnoreOwner() {
		return this.ignoreOwner;
	}
	
	public @Nullable ParticleOptions getCustomParticle() {
		return this.getEntityData().get(EXTRA_PARTICLE).orElse(null);
	}

	public void setCustomParticle(@Nullable ParticleOptions particleIn) {
		this.getEntityData().set(EXTRA_PARTICLE, Optional.ofNullable(particleIn));
	}

	public float getCustomParticleYOffset() {
		return entityData.get(EXTRA_PARTICLE_OFFSET_Y);
	}
	
	public void setCustomParticleYOffset(float offset) {
		this.entityData.set(EXTRA_PARTICLE_OFFSET_Y, offset);
	}
	
	public float getCustomParticleFrequency() {
		return entityData.get(EXTRA_PARTICLE_FREQUENCY);
	}
	
	public void setCustomParticleFrequency(float frequency) {
		this.entityData.set(EXTRA_PARTICLE_FREQUENCY, frequency);
	}
	
	public void addVFXFunc(IAreaVFX vfx) {
		this.manualVFX.add(vfx);
	}
	
	public void setHeight(float height) {
		if (!this.level.isClientSide) {
			this.entityData.set(HEIGHT, height); // triggers size refresh
		}
	}
	
	public void setWaiting(boolean ignore) {
		super.setWaiting(ignore);
	}
	
	@Override
	public void setWaitTime(int waitTimeIn) {
		// capture and save
		this.waitTime = waitTimeIn;
		super.setWaitTime(waitTimeIn);
	}
	
	/**
	 * Adds the provided number of ticks to this effect's duration.
	 * If the effect has finished 'waiting' and is in the 'duration' portion of its time,
	 * time is always added to the 'duration' count.
	 * Otherwise, time is either added to 'duration' or 'wait' based on
	 * whether 'allowWait' is true.
	 * @param ticks
	 * @param allowWait
	 */
	public void addTime(int ticks, boolean allowWait) {
		if (this.tickCount < this.waitTime && allowWait) {
			// Add to wait time
			this.setWaitTime(this.waitTime + ticks);
		} else {
			this.setDuration(this.getDuration() + ticks);
		}
	}
	
	public boolean canApply(Entity ent) {
		if (getIgnoreOwner()) {
			LivingEntity owner = this.getOwner();
			if (ent == owner) {
				return false;
			}
		}
		Integer delay = effectDelays.get(ent);
		return (delay == null || delay < this.tickCount);
	}
	
	public void markApplied(Entity ent) {
		effectDelays.put(ent, this.tickCount + this.effectDelay);
	}
	
	public void cleanDelays() {
		Iterator<Entity> it = effectDelays.keySet().iterator();
		while (it.hasNext()) {
			Entity ent = it.next();
			Integer delay = effectDelays.get(ent);
			if (delay == null || delay < this.tickCount) {
				it.remove();
			}
		}
	}
	
	public int getRemainingTicks() {
		return (this.getDuration() + this.waitTime) - this.tickCount;
	}
	
	protected void onFall(double prevY) {
		this.setRadius(this.getRadius() + (float) (Math.ceil(Math.abs(getY() - prevY)) * radiusPerFall));
	}
	
	protected void onClimb(double prevY) {
		;
	}
	
	@Override
	public boolean isPushedByFluid() {
		return false;
	}
	
	protected void waddle() {
		if (waddleDir != null && waddleMagnitude != 0) {
			final int period = 20 * 2;
			final float prog = ((float) ((this.tickCount + (period / 4)) % period)) / (float) period;
			final double offset = Math.sin(2 * Math.PI * prog) * waddleMagnitude;
			
			this.setDeltaMovement(waddleDir.x + (offset * waddleDir.z),
					this.getDeltaMovement().y,
					waddleDir.z + (offset * -waddleDir.x));
			
			setHeight(5f);
		}
	}
	
	@Override
	public void baseTick() {
		super.baseTick();
		
		// Motion
		this.waddle();
		final Vec3 motion = this.getDeltaMovement();
		this.setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        
        boolean elevated = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final double startY = this.getY();
        
        // Move up out of solid blocks
        if (this.doesVerticalSteps()) {
        	while (true) {
        		pos.set(this.blockPosition());
        		BlockState state = level.getBlockState(pos);
	        	if (state == null || !state.getMaterial().blocksMotion()) {
	        		
	        		if (state == null || !this.getWalksWater() || !state.getMaterial().isLiquid()) {
		        		// Done
		        		break;
	        		}
	        	}
	        	
	        	this.setPos(getX(), getY() + 1, getZ());
	        	elevated = true;
        	}
        	
        	if (elevated) {
        		this.onClimb(startY);
        	}
        }
        
        // Move down if too far up
    	// Skip doing if we just elevated because that means there was a solid block.
        if (this.hasGravity() && !elevated) {
        	double left = this.gravitySpeed;
        	while (getY() > 1 && left > 0) {
        		pos.set(getX(), getY() - 1, getZ());
        		BlockState state = level.getBlockState(pos);
	        	if (state != null && state.getMaterial().blocksMotion()) {
	        		// Done
	        		break;
	        	}
	        	
	        	// Also stop if state is liquid and we walk on liquid
	        	if (state != null && state.getMaterial().isLiquid() && this.walksOnLiquid) {
	        		break;
	        	}
	        	
        		if (left >= 1) {
        			this.setPos(getX(), getY() - 1, getZ());
	        		left -= 1;
        		} else {
        			this.setPos(getX(), getY() - left, getZ());
        			left = 0;
        		}
        		elevated = true;
        	}
        	
        	if (elevated) {
        		this.onFall(startY);
        	}
        }
        
        if (level.isClientSide) {
        	//prevHeight = this.getHeight();
        	//this.getHeight() = dataManager.get(HEIGHT);
        } else {
//	        if (this.getHeight() != prevHeight) { // don't know if I need this anymore
//	        	this.dataManager.set(HEIGHT, this.getHeight());
//	        	prevHeight = this.getHeight();
//	        }
        }
        //this.setPosition(posX, posY, posZ);
	}
	
	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityDimensions.scalable(this.getRadius() * 2.0F, this.entityData.get(HEIGHT));
	}
	
	protected void applyEffects(Entity ent) {
		for (IAreaEntityEffect effect : this.entityEffects) {
			effect.apply(level, ent);
		}
	}
	
	protected void applyEffects(BlockPos pos) {
		for (IAreaLocationEffect effect : this.locationEffects) {
			effect.apply(level, pos);
		}
	}
	
	protected void clientUpdateTick() {
		
		// Augment vanilla particles for tall areas
		if (this.getBbHeight() > 2 && !this.isWaiting()) {
			float radius = this.getRadius();
			float area = (float)Math.PI * radius * radius;
			ParticleOptions particle = this.getParticle();
	
			for (int i = 0; (float)i < area; ++i) {
				float f6 = this.random.nextFloat() * ((float)Math.PI * 2F);
				float f7 = Mth.sqrt(this.random.nextFloat()) * radius;
				float f8 = Mth.cos(f6) * f7;
				float f9 = Mth.sin(f6) * f7;
				double y = random.nextDouble() * (this.getBbHeight() - .5) + .5;
	
				if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
					int l1 = this.getColor();
					int i2 = l1 >> 16 & 255;
					int j2 = l1 >> 8 & 255;
					int j1 = l1 & 255;
					this.level.addAlwaysVisibleParticle(particle, this.getX() + (double)f8, this.getY() + y, this.getZ() + (double)f9, (double)((float)i2 / 255.0F), (double)((float)j2 / 255.0F), (double)((float)j1 / 255.0F));
				} else {
					this.level.addAlwaysVisibleParticle(particle, this.getX() + (double)f8, this.getY() + y, this.getZ() + (double)f9, (0.5D - this.random.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.random.nextDouble()) * 0.15D);
				}
			}
		}
		
		// Do custom particle spawning
		final ParticleOptions particle = this.getCustomParticle();
		final float frequency = this.getCustomParticleFrequency();
		if (particle != null && frequency > 0f) { // optional
			final float radius = this.getRadius();
			final float area = (float)Math.PI * radius * radius;
			final float yOffset = this.getCustomParticleYOffset();
	
			for (int i = 0; (float)i < area; ++i) {
				if (this.random.nextFloat() > frequency) {
					continue;
				}
				float f6 = this.random.nextFloat() * ((float)Math.PI * 2F);
				float f7 = Mth.sqrt(this.random.nextFloat()) * radius;
				float f8 = Mth.cos(f6) * f7;
				float f9 = Mth.sin(f6) * f7;
				double y = random.nextDouble() * (this.getBbHeight() - .5) + .5;
	
				this.level.addParticle(particle, this.getX() + (double)f8, this.getY() + y + yOffset, this.getZ() + (double)f9, (0.5D - this.random.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.random.nextDouble()) * 0.15D);
			}
		}
	}
	
	protected void serverVFXTick() {
		for (IAreaVFX vfx : this.manualVFX) {
			vfx.apply(level, this.tickCount, this);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		// Additional effects
		// Sadly, parent class doesn't make it easy to extend, so we redo some work here
		if (!level.isClientSide) {
			
			if (this.effectDelay < 5 || this.tickCount % 5 == 0) {
				// Entities...
				List<Entity> list = this.level.<Entity>getEntitiesOfClass(Entity.class, this.getBoundingBox(), (ent) -> { return ent != this;});
				if (list != null && !list.isEmpty()) {
					for (Entity ent : list) {
						double dx = ent.getX() - this.getX();
						double dz = ent.getZ() - this.getZ();
						double d = dx * dx + dz * dz;
						
						if (d > this.getRadius() * this.getRadius()) {
							continue;
						}
						
						if (canApply(ent)) {
							this.applyEffects(ent);
							markApplied(ent);
						}
					}
				}
			}
			
			if (this.tickCount % 5 == 0) {
				// Blocks
				AABB box = this.getBoundingBox();
				for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(box.minX, box.minY - 1, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
					double dx = (pos.getX() + .5) - this.getX();
					double dz = (pos.getZ() + .5) - this.getZ();
					double d = dx * dx + dz * dz;
					
					if (d > this.getRadius() * this.getRadius()) {
						continue;
					}
					
					this.applyEffects(pos);
				}
			}
			
			if (this.tickCount % 100 == 0) {
				this.cleanDelays();
			}
			
			serverVFXTick();
		} else {
			clientUpdateTick();
		}
	}
	
	@Override
	public void setRadius(float radiusIn) {
//		float height = this.getHeight(); parent used to set height when you did this
		super.setRadius(radiusIn);
//		this.getHeight() = height;
//		super.setPosition(posX, posY, posZ);
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(HEIGHT, 1f);
        this.getEntityData().define(EXTRA_PARTICLE, Optional.empty());
        this.getEntityData().define(EXTRA_PARTICLE_OFFSET_Y, 0f);
        this.getEntityData().define(EXTRA_PARTICLE_FREQUENCY, 1f);
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (key == HEIGHT) {
			this.refreshDimensions();
		}
		
		super.onSyncedDataUpdated(key);
	}
	
	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		if (compound.contains("Particle", 10)) {
			this.setCustomParticle(ParticleHelper.ReadFromNBT(compound.getCompound("Particle")));
		} else {
			this.setCustomParticle(null);
		}
		this.setCustomParticleYOffset(compound.getFloat("ParticleYOffset"));
		this.setCustomParticleFrequency(compound.getFloat("ParticleFreq"));
	}
	
	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		if (this.getCustomParticle() != null) {
			compound.put("Particle", ParticleHelper.WriteToNBT(this.getCustomParticle()));
		}
        compound.putFloat("ParticleYOffset", this.getCustomParticleYOffset());
        compound.putFloat("ParticleFreq", this.getCustomParticleFrequency());
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
