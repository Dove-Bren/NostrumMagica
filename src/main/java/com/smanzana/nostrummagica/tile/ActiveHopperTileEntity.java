package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.ActiveHopper;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ActiveHopperTileEntity extends TileEntity implements IHopper, ISidedInventory, ITickableTileEntity {
	
	private static final String NBT_SLOT = "slot";
	private static final String NBT_CUSTOMNAME = "custom_name";
	private static final String NBT_COOLDOWN = "cooldown";
	
	private @Nonnull ItemStack slot = ItemStack.EMPTY;
	private @Nullable String customName;
	private int transferCooldown = -1;
	
	public ActiveHopperTileEntity() {
		super(NostrumTileEntities.ActiveHopperTileEntityType);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (!slot.isEmpty()) {
			nbt.put(NBT_SLOT, slot.serializeNBT());
		}
		
		if (customName != null) {
			nbt.putString(NBT_CUSTOMNAME, customName);
		}
		
		nbt.putInt(NBT_COOLDOWN, transferCooldown);
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		slot = (nbt.contains(NBT_SLOT) ? ItemStack.read(nbt.getCompound(NBT_SLOT)) : ItemStack.EMPTY); // nulls if empty
		customName = (nbt.contains(NBT_CUSTOMNAME) ? nbt.getString(NBT_CUSTOMNAME) : null);
		transferCooldown = nbt.getInt(NBT_COOLDOWN);
	}
	
	private static final int NUM_SLOTS = 1;
	private static final int[] SLOTS_ARR = new int[NUM_SLOTS];
	{
		for (int i = 0; i < NUM_SLOTS; i++) {
			SLOTS_ARR[i] = i;
		}
	}

	@Override
	public int getSizeInventory() {
		return NUM_SLOTS;
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index != 0) {
			return ItemStack.EMPTY;
		}
		
		return slot;
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int index, int count) {
		if (index != 0 || slot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack ret = slot.split(count);
		if (slot.getCount() <= 0) {
			slot = ItemStack.EMPTY;
		}
		this.markDirty();
		return ret;
	}

	@Override
	public @Nonnull ItemStack removeStackFromSlot(int index) {
		if (index != 0 || slot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack ret = slot;
		slot = ItemStack.EMPTY;
		this.markDirty();
		return ret;
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		if (index == 0) {
			slot = stack;
			this.markDirty();
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
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
		return true;
	}

	@Override
	public void clear() {
		if (!slot.isEmpty()) {
			slot = ItemStack.EMPTY;
			this.markDirty();
		}
	}

	@Override
	public double getXPos() {
		return pos.getX() + .5;
	}

	@Override
	public double getYPos() {
		return pos.getY() + .5;
	}

	@Override
	public double getZPos() {
		return pos.getZ() + .5;
	}

	@Override
	public void tick() {
		if (world != null && !world.isRemote) {
			this.transferCooldown--;
			
			if (!isOnTransferCooldown() && ActiveHopper.GetEnabled(world.getBlockState(pos))) {
				this.hopperTick();
				this.setTransferCooldown(8); // same rate as vanilla
			}
		}
	}
	
	public void setTransferCooldown(int cooldown) {
		this.transferCooldown = cooldown;
	}
	
	public int getTransferCooldown() {
		return this.transferCooldown;
	}
	
	public boolean isOnTransferCooldown() {
		return this.transferCooldown > 0;
	}
	
	/**
	 * Cheap check to see if it's even possible to pull ANY item
	 * @return
	 */
	public boolean canPullAny() {
		return !slot.isEmpty()
				&& slot.isStackable()
				&& slot.getCount() < slot.getMaxStackSize();
	}
	
	public boolean canPull(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		
		if (!canPullAny()) {
			return false;
		}
		
		return ItemStacks.stacksMatch(stack, slot);
	}
	
	public void addStack(@Nonnull ItemStack stack) {
		if (slot.isEmpty()) {
			// Error condition
			slot = stack;
			this.markDirty();
		} else if (ItemStacks.stacksMatch(slot, stack)) {
			slot.setCount(Math.min(slot.getMaxStackSize(), slot.getCount() + stack.getCount()));
			this.markDirty();
		}
	}
	
	public boolean canPush() {
		return !slot.isEmpty()
				&& slot.isStackable()
				&& slot.getCount() > 1; // don't push out last item stack!
	}
	
	// Called to actually pull and push items
	private void hopperTick() {
		// Pull from inventory first. // TODO consider pulling from multiple blocks 'above' ?
		if (canPullAny()) {
			pullItems();
		}
		
		// Regardless of whether we were able to or not, try and pick up any floating items
		if (canPullAny()) {
			captureNearbyItems();
		}
		
		// Then push items into wherever we're facing
		if (canPush()) {
			pushItems();
		}
	}
	
	private boolean pushItems() {
		// Inventory we want to push into is in direction FACING
		final Direction direction = ActiveHopper.GetFacing(world.getBlockState(pos));
		@Nullable TileEntity te = world.getTileEntity(pos.offset(direction));
		
		if (te != null) {
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
				return pushInto(handler, direction);
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
				
				return pushInto(inv, direction);
			}
		}
		
		final AxisAlignedBB captureBox = getCaptureBB(false);
		for (Entity e : world.getEntitiesInAABBexcluding(null, captureBox, EntityPredicates.HAS_INVENTORY)) {
			// Vanilla uses a random entity in the list. We'll just use the first.
			return pushInto((IInventory) e, direction);
		}
		
		return false;
	}
	
	private boolean pushInto(IInventory inventory, Direction direction) {
		ItemStack copyToInsert = slot.copy();
		copyToInsert.setCount(1);
		if (inventory instanceof ISidedInventory) {
			ISidedInventory sided = (ISidedInventory) inventory;
			for (int insertIndex : sided.getSlotsForFace(direction.getOpposite())) {
				if (!sided.canInsertItem(insertIndex, copyToInsert, direction.getOpposite())) {
					continue;
				}
				
				// Can insert. Would it fit?
				@Nonnull ItemStack inSlot = sided.getStackInSlot(insertIndex);
				if (inSlot.isEmpty()) {
					sided.setInventorySlotContents(insertIndex, copyToInsert);
					this.decrStackSize(0, 1);
					return true;
				}
				
				if (!inSlot.isStackable()
						|| inSlot.getCount() >= inSlot.getMaxStackSize()
						|| inSlot.getCount() >= sided.getInventoryStackLimit()) {
					continue;
				}
				
				if (!ItemStacks.stacksMatch(copyToInsert, inSlot)) {
					continue;
				}
				
				inSlot.setCount(inSlot.getCount()+1);
				sided.setInventorySlotContents(insertIndex, inSlot);
				this.decrStackSize(0, 1);
				return true;
			}
		} else {
			if (Inventories.addItem(inventory, copyToInsert).isEmpty()) {
				this.decrStackSize(0, 1);
				return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	private boolean pushInto(IItemHandler handler, Direction direction) {
		ItemStack copyToInsert = slot.copy();
		copyToInsert.setCount(1);
		
		@Nonnull ItemStack ret = ItemHandlerHelper.insertItem(handler, copyToInsert, true);
		if (ret.isEmpty() || ret.getCount() == 0) {
			ItemHandlerHelper.insertItem(handler, copyToInsert, false);
			this.decrStackSize(0, 1);
			return true;
		}
		
		return false;
	}
	
	private boolean pullItems() {
		// We want to pull from opposite(FACING)
		final Direction direction = ActiveHopper.GetFacing(world.getBlockState(pos)).getOpposite();
		@Nullable TileEntity te = world.getTileEntity(pos.offset(direction));
		
		if (te != null) {
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).isPresent()) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);
				return pullFrom(handler, direction);
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
				return pullFrom(inv, direction);
			}
		}
		
		final AxisAlignedBB captureBox = getCaptureBB(true);
		for (Entity e : world.getEntitiesInAABBexcluding(null, captureBox, EntityPredicates.HAS_INVENTORY)) {
			// Vanilla uses a random entity in the list. We'll just use the first.
			return pullFrom((IInventory) e, direction);
		}
		
		return false;
	}
	
	private boolean pullFrom(IInventory inventory, Direction direction) {
		if (inventory instanceof ISidedInventory) {
			ISidedInventory sided = (ISidedInventory) inventory;
			for (int i : sided.getSlotsForFace(direction)) {
				@Nonnull ItemStack inSlot = sided.getStackInSlot(i);
				if (inSlot.isEmpty()) {
					continue;
				}
				
				if (!sided.canExtractItem(i, inSlot, direction)) {
					continue;
				}
				
				if (!canPull(inSlot)) {
					continue;
				}
				
				@Nonnull ItemStack pulled = sided.decrStackSize(i, 1);
				this.addStack(pulled);
				return true;
			}
		} else {
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				@Nonnull ItemStack inSlot = inventory.getStackInSlot(i);
				if (inSlot.isEmpty()) {
					continue;
				}
				
				if (!canPull(inSlot)) {
					continue;
				}
				
				ItemStack pulled = inventory.decrStackSize(i, 1);
				this.addStack(pulled);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean pullFrom(IItemHandler handler, Direction direction) {
		for (int i = 0; i < handler.getSlots(); i++) {
			
			@Nonnull ItemStack inSlot = handler.getStackInSlot(i);
			if (inSlot.isEmpty()) {
				continue;
			}
			
			if (!canPull(inSlot)) {
				continue;
			}
			
			if (handler.extractItem(i, 1, true) != ItemStack.EMPTY) {
				@Nonnull ItemStack drawn = handler.extractItem(i, 1, false);
				this.addStack(drawn);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean captureNearbyItems() {
		for (ItemEntity entity : world.getEntitiesWithinAABB(ItemEntity.class, getCaptureBB(true))) {
			// try and pull from the stack
			@Nonnull ItemStack stack = entity.getItem();
			if (canPull(stack)) {
				this.addStack(stack.split(1)); // reduces stack size by 1
				entity.setItem(stack); // Try and force an update
				return true;
			}
		}
		
		return false;
	}
	
	private AxisAlignedBB getCaptureBB(boolean forPull) {
		final Direction direction = ActiveHopper.GetFacing(world.getBlockState(pos));
		
		if (direction == Direction.DOWN) {
			// Down has different collision so do a custom box
			return new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos).expand(0, 1, 0);
		}
		
		final BlockPos spot = forPull ? pos.offset(direction.getOpposite()) : pos.offset(direction);
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(spot);
	}
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS_ARR;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction side) {
		final Direction direction = ActiveHopper.GetFacing(world.getBlockState(pos));
		if (side == direction) {
			// Coming in our output
			return false;
		}
		
		return canPull(itemStackIn);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction side) {
		final Direction direction = ActiveHopper.GetFacing(world.getBlockState(pos));
		if (side == direction.getOpposite()) {
			// pulling from our mouth?
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isEmpty() {
		return slot.isEmpty();
	}
}