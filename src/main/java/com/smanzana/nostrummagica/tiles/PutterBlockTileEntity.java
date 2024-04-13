package com.smanzana.nostrummagica.tiles;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.PutterBlock;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PutterBlockTileEntity extends TileEntity implements ITickableTileEntity {

	private static final String NBT_INVENTORY = "inventory";
	
	private final Inventory inventory;
	
	private ItemEntity itemEntCache = null;
	private int ticksExisted;
	
	public PutterBlockTileEntity() {
		super(NostrumTileEntities.PutterBlockTileEntityType);
		final PutterBlockTileEntity putter = this;
		this.inventory = new Inventory(9) {
			@Override
			public void markDirty() {
				putter.markDirty();
			}
		};
	}
	
	public IInventory getInventory() {
		return inventory;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.put(NBT_INVENTORY, Inventories.serializeInventory(inventory));
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.get(NBT_INVENTORY));
	}

	@Override
	public void tick() {
		ticksExisted++;
		if (world == null || world.isRemote) {
			return;
		}
		
		// If being powered, disable everything
		if (ticksExisted % 10 != 0 || world.isBlockPowered(pos)) {
			return;
		}
		
		// Validate entityItem
		if (itemEntCache != null) {
			if (!itemEntCache.isAlive()
					|| (int) itemEntCache.getPosX() != pos.getX()
					|| (int) itemEntCache.getPosY() != pos.getY()
					|| (int) itemEntCache.getPosZ() != pos.getZ()) {
				itemEntCache = null;
			}
		}
		
		// Search for item if cache is busted
		if (itemEntCache == null) {
			Direction direction = world.getBlockState(this.pos).get(PutterBlock.FACING);
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
			List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, VoxelShapes.fullCube().getBoundingBox().offset(pos).offset(dx, dy, dz));
			if (items != null && !items.isEmpty()) {
				itemEntCache = items.get(0);
			}
		}
		
		// If we have an item, make sure it survives
		if (itemEntCache != null) {
			if (itemEntCache.ticksExisted > (float) itemEntCache.lifespan * .05f) {
				itemEntCache = refreshEntityItem(itemEntCache);
			}
		} else {
			// Spawn an item if we have items in our inventory
			final int size = inventory.getSizeInventory();
			final int pos = NostrumMagica.rand.nextInt(size);
			@Nonnull
			ItemStack toSpawn = ItemStack.EMPTY;
			for (int i = 0; i < size; i++) {
				// Get random offset, and then walk until we find an item
				final int index = (pos + i) % size; 
				ItemStack stack = inventory.getStackInSlot(index);
				if (stack.isEmpty()) {
					continue;
				}
				
				toSpawn = inventory.decrStackSize(index, 1);
				break;
			}
			
			if (!toSpawn.isEmpty()) {
				// Play effects, and create item
				double dx = 0;
				double dy = 0;
				double dz = 0;
				Direction direction = world.getBlockState(this.pos).get(PutterBlock.FACING);
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
				itemEntCache = new ItemEntity(world, this.pos.getX() + .5 + dx, this.pos.getY() + .5 + dy, this.pos.getZ() + .5 + dz, toSpawn);
				itemEntCache.setMotion(0, 0, 0);
				world.addEntity(itemEntCache);
			}
		}
	}
	
	private ItemEntity refreshEntityItem(ItemEntity oldItem) {
		ItemEntity newItem = new ItemEntity(oldItem.world, oldItem.getPosX(), oldItem.getPosY(), oldItem.getPosZ(), oldItem.getItem().copy());
		newItem.setMotion(oldItem.getMotion());
		newItem.lifespan = oldItem.lifespan;
		oldItem.world.addEntity(newItem);
		oldItem.remove();
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
						return inventory.getSizeInventory();
					}

					@Override
					public ItemStack getStackInSlot(int slot) {
						return inventory.getStackInSlot(slot);
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
						ItemStack stack = inventory.getStackInSlot(slot);
						if (stack.isEmpty()) {
							return stack;
						}
						
						stack = stack.copy();
						ItemStack taken = stack.split(amount);
						if (!simulate) {
							inventory.setInventorySlotContents(slot, stack); // Set it back to dirty the inventory
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