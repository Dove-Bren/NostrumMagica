package com.smanzana.nostrummagica.blocks.tiles;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.PutterBlock;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PutterBlockTileEntity extends TileEntity implements ITickableTileEntity {

	private static final String NBT_INVENTORY = "inventory";
	
	private final InventoryBasic inventory;
	
	private EntityItem itemEntCache = null;
	private int ticksExisted;
	
	public PutterBlockTileEntity() {
		final PutterBlockTileEntity putter = this;
		this.inventory = new InventoryBasic("Putter Block", false, 9) {
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.setTag(NBT_INVENTORY, Inventories.serializeInventory(inventory));
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null)
			return;
		
		Inventories.deserializeInventory(inventory, nbt.getTag(NBT_INVENTORY));
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
			if (itemEntCache.isDead
					|| (int) itemEntCache.posX != pos.getX()
					|| (int) itemEntCache.posY != pos.getY()
					|| (int) itemEntCache.posZ != pos.getZ()) {
				itemEntCache = null;
			}
		}
		
		// Search for item if cache is busted
		if (itemEntCache == null) {
			EnumFacing direction = world.getBlockState(this.pos).getValue(PutterBlock.FACING);
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
			List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, Block.FULL_BLOCK_AABB.offset(pos).offset(dx, dy, dz));
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
				EnumFacing direction = world.getBlockState(this.pos).getValue(PutterBlock.FACING);
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
				itemEntCache = new EntityItem(world, this.pos.getX() + .5 + dx, this.pos.getY() + .5 + dy, this.pos.getZ() + .5 + dz, toSpawn);
				itemEntCache.motionX = itemEntCache.motionY = itemEntCache.motionZ = 0;
				world.spawnEntity(itemEntCache);
			}
		}
	}
	
	private EntityItem refreshEntityItem(EntityItem oldItem) {
		EntityItem newItem = new EntityItem(oldItem.world, oldItem.posX, oldItem.posY, oldItem.posZ, oldItem.getItem().copy());
		newItem.motionX = oldItem.motionX;
		newItem.motionY = oldItem.motionY;
		newItem.motionZ = oldItem.motionZ;
		newItem.lifespan = oldItem.lifespan;
		oldItem.world.spawnEntity(newItem);
		oldItem.setDead();
		return newItem;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}
	
	private IItemHandler handlerProxy = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (handlerProxy == null) {
				handlerProxy = new IItemHandler() {

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
						ItemStack taken = stack.splitStack(amount);
						if (!simulate) {
							inventory.setInventorySlotContents(slot, stack); // Set it back to dirty the inventory
						}
						
						return taken;
					}

					@Override
					public int getSlotLimit(int slot) {
						return 64;
					}
					
				};
			}
			
			return (T) handlerProxy;
		}
		
		return null;
	}
}