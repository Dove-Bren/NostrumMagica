package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.SpellPlate;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class OutcomeEnchantItem implements IRitualOutcome {

	private Enchantment enchantment;
	private int level;
	
	public OutcomeEnchantItem(Enchantment enchantment, int level) {
		this.enchantment = enchantment;
		this.level = level;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
		if (recipe.getTier() == 0 || centerItem.isEmpty()) {
			// enchant item on player
			ItemStack item = player.getHeldItemMainhand();
			if (item.isEmpty())
				return;
			
			if (!enchantment.canApply(item)) {
				return;
			}
			
			item.addEnchantment(enchantment, level);
		} else {
			if (!enchantment.canApply(centerItem)) {
				altar.setItem(centerItem);
				return;
			}
			
			centerItem.addEnchantment(enchantment, level);
			altar.setItem(centerItem);
		}
	}
	
	@Override
	public String getName() {
		return "enchant_item";
	}

	@Override
	public List<ITextComponent> getDescription() {
		String name = I18n.format(enchantment.getName(), (Object[]) null);
		String level = SpellPlate.toRoman(this.level);
		return TextUtils.GetTranslatedList("ritual.outcome.enchant.desc",
				new Object[] {name, level});
	}
}
