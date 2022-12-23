package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum NostrumPotionTypes {

	MANAREGEN("mana-regen", ManaRegenPotion.instance().getEffectName(), new PotionEffect(ManaRegenPotion.instance(), 20 * 60)),
	MANAREGEN_EXTENDED("extended_mana-regen", ManaRegenPotion.instance().getEffectName(), new PotionEffect(ManaRegenPotion.instance(), 20 * 3 * 60)),
	MANAREGEN_STRONG("strong_mana-regen", ManaRegenPotion.instance().getEffectName(), new PotionEffect(ManaRegenPotion.instance(), 20 * 60, 1)),
	MANAREGEN_REALLY_STRONG("reallystrong_mana-regen", ManaRegenPotion.instance().getEffectName(), new PotionEffect(ManaRegenPotion.instance(), 20 * 60, 2)),
	MANAREGEN_STRONG_AND_LONG("strongandlong_mana-regen", ManaRegenPotion.instance().getEffectName(), new PotionEffect(ManaRegenPotion.instance(), 20 * 3 * 60, 1)),
	;
	
	private final PotionType type;
	
	private NostrumPotionTypes(String registryName, String effectName, PotionEffect ... effects) {
		this.type = new PotionType(effectName, effects);
		type.setRegistryName(new ResourceLocation(NostrumMagica.MODID, registryName));
	}
	
	public PotionType getType() {
		return type;
	}
	
	public static void register(IForgeRegistry<PotionType> registry) {
		for (NostrumPotionTypes wrapper : NostrumPotionTypes.values()) {
			registry.register(wrapper.type);
		}
	}
	
}
