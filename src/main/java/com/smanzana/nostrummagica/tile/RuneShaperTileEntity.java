package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.SpellRune;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class RuneShaperTileEntity extends BlockEntity implements Container {

	private static final String NBT_INV = "runeshaperinv";
	
	/**
	 * Inventory:
	 *   0 - Rune slot
	 */
	
	private @Nonnull ItemStack slots[];
	
	public RuneShaperTileEntity() {
		super(NostrumTileEntities.RuneShaperEntityType);
		slots = new ItemStack[getContainerSize()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = ItemStack.EMPTY;
		}
	}
	
	public  @Nonnull ItemStack getRuneSlot() {
		return this.getItem(0);
	}
	
	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public  @Nonnull ItemStack getItem(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		return slots[index];
	}

	@Override
	public @Nonnull ItemStack removeItem(int index, int count) {
		if (index < 0 || index >= getContainerSize() || slots[index].isEmpty())
			return ItemStack.EMPTY;
		
		@Nonnull ItemStack stack;
		if (slots[index].getCount() <= count) {
			stack = slots[index];
			slots[index] = ItemStack.EMPTY;
		} else {
			stack = slots[index].split(count);
		}
		
		this.setChanged();
		
		return stack;
	}

	@Override
	public @Nonnull ItemStack removeItemNoUpdate(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index];
		slots[index] = ItemStack.EMPTY;
		
		this.setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, @Nonnull ItemStack stack) {
		if (!canPlaceItem(index, stack))
			return;
		
		slots[index] = stack;
		this.setChanged();
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
	}

	@Override
	public void stopOpen(Player player) {
	}

	@Override
	public boolean canPlaceItem(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getContainerSize())
			return false;
		
		if (stack.isEmpty())
			return true;
		
		if (index == 0) {
			return stack.getItem() instanceof SpellRune && canShapeRune(stack);
		}
		
		return false;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getContainerSize(); i++)
			removeItemNoUpdate(i);
	}
	
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		CompoundTag compound = new CompoundTag();
		
		for (int i = 0; i < getContainerSize(); i++) {
			if (getItem(i).isEmpty())
				continue;
			
			CompoundTag tag = new CompoundTag();
			compound.put(i + "", getItem(i).save(tag));
		}
		
		if (nbt == null)
			nbt = new CompoundTag();
		
		nbt.put(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		
		if (nbt == null || !nbt.contains(NBT_INV, NBT.TAG_COMPOUND))
			return;
		this.clearContent();
		CompoundTag items = nbt.getCompound(NBT_INV);
		for (String key : items.getAllKeys()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumMagica.logger.error("Failed reading RuneShaper inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.of(items.getCompound(key));
			this.setItem(id, stack);
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

	public Container getExtraInventory() {
		for (BlockPos checkPos : new BlockPos[] {worldPosition.above(), worldPosition.north(), worldPosition.east(), worldPosition.south(), worldPosition.west(), worldPosition.below(), worldPosition.above().north(), worldPosition.above().south(), worldPosition.above().east(), worldPosition.above().west(), worldPosition.north().east(), worldPosition.north().west(), worldPosition.south().east(), worldPosition.south().west()}) {
			@Nullable BlockEntity te = level.getBlockEntity(checkPos);
			if (te != null && te instanceof RuneLibraryTileEntity) {
				return ((RuneLibraryTileEntity) te).getInventory();
			}
		}
		
		return null;
	}
	
	public boolean canShapeRune(ItemStack stack) {
		return SpellRune.isShape(stack)
				&& !SpellRune.getShape(stack).getDefaultProperties().getProperties().isEmpty();
	}
}