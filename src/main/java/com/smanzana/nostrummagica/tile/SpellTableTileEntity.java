package com.smanzana.nostrummagica.tile;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.item.BlankScroll;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.equipment.SpellScroll;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SpellTableTileEntity extends BlockEntity implements ISpellCraftingInventory, ISpellCraftingTileEntity {

	private static final String NBT_INV = "inventory";
	
	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-16 - Rune Slots
	 *   17-25 - Reagent slots
	 */
	
	private @Nonnull ItemStack slots[];
	
	public SpellTableTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.SpellTable, pos, state);
		slots = new ItemStack[getContainerSize()];
		for (int i = 0; i < slots.length; i++)
			slots[i] = ItemStack.EMPTY;
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
	public int getContainerSize() {
		return 26;
	}

	@Override
	public @Nonnull ItemStack getItem(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		return slots[index];
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		if (index < 0 || index >= getContainerSize() || slots[index].isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index].split(count);
		this.setChanged();
		
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots[index];
		slots[index] = ItemStack.EMPTY;
		
		this.setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, @Nonnull ItemStack stack) {
		if (index < 0 || index >= getContainerSize())
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
			return stack.getItem() instanceof BlankScroll;
		}
		
		if (index < getReagentSlotIndex()) {
		
			if (!(stack.getItem() instanceof SpellRune))
				return false;

			return true;
		}
		
		// Reagent bag
		return stack.getItem() instanceof ReagentItem;
		
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getContainerSize(); i++)
			removeItemNoUpdate(i);
	}
	
	public void clearBoard() {
		for (int i = 0; i < getReagentSlotIndex(); i++) {
			removeItemNoUpdate(i);
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
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
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null || !nbt.contains(NBT_INV, Tag.TAG_COMPOUND))
			return;
		
		CompoundTag items = nbt.getCompound(NBT_INV);
		for (String key : items.getAllKeys()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumMagica.logger.error("Failed reading SpellTable inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.of(items.getCompound(key));
			this.setItem(id, stack);
		}
	}
	
	@Override
	public RegisteredSpell craft(Player crafter, ISpellCraftingInventory inventory, String name, int iconIndex, @Nullable SpellCraftPattern pattern) {
		ItemStack stack = this.getItem(0);
		if (stack.isEmpty() || !(stack.getItem() instanceof BlankScroll)) {
			return null;
		}
		
		SpellCraftContext context = new SpellCraftContext(crafter, this.level, this.worldPosition);
		Spell tempSpell = SpellCreationGui.SpellCreationContainer.craftSpell(
				context, pattern, name, iconIndex, this, crafter, null, null, null, true);
		RegisteredSpell spell = null;
		
		if (tempSpell != null) {
			spell = RegisteredSpell.WrapAndRegister(tempSpell);
			spell.setIcon(iconIndex);
			ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
			SpellScroll.setSpell(scroll, spell);
			this.clearBoard();
			this.setItem(0, scroll);
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

	@Override
	public int getRuneSlotStartingIndex() {
		return this.getRuneSlotIndex();
	}

	@Override
	public int getMaxWeight(Player crafter) {
		return 5;
	}
}