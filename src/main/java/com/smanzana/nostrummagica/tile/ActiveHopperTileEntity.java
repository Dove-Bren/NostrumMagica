package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.ActiveHopperBlock;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ActiveHopperTileEntity extends BlockEntity implements Hopper, WorldlyContainer, TickableBlockEntity {
	
	private static final String NBT_SLOT = "slot";
	private static final String NBT_CUSTOMNAME = "custom_name";
	private static final String NBT_COOLDOWN = "cooldown";
	
	private @Nonnull ItemStack slot = ItemStack.EMPTY;
	private @Nullable String customName;
	private int transferCooldown = -1;
	
	public ActiveHopperTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.ActiveHopperTileEntityType, pos, state);
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (!slot.isEmpty()) {
			nbt.put(NBT_SLOT, slot.serializeNBT());
		}
		
		if (customName != null) {
			nbt.putString(NBT_CUSTOMNAME, customName);
		}
		
		nbt.putInt(NBT_COOLDOWN, transferCooldown);
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		slot = (nbt.contains(NBT_SLOT) ? ItemStack.of(nbt.getCompound(NBT_SLOT)) : ItemStack.EMPTY); // nulls if empty
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
	public int getContainerSize() {
		return NUM_SLOTS;
	}

	@Override
	public @Nonnull ItemStack getItem(int index) {
		if (index != 0) {
			return ItemStack.EMPTY;
		}
		
		return slot;
	}

	@Override
	public @Nonnull ItemStack removeItem(int index, int count) {
		if (index != 0 || slot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack ret = slot.split(count);
		if (slot.getCount() <= 0) {
			slot = ItemStack.EMPTY;
		}
		this.setChanged();
		return ret;
	}

	@Override
	public @Nonnull ItemStack removeItemNoUpdate(int index) {
		if (index != 0 || slot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack ret = slot;
		slot = ItemStack.EMPTY;
		this.setChanged();
		return ret;
	}

	@Override
	public void setItem(int index, @Nonnull ItemStack stack) {
		if (index == 0) {
			slot = stack;
			this.setChanged();
		}
	}

	@Override
	public int getMaxStackSize() {
		return 64;
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
		return true;
	}

	@Override
	public void clearContent() {
		if (!slot.isEmpty()) {
			slot = ItemStack.EMPTY;
			this.setChanged();
		}
	}

	@Override
	public double getLevelX() {
		return worldPosition.getX() + .5;
	}

	@Override
	public double getLevelY() {
		return worldPosition.getY() + .5;
	}

	@Override
	public double getLevelZ() {
		return worldPosition.getZ() + .5;
	}

	@Override
	public void tick() {
		if (level != null && !level.isClientSide) {
			this.transferCooldown--;
			
			if (!isOnTransferCooldown() && ActiveHopperBlock.GetEnabled(level.getBlockState(worldPosition))) {
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
			this.setChanged();
		} else if (ItemStacks.stacksMatch(slot, stack)) {
			slot.setCount(Math.min(slot.getMaxStackSize(), slot.getCount() + stack.getCount()));
			this.setChanged();
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
		final Direction direction = ActiveHopperBlock.GetFacing(level.getBlockState(worldPosition));
		@Nullable BlockEntity te = level.getBlockEntity(worldPosition.relative(direction));
		
		if (te != null) {
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
				return pushInto(handler, direction);
			}
			
			if (te instanceof Container) {
				
				Container inv = (Container) te;
				
				// Special cast for stupid chests :P
				if (te instanceof ChestBlockEntity) {
					BlockState state = level.getBlockState(worldPosition.relative(direction));
					if (state != null && state.getBlock() instanceof ChestBlock) {
						inv = ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, worldPosition.relative(direction), true);
					}
				}
				
				return pushInto(inv, direction);
			}
		}
		
		final AABB captureBox = getCaptureBB(false);
		for (Entity e : level.getEntities((Entity) null, captureBox, EntitySelector.CONTAINER_ENTITY_SELECTOR)) {
			// Vanilla uses a random entity in the list. We'll just use the first.
			return pushInto((Container) e, direction);
		}
		
		return false;
	}
	
	private boolean pushInto(Container inventory, Direction direction) {
		ItemStack copyToInsert = slot.copy();
		copyToInsert.setCount(1);
		if (inventory instanceof WorldlyContainer) {
			WorldlyContainer sided = (WorldlyContainer) inventory;
			for (int insertIndex : sided.getSlotsForFace(direction.getOpposite())) {
				if (!sided.canPlaceItemThroughFace(insertIndex, copyToInsert, direction.getOpposite())) {
					continue;
				}
				
				// Can insert. Would it fit?
				@Nonnull ItemStack inSlot = sided.getItem(insertIndex);
				if (inSlot.isEmpty()) {
					sided.setItem(insertIndex, copyToInsert);
					this.removeItem(0, 1);
					return true;
				}
				
				if (!inSlot.isStackable()
						|| inSlot.getCount() >= inSlot.getMaxStackSize()
						|| inSlot.getCount() >= sided.getMaxStackSize()) {
					continue;
				}
				
				if (!ItemStacks.stacksMatch(copyToInsert, inSlot)) {
					continue;
				}
				
				inSlot.setCount(inSlot.getCount()+1);
				sided.setItem(insertIndex, inSlot);
				this.removeItem(0, 1);
				return true;
			}
		} else {
			if (Inventories.addItem(inventory, copyToInsert).isEmpty()) {
				this.removeItem(0, 1);
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
			this.removeItem(0, 1);
			return true;
		}
		
		return false;
	}
	
	private boolean pullItems() {
		// We want to pull from opposite(FACING)
		final Direction direction = ActiveHopperBlock.GetFacing(level.getBlockState(worldPosition)).getOpposite();
		@Nullable BlockEntity te = level.getBlockEntity(worldPosition.relative(direction));
		
		if (te != null) {
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).isPresent()) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);
				return pullFrom(handler, direction);
			}
			
			if (te instanceof Container) {
				
				Container inv = (Container) te;
				
				// Special cast for stupid chests :P
				if (te instanceof ChestBlockEntity) {
					BlockState state = level.getBlockState(worldPosition.relative(direction));
					if (state != null && state.getBlock() instanceof ChestBlock) {
						inv = ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, worldPosition.relative(direction), true);
					}
				}
				return pullFrom(inv, direction);
			}
		}
		
		final AABB captureBox = getCaptureBB(true);
		for (Entity e : level.getEntities((Entity) null, captureBox, EntitySelector.CONTAINER_ENTITY_SELECTOR)) {
			// Vanilla uses a random entity in the list. We'll just use the first.
			return pullFrom((Container) e, direction);
		}
		
		return false;
	}
	
	private boolean pullFrom(Container inventory, Direction direction) {
		if (inventory instanceof WorldlyContainer) {
			WorldlyContainer sided = (WorldlyContainer) inventory;
			for (int i : sided.getSlotsForFace(direction)) {
				@Nonnull ItemStack inSlot = sided.getItem(i);
				if (inSlot.isEmpty()) {
					continue;
				}
				
				if (!sided.canTakeItemThroughFace(i, inSlot, direction)) {
					continue;
				}
				
				if (!canPull(inSlot)) {
					continue;
				}
				
				@Nonnull ItemStack pulled = sided.removeItem(i, 1);
				this.addStack(pulled);
				return true;
			}
		} else {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				@Nonnull ItemStack inSlot = inventory.getItem(i);
				if (inSlot.isEmpty()) {
					continue;
				}
				
				if (!canPull(inSlot)) {
					continue;
				}
				
				ItemStack pulled = inventory.removeItem(i, 1);
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
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, getCaptureBB(true))) {
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
	
	private AABB getCaptureBB(boolean forPull) {
		final Direction direction = ActiveHopperBlock.GetFacing(level.getBlockState(worldPosition));
		
		if (direction == Direction.DOWN) {
			// Down has different collision so do a custom box
			return new AABB(0, 0, 0, 1, 1, 1).move(worldPosition).expandTowards(0, 1, 0);
		}
		
		final BlockPos spot = forPull ? worldPosition.relative(direction.getOpposite()) : worldPosition.relative(direction);
		return new AABB(0, 0, 0, 1, 1, 1).move(spot);
	}
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS_ARR;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction side) {
		final Direction direction = ActiveHopperBlock.GetFacing(level.getBlockState(worldPosition));
		if (side == direction) {
			// Coming in our output
			return false;
		}
		
		return canPull(itemStackIn);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction side) {
		final Direction direction = ActiveHopperBlock.GetFacing(level.getBlockState(worldPosition));
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