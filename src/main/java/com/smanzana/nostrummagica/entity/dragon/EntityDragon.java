package com.smanzana.nostrummagica.entity.dragon;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class EntityDragon extends EntityMob implements ILoreTagged {
	
	public EntityDragon(World worldIn) {
        super(worldIn);
    }
	
	protected void setSize(float width, float length, float height) {
		if (width != this.width || height != this.height) {
			float f = this.width;
			this.width = width;
			this.height = height;
			AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
			this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.width, axisalignedbb.minY + (double)this.height, axisalignedbb.minZ + (double)length));

			if (this.width > f && !this.firstUpdate && !this.world.isRemote) {
				this.move(MoverType.SELF, (double)(f - this.width), 0.0D, (double)(f - length));
			}
		}
	}
    
	/*
	 * TODO: Make a 'FlyingDragon' abstract class. Move bite and slash data things to this class.
	 * Rething how to diversify.
	 */
	public abstract void slash(EntityLivingBase target);
	public abstract void bite(EntityLivingBase target);
	
	public void dragonJump() {
		; // Default, do nothing
	}
    

    protected SoundEvent getHurtSound() {
        return NostrumMagicaSounds.DRAGON_LAND_HURT.getEvent();
    }

    protected SoundEvent getDeathSound() {
    	return NostrumMagicaSounds.DRAGON_DEATH.getEvent();
    }
    
    protected SoundEvent getAmbientSound() {
    	return NostrumMagicaSounds.DRAGON_IDLE.getEvent();
    }
    
    protected SoundEvent getAttackSound() {
    	return NostrumMagicaSounds.DRAGON_BITE.getEvent();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 2F;
    }

    public float getEyeHeight()
    {
        return this.height * 0.95F;
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack)
    {
        return false;
    }

    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return false;
    }

	@Override
	public void fall(float distance, float damageMulti) {
		; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, IBlockState stae, BlockPos pos) {
		
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	public abstract boolean isTryingToLand();
	
	static class DragonFlyMoveHelper extends EntityMoveHelper
    {
        private final EntityDragon parentEntity;
        private double lastDist;
        private int courseChangeCooldown;

        public DragonFlyMoveHelper(EntityDragon dragon)
        {
            super(dragon);
            this.parentEntity = dragon;
        }

        public void onUpdateMoveHelper()
        {
//        	if (this.action == EntityMoveHelper.Action.STRAFE)
//    		{
//    			float f = (float)this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
//    			float f1 = (float)this.speed * f;
//    			float f2 = this.moveForward;
//    			float f3 = this.moveStrafe;
//    			float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
//
//    			if (f4 < 1.0F)
//    			{
//    				f4 = 1.0F;
//    			}
//
//    			f4 = f1 / f4;
//    			f2 = f2 * f4;
//    			f3 = f3 * f4;
//    			float f5 = MathHelper.sin(this.entity.rotationYaw * 0.017453292F);
//    			float f6 = MathHelper.cos(this.entity.rotationYaw * 0.017453292F);
//    			float f7 = f2 * f6 - f3 * f5;
//    			float f8 = f3 * f6 + f2 * f5;
//    			PathNavigate pathnavigate = this.entity.getNavigator();
//
//    			if (pathnavigate != null)
//    			{
//    				NodeProcessor nodeprocessor = pathnavigate.getNodeProcessor();
//
//    				if (nodeprocessor != null && nodeprocessor.getPathNodeType(this.entity.world, MathHelper.floor(this.entity.posX + (double)f7), MathHelper.floor(this.entity.posY), MathHelper.floor(this.entity.posZ + (double)f8)) != PathNodeType.OPEN)
//    				{
//    					this.moveForward = 1.0F;
//    					this.moveStrafe = 0.0F;
//    					f1 = f;
//    				}
//    			}
//
//    			this.entity.setAIMoveSpeed(f * 3);
//    			this.entity.setMoveForward(this.moveForward);
//    			this.entity.setMoveStrafing(this.moveStrafe);
//    			this.action = EntityMoveHelper.Action.WAIT;
//    		}
//        	else 
        	if (this.action == EntityMoveHelper.Action.MOVE_TO)
            {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                d3 = (double)MathHelper.sqrt(d3);
                
                if (Math.abs(d3) < 1) {
                	lastDist = 0.0D;
                	this.action = EntityMoveHelper.Action.WAIT;
                } else if (lastDist != 0.0D && Math.abs(lastDist - d3) < 0.05) {
                	courseChangeCooldown--;
                } else {
                	courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
                }
                
                if (courseChangeCooldown <= 0) {
                	lastDist = 0.0D;
                	this.action = EntityMoveHelper.Action.WAIT;
                } else {
                	float speed = (float) this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                	speed *= 3f;
	                this.parentEntity.motionX = (d0 / d3) * speed;
	                this.parentEntity.motionY = (d1 / d3) * speed;
	                this.parentEntity.motionZ = (d2 / d3) * speed;
	                
	                lastDist = d3;
	                
	                float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
	                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
                }
            }
        }
    }
	
	static public class FlyNodeProcessor extends NodeProcessor
	{
	    public PathPoint getStart()
	    {
	        return this.openPoint(MathHelper.floor(this.entity.getEntityBoundingBox().minX), MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D), MathHelper.floor(this.entity.getEntityBoundingBox().minZ));
	    }

	    /**
	     * Returns PathPoint for given coordinates
	     */
	    public PathPoint getPathPointToCoords(double x, double y, double z)
	    {
	        return this.openPoint(MathHelper.floor(x - (double)(this.entity.width / 2.0F)), MathHelper.floor(y + 0.5D), MathHelper.floor(z - (double)(this.entity.width / 2.0F)));
	    }

	    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance)
	    {
	        int i = 0;

	        for (EnumFacing enumfacing : EnumFacing.values())
	        {
	            PathPoint pathpoint = this.getAirNode(currentPoint.x + enumfacing.getFrontOffsetX(), currentPoint.y + enumfacing.getFrontOffsetY(), currentPoint.z + enumfacing.getFrontOffsetZ());

	            if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance)
	            {
	                pathOptions[i++] = pathpoint;
	            }
	        }

	        return i;
	    }

	    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn)
	    {
	        return PathNodeType.OPEN;
	    }

	    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z)
	    {
	        return PathNodeType.OPEN;
	    }

	    @Nullable
	    private PathPoint getAirNode(int p_186328_1_, int p_186328_2_, int p_186328_3_)
	    {
	        PathNodeType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
	        return pathnodetype == PathNodeType.OPEN ? this.openPoint(p_186328_1_, p_186328_2_, p_186328_3_) : null;
	    }

	    private PathNodeType isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_)
	    {
	        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

	        for (int i = p_186327_1_; i < p_186327_1_ + this.entitySizeX; ++i)
	        {
	            for (int j = p_186327_2_; j < p_186327_2_ + this.entitySizeY; ++j)
	            {
	                for (int k = p_186327_3_; k < p_186327_3_ + this.entitySizeZ; ++k)
	                {
	                    IBlockState iblockstate = this.blockaccess.getBlockState(blockpos$mutableblockpos.setPos(i, j, k));

	                    if (iblockstate.getMaterial() != Material.AIR)
	                    {
	                        return PathNodeType.BLOCKED;
	                    }
	                }
	            }
	        }

	        return PathNodeType.OPEN;
	    }
	}
	
	static public class PathNavigateDragonFlier extends PathNavigate
	{
	    public PathNavigateDragonFlier(EntityLiving entitylivingIn, World worldIn)
	    {
	        super(entitylivingIn, worldIn);
	    }
	
	    protected PathFinder getPathFinder()
	    {
	        return new PathFinder(new FlyNodeProcessor());
	    }
	
	    /**
	     * If on ground or swimming and can swim
	     */
	    protected boolean canNavigate()
	    {
	        return true;
	    }
	
	    protected Vec3d getEntityPosition()
	    {
	        return new Vec3d(this.entity.posX, this.entity.posY + (double)this.entity.height * 0.5D, this.entity.posZ);
	    }
	
	    protected void pathFollow()
	    {
	        Vec3d vec3d = this.getEntityPosition();
	        float f = this.entity.width * this.entity.width;
	
	        if (vec3d.squareDistanceTo(this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex())) < (double)f)
	        {
	            this.currentPath.incrementPathIndex();
	        }
	
	        for (int j = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); j > this.currentPath.getCurrentPathIndex(); --j)
	        {
	            Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, j);
	
	            if (vec3d1.squareDistanceTo(vec3d) <= 36.0D && this.isDirectPathBetweenPoints(vec3d, vec3d1, 0, 0, 0))
	            {
	                this.currentPath.setCurrentPathIndex(j);
	                break;
	            }
	        }
	
	        this.checkForStuck(vec3d);
	    }
	
	    /**
	     * Trims path data from the end to the first sun covered block
	     */
	    protected void removeSunnyPath()
	    {
	        super.removeSunnyPath();
	    }
	
	    /**
	     * Checks if the specified entity can safely walk to the specified location.
	     */
	    protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ)
	    {
	        RayTraceResult raytraceresult = this.world.rayTraceBlocks(posVec31, new Vec3d(posVec32.x, posVec32.y + (double)this.entity.height * 0.5D, posVec32.z), false, true, false);
	        return raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS;
	    }
	
	    public boolean canEntityStandOnPos(BlockPos pos)
	    {
	        return !this.world.getBlockState(pos).isFullBlock();
	    }
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
			public void onChange(@Nullable DragonEquipmentSlot slot, @Nullable ItemStack oldStack, @Nullable ItemStack newStack);
		}
		
		private static final String NBT_LIST = "slots";
		private static final String NBT_SLOT = "slot";
		private static final String NBT_ITEM = "item";
		
		private final Map<DragonEquipmentSlot, ItemStack> slots;
		private IChangeListener listener; // Runtime only
		
		public DragonEquipmentInventory() {
			slots = new EnumMap<>(DragonEquipmentSlot.class);
		}
		
		public DragonEquipmentInventory(IChangeListener listener) {
			this();
			setListener(listener);
		}
		
		public void setListener(@Nullable IChangeListener listener) {
			this.listener = listener;
		}
		
		public @Nullable ItemStack getStackInSlot(DragonEquipmentSlot slot) {
			return slots.get(slot);
		}
		
		public void setStackInSlot(DragonEquipmentSlot slot, @Nullable ItemStack stack) {
			@Nullable ItemStack oldStack = slots.get(slot);
			slots.put(slot, stack);
			
			if (listener != null) {
				listener.onChange(slot, oldStack, stack);
			}
		}
		
		public void clear() {
			slots.clear();
			if (listener != null) {
				listener.onChange(null, null, null);
			}
		}
		
		public NBTTagCompound serializeNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			writeToNBT(tag);
			return tag;
		}
		
		public void writeToNBT(NBTTagCompound nbt) {
			NBTTagList list = new NBTTagList();
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				@Nullable ItemStack stack = getStackInSlot(slot);
				if (stack != null) {
					NBTTagCompound wrapper = new NBTTagCompound();
					wrapper.setString(NBT_SLOT, slot.name().toLowerCase());
					wrapper.setTag(NBT_ITEM, stack.serializeNBT());
					list.appendTag(wrapper);
				}
			}
			
			nbt.setTag(NBT_LIST, list);
		}
		
		public void readFromNBT(NBTTagCompound nbt) {
			this.clear();
			
			NBTTagList list = nbt.getTagList(NBT_LIST, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound wrapper = list.getCompoundTagAt(i);
				try {
					DragonEquipmentSlot slot = DragonEquipmentSlot.valueOf(wrapper.getString(NBT_SLOT).toUpperCase());
					ItemStack stack = new ItemStack(wrapper.getCompoundTag(NBT_ITEM));
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
		
		public static DragonEquipmentInventory FromNBT(NBTTagCompound nbt) {
			DragonEquipmentInventory inventory = new DragonEquipmentInventory();
			inventory.readFromNBT(nbt);
			return inventory;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}

		@Override
		public ITextComponent getDisplayName() {
			return null;
		}

		@Override
		public int getSizeInventory() {
			return DragonEquipmentSlot.values().length;
		}
		
		protected static final DragonEquipmentSlot GETSLOT(int index) {
			return DragonEquipmentSlot.values()[index];
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			return this.getStackInSlot(GETSLOT(index));
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			DragonEquipmentSlot slot = GETSLOT(index);
			ItemStack inSlot = slots.get(slot);
			ItemStack taken = null;
			if (inSlot != null) {
				taken = inSlot.splitStack(count);
				if (inSlot.getCount() <= 0) {
					inSlot = null;
				}
				setStackInSlot(slot, inSlot); // Handles dirty and setting null
			}
			
			return taken;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
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
		public boolean isUsableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void openInventory(EntityPlayer player) {
			;
		}

		@Override
		public void closeInventory(EntityPlayer player) {
			;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (stack == null) {
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
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {
			;
		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				ItemStack stack = slots.get(slot);
				if (stack != null && stack != ItemStack.EMPTY) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public @Nullable ItemStack getDragonEquipment(DragonEquipmentSlot slot) {
		return null;
	}
	
	@Override
	public @Nullable ItemStack getItemStackFromSlot(EntityEquipmentSlot slot) {
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
