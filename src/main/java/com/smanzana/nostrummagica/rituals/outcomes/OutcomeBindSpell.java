package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeBindSpell implements IRitualOutcome {

	public OutcomeBindSpell() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Take the spell and tome and begin the player binding
		
		// Tome has to be center.
		ItemStack tome = centerItem;
		ItemStack scroll = null;
		if (otherItems != null && otherItems.length > 0)
		for (ItemStack other : otherItems) {
			if (other != null && other.getItem() instanceof SpellScroll) {
				scroll = other;
				break;
			}
		}
		
		if (tome == null || !(tome.getItem() instanceof SpellTome)
				|| scroll == null)
			return;
		
		AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
		altar.setItem(centerItem);
		
		if (!SpellTome.isOwner(tome, player)) {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tome.noowner"));
				altar = (AltarTileEntity) world.getTileEntity(center.add(4, 0, 0));
				altar.setItem(scroll);
			}
			return;
		}
		
		int capacity = SpellTome.getCapacity(tome);
		List<Spell> spells = SpellTome.getSpells(tome);
		int taken = spells == null ? 0 : spells.size();
		if (taken >= capacity) {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tome.full"));
				altar = (AltarTileEntity) world.getTileEntity(center.add(4, 0, 0));
				altar.setItem(scroll);
			}
			return;
			
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		Spell spell = SpellScroll.getSpell(scroll);
		if (spell == null)
			return;
		SpellComponentWrapper comp = spell.getRandomComponent();
		if (comp == null)
			return;
		
		String compName;
		if (comp.isAlteration())
			compName = comp.getAlteration().getName();
		else if (comp.isElement())
			compName = comp.getElement().getName();
		else if (comp.isTrigger())
			compName = comp.getTrigger().getDisplayName();
		else if (comp.isShape())
			compName = comp.getShape().getDisplayName();
		else
			compName = "Physic";
		
		attr.startBinding(spell, comp, SpellTome.getTomeID(tome));
		player.addChatComponentMessage(new TextComponentTranslation("info.tome.bind", new Object[] {spell.getName(), compName}));
	}
}
