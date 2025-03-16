package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.SpellPlate;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class OutcomePotionEffect implements IRitualOutcome {

	private MobEffectInstance effect;
	
	public OutcomePotionEffect(MobEffect effect, int amplitude, int duration) {
		this.effect = new MobEffectInstance(effect, duration, amplitude);
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Apply effect to the player
		player.addEffect(new MobEffectInstance(effect)); // copy
	}
	
	@Override
	public String getName() {
		return "apply_potion_effect";
	}

	@Override
	public List<Component> getDescription() {
		String name = I18n.get(effect.getDescriptionId(), (Object[]) null);
		String display = SpellPlate.toRoman(effect.getAmplifier() + 1);
		String secs = "" + (effect.getDuration() / 20);
		
		return TextUtils.GetTranslatedList("ritual.outcome.potion_effect.desc",
				new Object[] {name, display, secs});
	}
}
