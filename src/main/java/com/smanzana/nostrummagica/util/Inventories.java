package com.smanzana.nostrummagica.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class Inventories {

	private static final @Nonnull ItemStack attemptAddToInventory(IInventory inventory, @Nonnull ItemStack stack, boolean commit) {
    	if (stack.isEmpty()) {
    		return ItemStack.EMPTY;
    	}
    	
    	ItemStack itemstack = stack.copy();
    	int emptyPos = -1;

    	for (int i = 0; i < inventory.getContainerSize(); ++i) {
    		if (!inventory.canPlaceItem(i, itemstack)) {
    			continue;
    		}
    		
            ItemStack itemstack1 = inventory.getItem(i);

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
	            		inventory.setChanged();
            		}
            		return ItemStack.EMPTY;
            	} else if (room > 0) {
            		if (commit) {
	            		itemstack1.grow(room);
	            		inventory.setChanged();
            		}
            		itemstack.shrink(room);
            	}
            }
        }
    	
    	// If we found an empty spot, add it now
    	if (emptyPos != -1) {
    		if (commit) {
                inventory.setItem(emptyPos, itemstack);
                inventory.setChanged();
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
    	
    	for (int i = inventory.getContainerSize() - 1; i >= 0 ; i--) {
    		ItemStack inSlot = inventory.getItem(i);
    		
    		if (inSlot.isEmpty()) {
    			continue;
    		}
    		
            if (ItemStacks.stacksMatch(itemstack, inSlot)) {
            	// stacks appear to match. Deduct stack size
            	if (inSlot.getCount() > itemstack.getCount()) {
            		if (commit) {
	            		inSlot.shrink(itemstack.getCount());
	            		inventory.setChanged();
            		}
            		return ItemStack.EMPTY;
            	} else {
            		itemstack.shrink(inSlot.getCount());
            		if (commit) {
            			inventory.removeItemNoUpdate(i);
	            		inventory.setChanged();
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
	
	public static final INBT serializeInventory(IInventory inv) {
		ListNBT list = new ListNBT();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			@Nonnull ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				list.add(stack.serializeNBT());
			} else {
				list.add(new CompoundNBT());
			}
		}
		
		return list;
	}
	
	public static final boolean deserializeInventory(IInventory base, INBT nbt) {
		if (base == null) {
			return false;
		}
		
		base.clearContent();
		
		if (nbt != null && nbt instanceof ListNBT) {
			ListNBT list = (ListNBT) nbt;
			for (int i = 0; i < list.size(); i++) {
				CompoundNBT tag = list.getCompound(i);
				@Nonnull ItemStack stack = ItemStack.of(tag);
				base.setItem(i, stack);
			}
		}
		
		return true;
	}
	
	public static final int getPlayerHandSlotIndex(PlayerInventory inv, Hand hand) {
		// Hardcoded stuff
		if (hand == Hand.MAIN_HAND) {
			return inv.selected;
		} else {
			return 40;
		}
	}
	
	public static final boolean attemptAddToTile(Iterable<ItemStack> itemsToAdd, BlockState state, TileEntity te, Direction direction) {
		boolean attemptedAny = false;
		if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).isPresent()) {
			@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);
			
			if (handler != null) {
				attemptedAny = true;
				Iterator<ItemStack> it = itemsToAdd.iterator();
				while (it.hasNext()) {
					ItemStack slotStack = it.next();
					if (slotStack.isEmpty()) {
						continue;
					}
					
					ItemStack leftover = addItem(handler, slotStack);
					if (leftover.isEmpty() || leftover.getCount() != slotStack.getCount()) {
						slotStack.setCount(leftover.isEmpty() ? 0 : leftover.getCount());
					}
				}
			}
		} else if (te instanceof IInventory) {
			
			IInventory inv = (IInventory) te;
			
			// Special cast for stupid chests :P
			if (te instanceof ChestTileEntity) {
				if (state != null && state.getBlock() instanceof ChestBlock) {
					inv = ChestBlock.getContainer((ChestBlock) state.getBlock(), state, te.getLevel(), te.getBlockPos(), true);
				}
			}
			
			attemptedAny = true;
			Iterator<ItemStack> it = itemsToAdd.iterator();
			while (it.hasNext()) {
				ItemStack slotStack = it.next();
				if (slotStack.isEmpty()) {
					continue;
				}
				
				ItemStack leftover = addItem(inv, slotStack);
				if (leftover.isEmpty() || leftover.getCount() != slotStack.getCount()) {
					slotStack.setCount(leftover.isEmpty() ? 0 : leftover.getCount());
				}
			}
		}
		
		return attemptedAny;
	}
	
	// TODO make a pool of these and implement a 'set' interface to avoid allocating and deallocing these
	public static class ItemStackArrayWrapper implements IInventory {

		private final @Nonnull ItemStack[] array;
		
		public ItemStackArrayWrapper(ItemStack array[]) {
			this.array = array;
		}
		
		@Override
		public int getContainerSize() {
			return array.length;
		}

		@Override
		public @Nonnull ItemStack getItem(int index) {
			return array[index];
		}

		@Override
		public @Nonnull ItemStack removeItem(int index, int count) {
			ItemStack split = ItemStack.EMPTY;
			if (index < array.length) {
				split = array[index].split(count);
			}
			setChanged();
			return split;
		}

		@Override
		public @Nonnull ItemStack removeItemNoUpdate(int index) {
			ItemStack stack = array[index];
			array[index] = ItemStack.EMPTY;
			setChanged();
			return stack;
		}

		@Override
		public void setItem(int index, @Nonnull ItemStack stack) {
			array[index] = stack;
			setChanged();
		}

		@Override
		public int getMaxStackSize() {
			return 256;
		}

		@Override
		public void setChanged() {
			;
		}

		@Override
		public void startOpen(PlayerEntity player) {
			;
		}

		@Override
		public void stopOpen(PlayerEntity player) {
			;
		}

		@Override
		public boolean canPlaceItem(int index, ItemStack stack) {
			return true;
		}

		@Override
		public void clearContent() {
			for (int i = 0; i < array.length; i++) {
				array[i] = ItemStack.EMPTY;
			}
			setChanged();
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
		public boolean stillValid(PlayerEntity player) {
			return true;
		}
		
	}
	
	public static class IterableInventoryWrapper implements Iterable<ItemStack> {
		
		private final IInventory inventory;
		
		public IterableInventoryWrapper(IInventory inventory) {
			this.inventory = inventory;
		}

		@Override
		public Iterator<ItemStack> iterator() {
			return new Iterator<ItemStack>() {
				
				int i = 0;

				@Override
				public boolean hasNext() {
					return i < inventory.getContainerSize();
				}

				@Override
				public ItemStack next() {
					if (hasNext()) {
						return inventory.getItem(i++);
					}
					
					throw new NoSuchElementException();
				}
				
			};
		}
		
	}
}
