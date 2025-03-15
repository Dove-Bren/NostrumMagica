package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OutcomeBindSpell implements IRitualOutcome {

	public OutcomeBindSpell() {
		;
	}
	
	@Override
	public boolean canPerform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout) {
		ItemStack tome = layout.getCenterItem(world, center);
		ItemStack scroll = ItemStack.EMPTY;
		List<ItemStack> extras = layout.getExtraItems(world, center);
		if (extras != null && extras.size() > 0)
		for (ItemStack other : extras) {
			if (!other.isEmpty() && other.getItem() instanceof SpellScroll) {
				scroll = other;
				break;
			}
		}
		
		if (tome.isEmpty() || !(tome.getItem() instanceof SpellTome)
				|| scroll.isEmpty())
			return false;
		
		Spell spell = SpellScroll.GetSpell(scroll);
		if (spell == null) {
			if (!player.level.isClientSide) {
				player.sendMessage(new StringTextComponent("The scroll is missing it's spell..."), Util.NIL_UUID);
			}
			return false;
		}
		
		if (!SpellTome.hasRoom(tome, spell)) {
			if (!player.level.isClientSide) {
				player.sendMessage(new TranslationTextComponent("info.tome.full"), Util.NIL_UUID);
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Take the spell and tome and begin the player binding
		
		// Tome has to be center.
		ItemStack tome = layout.getCenterItem(world, center);
		ItemStack scroll = ItemStack.EMPTY;
		List<ItemStack> otherItems = layout.getExtraItems(world, center);
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
		
		AltarTileEntity altar = (AltarTileEntity) world.getBlockEntity(center);
		altar.setItem(tome); // Re-set tome back into altar
		
		if (!SpellTome.startBinding(player, tome, scroll)) {
			altar = (AltarTileEntity) world.getBlockEntity(center.offset(4, 0, 0));
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
