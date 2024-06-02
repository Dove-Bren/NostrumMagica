package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
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
			final ItemStack stack = ItemStack.read(tag.getCompound(NBT_ITEM));
			final Direction dir = Direction.values()[tag.getInt(NBT_DIRECTION)];
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

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot != 0) {
				return false;
			}
			
			return true;
		}
		
	}
	
	private static final String NBT_SORTED = "sorted_list";
	private static final String NBT_TICKS = "ticks";
	
	private static final int MAX_STACKS = 16;
	private static final long TICKS_LATENCY = 8; // how long it takes stacks to get through the pipe in ticks
	
	private final List<ItemDuctTileEntity.ItemEntry> itemQueue; // List of items being moved through. Naturally sorted by input (and therefore output) time
	private long ticks;
	private final LazyOptional<ItemDuctTileEntity.SidedItemHandler> handlerDown;
	private final LazyOptional<ItemDuctTileEntity.SidedItemHandler> handlerUp;
	private final LazyOptional<ItemDuctTileEntity.SidedItemHandler> handlerNorth;
	private final LazyOptional<ItemDuctTileEntity.SidedItemHandler> handlerSouth;
	private final LazyOptional<ItemDuctTileEntity.SidedItemHandler> handlerWest;
	private final LazyOptional<ItemDuctTileEntity.SidedItemHandler> handlerEast;
	
	public ItemDuctTileEntity() {
		super(NostrumTileEntities.ItemDuctTileEntityType);
		itemQueue = new LinkedList<>();
		ticks = 0;
		handlerDown = LazyOptional.of(() -> new SidedItemHandler(this, Direction.DOWN));
		handlerUp = LazyOptional.of(() -> new SidedItemHandler(this, Direction.UP));
		handlerNorth = LazyOptional.of(() -> new SidedItemHandler(this, Direction.NORTH));
		handlerSouth = LazyOptional.of(() -> new SidedItemHandler(this, Direction.SOUTH));
		handlerWest = LazyOptional.of(() -> new SidedItemHandler(this, Direction.WEST));
		handlerEast = LazyOptional.of(() -> new SidedItemHandler(this, Direction.EAST));
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		ListNBT list = new ListNBT();
		for (ItemDuctTileEntity.ItemEntry entry : itemQueue) {
			list.add(entry.toNBT());
		}
		nbt.put(NBT_SORTED, list);
		
		nbt.putLong(NBT_TICKS, ticks);
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		itemQueue.clear();
		ListNBT list = nbt.getList(NBT_SORTED, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompound(i);
			itemQueue.add(ItemEntry.fromNBT(tag));
		}
		
		ticks= nbt.getLong(NBT_TICKS);
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
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			switch (facing) {
			case DOWN:
				return handlerDown.cast();
			case EAST:
				return handlerEast.cast();
			case NORTH:
				return handlerNorth.cast();
			case SOUTH:
				return handlerSouth.cast();
			case UP:
				return handlerUp.cast();
			case WEST:
				return handlerWest.cast();
			}
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
			List<Direction> rand = Lists.newArrayList(Direction.values());
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
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
				return pushInto(stack, handler, direction);
			}
			
			if (te instanceof IInventory) {
				
				IInventory inv = (IInventory) te;
				
				// Special cast for stupid chests :P
				if (te instanceof ChestTileEntity) {
					BlockState state = world.getBlockState(pos.offset(direction));
					if (state != null && state.getBlock() instanceof ChestBlock) {
						inv = ChestBlock.getChestInventory((ChestBlock) state.getBlock(), state, world, pos.offset(direction), true);
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
					inSlot = stack.split(maxStack);
				} else if (ItemStacks.stacksMatch(stack, inSlot)) {
					taken = Math.min(maxStack - inSlot.getCount(), stack.getCount());
					if (taken > 0) {
						stack.split(taken);
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