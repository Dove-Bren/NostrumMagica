package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellRune;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class RuneShaperEntity extends TileEntity implements IInventory {

	private static final String NBT_INV = "runeshaperinv";
	
	/**
	 * Inventory:
	 *   0 - Shape slot
	 *   1, 2, 3, 4 - Element slots
	 *   5 - Alteration slot
	 */
	
	private @Nonnull ItemStack slots[];
	
	public RuneShaperEntity() {
		super(NostrumTileEntities.RuneShaperEntityType);
		slots = new ItemStack[getSizeInventory()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = ItemStack.EMPTY;
		}
	}
	
	public  @Nonnull ItemStack getShapeSlot() {
		return this.getStackInSlot(0);
	}
	
	public @Nonnull ItemStack getElementSlot(int idx) {
		if (idx < 0 || idx > 4) {
			return ItemStack.EMPTY;
		}
		return this.getStackInSlot(1 + idx);
	}
	
	public @Nonnull ItemStack getAlterationSlot() {
		return this.getStackInSlot(5);
	}
	
	@Override
	public int getSizeInventory() {
		return 6;
	}

	@Override
	public  @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		return slots[index];
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int index, int count) {
		if (index < 0 || index >= getSizeInventory() || slots[index].isEmpty())
			return ItemStack.EMPTY;
		
		@Nonnull ItemStack stack;
		if (slots[index].getCount() <= count) {
			stack = slots[index];
			slots[index] = ItemStack.EMPTY;
		} else {
			stack = slots[index].split(count);
		}
		
		this.markDirty();
		
		return stack;
	}

	@Override
	public @Nonnull ItemStack removeStackFromSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index];
		slots[index] = ItemStack.EMPTY;
		
		this.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		if (!isItemValidForSlot(index, stack))
			return;
		
		slots[index] = stack;
		this.markDirty();
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
	}

	@Override
	public void closeInventory(PlayerEntity player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
			return false;
		
		if (stack.isEmpty())
			return true;
		
		if (index == 0) {
			return SpellRune.isShape(stack) && !SpellRune.isPackedShape(stack);
		}
		
		if (index >= 1 && index <= 4) {
			return SpellRune.isElement(stack);
		}
		
		if (index == 5) {
			return SpellRune.isAlteration(stack);
		}
		
		return false;
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++)
			removeStackFromSlot(i);
	}
	
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		CompoundNBT compound = new CompoundNBT();
		
		for (int i = 0; i < getSizeInventory(); i++) {
			if (getStackInSlot(i).isEmpty())
				continue;
			
			CompoundNBT tag = new CompoundNBT();
			compound.put(i + "", getStackInSlot(i).write(tag));
		}
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.put(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null || !nbt.contains(NBT_INV, NBT.TAG_COMPOUND))
			return;
		this.clear();
		CompoundNBT items = nbt.getCompound(NBT_INV);
		for (String key : items.keySet()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumMagica.logger.error("Failed reading SpellTable inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.read(items.getCompound(key));
			this.setInventorySlotContents(id, stack);
		}
	}
	
	@Override
	public boolean isEmpty() {
		for (@Nonnull ItemStack stack : slots) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public IInventory getExtraInventory() {
		for (BlockPos checkPos : new BlockPos[] {pos.up(), pos.north(), pos.east(), pos.south(), pos.west(), pos.down(), pos.up().north(), pos.up().south(), pos.up().east(), pos.up().west(), pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west()}) {
			@Nullable TileEntity te = world.getTileEntity(checkPos);
			if (te != null && te instanceof RuneLibraryTileEntity) {
				return ((RuneLibraryTileEntity) te).getInventory();
			}
		}
		
		return null;
	}
}