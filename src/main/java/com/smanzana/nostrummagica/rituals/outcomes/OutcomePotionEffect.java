package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomePotionEffect implements IRitualOutcome {

	private PotionEffect effect;
	
	public OutcomePotionEffect(Potion effect, int amplitude, int duration) {
		this.effect = new PotionEffect(effect, duration, amplitude);
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Apply effect to the player
		player.addPotionEffect(effect);
	}

	@Override
	public List<String> getDescription() {
		String name = I18n.format(effect.getEffectName(), (Object[]) null);
		String display = SpellPlate.toRoman(effect.getAmplifier() + 1);
		String secs = "" + (effect.getDuration() / 20);
		
		return Lists.newArrayList(I18n.format("ritual.outcome.potion_effect.desc",
				new Object[] {name, display, secs})
				.split("\\|"));
	}
}
