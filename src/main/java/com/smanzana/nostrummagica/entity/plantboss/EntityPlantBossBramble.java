package com.smanzana.nostrummagica.entity.plantboss;

import java.util.List;

import com.smanzana.nostrummagica.effects.NostrumEffects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityPlantBossBramble extends Entity {
	
	public static final String ID = "entity_plant_boss.bramble";

	protected static final DataParameter<Float> WIDTH = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Float> DEPTH = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityPlantBossBramble.class, DataSerializers.FLOAT);
	protected static final DataParameter<Direction> FACING = EntityDataManager.<Direction>createKey(EntityPlantBossBramble.class, DataSerializers.DIRECTION);
	
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
		this.setDims(width, depth, height);
	}
	
	@Override
	protected void registerData() {
		//super.registerData();
		this.dataManager.register(WIDTH, 5f);
		this.dataManager.register(DEPTH, .5f);
		this.dataManager.register(HEIGHT, 5f);
		this.dataManager.register(FACING, Direction.SOUTH);
	}
	
	public float getBrambleWidth() {
		return this.dataManager.get(WIDTH);
	}
	
	public float getBrambleDepth() {
		return this.dataManager.get(DEPTH);
	}
	
	public float getBrambleHeight() {
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
			if (this.getBrambleWidth() != width) {
				this.dataManager.set(WIDTH, width);
				change = true;
			}
			if (this.getBrambleDepth() != depth) {
				this.dataManager.set(DEPTH, depth);
				change = true;
			}
			if (this.getBrambleHeight() != height) {
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
			this.recalculateSize();
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
				this.setDims(this.getBrambleWidth(), this.getBrambleDepth(), this.getBrambleHeight());
			} else if (key == FACING && this.world.isRemote) {
				// Adjust width/depth to rotate if not moving n/s
				final Direction dir = this.dataManager.get(FACING);
				if (dir == Direction.WEST || dir == Direction.EAST) {
					final float w = this.getBrambleWidth();
					final float d = this.getBrambleDepth();
					this.setDims(d, w, this.getBrambleHeight());
				}
			}
		}
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
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
			entity.removePotionEffect(NostrumEffects.rooted);
			entity.attackEntityAsMob(this);
			entity.attackEntityFrom(new BrambleDamageSource(this.plant), 6f);
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
				if (startPos.distanceSq(this.posX, this.posY, this.posZ, true) > this.distance * this.distance) {
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
		
		final float w = turned ? this.getBrambleDepth() : this.getBrambleWidth();
		final float h = this.getBrambleHeight();
		final float d = turned ? this.getBrambleWidth() : this.getBrambleDepth();
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
	public AxisAlignedBB getBoundingBox() {
		checkBoundingBox();
		return this.entityBBOverride;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	public static class BrambleDamageSource extends EntityDamageSource {
		
		public BrambleDamageSource(EntityPlantBoss parent) {
			super("plantboss.bramble", parent);
		}
		
		@Override
		public ITextComponent getDeathMessage(LivingEntity entityLivingBaseIn) {
	        String untranslated = "death.attack.plantboss.bramble";
	        return new TranslationTextComponent(untranslated, new Object[] {entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName()});
	    }
	}
}
