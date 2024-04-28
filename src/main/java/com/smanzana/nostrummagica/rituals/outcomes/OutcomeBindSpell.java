package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRecipe.RitualMatchInfo;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.utils.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OutcomeBindSpell implements IRitualOutcome {

	public OutcomeBindSpell() {
		;
	}
	
	@Override
	public boolean canPerform(World world, PlayerEntity player, BlockPos center, RitualMatchInfo ingredients) {
		ItemStack tome = ingredients.center;
		ItemStack scroll = ItemStack.EMPTY;
		if (ingredients.extras != null && ingredients.extras.size() > 0)
		for (ItemStack other : ingredients.extras) {
			if (!other.isEmpty() && other.getItem() instanceof SpellScroll) {
				scroll = other;
				break;
			}
		}
		
		if (tome.isEmpty() || !(tome.getItem() instanceof SpellTome)
				|| scroll.isEmpty())
			return false;
		
		if (!SpellTome.hasRoom(tome)) {
			if (!player.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tome.full"), Util.DUMMY_UUID);
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
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
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.bind_spell.desc");
	}
}
