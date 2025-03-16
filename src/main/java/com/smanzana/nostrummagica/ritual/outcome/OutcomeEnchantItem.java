package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.SpellPlate;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class OutcomeEnchantItem implements IRitualOutcome {

	private Enchantment enchantment;
	private int level;
	
	public OutcomeEnchantItem(Enchantment enchantment, int level) {
		this.enchantment = enchantment;
		this.level = level;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getBlockEntity(center);
		final ItemStack centerItem = layout.getCenterItem(world, center);
		if (recipe.getTier() == 0 || centerItem.isEmpty()) {
			// enchant item on player
			ItemStack item = player.getMainHandItem();
			if (item.isEmpty())
				return;
			
			if (!enchantment.canEnchant(item)) {
				return;
			}
			
			item.enchant(enchantment, level);
		} else {
			if (!enchantment.canEnchant(centerItem)) {
				altar.setItem(centerItem);
				return;
			}
			
			centerItem.enchant(enchantment, level);
			altar.setItem(centerItem);
		}
	}
	
	@Override
	public String getName() {
		return "enchant_item";
	}

	@Override
	public List<Component> getDescription() {
		String name = I18n.get(enchantment.getDescriptionId(), (Object[]) null);
		String level = SpellPlate.toRoman(this.level);
		return TextUtils.GetTranslatedList("ritual.outcome.enchant.desc",
				new Object[] {name, level});
	}
}
