package com.smanzana.nostrummagica.entity.plantboss;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityPlantBossBramble extends Entity {

	protected static final DataParameter<Float> WIDTH = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Float> DEPTH = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<EnumFacing> FACING = EntityDataManager.<EnumFacing>createKey(EntityPlantBossBramble.class, DataSerializers.FACING);
	
	protected EntityPlantBoss plant;
	protected float distance;
	protected BlockPos startPos;
	
	protected AxisAlignedBB entityBBOverride;
	
	public EntityPlantBossBramble(World worldIn) {
		super(worldIn);
	}
	
	public EntityPlantBossBramble(World worldIn, EntityPlantBoss plant, float length) {
		this(worldIn, plant, length, .5f, .75f);
	}
	
	public EntityPlantBossBramble(World worldIn, EntityPlantBoss plant, float width, float depth, float height) {
		this(worldIn);
		this.plant = plant;
		this.setSize(width, height);
		
		this.setDims(width, depth, height);
	}
	
	@Override
	protected void entityInit() {
		//super.entityInit();
		this.dataManager.register(WIDTH, 5f);
		this.dataManager.register(DEPTH, .5f);
		this.dataManager.register(HEIGHT, 5f);
		this.dataManager.register(FACING, EnumFacing.SOUTH);
	}
	
	public float getWidth() {
		return this.dataManager.get(WIDTH);
	}
	
	public float getDepth() {
		return this.dataManager.get(DEPTH);
	}
	
	public float getHeight() {
		return this.dataManager.get(HEIGHT);
	}
	
	public EnumFacing getFacing() {
		return this.dataManager.get(FACING);
	}
	
	protected void setDims(float width, float depth, float height) {
		
		// Server side gets this called from outside data manager and so should update data manager.
		// client calls this as data manager event and shouldn't set data manager.
		
		boolean change = false;
		
		if (world.isRemote) {
			change = true; // Just adjust BB
		} else {
			if (this.getWidth() != width) {
				this.dataManager.set(WIDTH, width);
				change = true;
			}
			if (this.getDepth() != depth) {
				this.dataManager.set(DEPTH, depth);
				change = true;
			}
			if (this.getHeight() != height) {
				this.dataManager.set(HEIGHT, height);
				change = true;
			}
		}
		
		if (change) {
//			AxisAlignedBB old = this.getEntityBoundingBox();
//			this.setEntityBoundingBox(new AxisAlignedBB(
//					old.minX, old.minY, old.minZ,
//					old.minX + width,
//					old.minY + height,
//					old.minZ + depth
//					));
		}
	}
	
	public void setMotion(EnumFacing direction, float distance) {
		this.dataManager.set(FACING, direction);
		this.distance = distance;
		
		this.startPos = this.getPosition();
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (this.world != null && this.world.isRemote) {
			if (key == WIDTH
					|| key == HEIGHT
					|| key == DEPTH) {
				this.setDims(this.getWidth(), this.getDepth(), this.getHeight());
			} else if (key == FACING && this.world.isRemote) {
				// Adjust width/depth to rotate if not moving n/s
				final EnumFacing dir = this.dataManager.get(FACING);
				if (dir == EnumFacing.WEST || dir == EnumFacing.EAST) {
					final float w = this.getWidth();
					final float d = this.getDepth();
					this.setDims(d, w, this.getHeight());
				}
			}
		}
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return false;
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		//super.readEntityFromNBT(compound);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
    	//super.writeEntityToNBT(compound);
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	@Override
	public float getCollisionBorderSize() {
		return 1f;
	}
	
	protected void onImpact(EntityLivingBase entity) {
		if (entity != this.plant && !entity.equals(this.plant)) {
			entity.attackEntityAsMob(this);
			entity.attackEntityFrom(new EntityDamageSource("mob", this), 6f);
			entity.knockBack(this, 1f, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (this.getFacing() != null) {
			this.setRotation(this.getFacing().getHorizontalAngle(), 0f);
		}
		
		if (!world.isRemote) {
			// Move if given a direction
			if (this.getFacing() != null) {
				if (this.getDistanceSqToCenter(startPos) > this.distance * this.distance) {
					this.setDead();
				}
				
				Vec3d motion = new Vec3d(this.getFacing().getDirectionVec())
						.scale(.2)
						;
				this.setPositionAndUpdate(posX + motion.x, posY + motion.y, posZ + motion.z);
			}
			
			List<Entity> collidedEnts = world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), (ent) -> {
				return ent instanceof EntityLivingBase;
			});
			
			for (Entity ent : collidedEnts) {
				onImpact((EntityLivingBase) ent);
			}
		}
	}
	
	protected void checkBoundingBox() {
		// When moving E/W, w should be d
		final boolean turned = this.getFacing() == EnumFacing.EAST || this.getFacing() == EnumFacing.WEST;
		
		final float w = turned ? this.getDepth() : this.getWidth();
		final float h = this.getHeight();
		final float d = turned ? this.getWidth() : this.getDepth();
		this.entityBBOverride = new AxisAlignedBB(
				this.posX - (w/2f),
				this.posY,
				this.posZ - (d/2f),
				this.posX + (w/2f),
				this.posY + h,
				this.posZ + (d/2f)
				);
	}
	
	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		checkBoundingBox();
		return this.entityBBOverride;
	}
	
	// TODO bounding box isn't owrking. Just implement custom override I guess

}
