package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.block.RuneLibraryBlock;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class RuneLibraryTileEntity extends TileEntity {

	private static final String NBT_INVENTORY = "inventory";
	
	private final Inventory inventory;
	
	public RuneLibraryTileEntity() {
		super(NostrumTileEntities.RuneLibraryType);
		this.inventory = new Inventory(27) {
			@Override
			public boolean canPlaceItem(int index, ItemStack stack) {
				return stack.isEmpty() || stack.getItem() instanceof SpellRune;
			}
		};
		this.inventory.addListener((inv) -> {
			RuneLibraryTileEntity.this.setChanged();
			RuneLibraryTileEntity.this.refreshBlock();
		});
	}
	
	public IInventory getInventory() {
		return inventory;
	}
	
	protected RuneLibraryBlock.Fill getFillLevel() {
		int count = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (!inventory.getItem(i).isEmpty()) {
				count++;
			}
		}
		
		float prog = (float) count / (float) inventory.getContainerSize();
		if (prog >= .75f) {
			return RuneLibraryBlock.Fill.FULL;
		}
		if (prog > .5f) {
			return RuneLibraryBlock.Fill.MOST;
		}
		if (prog > 0f) {
			return RuneLibraryBlock.Fill.SOME;
		}
		return RuneLibraryBlock.Fill.EMPTY;
	}
	
	protected void refreshBlock() {
		if (this.level != null) {
			final RuneLibraryBlock.Fill now = getFillLevel();
			BlockState state = this.getBlockState();
			if (state.getValue(RuneLibraryBlock.FILL) != now) {
				level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(RuneLibraryBlock.FILL, now));
			}
		}
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		nbt.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INVENTORY));
	}

	private LazyOptional<IItemHandler> handlerProxy = null;
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (handlerProxy == null) {
				handlerProxy = LazyOptional.of(() -> new IItemHandler() {

					@Override
					public int getSlots() {
						return inventory.getContainerSize();
					}

					@Override
					public ItemStack getStackInSlot(int slot) {
						return inventory.getItem(slot);
					}

					@Override
					public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
						if (simulate) {
							return Inventories.simulateAddItem(inventory, stack);
						} else {
							return Inventories.addItem(inventory, stack);
						}
					}

					@Override
					public ItemStack extractItem(int slot, int amount, boolean simulate) {
						ItemStack stack = inventory.getItem(slot);
						if (stack.isEmpty()) {
							return stack;
						}
						
						stack = stack.copy();
						ItemStack taken = stack.split(amount);
						if (!simulate) {
							inventory.setItem(slot, stack); // Set it back to dirty the inventory
						}
						
						return taken;
					}

					@Override
					public int getSlotLimit(int slot) {
						return 64;
					}

					@Override
					public boolean isItemValid(int slot, ItemStack stack) {
						return slot < this.getSlots(); // no restriction on stack?
					}
					
				});
			}
			
			return handlerProxy.cast();
		}
		
		return null;
	}
}