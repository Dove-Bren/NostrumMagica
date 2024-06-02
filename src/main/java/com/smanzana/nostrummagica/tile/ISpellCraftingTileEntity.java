package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.entity.player.PlayerEntity;

public interface ISpellCraftingTileEntity {

	public ISpellCraftingInventory getSpellCraftingInventory();
	
	public Spell craft(PlayerEntity crafter, ISpellCraftingInventory inventory, String name, int iconIndex, @Nullable SpellCraftPattern pattern);
	
}
