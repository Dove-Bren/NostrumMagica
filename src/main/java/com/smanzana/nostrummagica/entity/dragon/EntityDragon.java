package com.smanzana.nostrummagica.entity.dragon;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.utils.NonNullEnumMap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class EntityDragon extends MonsterEntity implements ILoreTagged {
	
	//protected EntitySize size;
	//protected AxisAlignedBB entityBBOverride;
	
	public EntityDragon(EntityType<? extends EntityDragon> type, World worldIn) {
        super(type, worldIn);
    }
	
//	protected void refreshBoundingBox() {
//		AxisAlignedBB axisalignedbb = this.getBoundingBox();
//		this.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.getWidth(), axisalignedbb.minY + (double)this.getHeight(), axisalignedbb.minZ + (double)length));
//	}
//	
//	protected void setSize(float width, float length, float height) {
//		if (width != this.getWidth() || height != this.getHeight()) {
//			float f = this.getWidth();
//			this.getWidth() = width;
//			this.getHeight() = height;
//			AxisAlignedBB axisalignedbb = this.getBoundingBox();
//			this.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.getWidth(), axisalignedbb.minY + (double)this.getHeight(), axisalignedbb.minZ + (double)length));
//
//			if (this.getWidth() > f && !this.firstUpdate && !this.world.isRemote) {
//				this.move(MoverType.SELF, (double)(f - this.getWidth()), 0.0D, (double)(f - length));
//			}
//		}
//	}
    
	/*
	 * TODO: Make a 'FlyingDragon' abstract class. Move bite and slash data things to this class.
	 * Rething how to diversify.
	 */
	public abstract void slash(LivingEntity target);
	public abstract void bite(LivingEntity target);
	
	public void dragonJump() {
		; // Default, do nothing
	}
    

	@Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return NostrumMagicaSounds.DRAGON_LAND_HURT.getEvent();
    }

	@Override
    protected SoundEvent getDeathSound() {
    	return NostrumMagicaSounds.DRAGON_DEATH.getEvent();
    }

	@Override
    protected SoundEvent getAmbientSound() {
    	return NostrumMagicaSounds.DRAGON_IDLE.getEvent();
    }

    protected SoundEvent getAttackSound() {
    	return NostrumMagicaSounds.DRAGON_BITE.getEvent();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
	@Override
    protected float getSoundVolume() {
        return 2F;
    }

	@Override
    protected float getStandingEyeHeight(Pose pose, EntitySize size) {
        return this.getHeight() * 0.95F;
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        return false;
    }

	@Override
    public boolean canBeLeashedTo(PlayerEntity player) {
        return false;
    }

	@Override
	public void fall(float distance, float damageMulti) {
		; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	public abstract boolean isTryingToLand();
	
	public abstract boolean isCasting();
	
	static class DragonFlyMoveHelper extends FlyingMovementController {
        protected final EntityDragon parentEntity;
        //private double lastDist;
        //private int courseChangeCooldown;

        public DragonFlyMoveHelper(EntityDragon dragon) {
            super(dragon);
            this.parentEntity = dragon;
        }
        
//        @Override
//        public void tick() {
////        	if (this.action == MovementController.Action.STRAFE)
////    		{
////    			float f = (float)this.entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
////    			float f1 = (float)this.speed * f;
////    			float f2 = this.moveForward;
////    			float f3 = this.moveStrafe;
////    			float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
////
////    			if (f4 < 1.0F)
////    			{
////    				f4 = 1.0F;
////    			}
////
////    			f4 = f1 / f4;
////    			f2 = f2 * f4;
////    			f3 = f3 * f4;
////    			float f5 = MathHelper.sin(this.entity.rotationYaw * 0.017453292F);
////    			float f6 = MathHelper.cos(this.entity.rotationYaw * 0.017453292F);
////    			float f7 = f2 * f6 - f3 * f5;
////    			float f8 = f3 * f6 + f2 * f5;
////    			PathNavigator PathNavigator = this.entity.getNavigator();
////
////    			if (PathNavigator != null)
////    			{
////    				NodeProcessor nodeprocessor = PathNavigator.getNodeProcessor();
////
////    				if (nodeprocessor != null && nodeprocessor.getPathNodeType(this.entity.world, MathHelper.floor(this.entity.getPosX() + (double)f7), MathHelper.floor(this.entity.getPosY()), MathHelper.floor(this.entity.getPosZ() + (double)f8)) != PathNodeType.OPEN)
////    				{
////    					this.moveForward = 1.0F;
////    					this.moveStrafe = 0.0F;
////    					f1 = f;
////    				}
////    			}
////
////    			this.entity.setAIMoveSpeed(f * 3);
////    			this.entity.setMoveForward(this.moveForward);
////    			this.entity.setMoveStrafing(this.moveStrafe);
////    			this.action = MovementController.Action.WAIT;
////    		}
////        	else 
//        	if (this.action == MovementController.Action.MOVE_TO)
//            {
//                double d0 = this.getPosX() - this.parentEntity.getPosX();
//                double d1 = this.getPosY() - this.parentEntity.getPosY();
//                double d2 = this.getPosZ() - this.parentEntity.getPosZ();
//                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
//
//                d3 = (double)MathHelper.sqrt(d3);
//                
//                if (Math.abs(d3) < 1) {
//                	lastDist = 0.0D;
//                	this.action = MovementController.Action.WAIT;
//                } else if (lastDist != 0.0D && Math.abs(lastDist - d3) < 0.05) {
//                	courseChangeCooldown--;
//                } else {
//                	courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
//                }
//                
//                if (courseChangeCooldown <= 0) {
//                	lastDist = 0.0D;
//                	this.action = MovementController.Action.WAIT;
//                } else {
//                	float speed = (float) this.parentEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
//                	speed *= 3f;
//                	this.parentEntity.setMotion(
//                			(d0 / d3) * speed,
//                			(d1 / d3) * speed,
//                			(d2 / d3) * speed
//                			);
//	                
//	                lastDist = d3;
//	                
//	                float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//	                this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f9, 90.0F);
//                }
//            }
//        }
    }
	
	static public class FlyNodeProcessor extends FlyingNodeProcessor {
//	    public PathPoint getStart() {
//	        return this.openPoint(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY + 0.5D), MathHelper.floor(this.entity.getBoundingBox().minZ));
//	    }
//
//	    /**
//	     * Returns PathPoint for given coordinates
//	     */
//	    public PathPoint getPathPointToCoords(double x, double y, double z) {
//	        return this.openPoint(MathHelper.floor(x - (double)(this.entity.getWidth() / 2.0F)), MathHelper.floor(y + 0.5D), MathHelper.floor(z - (double)(this.entity.getWidth() / 2.0F)));
//	    }
//
//	    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
//	        int i = 0;
//
//	        for (Direction enumfacing : Direction.values()) {
//	            PathPoint pathpoint = this.getAirNode(currentPoint.x + enumfacing.getXOffset(), currentPoint.y + enumfacing.getYOffset(), currentPoint.z + enumfacing.getZOffset());
//
//	            if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance) {
//	                pathOptions[i++] = pathpoint;
//	            }
//	        }
//
//	        return i;
//	    }
//
//	    public PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z, MobEntity entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn) {
//	        return PathNodeType.OPEN;
//	    }
//
//	    public PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z) {
//	        return PathNodeType.OPEN;
//	    }
//
//	    @Nullable
//	    private PathPoint getAirNode(int p_186328_1_, int p_186328_2_, int p_186328_3_) {
//	        PathNodeType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
//	        return pathnodetype == PathNodeType.OPEN ? this.openPoint(p_186328_1_, p_186328_2_, p_186328_3_) : null;
//	    }
//
//	    private PathNodeType isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_) {
//	        BlockPos.BlockPos.Mutable blockpos$BlockPos.Mutable = new BlockPos.BlockPos.Mutable();
//
//	        for (int i = p_186327_1_; i < p_186327_1_ + this.entitySizeX; ++i)
//	        {
//	            for (int j = p_186327_2_; j < p_186327_2_ + this.entitySizeY; ++j)
//	            {
//	                for (int k = p_186327_3_; k < p_186327_3_ + this.entitySizeZ; ++k)
//	                {
//	                    BlockState iblockstate = this.blockaccess.getBlockState(blockpos$BlockPos.Mutable.setPos(i, j, k));
//
//	                    if (iblockstate.getMaterial() != Material.AIR)
//	                    {
//	                        return PathNodeType.BLOCKED;
//	                    }
//	                }
//	            }
//	        }
//
//	        return PathNodeType.OPEN;
//	    }
//	    
//	    @Override
//	    public FlaggedPathPoint func_224768_a(double p_224768_1_, double p_224768_3_, double p_224768_5_) {
//	        return new FlaggedPathPoint(super.openPoint(MathHelper.floor(p_224768_1_), MathHelper.floor(p_224768_3_), MathHelper.floor(p_224768_5_)));
//	    }
//
//	    @Override
//	    public int func_222859_a(PathPoint[] p_222859_1_, PathPoint p_222859_2_) {
//	        int i = 0;
//	        PathPoint pathpoint = this.openPoint(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z + 1);
//	        PathPoint pathpoint1 = this.openPoint(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z);
//	        PathPoint pathpoint2 = this.openPoint(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z);
//	        PathPoint pathpoint3 = this.openPoint(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z - 1);
//	        PathPoint pathpoint4 = this.openPoint(p_222859_2_.x, p_222859_2_.y + 1, p_222859_2_.z);
//	        PathPoint pathpoint5 = this.openPoint(p_222859_2_.x, p_222859_2_.y - 1, p_222859_2_.z);
//	        if (pathpoint != null && !pathpoint.visited) {
//	           p_222859_1_[i++] = pathpoint;
//	        }
//
//	        if (pathpoint1 != null && !pathpoint1.visited) {
//	           p_222859_1_[i++] = pathpoint1;
//	        }
//
//	        if (pathpoint2 != null && !pathpoint2.visited) {
//	           p_222859_1_[i++] = pathpoint2;
//	        }
//
//	        if (pathpoint3 != null && !pathpoint3.visited) {
//	           p_222859_1_[i++] = pathpoint3;
//	        }
//
//	        if (pathpoint4 != null && !pathpoint4.visited) {
//	           p_222859_1_[i++] = pathpoint4;
//	        }
//
//	        if (pathpoint5 != null && !pathpoint5.visited) {
//	           p_222859_1_[i++] = pathpoint5;
//	        }
//
//	        boolean flag = pathpoint3 == null || pathpoint3.costMalus != 0.0F;
//	        boolean flag1 = pathpoint == null || pathpoint.costMalus != 0.0F;
//	        boolean flag2 = pathpoint2 == null || pathpoint2.costMalus != 0.0F;
//	        boolean flag3 = pathpoint1 == null || pathpoint1.costMalus != 0.0F;
//	        boolean flag4 = pathpoint4 == null || pathpoint4.costMalus != 0.0F;
//	        boolean flag5 = pathpoint5 == null || pathpoint5.costMalus != 0.0F;
//	        if (flag && flag3) {
//	           PathPoint pathpoint6 = this.openPoint(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z - 1);
//	           if (pathpoint6 != null && !pathpoint6.visited) {
//	              p_222859_1_[i++] = pathpoint6;
//	           }
//	        }
//
//	        if (flag && flag2) {
//	           PathPoint pathpoint7 = this.openPoint(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z - 1);
//	           if (pathpoint7 != null && !pathpoint7.visited) {
//	              p_222859_1_[i++] = pathpoint7;
//	           }
//	        }
//
//	        if (flag1 && flag3) {
//	           PathPoint pathpoint8 = this.openPoint(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z + 1);
//	           if (pathpoint8 != null && !pathpoint8.visited) {
//	              p_222859_1_[i++] = pathpoint8;
//	           }
//	        }
//
//	        if (flag1 && flag2) {
//	           PathPoint pathpoint9 = this.openPoint(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z + 1);
//	           if (pathpoint9 != null && !pathpoint9.visited) {
//	              p_222859_1_[i++] = pathpoint9;
//	           }
//	        }
//
//	        if (flag && flag4) {
//	           PathPoint pathpoint10 = this.openPoint(p_222859_2_.x, p_222859_2_.y + 1, p_222859_2_.z - 1);
//	           if (pathpoint10 != null && !pathpoint10.visited) {
//	              p_222859_1_[i++] = pathpoint10;
//	           }
//	        }
//
//	        if (flag1 && flag4) {
//	           PathPoint pathpoint11 = this.openPoint(p_222859_2_.x, p_222859_2_.y + 1, p_222859_2_.z + 1);
//	           if (pathpoint11 != null && !pathpoint11.visited) {
//	              p_222859_1_[i++] = pathpoint11;
//	           }
//	        }
//
//	        if (flag2 && flag4) {
//	           PathPoint pathpoint12 = this.openPoint(p_222859_2_.x + 1, p_222859_2_.y + 1, p_222859_2_.z);
//	           if (pathpoint12 != null && !pathpoint12.visited) {
//	              p_222859_1_[i++] = pathpoint12;
//	           }
//	        }
//
//	        if (flag3 && flag4) {
//	           PathPoint pathpoint13 = this.openPoint(p_222859_2_.x - 1, p_222859_2_.y + 1, p_222859_2_.z);
//	           if (pathpoint13 != null && !pathpoint13.visited) {
//	              p_222859_1_[i++] = pathpoint13;
//	           }
//	        }
//
//	        if (flag && flag5) {
//	           PathPoint pathpoint14 = this.openPoint(p_222859_2_.x, p_222859_2_.y - 1, p_222859_2_.z - 1);
//	           if (pathpoint14 != null && !pathpoint14.visited) {
//	              p_222859_1_[i++] = pathpoint14;
//	           }
//	        }
//
//	        if (flag1 && flag5) {
//	           PathPoint pathpoint15 = this.openPoint(p_222859_2_.x, p_222859_2_.y - 1, p_222859_2_.z + 1);
//	           if (pathpoint15 != null && !pathpoint15.visited) {
//	              p_222859_1_[i++] = pathpoint15;
//	           }
//	        }
//
//	        if (flag2 && flag5) {
//	           PathPoint pathpoint16 = this.openPoint(p_222859_2_.x + 1, p_222859_2_.y - 1, p_222859_2_.z);
//	           if (pathpoint16 != null && !pathpoint16.visited) {
//	              p_222859_1_[i++] = pathpoint16;
//	           }
//	        }
//
//	        if (flag3 && flag5) {
//	           PathPoint pathpoint17 = this.openPoint(p_222859_2_.x - 1, p_222859_2_.y - 1, p_222859_2_.z);
//	           if (pathpoint17 != null && !pathpoint17.visited) {
//	              p_222859_1_[i++] = pathpoint17;
//	           }
//	        }
//
//	        return i;
//	     }
	}
	
	static public class PathNavigatorDragonFlier extends FlyingPathNavigator {
	    public PathNavigatorDragonFlier(MobEntity entitylivingIn, World worldIn) {
	        super(entitylivingIn, worldIn);
	    }
	
//	    @Override
//	    protected PathFinder getPathFinder(int i) {
//	        return new PathFinder(new FlyNodeProcessor(), i);
//	    }
//	
//	    /**
//	     * If on ground or swimming and can swim
//	     */
//	    @Override
//	    protected boolean canNavigate() {
//	        return true;
//	    }
//
//	    @Override
//	    protected Vector3d getEntityPosition() {
//	        return new Vector3d(this.entity.getPosX(), this.entity.getPosY() + (double)this.entity.getHeight() * 0.5D, this.entity.getPosZ());
//	    }
//
//	    @Override
//	    protected void pathFollow() {
//	        Vector3d Vector3d = this.getEntityPosition();
//	        float f = this.entity.getWidth() * this.entity.getWidth();
//	
//	        if (Vector3d.squareDistanceTo(this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex())) < (double)f) {
//	            this.currentPath.incrementPathIndex();
//	        }
//	
//	        for (int j = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); j > this.currentPath.getCurrentPathIndex(); --j) {
//	            Vector3d Vector3d1 = this.currentPath.getVectorFromIndex(this.entity, j);
//	
//	            if (Vector3d1.squareDistanceTo(Vector3d) <= 36.0D && this.isDirectPathBetweenPoints(Vector3d, Vector3d1, 0, 0, 0)) {
//	                this.currentPath.setCurrentPathIndex(j);
//	                break;
//	            }
//	        }
//	
//	        this.checkForStuck(Vector3d);
//	    }
//	
//	    /**
//	     * Trims path data from the end to the first sun covered block
//	     */
//	    @Override
//	    protected void removeSunnyPath() {
//	        super.removeSunnyPath();
//	    }
//	
//	    /**
//	     * Checks if the specified entity can safely walk to the specified location.
//	     */
//	    @Override
//	    protected boolean isDirectPathBetweenPoints(Vector3d posVec31, Vector3d posVec32, int sizeX, int sizeY, int sizeZ) {
//	        RayTraceResult raytraceresult = this.world.rayTraceBlocks(posVec31, new Vector3d(posVec32.x, posVec32.y + (double)this.entity.getHeight() * 0.5D, posVec32.z), false, true, false);
//	        return raytraceresult == null || raytraceresult.getType() == RayTraceResult.Type.MISS;
//	    }
//
//	    @Override
//	    public boolean canEntityStandOnPos(BlockPos pos) {
//	        return !this.world.getBlockState(pos).func_215682_a(this.world, pos, this.entity);
//	    }
	}
	
	public static class DragonEquipmentInventory implements IInventory {
		
		public static interface IChangeListener {
			/**
			 * Called any time a slot has been changed (including deserialization).
			 * This includes when the inventory is cleared. To denote that ALL FIELDS ARE POSSIBLY CHANGING,
			 * clearing sends with a null slot.
			 * @param slot The slot that was changed, or null if all slots were possibly touched (clear, deserialization)
			 * @param oldStack
			 * @param newStack
			 */
			public void onChange(@Nullable DragonEquipmentSlot slot, @Nonnull ItemStack oldStack, @Nonnull ItemStack newStack);
		}
		
		private static final String NBT_LIST = "slots";
		private static final String NBT_SLOT = "slot";
		private static final String NBT_ITEM = "item";
		
		private final Map<DragonEquipmentSlot, ItemStack> slots;
		private IChangeListener listener; // Runtime only
		
		public DragonEquipmentInventory() {
			slots = new NonNullEnumMap<>(DragonEquipmentSlot.class, ItemStack.EMPTY);
		}
		
		public DragonEquipmentInventory(IChangeListener listener) {
			this();
			setListener(listener);
		}
		
		public void setListener(@Nullable IChangeListener listener) {
			this.listener = listener;
		}
		
		public @Nonnull ItemStack getStackInSlot(DragonEquipmentSlot slot) {
			return slots.get(slot);
		}
		
		public void setStackInSlot(DragonEquipmentSlot slot, @Nonnull ItemStack stack) {
			@Nonnull ItemStack oldStack = slots.get(slot);
			slots.put(slot, stack);
			
			if (listener != null) {
				listener.onChange(slot, oldStack, stack);
			}
		}
		
		public void clear() {
			slots.clear();
			if (listener != null) {
				listener.onChange(null, ItemStack.EMPTY, ItemStack.EMPTY);
			}
		}
		
		public CompoundNBT serializeNBT() {
			CompoundNBT tag = new CompoundNBT();
			writeToNBT(tag);
			return tag;
		}
		
		public void writeToNBT(CompoundNBT nbt) {
			ListNBT list = new ListNBT();
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				@Nonnull ItemStack stack = getStackInSlot(slot);
				if (!stack.isEmpty()) {
					CompoundNBT wrapper = new CompoundNBT();
					wrapper.putString(NBT_SLOT, slot.name().toLowerCase());
					wrapper.put(NBT_ITEM, stack.serializeNBT());
					list.add(wrapper);
				}
			}
			
			nbt.put(NBT_LIST, list);
		}
		
		public void readFromNBT(CompoundNBT nbt) {
			this.clear();
			
			ListNBT list = nbt.getList(NBT_LIST, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundNBT wrapper = list.getCompound(i);
				try {
					DragonEquipmentSlot slot = DragonEquipmentSlot.valueOf(wrapper.getString(NBT_SLOT).toUpperCase());
					ItemStack stack = ItemStack.read(wrapper.getCompound(NBT_ITEM));
					//this.setStackInSlot(slot, stack); Don't want to send updates to listener for each item
					slots.put(slot, stack);
				} catch (Exception e) {
					;
				}
			}
			
			if (listener != null) {
				listener.onChange(null, null, null);
			}
		}
		
		public static DragonEquipmentInventory FromNBT(CompoundNBT nbt) {
			DragonEquipmentInventory inventory = new DragonEquipmentInventory();
			inventory.readFromNBT(nbt);
			return inventory;
		}

		@Override
		public int getSizeInventory() {
			return DragonEquipmentSlot.values().length;
		}
		
		protected static final DragonEquipmentSlot GETSLOT(int index) {
			return DragonEquipmentSlot.values()[index];
		}

		@Override
		public @Nonnull ItemStack getStackInSlot(int index) {
			return this.getStackInSlot(GETSLOT(index));
		}

		@Override
		public @Nonnull ItemStack decrStackSize(int index, int count) {
			DragonEquipmentSlot slot = GETSLOT(index);
			ItemStack inSlot = slots.get(slot);
			ItemStack taken = ItemStack.EMPTY;
			if (!inSlot.isEmpty()) {
				taken = inSlot.split(count);
				if (inSlot.getCount() <= 0) {
					inSlot = ItemStack.EMPTY;
				}
				setStackInSlot(slot, inSlot); // Handles dirty and setting null
			}
			
			return taken;
		}

		@Override
		public @Nonnull ItemStack removeStackFromSlot(int index) {
			return slots.remove(GETSLOT(index));
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			this.setStackInSlot(GETSLOT(index), stack);
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public void markDirty() {
			;
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity player) {
			return true;
		}

		@Override
		public void openInventory(PlayerEntity player) {
			;
		}

		@Override
		public void closeInventory(PlayerEntity player) {
			;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (stack.isEmpty()) {
				return true;
			}
			
			if (!(stack.getItem() instanceof DragonArmor)) {
				return false;
			}

			DragonEquipmentSlot slot = GETSLOT(index);
			if (slot == null) {
				return false;
			}
			
			DragonArmor armor = (DragonArmor) stack.getItem();
			return armor.getSlot() == slot;
		}

		@Override
		public boolean isEmpty() {
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				ItemStack stack = slots.get(slot);
				if (!stack.isEmpty()) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public @Nonnull ItemStack getDragonEquipment(DragonEquipmentSlot slot) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public @Nonnull ItemStack getItemStackFromSlot(EquipmentSlotType slot) {
		// Adapt to dragon equipment slot system to take advantage of vanilla's equipment tracking
		// and attribute system
		final DragonEquipmentSlot dragonSlot = DragonEquipmentSlot.FindForSlot(slot);
		
		if (dragonSlot != null) {
			return getDragonEquipment(dragonSlot);
		} else {		
			return super.getItemStackFromSlot(slot);
		}
	}
	
}
