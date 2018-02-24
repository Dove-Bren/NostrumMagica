package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.rituals.RitualRecipe;

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

	
	
}
