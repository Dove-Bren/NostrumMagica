package com.smanzana.nostrummagica.tiles;

import java.util.LinkedList;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public class BasicSpellTableEntity extends TileEntity implements ISpellCraftingInventory, ISpellCraftingTileEntity {

	private static final String NBT_INV = "inventory";
	
	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-3 - Rune Slots
	 */
	
	private @Nonnull ItemStack slots[];
	
	public BasicSpellTableEntity() {
		this(NostrumTileEntities.BasicSpellTableType);
	}
	
	protected BasicSpellTableEntity(TileEntityType<? extends BasicSpellTableEntity> type) {
		super(type);
		slots = new ItemStack[getSizeInventory()];
		for (int i = 0; i < slots.length; i++)
			slots[i] = ItemStack.EMPTY;
	}
	
	@Override
	public int getRuneSlotStartingIndex() {
		return 1;
	}
	
	public int getRuneSlotCount() {
		return 3;
	}
	
	public int getScrollSlotIndex() {
		return 0;
	}

	@Override
	public int getMaxWeight(PlayerEntity crafter) {
		return 1;
	}
	
	@Override
	public int getSizeInventory() {
		return 4;
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
		
		ItemStack stack = slots[index].split(count);
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
			return stack.getItem() instanceof BlankScroll;
		}
		
		
		if (!(stack.getItem() instanceof SpellRune))
			return false;
		
		if (index == 1) {
			return SpellRune.isTrigger(stack);
		}

		return true;
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
	
	public Spell craft(PlayerEntity crafter, String name, int iconIndex) {
		ItemStack stack = this.getStackInSlot(0);
		if (stack.isEmpty() || !(stack.getItem() instanceof BlankScroll)) {
			return null;
		}
		
		Spell spell = SpellCreationGui.SpellCreationContainer.craftSpell(
				name, iconIndex, this, crafter, new LinkedList<ITextComponent>(), new LinkedList<ITextComponent>(), true);
		
		if (spell != null) {
			spell.promoteFromTrans();
			spell.setIcon(iconIndex);
			ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
			SpellScroll.setSpell(scroll, spell);
			this.clearSpellBoard();
			this.setScrollSlotContents(scroll);
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

	@Override
	public ISpellCraftingInventory getSpellCraftingInventory() {
		return this;
	}
}