package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.spellcraft.SpellCraftPattern;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.player.PlayerEntity;

public interface ISpellCraftingTileEntity {

	public ISpellCraftingInventory getSpellCraftingInventory();
	
	public Spell craft(PlayerEntity crafter, ISpellCraftingInventory inventory, String name, int iconIndex, @Nullable SpellCraftPattern pattern);
	
}
