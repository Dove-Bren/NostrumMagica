package com.smanzana.nostrummagica.utils;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

public class Inventories {

	private static final ItemStack attemptAddToInventory(IInventory inventory, @Nullable ItemStack stack, boolean commit) {
    	if (stack == null) {
    		return null;
    	}
    	
    	ItemStack itemstack = stack.copy();
    	int emptyPos = -1;

    	for (int i = 0; i < inventory.getSizeInventory(); ++i) {
    		if (!inventory.isItemValidForSlot(i, itemstack)) {
    			continue;
    		}
    		
            ItemStack itemstack1 = inventory.getStackInSlot(i);

            if (itemstack1 == null) {
            	// If just looking to see if it'll fit, return success.
            	if (!commit) {
            		return null;
            	}
            	
            	// Otherwise, mark the first empty spot but keep looking for places to stack
            	if (emptyPos == -1) {
            		emptyPos = i;
            	}
            	
            	continue;
            }
            
            if (ItemStacks.stacksMatch(itemstack, itemstack1)) {
            	// stacks appear to match. Deduct stack size
            	int room = itemstack1.getMaxStackSize() - itemstack1.stackSize;
            	if (room >= itemstack.stackSize) {
            		if (commit) {
	            		itemstack1.stackSize += itemstack.stackSize;
	            		inventory.markDirty();
            		}
            		return null;
            	} else if (room > 0) {
            		if (commit) {
	            		itemstack1.stackSize += room;
	            		inventory.markDirty();
            		}
            		itemstack.stackSize -= room;
            	}
            }
        }
    	
    	// If we found an empty spot, add it now
    	if (emptyPos != -1) {
    		if (commit) {
                inventory.setInventorySlotContents(emptyPos, itemstack);
                inventory.markDirty();
        	}
            return null;
    	}

        return itemstack;
    }
	 
	public static final ItemStack addItem(IInventory inventory, @Nullable ItemStack stack) {
		return attemptAddToInventory(inventory, stack, true);
	}
	
	public static final ItemStack addItem(ItemStack[] inventory, @Nullable ItemStack stack) {
		return addItem(new ItemStackArrayWrapper(inventory), stack);
	}
	
	public static final boolean canFit(IInventory inventory, @Nullable ItemStack stack) {
		return null == attemptAddToInventory(inventory, stack, false);
	}
	
	public static final boolean canFit(ItemStack[] inventory, @Nullable ItemStack stack) {
		return canFit(new ItemStackArrayWrapper(inventory), stack);
	}
	
	public static final ItemStack simulateAddItem(IInventory inventory, @Nullable ItemStack stack) {
		return attemptAddToInventory(inventory, stack, false);
	}
	
	private static final ItemStack attemptRemoveFromInventory(IInventory inventory, @Nullable ItemStack stack, boolean commit) {
		if (stack == null) {
    		return null;
    	}
    	
    	ItemStack itemstack = stack.copy();
    	
    	for (int i = inventory.getSizeInventory() - 1; i >= 0 ; i--) {
    		ItemStack inSlot = inventory.getStackInSlot(i);
    		
    		if (inSlot == null) {
    			continue;
    		}
    		
            if (ItemStacks.stacksMatch(itemstack, inSlot)) {
            	// stacks appear to match. Deduct stack size
            	if (inSlot.stackSize > itemstack.stackSize) {
            		if (commit) {
	            		inSlot.stackSize -= itemstack.stackSize;
	            		inventory.markDirty();
            		}
            		return null;
            	} else {
            		itemstack.stackSize -= inSlot.stackSize;
            		if (commit) {
            			inventory.removeStackFromSlot(i);
	            		inventory.markDirty();
            		}
            		
            		if (itemstack.stackSize <= 0) {
            			return null;
            		}
            	}
           	}
        }

        return itemstack;
	}
	
	public static final boolean contains(IInventory inventory, @Nullable ItemStack items) {
		return null == attemptRemoveFromInventory(inventory, items, false);
	}
	
	public static final boolean contains(ItemStack[] inventory, @Nullable ItemStack items) {
		return contains(new ItemStackArrayWrapper(inventory), items);
	}
	
	public static final ItemStack remove(IInventory inventory, @Nullable ItemStack items) {
		return attemptRemoveFromInventory(inventory, items, true);
	}
	
	public static final ItemStack remove(ItemStack[] inventory, @Nullable ItemStack items) {
		return remove(new ItemStackArrayWrapper(inventory), items);
	}
	
	public static final NBTBase serializeInventory(IInventory inv) {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			@Nullable ItemStack stack = inv.getStackInSlot(i);
			if (stack != null) {
				list.appendTag(stack.serializeNBT());
			} else {
				list.appendTag(new NBTTagCompound());
			}
		}
		
		return list;
	}
	
	public static final boolean deserializeInventory(IInventory base, NBTBase nbt) {
		if (base == null || nbt == null || !(nbt instanceof NBTTagList)) {
			return false;
		}
		
		base.clear();
		NBTTagList list = (NBTTagList) nbt;
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			@Nullable ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
			base.setInventorySlotContents(i, stack);
		}
		
		return true;
	}
	
	// TODO make a pool of these and implement a 'set' interface to avoid allocating and deallocing these
	public static class ItemStackArrayWrapper implements IInventory {

		private final ItemStack[] array;
		
		public ItemStackArrayWrapper(ItemStack array[]) {
			this.array = array;
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
			return array.length;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			return array[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			ItemStack split =  ItemStackHelper.getAndSplit(array, index, count);
			markDirty();
			return split;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			ItemStack stack = array[index];
			array[index] = null;
			markDirty();
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			array[index] = stack;
			markDirty();
		}

		@Override
		public int getInventoryStackLimit() {
			return 256;
		}

		@Override
		public void markDirty() {
			;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return false;
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
			return true;
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
		public void clear() {
			for (int i = 0; i < array.length; i++) {
				array[i] = null;
			}
			markDirty();
		}
		
	}
}
