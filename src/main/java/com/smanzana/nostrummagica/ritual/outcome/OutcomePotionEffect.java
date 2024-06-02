package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.SpellPlate;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
	public List<ITextComponent> getDescription() {
		String name = I18n.format(effect.getEffectName(), (Object[]) null);
		String display = SpellPlate.toRoman(effect.getAmplifier() + 1);
		String secs = "" + (effect.getDuration() / 20);
		
		return TextUtils.GetTranslatedList("ritual.outcome.potion_effect.desc",
				new Object[] {name, display, secs});
	}
}
