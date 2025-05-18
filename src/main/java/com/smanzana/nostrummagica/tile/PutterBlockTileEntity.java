package com.smanzana.nostrummagica.tile;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PutterBlock;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PutterBlockTileEntity extends BlockEntity implements TickableBlockEntity {

	private static final String NBT_INVENTORY = "inventory";
	
	private final SimpleContainer inventory;
	
	private ItemEntity itemEntCache = null;
	private int ticksExisted;
	
	public PutterBlockTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.PutterBlock, pos, state);
		final PutterBlockTileEntity putter = this;
		this.inventory = new SimpleContainer(9) {
			@Override
			public void setChanged() {
				putter.setChanged();
			}
		};
	}
	
	public Container getInventory() {
		return inventory;
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INVENTORY));
	}

	@Override
	public void tick() {
		ticksExisted++;
		if (level == null || level.isClientSide) {
			return;
		}
		
		// If being powered, disable everything
		if (ticksExisted % 10 != 0 || level.hasNeighborSignal(worldPosition)) {
			return;
		}
		
		// Validate entityItem
		if (itemEntCache != null) {
			if (!itemEntCache.isAlive()
					|| (int) itemEntCache.getX() != worldPosition.getX()
					|| (int) itemEntCache.getY() != worldPosition.getY()
					|| (int) itemEntCache.getZ() != worldPosition.getZ()) {
				itemEntCache = null;
			}
		}
		
		// Search for item if cache is busted
		if (itemEntCache == null) {
			Direction direction = level.getBlockState(this.worldPosition).getValue(PutterBlock.FACING);
			int dx = 0;
			int dy = 0;
			int dz = 0;
			switch (direction) {
			case DOWN:
				dy = -1;
				break;
			case EAST:
			default:
				dx = 1;
				break;
			case NORTH:
				dz = -1;
				break;
			case SOUTH:
				dz = 1;
				break;
			case UP:
				dy = 1;
				break;
			case WEST:
				dx = -1;
				break;
			}
			List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, Shapes.block().bounds().move(worldPosition).move(dx, dy, dz));
			if (items != null && !items.isEmpty()) {
				itemEntCache = items.get(0);
			}
		}
		
		// If we have an item, make sure it survives
		if (itemEntCache != null) {
			if (itemEntCache.tickCount > (float) itemEntCache.lifespan * .05f) {
				itemEntCache = refreshEntityItem(itemEntCache);
			}
		} else {
			// Spawn an item if we have items in our inventory
			final int size = inventory.getContainerSize();
			final int pos = NostrumMagica.rand.nextInt(size);
			@Nonnull
			ItemStack toSpawn = ItemStack.EMPTY;
			for (int i = 0; i < size; i++) {
				// Get random offset, and then walk until we find an item
				final int index = (pos + i) % size; 
				ItemStack stack = inventory.getItem(index);
				if (stack.isEmpty()) {
					continue;
				}
				
				toSpawn = inventory.removeItem(index, 1);
				break;
			}
			
			if (!toSpawn.isEmpty()) {
				// Play effects, and create item
				double dx = 0;
				double dy = 0;
				double dz = 0;
				Direction direction = level.getBlockState(this.worldPosition).getValue(PutterBlock.FACING);
				switch (direction) {
				case DOWN:
					dy = -.75;
					break;
				case EAST:
				default:
					dx = .75;
					break;
				case NORTH:
					dz = -.75;
					break;
				case SOUTH:
					dz = .75;
					break;
				case UP:
					dy = .75;
					break;
				case WEST:
					dx = -.75;
					break;
				}
				itemEntCache = new ItemEntity(level, this.worldPosition.getX() + .5 + dx, this.worldPosition.getY() + .5 + dy, this.worldPosition.getZ() + .5 + dz, toSpawn);
				itemEntCache.setDeltaMovement(0, 0, 0);
				level.addFreshEntity(itemEntCache);
			}
		}
	}
	
	private ItemEntity refreshEntityItem(ItemEntity oldItem) {
		ItemEntity newItem = new ItemEntity(oldItem.level, oldItem.getX(), oldItem.getY(), oldItem.getZ(), oldItem.getItem().copy());
		newItem.setDeltaMovement(oldItem.getDeltaMovement());
		newItem.lifespan = oldItem.lifespan;
		oldItem.level.addFreshEntity(newItem);
		oldItem.discard();
		return newItem;
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