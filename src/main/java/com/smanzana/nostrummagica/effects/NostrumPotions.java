package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.items.NostrumItemTags;

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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum NostrumPotions {

	MANAREGEN("mana-regen", NostrumEffects.manaRegen.getEffectName(), new EffectInstance(NostrumEffects.manaRegen, 20 * 60)),
	MANAREGEN_EXTENDED("extended_mana-regen", NostrumEffects.manaRegen.getEffectName(), new EffectInstance(NostrumEffects.manaRegen, 20 * 3 * 60)),
	MANAREGEN_STRONG("strong_mana-regen", NostrumEffects.manaRegen.getEffectName(), new EffectInstance(NostrumEffects.manaRegen, 20 * 60, 1)),
	MANAREGEN_REALLY_STRONG("reallystrong_mana-regen", NostrumEffects.manaRegen.getEffectName(), new EffectInstance(NostrumEffects.manaRegen, 20 * 60, 2)),
	MANAREGEN_STRONG_AND_LONG("strongandlong_mana-regen", NostrumEffects.manaRegen.getEffectName(), new EffectInstance(NostrumEffects.manaRegen, 20 * 3 * 60, 1)),
	;
	
	private final Potion type;
	
	private NostrumPotions(String registryName, String effectName, EffectInstance ... effects) {
		this.type = new Potion(effectName, effects);
		type.setRegistryName(new ResourceLocation(NostrumMagica.MODID, registryName));
	}
	
	public Potion getType() {
		return type;
	}
	
	@SubscribeEvent
	public static void register(IForgeRegistry<Potion> registry) {
		for (NostrumPotions wrapper : NostrumPotions.values()) {
			registry.register(wrapper.type);
		}
		
		registerPotionMixes();
	}
	
	protected static final void registerPotionMixes() {
		// Mana regen potion
    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(Potions.THICK),
    			Ingredient.fromTag(NostrumItemTags.Items.ReagentManiDust),
    			MakePotion(NostrumPotions.MANAREGEN.getType()));
    	
    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
    			Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
    			MakePotion(NostrumPotions.MANAREGEN_EXTENDED.getType()));
    	
    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN.getType()),
    			Ingredient.fromTag(Tags.Items.DUSTS_GLOWSTONE),
    			MakePotion(NostrumPotions.MANAREGEN_STRONG.getType()));
    	
    	if (NostrumMagica.instance.aetheria.isEnabled()) {
    		BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN_STRONG.getType()),
        			Ingredient.fromStacks(NostrumMagica.instance.aetheria.getResourceItem(AetherResourceType.FLOWER_MANDRAKE, 1)),
        			MakePotion(NostrumPotions.MANAREGEN_REALLY_STRONG.getType()));
    		
    		BrewingRecipeRegistry.addRecipe(new PotionIngredient(NostrumPotions.MANAREGEN_STRONG.getType()),
        			Ingredient.fromStacks(NostrumMagica.instance.aetheria.getResourceItem(AetherResourceType.FLOWER_GINSENG, 1)),
        			MakePotion(NostrumPotions.MANAREGEN_STRONG_AND_LONG.getType()));
    	}
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
