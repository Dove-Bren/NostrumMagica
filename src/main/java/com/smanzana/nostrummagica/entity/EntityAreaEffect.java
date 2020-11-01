package com.smanzana.nostrummagica.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * More generic version of EntityAreaEffectCloud
 * @author Skyler
 *
 */
public class EntityAreaEffect extends EntityAreaEffectCloud {
	
	public static interface IAreaEntityEffect {
		public void apply(World world, Entity ent);
	}
	
	public static interface IAreaLocationEffect {
		public void apply(World world, BlockPos pos);
	}
	
	 private static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityAreaEffect.class, DataSerializers.FLOAT);
	
	protected final List<IAreaEntityEffect> entityEffects;
	protected final List<IAreaLocationEffect> locationEffects;
	protected final Map<Entity, Integer> effectDelays;
	
	// Same as parent's reapplicationDelay
	protected int effectDelay;
	
	protected boolean verticalSteps;
	protected boolean gravity;
	protected double gravitySpeed;
	protected float radiusPerFall;
	protected boolean walksOnLiquid;
	
	private float prevHeight;
	private Vec3d waddleDir;
	private double waddleMagnitude;

	public EntityAreaEffect(World worldIn) {
		super(worldIn);
		entityEffects = new LinkedList<>();
		locationEffects = new LinkedList<>();
		effectDelays = new HashMap<>();
		effectDelay = 20;
		verticalSteps = false;
		gravity = false;
		gravitySpeed = 0;
	}
	
	public EntityAreaEffect(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		entityEffects = new LinkedList<>();
		locationEffects = new LinkedList<>();
		effectDelays = new HashMap<>();
		effectDelay = 20;
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
	
	public boolean canApply(Entity ent) {
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
			
			motionX = waddleDir.xCoord + (offset * waddleDir.zCoord);
			motionZ = waddleDir.zCoord + (offset * -waddleDir.xCoord);
			height = 5f;
		}
	}
	
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
		// Motion
		this.waddle();
		this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        
        boolean elevated = false;
        MutableBlockPos pos = new MutableBlockPos();
        final double startY = this.posY;
        
        // Move up out of solid blocks
        if (this.doesVerticalSteps()) {
        	while (true) {
        		pos.setPos(this.getPosition());
        		IBlockState state = worldObj.getBlockState(pos);
	        	if (state == null || !state.getMaterial().blocksMovement()) {
	        		
	        		if (!this.getWalksWater() || !state.getMaterial().isLiquid()) {
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
        		IBlockState state = worldObj.getBlockState(pos);
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
        
        if (worldObj.isRemote) {
        	prevHeight = this.height;
        	this.height = dataManager.get(HEIGHT);
        } else {
	        if (this.height != prevHeight) {
	        	this.dataManager.set(HEIGHT, this.height);
	        	prevHeight = this.height;
	        }
        }
        this.setPosition(posX, posY, posZ);
	}
	
	protected void applyEffects(Entity ent) {
		for (IAreaEntityEffect effect : this.entityEffects) {
			effect.apply(worldObj, ent);
		}
	}
	
	protected void applyEffects(BlockPos pos) {
		for (IAreaLocationEffect effect : this.locationEffects) {
			effect.apply(worldObj, pos);
		}
	}
	
	protected void clientUpdateTick() {
		
		if (this.height > 2) {
			float radius = this.getRadius();
			float area = (float)Math.PI * radius * radius;
			EnumParticleTypes enumparticletypes = this.getParticle();
			int[] aint = new int[enumparticletypes.getArgumentCount()];
	
			if (aint.length > 0) {
				aint[0] = this.getParticleParam1();
			}
	
			if (aint.length > 1) {
				aint[1] = this.getParticleParam2();
			}
	
			for (int i = 0; (float)i < area; ++i) {
				float f6 = this.rand.nextFloat() * ((float)Math.PI * 2F);
				float f7 = MathHelper.sqrt_float(this.rand.nextFloat()) * radius;
				float f8 = MathHelper.cos(f6) * f7;
				float f9 = MathHelper.sin(f6) * f7;
				double y = rand.nextDouble() * (this.height - .5) + .5;
	
				if (this.getParticle() == EnumParticleTypes.SPELL_MOB) {
					int l1 = this.getColor();
					int i2 = l1 >> 16 & 255;
					int j2 = l1 >> 8 & 255;
					int j1 = l1 & 255;
					this.worldObj.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (double)f8, this.posY + y, this.posZ + (double)f9, (double)((float)i2 / 255.0F), (double)((float)j2 / 255.0F), (double)((float)j1 / 255.0F), new int[0]);
				} else {
					this.worldObj.spawnParticle(this.getParticle(), this.posX + (double)f8, this.posY + y, this.posZ + (double)f9, (0.5D - this.rand.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.rand.nextDouble()) * 0.15D, aint);
				}
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// Additional effects
		// Sadly, parent class doesn't make it easy to extend, so we redo some work here
		if (!worldObj.isRemote) {
			
			if (this.effectDelay < 5 || this.ticksExisted % 5 == 0) {
				// Entities...
				List<Entity> list = this.worldObj.<Entity>getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox(), (ent) -> { return ent != this;});
				if (list != null && !list.isEmpty()) {
					for (Entity ent : list) {
						double dx = ent.posX - this.posX;
						double dz = ent.posZ - this.posZ;
						double d = dx * dx + dz * dz;
						
						if (d > this.getRadius()) {
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
				AxisAlignedBB box = this.getEntityBoundingBox();
				for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(box.minX, box.minY - 1, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ))) {
					double dx = (pos.getX() + .5) - this.posX;
					double dz = (pos.getZ() + .5) - this.posZ;
					double d = dx * dx + dz * dz;
					
					if (d > this.getRadius()) {
						continue;
					}
					
					this.applyEffects(pos);
				}
			}
			
			if (this.ticksExisted % 100 == 0) {
				this.cleanDelays();
			}
		} else {
			clientUpdateTick();
		}
	}
	
	@Override
	public void setRadius(float radiusIn) {
		float height = this.height;
		super.setRadius(radiusIn);
		this.height = height;
		super.setPosition(posX, posY, posZ);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.getDataManager().register(HEIGHT, 1f);
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (key == HEIGHT) {
			this.height = this.dataManager.get(HEIGHT);
			super.setPosition(posX, posY, posZ);
		}
		
		super.notifyDataManagerChange(key);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
	}
}
