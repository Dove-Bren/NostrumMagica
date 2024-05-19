package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
	
	public Spell craft(PlayerEntity crafter, String name, int iconIndex) {
		return super.craft(crafter, name, iconIndex);
	}
}