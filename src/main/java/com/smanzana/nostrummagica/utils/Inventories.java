package com.smanzana.nostrummagica.utils;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

public class Inventories {

	private static final @Nonnull ItemStack attemptAddToInventory(IInventory inventory, @Nonnull ItemStack stack, boolean commit) {
    	if (stack.isEmpty()) {
    		return ItemStack.EMPTY;
    	}
    	
    	ItemStack itemstack = stack.copy();
    	int emptyPos = -1;

    	for (int i = 0; i < inventory.getSizeInventory(); ++i) {
    		if (!inventory.isItemValidForSlot(i, itemstack)) {
    			continue;
    		}
    		
            ItemStack itemstack1 = inventory.getStackInSlot(i);

            if (itemstack1.isEmpty()) {
            	// If just looking to see if it'll fit, return success.
            	if (!commit) {
            		return ItemStack.EMPTY;
            	}
            	
            	// Otherwise, mark the first empty spot but keep looking for places to stack
            	if (emptyPos == -1) {
            		emptyPos = i;
            	}
            	
            	continue;
            }
            
            if (ItemStacks.stacksMatch(itemstack, itemstack1)) {
            	// stacks appear to match. Deduct stack size
            	int room = itemstack1.getMaxStackSize() - itemstack1.getCount();
            	if (room >= itemstack.getCount()) {
            		if (commit) {
	            		itemstack1.grow(itemstack.getCount());
	            		inventory.markDirty();
            		}
            		return ItemStack.EMPTY;
            	} else if (room > 0) {
            		if (commit) {
	            		itemstack1.grow(room);
	            		inventory.markDirty();
            		}
            		itemstack.shrink(room);
            	}
            }
        }
    	
    	// If we found an empty spot, add it now
    	if (emptyPos != -1) {
    		if (commit) {
                inventory.setInventorySlotContents(emptyPos, itemstack);
                inventory.markDirty();
        	}
            return ItemStack.EMPTY;
    	}

        return itemstack;
    }
	
	private static final @Nonnull ItemStack attemptAddToInventory(IItemHandler handler, @Nonnull ItemStack stack, boolean commit) {
    	if (stack.isEmpty()) {
    		return ItemStack.EMPTY;
    	}
    	
    	stack = stack.copy();

    	for (int i = 0; i < handler.getSlots() && !stack.isEmpty(); ++i) {
    		stack = handler.insertItem(i, stack, !commit);
        }
    	
        return stack;
    }
	 
	public static final @Nonnull ItemStack addItem(IInventory inventory, @Nonnull ItemStack stack) {
		return attemptAddToInventory(inventory, stack, true);
	}
	
	public static final @Nonnull ItemStack addItem(ItemStack[] inventory, @Nonnull ItemStack stack) {
		return addItem(new ItemStackArrayWrapper(inventory), stack);
	}
	
	public static final @Nonnull ItemStack addItem(IItemHandler handler, @Nonnull ItemStack stack) {
		return attemptAddToInventory(handler, stack, true);
	}
	
	public static final boolean canFit(IInventory inventory, @Nonnull ItemStack stack) {
		return attemptAddToInventory(inventory, stack, false).isEmpty();
	}
	
	public static final boolean canFit(ItemStack[] inventory, @Nonnull ItemStack stack) {
		return canFit(new ItemStackArrayWrapper(inventory), stack);
	}
	
	public static final boolean canFit(IItemHandler handler, @Nonnull ItemStack stack) {
		return attemptAddToInventory(handler, stack, false).isEmpty();
	}
	
	public static final @Nonnull ItemStack simulateAddItem(IInventory inventory, @Nonnull ItemStack stack) {
		return attemptAddToInventory(inventory, stack, false);
	}
	
	public static final @Nonnull ItemStack simulateAddItem(IItemHandler handler, @Nonnull ItemStack stack) {
		return attemptAddToInventory(handler, stack, false);
	}
	
