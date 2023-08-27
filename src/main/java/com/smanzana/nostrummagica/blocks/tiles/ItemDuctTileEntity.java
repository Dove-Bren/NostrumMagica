package com.smanzana.nostrummagica.blocks.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.ItemDuct;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemDuctTileEntity extends TileEntity implements /* IInventory, */ ITickableTileEntity {
	
	private static final class ItemEntry {
		
		private static final String NBT_TICK = "tick";
		private static final String NBT_ITEM = "item";
		private static final String NBT_DIRECTION = "dir";
		
		public final long addTick;
		public final ItemStack stack;
		public final Direction inputDirection;
		
		public ItemEntry(long addTick, ItemStack stack, Direction inputDirection) {
			this.addTick = addTick;
			this.stack = stack;
			this.inputDirection = inputDirection;
		}
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putLong(NBT_TICK, addTick);
			tag.put(NBT_ITEM, stack.serializeNBT());
			tag.putInt(NBT_DIRECTION, inputDirection.getIndex());
			return tag;
		}
		
		public static final ItemDuctTileEntity.ItemEntry fromNBT(CompoundNBT tag) {
			final long tick = tag.getLong(NBT_TICK);
			final ItemStack stack = new ItemStack(tag.getCompound(NBT_ITEM));
			final Direction dir = Direction.VALUES[tag.getInt(NBT_DIRECTION)];
			return new ItemEntry(tick, stack, dir);
		}
	}
	
	private static final class SidedItemHandler implements IItemHandler {

		public final Direction side;
		private final ItemDuctTileEntity entity;
		
		public SidedItemHandler(ItemDuctTileEntity entity, Direction side) {
			this.entity = entity;
			this.side = side;
		}
		
		@Override
		public int getSlots() {
			return 1 + entity.itemQueue.size();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			// Treat slot 0 as special always-available slot for insertion
			if (slot == 0) {
				return ItemStack.EMPTY;
			}
			
			if (slot >= entity.itemQueue.size()) {
				return ItemStack.EMPTY;
			}
			
			return entity.itemQueue.get(slot).stack;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0) {
				return stack;
			}
			
			return entity.insertItem(stack, this.side, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot != 0) {
				return ItemStack.EMPTY;
			}
			
			return entity.extractItem(amount, this.side, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return 64;
		}
		
	}
	
	private static final String NBT_SORTED = "sorted_list";
	private static final String NBT_TICKS = "ticks";
	
	private static final int MAX_STACKS = 16;
	private static final long TICKS_LATENCY = 8; // how long it takes stacks to get through the pipe in ticks
	
	private final List<ItemDuctTileEntity.ItemEntry> itemQueue; // List of items being moved through. Naturally sorted by input (and therefore output) time
	private long ticks;
	private final ItemDuctTileEntity.SidedItemHandler[] handlers;
	
	public ItemDuctTileEntity() {
		super();
		itemQueue = new LinkedList<>();
		ticks = 0;
		handlers = new ItemDuctTileEntity.SidedItemHandler[] { // D U N S W E
			new SidedItemHandler(this, Direction.DOWN),
			new SidedItemHandler(this, Direction.UP),
			new SidedItemHandler(this, Direction.NORTH),
			new SidedItemHandler(this, Direction.SOUTH),
			new SidedItemHandler(this, Direction.WEST),
			new SidedItemHandler(this, Direction.EAST),
		};
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		ListNBT list = new ListNBT();
		for (ItemDuctTileEntity.ItemEntry entry : itemQueue) {
			list.add(entry.toNBT());
		}
		nbt.put(NBT_SORTED, list);
		
		nbt.putLong(NBT_TICKS, ticks);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		
		itemQueue.clear();
		ListNBT list = nbt.getList(NBT_SORTED, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompoundTagAt(i);
			itemQueue.add(ItemEntry.fromNBT(tag));
		}
		
		ticks= nbt.getLong(NBT_TICKS);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return !(newState.getBlock() instanceof ItemDuct);
	}
	
	/**
	 * Returns the real list of all items in this duct
	 * @return
	 */
	public List<ItemStack> getAllItems() {
		List<ItemStack> list = new ArrayList<>();
		
		for (ItemDuctTileEntity.ItemEntry entry : itemQueue) {
			list.add(entry.stack);
		}
		
		return list;
	}
	
	public void clear() {
		this.itemQueue.clear();
		this.markDirty();
	}
	
	protected ItemDuctTileEntity.ItemEntry addItem(@Nonnull ItemStack stack, Direction dir) {
		ItemDuctTileEntity.ItemEntry entry = new ItemEntry(ticks, stack, dir);
		this.itemQueue.add(entry);
		this.markDirty();
		return entry;
	}
	
	public boolean isFull() {
		return itemQueue.size() >= MAX_STACKS;
	}
	
	protected boolean tryAdd(@Nullable ItemStack stack, Direction dir) {
		if (stack.isEmpty()) {
			return false;
		}
		
		if (isFull()) {
			// Drop on floor
			// TODO make it actually drop on the face that it's being added?
			InventoryHelper.spawnItemStack(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
			return false;
		}
		
		addItem(stack, dir);
		return true;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		// Maybe this should check if we're connected?
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) handlers[facing.getIndex()];
		}
		return super.getCapability(capability, facing);
	}
	
	public ItemStack insertItem(ItemStack stack, Direction side, boolean simulate) {
		if (!simulate) {
			tryAdd(stack, side);
		}
		
		return ItemStack.EMPTY; // Always 'take' it
	}

	public ItemStack extractItem(int amount, Direction side, boolean simulate) {
		// No extraction at all!
		return ItemStack.EMPTY;
	}
	
	@Override
	public void tick() {
		if (world != null && !world.isRemote) {
			this.ticks++;
			
			// Check any items and see if it's time they move on.
			// Queue is sorted with earliest inserted at head. So as soon as we find false, stop.
			while (true) {
				if (!pushItem()) {
					break;
				}
			}
		}
	}
	
	/**
	 * Attemps to push the first item in the queue to where it needs to go.
	 * @return true if item was remove from inventory
	 */
	protected boolean pushItem() {
		if (itemQueue.isEmpty()) {
			return false;
		}
		
		final ItemDuctTileEntity.ItemEntry entry = itemQueue.get(0);
		final long duration = ticks - entry.addTick;
		if (duration < 0 || duration > TICKS_LATENCY) {
			// Item is good to push!
			pushItem(entry);
			itemQueue.remove(0);
			this.markDirty();
			return true;
		}
		
		return false;
	}
	
	private void pushItem(ItemDuctTileEntity.ItemEntry entry) {
		// Insert into targetted inventory.
		// Attempt to keep the item going in the same direction. If that doesn't work,
		// pick a random one!
		
		// Special case: 0 connections or 1 connection but it's where the item came from.
		// If that happens, pop it on the floor
		
		ItemStack stack = entry.stack;
		
		// First, try direction it wants
		stack = attemptPush(stack, entry.inputDirection.getOpposite());
		
		if (!stack.isEmpty()) {
			// Push in random directions
			List<Direction> rand = Lists.newArrayList(Direction.VALUES);
			Collections.shuffle(rand);
			for (Direction dir : rand) {
				// Don't go backwards and skip forwards since we just tried that
				if (dir == entry.inputDirection || dir == entry.inputDirection.getOpposite()) {
					continue;
				}
				
				stack = attemptPush(stack, dir);
						
				if (stack.isEmpty()) {
					break;
				}
			}
		}
		
		if (!stack.isEmpty()) {
			// Throw on the floor!
			// TODO make it actually drop on the face that we would be coming out of
			InventoryHelper.spawnItemStack(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
		}
	}
	
	/**
	 * Tries to push the provided item stack in the given direction.
	 * Pushes as many of the items in the stack as possible.
	 * Returns what couldn't be pushed.
	 * @param stack
	 * @param direction
	 * @return
	 */
	private @Nonnull ItemStack attemptPush(ItemStack stack, Direction direction) {
		@Nullable TileEntity te = world.getTileEntity(pos.offset(direction));
		
		if (te != null) {
			if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
				return pushInto(stack, handler, direction);
			}
			
			if (te instanceof IInventory) {
				
				IInventory inv = (IInventory) te;
				
				// Special cast for stupid chests :P
				if (te instanceof TileEntityChest) {
					IBlockState state = world.getBlockState(pos.offset(direction));
					if (state != null && state.getBlock() instanceof BlockChest) {
						inv = ((BlockChest)state.getBlock()).getContainer(world, pos.offset(direction), true);
					}
				}
				
				return pushInto(stack, inv, direction);
			}
		}
		
		return stack;
	}
	
	private @Nonnull ItemStack pushInto(ItemStack stack, IInventory inventory, Direction direction) {
		if (inventory instanceof ISidedInventory) {
			ISidedInventory sided = (ISidedInventory) inventory;
			for (int insertIndex : sided.getSlotsForFace(direction.getOpposite())) {
				if (!sided.canInsertItem(insertIndex, stack, direction.getOpposite())) {
					continue;
				}
				
				// Can insert. Would it fit?
				@Nullable ItemStack inSlot = sided.getStackInSlot(insertIndex);
				final int maxStack = (stack.isStackable() ? Math.min(stack.getMaxStackSize(), sided.getInventoryStackLimit()) : 1);
				final int taken;
				if (inSlot.isEmpty()) {
					taken = Math.min(maxStack, stack.getCount());
					inSlot = stack.splitStack(maxStack);
				} else if (ItemStacks.stacksMatch(stack, inSlot)) {
					taken = Math.min(maxStack - inSlot.getCount(), stack.getCount());
					if (taken > 0) {
						stack.splitStack(taken);
						inSlot.setCount(inSlot.getCount() + taken);
					}
				} else {
					taken = 0;
				}
				
				if (taken > 0) {
					sided.setInventorySlotContents(insertIndex, inSlot);
					if (stack.getCount() <= 0) {
						stack = ItemStack.EMPTY;
						break;
					}
				}
			}
		} else {
			stack = Inventories.addItem(inventory, stack);
		}
		
		return stack;
	}
	
	private @Nonnull ItemStack pushInto(ItemStack stack, IItemHandler handler, Direction direction) {
		// TODO safe to always run with false?
		return ItemHandlerHelper.insertItem(handler, stack, false);
	}
	
	public float getFilledPercent() {
		return (float) itemQueue.size() / (float) ItemDuctTileEntity.MAX_STACKS;
	}
}