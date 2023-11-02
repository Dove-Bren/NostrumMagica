package com.smanzana.nostrummagica.effects;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.crafting.NostrumTags;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum NostrumPotions {

	MANAREGEN("mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new EffectInstance(NostrumEffects.manaRegen, 20 * 60)),
	MANAREGEN_EXTENDED("extended_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new EffectInstance(NostrumEffects.manaRegen, 20 * 3 * 60)),
	MANAREGEN_STRONG("strong_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new EffectInstance(NostrumEffects.manaRegen, 20 * 60, 1)),
	MANAREGEN_REALLY_STRONG("reallystrong_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new EffectInstance(NostrumEffects.manaRegen, 20 * 60, 2)),
	MANAREGEN_STRONG_AND_LONG("strongandlong_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new EffectInstance(NostrumEffects.manaRegen, 20 * 3 * 60, 1)),
	;
	
	private final String registryName;
	private final Supplier<String> effectNameSupp;
	private final Supplier<EffectInstance> effectsSupp;
	
	private Potion type;
	
	private NostrumPotions(String registryName, Supplier<String> effectName, Supplier<EffectInstance> effects) {
		// Note: using suppliers because effects won't be set up when enum is done.
		this.registryName = registryName;
		this.effectNameSupp = effectName;
		this.effectsSupp = effects;
	}
	
	protected Potion getTypeInternal() {
		if (type == null) {
			this.type = new Potion(effectNameSupp.get(), effectsSupp.get());
			type.setRegistryName(new ResourceLocation(NostrumMagica.MODID, registryName));
		}
		return this.type;
	}
	
	public Potion getType() {
		return getTypeInternal();
	}
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<Potion> event) {
		for (NostrumPotions wrapper : NostrumPotions.values()) {
			event.getRegistry().register(wrapper.getTypeInternal());
		}
		
		registerPotionMixes();
	}
	
	protected static final void registerPotionMixes() {
		// Mana regen potion
    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(Potions.THICK),
    			Ingredient.fromTag(NostrumTags.Items.ReagentManiDust),
    			MakePotion(NostrumPotions.MANAREGEN.getType()));
    	
    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
    			Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
    			MakePotion(NostrumPotions.MANAREGEN_EXTENDED.getType()));
    	
    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
    			Ingredient.fromTag(Tags.Items.DUSTS_GLOWSTONE),
    			MakePotion(NostrumPotions.MANAREGEN_STRONG.getType()));
	}
	
	public static final ItemStack MakePotion(Potion potion) {
		return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), potion);
	}
	
	public static class PotionIngredient extends IngredientNBT {

		public PotionIngredient(Potion potion) {
			super(MakePotion(potion));
		}
		
	}
	
}
