package com.smanzana.nostrummagica.entity.dragon;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.NonNullEnumMap;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;

public abstract class DragonEntity extends Monster {
	
	//protected EntitySize size;
	//protected AxisAlignedBB entityBBOverride;
	
	public DragonEntity(EntityType<? extends DragonEntity> type, Level worldIn) {
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
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return this.getBbHeight() * 0.95F;
    }

    @Override
    public InteractionResult /*processInteract*/ mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

	@Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

	@Override
	public boolean causeFallDamage(float distance, float damageMulti) {
		return false; // No fall damage
	}
	
	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
	}
	
	public abstract boolean isTryingToLand();
	
	public abstract boolean isCasting();
	
	static class DragonFlyMoveHelper extends FlyingMoveControl {
        protected final DragonEntity parentEntity;
        //private double lastDist;
        //private int courseChangeCooldown;

        public DragonFlyMoveHelper(DragonEntity dragon) {
            super(dragon, 5, false);
            this.parentEntity = dragon;
        }
    }
	
	static public class FlyNodeProcessor extends FlyNodeEvaluator {
	}
	
	static public class PathNavigatorDragonFlier extends FlyingPathNavigation {
	    public PathNavigatorDragonFlier(Mob entitylivingIn, Level worldIn) {
	        super(entitylivingIn, worldIn);
	    }
	}
	
	public static class DragonEquipmentInventory implements Container {
		
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
		
		public void clearContent() {
			slots.clear();
			if (listener != null) {
				listener.onChange(null, ItemStack.EMPTY, ItemStack.EMPTY);
			}
		}
		
		public CompoundTag serializeNBT() {
			CompoundTag tag = new CompoundTag();
			writeToNBT(tag);
			return tag;
		}
		
		public void writeToNBT(CompoundTag nbt) {
			ListTag list = new ListTag();
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				@Nonnull ItemStack stack = getStackInSlot(slot);
				if (!stack.isEmpty()) {
					CompoundTag wrapper = new CompoundTag();
					wrapper.putString(NBT_SLOT, slot.name().toLowerCase());
					wrapper.put(NBT_ITEM, stack.serializeNBT());
					list.add(wrapper);
				}
			}
			
			nbt.put(NBT_LIST, list);
		}
		
		public void readFromNBT(CompoundTag nbt) {
			this.clearContent();
			
			ListTag list = nbt.getList(NBT_LIST, Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag wrapper = list.getCompound(i);
				try {
					DragonEquipmentSlot slot = DragonEquipmentSlot.valueOf(wrapper.getString(NBT_SLOT).toUpperCase());
					ItemStack stack = ItemStack.of(wrapper.getCompound(NBT_ITEM));
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
		
		public static DragonEquipmentInventory FromNBT(CompoundTag nbt) {
			DragonEquipmentInventory inventory = new DragonEquipmentInventory();
			inventory.readFromNBT(nbt);
			return inventory;
		}

		@Override
		public int getContainerSize() {
			return DragonEquipmentSlot.values().length;
		}
		
		protected static final DragonEquipmentSlot GETSLOT(int index) {
			return DragonEquipmentSlot.values()[index];
		}

		@Override
		public @Nonnull ItemStack getItem(int index) {
			return this.getStackInSlot(GETSLOT(index));
		}

		@Override
		public @Nonnull ItemStack removeItem(int index, int count) {
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
		public @Nonnull ItemStack removeItemNoUpdate(int index) {
			return slots.remove(GETSLOT(index));
		}

		@Override
		public void setItem(int index, ItemStack stack) {
			this.setStackInSlot(GETSLOT(index), stack);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}

		@Override
		public void setChanged() {
			;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}

		@Override
		public void startOpen(Player player) {
			;
		}

		@Override
		public void stopOpen(Player player) {
			;
		}

		@Override
		public boolean canPlaceItem(int index, ItemStack stack) {
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
	public @Nonnull ItemStack getItemBySlot(EquipmentSlot slot) {
		// Adapt to dragon equipment slot system to take advantage of vanilla's equipment tracking
		// and attribute system
		final DragonEquipmentSlot dragonSlot = DragonEquipmentSlot.FindForSlot(slot);
		
		if (dragonSlot != null) {
			return getDragonEquipment(dragonSlot);
		} else {		
			return super.getItemBySlot(slot);
		}
	}
	
	protected static final Builder BuildBaseDragonAttributes() {
		return Monster.createMonsterAttributes();
	}
	
}
