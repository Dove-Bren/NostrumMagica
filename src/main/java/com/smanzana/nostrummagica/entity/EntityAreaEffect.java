package com.smanzana.nostrummagica.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.serializers.OptionalParticleDataSerializer;
import com.smanzana.nostrummagica.utils.ParticleHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * More generic version of EntityAreaEffectCloud
 * @author Skyler
 *
 */
public class EntityAreaEffect extends AreaEffectCloudEntity {
	
	public static interface IAreaEntityEffect {
		public void apply(World world, Entity ent);
	}
	
	public static interface IAreaLocationEffect {
		public void apply(World world, BlockPos pos);
	}
	
	public static interface IAreaVFX {
		public void apply(World world, int ticksExisted, EntityAreaEffect cloud);
	}
	
	public static final String ID = "entity_effect_cloud";
	
	private static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityAreaEffect.class, DataSerializers.FLOAT);
	private static final DataParameter<Optional<IParticleData>> EXTRA_PARTICLE = EntityDataManager.<Optional<IParticleData>>createKey(EntityAreaEffect.class, OptionalParticleDataSerializer.instance);
	private static final DataParameter<Float> EXTRA_PARTICLE_OFFSET_Y = EntityDataManager.<Float>createKey(EntityAreaEffect.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> EXTRA_PARTICLE_FREQUENCY = EntityDataManager.<Float>createKey(EntityAreaEffect.class, DataSerializers.FLOAT);
	
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
	
	private float prevHeight;
	private Vec3d waddleDir;
	private double waddleMagnitude;
	
	protected final List<IAreaVFX> manualVFX;

	public EntityAreaEffect(EntityType<? extends EntityAreaEffect> type, World worldIn) {
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
	
	public EntityAreaEffect(EntityType<? extends EntityAreaEffect> type, World worldIn, double x, double y, double z) {
		super(type, worldIn);
		this.setPosition(x, y, z);
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
	
	public void setWaddle(Vec3d direction, double waddle) {
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
	
	public @Nullable IParticleData getCustomParticle() {
		return this.getDataManager().get(EXTRA_PARTICLE).orElse(null);
	}

	public void setCustomParticle(@Nullable IParticleData particleIn) {
		this.getDataManager().set(EXTRA_PARTICLE, Optional.ofNullable(particleIn));
	}

	public float getCustomParticleYOffset() {
		return dataManager.get(EXTRA_PARTICLE_OFFSET_Y);
	}
	
	public void setCustomParticleYOffset(float offset) {
		this.dataManager.set(EXTRA_PARTICLE_OFFSET_Y, offset);
	}
	
	public float getCustomParticleFrequency() {
		return dataManager.get(EXTRA_PARTICLE_FREQUENCY);
	}
	
	public void setCustomParticleFrequency(float frequency) {
		this.dataManager.set(EXTRA_PARTICLE_FREQUENCY, frequency);
	}
	
	public void addVFXFunc(IAreaVFX vfx) {
		this.manualVFX.add(vfx);
	}
	
	public void setHeight(float height) {
		if (!this.world.isRemote) {
			this.dataManager.set(HEIGHT, height); // triggers size refresh
		}
	}
	
	public void setIgnoreRadius(boolean ignore) {
		super.setIgnoreRadius(ignore);
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
		if (this.ticksExisted < this.waitTime && allowWait) {
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
		return (delay == null || delay < this.ticksExisted);
	}
	
	public void markApplied(Entity ent) {
		effectDelays.put(ent, this.ticksExisted + this.effectDelay);
	}
	
	public void cleanDelays() {
		Iterator<Entity> it = effectDelays.keySet().iterator();
		while (it.hasNext()) {
			Entity ent = it.next();
			Integer delay = effectDelays.get(ent);
			if (delay == null || delay < this.ticksExisted) {
				it.remove();
			}
		}
	}
	
	public int getRemainingTicks() {
		return (this.getDuration() + this.waitTime) - this.ticksExisted;
	}
	
	protected void onFall(double prevY) {
		this.setRadius(this.getRadius() + (float) (Math.ceil(Math.abs(posY - prevY)) * radiusPerFall));
	}
	
	protected void onClimb(double prevY) {
		;
	}
	
	@Override
	public boolean isPushedByWater() {
		return false;
	}
	
	protected void waddle() {
		if (waddleDir != null && waddleMagnitude != 0) {
			final int period = 20 * 2;
			final float prog = ((float) ((this.ticksExisted + (period / 4)) % period)) / (float) period;
			final double offset = Math.sin(2 * Math.PI * prog) * waddleMagnitude;
			
			this.setMotion(waddleDir.x + (offset * waddleDir.z),
					this.getMotion().y,
					waddleDir.z + (offset * -waddleDir.x));
			
			setHeight(5f);
		}
	}
	
	@Override
	public void baseTick() {
		super.baseTick();
		
		// Motion
		this.waddle();
		this.posX += this.getMotion().x;
        this.posY += this.getMotion().y;
        this.posZ += this.getMotion().z;
        
        boolean elevated = false;
        MutableBlockPos pos = new MutableBlockPos();
        final double startY = this.posY;
        
        // Move up out of solid blocks
        if (this.doesVerticalSteps()) {
        	while (true) {
        		pos.setPos(this.getPosition());
        		BlockState state = world.getBlockState(pos);
	        	if (state == null || !state.getMaterial().blocksMovement()) {
	        		
	        		if (state == null || !this.getWalksWater() || !state.getMaterial().isLiquid()) {
		        		// Done
		        		break;
	        		}
	        	}
	        	
	        	this.posY += 1;
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
        	while (posY > 1 && left > 0) {
        		pos.setPos(posX, posY - 1, posZ);
        		BlockState state = world.getBlockState(pos);
	        	if (state != null && state.getMaterial().blocksMovement()) {
	        		// Done
	        		break;
	        	}
	        	
	        	// Also stop if state is liquid and we walk on liquid
	        	if (state != null && state.getMaterial().isLiquid() && this.walksOnLiquid) {
	        		break;
	        	}
	        	
        		if (left >= 1) {
	        		this.posY -= 1;
	        		left -= 1;
        		} else {
        			this.posY -= left;
        			left = 0;
        		}
        		elevated = true;
        	}
        	
        	if (elevated) {
        		this.onFall(startY);
        	}
        }
        
        if (world.isRemote) {
        	prevHeight = this.getHeight();
        	//this.getHeight() = dataManager.get(HEIGHT);
        } else {
//	        if (this.getHeight() != prevHeight) { // don't know if I need this anymore
//	        	this.dataManager.set(HEIGHT, this.getHeight());
//	        	prevHeight = this.getHeight();
//	        }
        }
        this.setPosition(posX, posY, posZ);
	}
	
	@Override
	public EntitySize getSize(Pose pose) {
		return EntitySize.flexible(this.getRadius() * 2.0F, this.dataManager.get(HEIGHT));
	}
	
	protected void applyEffects(Entity ent) {
		for (IAreaEntityEffect effect : this.entityEffects) {
			effect.apply(world, ent);
		}
	}
	
	protected void applyEffects(BlockPos pos) {
		for (IAreaLocationEffect effect : this.locationEffects) {
			effect.apply(world, pos);
		}
	}
	
	protected void clientUpdateTick() {
		
		// Augment vanilla particles for tall areas
		if (this.getHeight() > 2 && !this.shouldIgnoreRadius()) {
			float radius = this.getRadius();
			float area = (float)Math.PI * radius * radius;
			IParticleData particle = this.getParticleData();
	
			for (int i = 0; (float)i < area; ++i) {
				float f6 = this.rand.nextFloat() * ((float)Math.PI * 2F);
				float f7 = MathHelper.sqrt(this.rand.nextFloat()) * radius;
				float f8 = MathHelper.cos(f6) * f7;
				float f9 = MathHelper.sin(f6) * f7;
				double y = rand.nextDouble() * (this.getHeight() - .5) + .5;
	
				if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
					int l1 = this.getColor();
					int i2 = l1 >> 16 & 255;
					int j2 = l1 >> 8 & 255;
					int j1 = l1 & 255;
					this.world.addOptionalParticle(particle, this.posX + (double)f8, this.posY + y, this.posZ + (double)f9, (double)((float)i2 / 255.0F), (double)((float)j2 / 255.0F), (double)((float)j1 / 255.0F));
				} else {
					this.world.addOptionalParticle(particle, this.posX + (double)f8, this.posY + y, this.posZ + (double)f9, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D);
				}
			}
		}
		
		// Do custom particle spawning
		final IParticleData particle = this.getCustomParticle();
		final float frequency = this.getCustomParticleFrequency();
		if (particle != null && frequency > 0f) { // optional
			final float radius = this.getRadius();
			final float area = (float)Math.PI * radius * radius;
			final float yOffset = this.getCustomParticleYOffset();
	
			for (int i = 0; (float)i < area; ++i) {
				if (this.rand.nextFloat() > frequency) {
					continue;
				}
				float f6 = this.rand.nextFloat() * ((float)Math.PI * 2F);
				float f7 = MathHelper.sqrt(this.rand.nextFloat()) * radius;
				float f8 = MathHelper.cos(f6) * f7;
				float f9 = MathHelper.sin(f6) * f7;
				double y = rand.nextDouble() * (this.getHeight() - .5) + .5;
	
				this.world.addParticle(particle, this.posX + (double)f8, this.posY + y + yOffset, this.posZ + (double)f9, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D);
			}
		}
	}
	
	protected void serverVFXTick() {
		for (IAreaVFX vfx : this.manualVFX) {
			vfx.apply(world, this.ticksExisted, this);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		// Additional effects
		// Sadly, parent class doesn't make it easy to extend, so we redo some work here
		if (!world.isRemote) {
			
			if (this.effectDelay < 5 || this.ticksExisted % 5 == 0) {
				// Entities...
				List<Entity> list = this.world.<Entity>getEntitiesWithinAABB(Entity.class, this.getBoundingBox(), (ent) -> { return ent != this;});
				if (list != null && !list.isEmpty()) {
					for (Entity ent : list) {
						double dx = ent.posX - this.posX;
						double dz = ent.posZ - this.posZ;
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
			
			if (this.ticksExisted % 5 == 0) {
				// Blocks
				AxisAlignedBB box = this.getBoundingBox();
				for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY - 1, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
					double dx = (pos.getX() + .5) - this.posX;
					double dz = (pos.getZ() + .5) - this.posZ;
					double d = dx * dx + dz * dz;
					
					if (d > this.getRadius() * this.getRadius()) {
						continue;
					}
					
					this.applyEffects(pos);
				}
			}
			
			if (this.ticksExisted % 100 == 0) {
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
	protected void registerData() {
		this.getDataManager().register(HEIGHT, 1f);
        this.getDataManager().register(EXTRA_PARTICLE, Optional.empty());
        this.getDataManager().register(EXTRA_PARTICLE_OFFSET_Y, 0f);
        this.getDataManager().register(EXTRA_PARTICLE_FREQUENCY, 1f);
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (key == HEIGHT) {
			this.recalculateSize();
		}
		
		super.notifyDataManagerChange(key);
	}
	
	@Override
	protected void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		if (compound.contains("Particle", 10)) {
			this.setCustomParticle(ParticleHelper.ReadFromNBT(compound.getCompound("Particle")));
		} else {
			this.setCustomParticle(null);
		}
		this.setCustomParticleYOffset(compound.getFloat("ParticleYOffset"));
		this.setCustomParticleFrequency(compound.getFloat("ParticleFreq"));
	}
	
	@Override
	protected void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		if (this.getCustomParticle() != null) {
			compound.put("Particle", ParticleHelper.WriteToNBT(this.getCustomParticle()));
		}
        compound.putFloat("ParticleYOffset", this.getCustomParticleYOffset());
        compound.putFloat("ParticleFreq", this.getCustomParticleFrequency());
	}
}
