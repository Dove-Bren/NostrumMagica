package com.smanzana.nostrummagica.tiles;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.spellcraft.SpellCraftPattern;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class MysticSpellTableEntity extends BasicSpellTableEntity {

	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-8 - Rune Slots
	 */
	
	private @Nonnull ItemStack slots[];
	
	public MysticSpellTableEntity() {
		super(NostrumTileEntities.MysticSpellTableType);
	}
	
	@Override
	public int getRuneSlotStartingIndex() {
		return 1;
	}
	
	public int getRuneSlotCount() {
		return 8;
	}
	
	public int getScrollSlotIndex() {
		return 0;
	}

	@Override
	public int getMaxWeight(PlayerEntity crafter) {
		return 10;
	}
	
	@Override
	public int getSizeInventory() {
		return getRuneSlotCount() + 1;
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
		int unused; // use pattern!
		return super.craft(crafter, inventory, name, iconIndex, pattern);
	}

	public static SpellCraftPattern[] CalculatePatternChoices(PlayerEntity crafter,
			ISpellCraftingInventory tableInventory, BlockPos tablePos) {
		int unused; // Make based on player... and maybe world and position?
		Collection<SpellCraftPattern> values = SpellCraftPattern.GetAll();
		
		SpellCraftPattern[] choices = new SpellCraftPattern[values.size() + 1];
		
		// Always put null/none as first choice
		choices[0] = null;
		int index = 1;
		for (SpellCraftPattern pattern : values) {
			choices[index++] = pattern;
		}
		
		return choices;
	}
}