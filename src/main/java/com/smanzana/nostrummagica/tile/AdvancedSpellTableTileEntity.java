package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class AdvancedSpellTableTileEntity extends BasicSpellTableTileEntity {

	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-6 - Rune Slots
	 */
	
	private @Nonnull ItemStack slots[];
	
	public AdvancedSpellTableTileEntity() {
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
	public int getContainerSize() {
		return 6;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
	}
	
	@Override
	public Spell craft(PlayerEntity crafter, ISpellCraftingInventory inventory, String name, int iconIndex, @Nullable SpellCraftPattern pattern) {
		return super.craft(crafter, inventory, name, iconIndex, pattern);
	}
}