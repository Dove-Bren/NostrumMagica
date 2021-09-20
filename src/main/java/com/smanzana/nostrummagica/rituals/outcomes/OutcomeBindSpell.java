package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeBindSpell implements IRitualOutcome {

	public OutcomeBindSpell() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// Take the spell and tome and begin the player binding
		
		// Tome has to be center.
		ItemStack tome = centerItem;
		ItemStack scroll = ItemStack.EMPTY;
		if (otherItems != null && otherItems.size() > 0)
		for (ItemStack other : otherItems) {
			if (!other.isEmpty() && other.getItem() instanceof SpellScroll) {
				scroll = other;
				break;
			}
		}
		
		if (tome.isEmpty() || !(tome.getItem() instanceof SpellTome)
				|| scroll.isEmpty())
			return;
		
		AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
		altar.setItem(centerItem);
		
		if (!SpellTome.startBinding(player, tome, scroll, true)) {
			altar = (AltarTileEntity) world.getTileEntity(center.add(4, 0, 0));
			altar.setItem(scroll);
		}
	}
	
	@Override
	public String getName() {
		return "bind_spell";
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.bind_spell.desc",
				(Object[]) null)
				.split("\\|"));
	}
}
