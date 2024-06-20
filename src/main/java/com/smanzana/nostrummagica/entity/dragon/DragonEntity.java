package com.smanzana.nostrummagica.entity.dragon;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.NonNullEnumMap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class DragonEntity extends MonsterEntity {
	
	//protected EntitySize size;
	//protected AxisAlignedBB entityBBOverride;
	
	public DragonEntity(EntityType<? extends DragonEntity> type, World worldIn) {
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
    public ActionResultType /*processInteract*/ func_230254_b_(PlayerEntity player, Hand hand) {
        return ActionResultType.PASS;
    }

	@Override
    public boolean canBeLeashedTo(PlayerEntity player) {
        return false;
    }

	@Override
	public boolean onLivingFall(float distance, float damageMulti) {
		return false; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
	}
	
	public abstract boolean isTryingToLand();
	
	public abstract boolean isCasting();
	
	static class DragonFlyMoveHelper extends FlyingMovementController {
        protected final DragonEntity parentEntity;
        //private double lastDist;
        //private int courseChangeCooldown;

        public DragonFlyMoveHelper(DragonEntity dragon) {
            super(dragon, 5, false);
            this.parentEntity = dragon;
        }
    }
	
	static public class FlyNodeProcessor extends FlyingNodeProcessor {
	}
	
	static public class PathNavigatorDragonFlier extends FlyingPathNavigator {
	    public PathNavigatorDragonFlier(MobEntity entitylivingIn, World worldIn) {
	        super(entitylivingIn, worldIn);
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
	
	protected static final MutableAttribute BuildBaseDragonAttributes() {
		return MonsterEntity.func_234295_eP_();
	}
	
}
