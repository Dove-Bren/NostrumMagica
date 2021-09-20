package com.smanzana.nostrummagica.blocks.tiles;

import java.util.Arrays;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellTableEntity extends TileEntity implements IInventory {

	private static final String NBT_INV = "inventory";
	
	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-16 - Rune Slots
	 *   17-25 - Reagent slots
	 */
	
	private String displayName;
	private @Nonnull ItemStack slots[];
	
	public SpellTableEntity() {
		displayName = "Spell Table";
		slots = new ItemStack[getSizeInventory()];
		for (int i = 0; i < slots.length; i++)
			slots[i] = ItemStack.EMPTY;
	}
	
	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	public int getRuneSlotIndex() {
		return 1;
	}
	
	public int getRuneSlotCount() {
		return 16;
	}
	
	public int getScrollSlotIndex() {
		return 0;
	}
	
	public int getReagentSlotIndex() {
		return 17;
	}
	
	public int getReagentSlotCount() {
		return 9;
	}
	
	public @Nonnull ItemStack[] getReagentSlots() {
		return Arrays.copyOfRange(slots, getReagentSlotIndex(), getReagentSlotIndex() + getReagentSlotCount() - 1);
	}

	@Override
	public int getSizeInventory() {
		return 26;
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		return slots[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index < 0 || index >= getSizeInventory() || slots[index].isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index].splitStack(count);
		this.markDirty();
		
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index];
		slots[index] = ItemStack.EMPTY;
		
		this.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
			return;
		
		slots[index] = stack;
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
			return false;
		
		if (stack.isEmpty())
			return true;
		
		if (index == 0) {
			return stack.getItem() instanceof BlankScroll;
		}
		
		if (index < getReagentSlotIndex()) {
		
			if (!(stack.getItem() instanceof SpellRune))
				return false;
			
			if (index == 1) {
				return SpellRune.isTrigger(stack);
			}

			return true;
		}
		
		// Reagent bag
		return stack.getItem() instanceof ReagentItem;
		
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++)
			removeStackFromSlot(i);
	}
	
	public void clearBoard() {
		for (int i = 0; i < getReagentSlotIndex(); i++) {
			removeStackFromSlot(i);
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		NBTTagCompound compound = new NBTTagCompound();
		
		for (int i = 0; i < getSizeInventory(); i++) {
			if (getStackInSlot(i).isEmpty())
				continue;
			
			NBTTagCompound tag = new NBTTagCompound();
			compound.setTag(i + "", getStackInSlot(i).writeToNBT(tag));
		}
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setTag(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null || !nbt.hasKey(NBT_INV, NBT.TAG_COMPOUND))
			return;
		
		NBTTagCompound items = nbt.getCompoundTag(NBT_INV);
		for (String key : items.getKeySet()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumMagica.logger.error("Failed reading SpellTable inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = new ItemStack(items.getCompoundTag(key));
			this.setInventorySlotContents(id, stack);
		}
	}
	
	public Spell craft(EntityPlayer crafter, String name, int iconIndex) {
		ItemStack stack = this.getStackInSlot(0);
		if (stack.isEmpty() || !(stack.getItem() instanceof BlankScroll)) {
			return null;
		}
		
		Spell spell = SpellCreationGui.SpellCreationContainer.craftSpell(
				name, iconIndex, this, crafter, new LinkedList<String>(), new LinkedList<String>(), true, true);
		
		if (spell != null) {
			spell.promoteFromTrans();
			spell.setIcon(iconIndex);
			ItemStack scroll = new ItemStack(SpellScroll.instance(), 1);
			SpellScroll.setSpell(scroll, spell);
			this.clearBoard();
			this.setInventorySlotContents(0, scroll);
		}
		
		return spell;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : slots) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}