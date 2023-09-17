package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomePotionEffect implements IRitualOutcome {

	private EffectInstance effect;
	
	public OutcomePotionEffect(Effect effect, int amplitude, int duration) {
		this.effect = new EffectInstance(effect, duration, amplitude);
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// Apply effect to the player
		player.addPotionEffect(new EffectInstance(effect)); // copy
	}
	
	@Override
	public String getName() {
		return "apply_potion_effect";
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
