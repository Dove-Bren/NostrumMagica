package com.smanzana.nostrummagica.effect;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.crafting.NostrumTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum NostrumPotions {

	MANAREGEN("mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new MobEffectInstance(NostrumEffects.manaRegen, 20 * 60)),
	MANAREGEN_EXTENDED("extended_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new MobEffectInstance(NostrumEffects.manaRegen, 20 * 3 * 60)),
	MANAREGEN_STRONG("strong_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new MobEffectInstance(NostrumEffects.manaRegen, 20 * 60, 1)),
	MANAREGEN_REALLY_STRONG("reallystrong_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new MobEffectInstance(NostrumEffects.manaRegen, 20 * 60, 2)),
	MANAREGEN_STRONG_AND_LONG("strongandlong_mana-regen", () -> NostrumEffects.manaRegen.getEffectName(), () -> new MobEffectInstance(NostrumEffects.manaRegen, 20 * 3 * 60, 1)),
	SWIFT_CAST("swift_cast", () -> SwiftCastEffect.ID_INSTANT, () -> new MobEffectInstance(NostrumEffects.swiftCast, 20 * 15, 0)),
	SWIFT_CAST_TRIPLE("swift_cast_tri", () -> SwiftCastEffect.ID_INSTANT, () -> new MobEffectInstance(NostrumEffects.swiftCast, 20 * 15, 2)),
	SWIFT_CAST_LONG("lasting_swift_cast", () -> SwiftCastEffect.ID_INSTANT, () -> new MobEffectInstance(NostrumEffects.lastingSwiftCast, 20 * 15)),
	;
	
	private final String registryName;
	private final Supplier<String> effectNameSupp;
	private final Supplier<MobEffectInstance> effectsSupp;
	
	private Potion type;
	
	private NostrumPotions(String registryName, Supplier<String> effectName, Supplier<MobEffectInstance> effects) {
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
	}
	
	@SubscribeEvent
	public static final void registerPotionMixes(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			// Mana regen potion
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(Potions.THICK),
	    			Ingredient.of(NostrumTags.Items.ReagentManiDust),
	    			MakePotion(NostrumPotions.MANAREGEN.getType()));
	    	
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
	    			Ingredient.of(Tags.Items.DUSTS_REDSTONE),
	    			MakePotion(NostrumPotions.MANAREGEN_EXTENDED.getType()));
	    	
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
	    			Ingredient.of(Tags.Items.DUSTS_GLOWSTONE),
	    			MakePotion(NostrumPotions.MANAREGEN_STRONG.getType()));
	    	
	    	// Swift cast potion
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
	    			Ingredient.of(NostrumTags.Items.ReagentBlackPearl),
	    			MakePotion(NostrumPotions.SWIFT_CAST.getType()));
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.SWIFT_CAST.getType()),
	    			Ingredient.of(NostrumTags.Items.Essence),
	    			MakePotion(NostrumPotions.SWIFT_CAST_TRIPLE.getType()));
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.SWIFT_CAST_TRIPLE.getType()),
	    			Ingredient.of(NostrumTags.Items.WispPebble),
	    			MakePotion(NostrumPotions.SWIFT_CAST_LONG.getType()));
		});
	}
	
	public static final ItemStack MakePotion(Potion potion) {
		return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
	}
	
	public static class PotionIngredient extends NBTIngredient {

		public PotionIngredient(Potion potion) {
			super(MakePotion(potion));
		}
		
	}
	
}
