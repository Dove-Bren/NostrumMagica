package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedSpellTableTileEntity extends BasicSpellTableTileEntity {

	/**
	 * Inventory:
	 *   0 - Spell scroll slot
	 *   1-6 - Rune Slots
	 */
	
	private @Nonnull ItemStack slots[];
	
	public AdvancedSpellTableTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.AdvancedSpellTable, pos, state);
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
	public int getMaxWeight(Player crafter) {
		return 5;
	}
	
	@Override
	public int getContainerSize() {
		return 6;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
	}
	
	@Override
	public RegisteredSpell craft(Player crafter, ISpellCraftingInventory inventory, String name, int iconIndex, @Nullable SpellCraftPattern pattern) {
		return super.craft(crafter, inventory, name, iconIndex, pattern);
	}
}