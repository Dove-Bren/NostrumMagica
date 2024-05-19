package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.spellcraft.SpellCraftPattern;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class AdvancedSpellTableEntity extends BasicSpellTableEntity {

	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-6 - Rune Slots
	 */
	
	private @Nonnull ItemStack slots[];
	
	public AdvancedSpellTableEntity() {
		super(NostrumTileEntities.AdvancedSpellTableType);
	}
	
	@Override
	public int getRuneSlotStartingIndex() {
		return 1;
	}
	
	public int getRuneSlotCount() {
		return 5;
	}
	
	public int getScrollSlotIndex() {
		return 0;
	}

	@Override
	public int getMaxWeight(PlayerEntity crafter) {
		return 5;
	}
	
	@Override
	public int getSizeInventory() {
		return 6;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
	}
	
	@Override
	public Spell craft(PlayerEntity crafter, ISpellCraftingInventory inventory, String name, int iconIndex, @Nullable SpellCraftPattern pattern) {
		return super.craft(crafter, inventory, name, iconIndex, pattern);
	}
}