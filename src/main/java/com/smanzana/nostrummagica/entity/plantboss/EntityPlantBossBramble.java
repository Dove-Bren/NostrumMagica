package com.smanzana.nostrummagica.entity.plantboss;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityPlantBossBramble extends Entity {
	
	public static final String ID = "entity_plant_boss.bramble";

	protected static final DataParameter<Float> WIDTH = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Float> DEPTH = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Direction> FACING = EntityDataManager.<Direction>createKey(EntityPlantBossBramble.class, DataSerializers.FACING);
	
	protected EntityPlantBoss plant;
	protected float distance;
	protected BlockPos startPos;
	
	protected AxisAlignedBB entityBBOverride;
	
	public EntityPlantBossBramble(EntityType<EntityPlantBossBramble> type, World worldIn) {
		super(type, worldIn);
	}
	
	public EntityPlantBossBramble(EntityType<EntityPlantBossBramble> type, World worldIn, EntityPlantBoss plant, float length) {
		this(type, worldIn, plant, length, .5f, .75f);
	}
	
	public EntityPlantBossBramble(EntityType<EntityPlantBossBramble> type, World worldIn, EntityPlantBoss plant, float width, float depth, float height) {
		this(type, worldIn);
		this.plant = plant;
		this.setSize(width, height);
		
		this.setDims(width, depth, height);
	}
	
	@Override
	protected void registerData() { int unused; // TODO
		//super.entityInit();
		this.dataManager.register(WIDTH, 5f);
		this.dataManager.register(DEPTH, .5f);
		this.dataManager.register(HEIGHT, 5f);
		this.dataManager.register(FACING, Direction.SOUTH);
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
	
	public Direction getFacing() {
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
//			AxisAlignedBB old = this.getBoundingBox();
//			this.setEntityBoundingBox(new AxisAlignedBB(
//					old.minX, old.minY, old.minZ,
//					old.minX + width,
//					old.minY + height,
//					old.minZ + depth
//					));
		}
	}
	
	public void setMotion(Direction direction, float distance) {
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
				final Direction dir = this.dataManager.get(FACING);
				if (dir == Direction.WEST || dir == Direction.EAST) {
					final float w = this.getWidth();
					final float d = this.getDepth();
					this.setDims(d, w, this.getHeight());
				}
			}
		}
	}
	
	@Override
	public boolean writeToNBTOptional(CompoundNBT compound) {
		return false;
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		//super.readEntityFromNBT(compound);
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
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
	
	protected void onImpact(LivingEntity entity) {
		if (entity != this.plant && !entity.equals(this.plant)) {
			entity.attackEntityAsMob(this);
			entity.attackEntityFrom(new EntityDamageSource("mob", this), 6f);
			entity.knockBack(this, 1f, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.getFacing() != null) {
			this.setRotation(this.getFacing().getHorizontalAngle(), 0f);
		}
		
		if (!world.isRemote) {
			// Move if given a direction
			if (this.getFacing() != null) {
				if (this.getDistanceSqToCenter(startPos) > this.distance * this.distance) {
					this.remove();
				}
				
				Vec3d motion = new Vec3d(this.getFacing().getDirectionVec())
						.scale(.2)
						;
				this.setPositionAndUpdate(posX + motion.x, posY + motion.y, posZ + motion.z);
			}
			
			List<Entity> collidedEnts = world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), (ent) -> {
				return ent instanceof LivingEntity;
			});
			
			for (Entity ent : collidedEnts) {
				onImpact((LivingEntity) ent);
			}
		}
	}
	
	protected void checkBoundingBox() {
		// When moving E/W, w should be d
		final boolean turned = this.getFacing() == Direction.EAST || this.getFacing() == Direction.WEST;
		
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