	private static final @Nonnull ItemStack attemptRemoveFromInventory(IInventory inventory, @Nonnull ItemStack stack, boolean commit) {
		if (stack.isEmpty()) {
    		return ItemStack.EMPTY;
    	}
    	
    	ItemStack itemstack = stack.copy();
    	
    	for (int i = inventory.getSizeInventory() - 1; i >= 0 ; i--) {
    		ItemStack inSlot = inventory.getStackInSlot(i);
    		
    		if (inSlot.isEmpty()) {
    			continue;
    		}
    		
            if (ItemStacks.stacksMatch(itemstack, inSlot)) {
            	// stacks appear to match. Deduct stack size
            	if (inSlot.getCount() > itemstack.getCount()) {
            		if (commit) {
	            		inSlot.shrink(itemstack.getCount());
	            		inventory.markDirty();
            		}
            		return ItemStack.EMPTY;
            	} else {
            		itemstack.shrink(inSlot.getCount());
            		if (commit) {
            			inventory.removeStackFromSlot(i);
	            		inventory.markDirty();
            		}
            		
            		if (itemstack.getCount() <= 0) {
            			return ItemStack.EMPTY;
            		}
            	}
           	}
        }

        return itemstack;
	}
	
	public static final boolean contains(IInventory inventory, @Nonnull ItemStack items) {
		return attemptRemoveFromInventory(inventory, items, false).isEmpty();
	}
	
	public static final boolean contains(ItemStack[] inventory, @Nonnull ItemStack items) {
		return contains(new ItemStackArrayWrapper(inventory), items);
	}
	
	public static final @Nonnull ItemStack remove(IInventory inventory, @Nonnull ItemStack items) {
		return attemptRemoveFromInventory(inventory, items, true);
	}
	
	public static final @Nonnull ItemStack remove(ItemStack[] inventory, @Nonnull ItemStack items) {
		return remove(new ItemStackArrayWrapper(inventory), items);
	}
	
	public static final INBTBase serializeInventory(IInventory inv) {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			@Nonnull ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				list.add(stack.serializeNBT());
			} else {
				list.add(new NBTTagCompound());
			}
		}
		
		return list;
	}
	
	public static final boolean deserializeInventory(IInventory base, INBTBase nbt) {
		if (base == null) {
			return false;
		}
		
		base.clear();
		
		if (nbt != null && nbt instanceof NBTTagList) {
			NBTTagList list = (NBTTagList) nbt;
			for (int i = 0; i < list.size(); i++) {
				NBTTagCompound tag = list.getCompound(i);
				@Nonnull ItemStack stack = ItemStack.read(tag);
				base.setInventorySlotContents(i, stack);
			}
		}
		
		return true;
	}
	
	// TODO make a pool of these and implement a 'set' interface to avoid allocating and deallocing these
	public static class ItemStackArrayWrapper implements IInventory {

		private final @Nonnull ItemStack[] array;
		
		public ItemStackArrayWrapper(ItemStack array[]) {
			this.array = array;
		}
		
		@Override
		public ITextComponent getName() {
			return null;
		}
		
		@Override
		public ITextComponent getCustomName() {
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
		public @Nonnull ItemStack getStackInSlot(int index) {
			return array[index];
		}

		@Override
		public @Nonnull ItemStack decrStackSize(int index, int count) {
			ItemStack split = ItemStack.EMPTY;
			if (index < array.length) {
				split = array[index].split(count);
			}
			markDirty();
			return split;
		}

		@Override
		public @Nonnull ItemStack removeStackFromSlot(int index) {
			ItemStack stack = array[index];
			array[index] = ItemStack.EMPTY;
			markDirty();
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
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
				array[i] = ItemStack.EMPTY;
			}
			markDirty();
		}

		@Override
		public boolean isEmpty() {
			for (int i = 0; i < array.length; i++) {
				if (!array[i].isEmpty()) {
					return false;
				}
			}
			
			return true;
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return true;
		}
		
	}
}
