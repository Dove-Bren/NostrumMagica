package com.smanzana.nostrummagica.crafting;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumCrafting {
	
	@ObjectHolder(BaubleColorRecipe.Serializer.ID) public static BaubleColorRecipe.Serializer baubleColorSerializer;
	@ObjectHolder(ShapedWithRemainingRecipe.Serializer.ID) public static ShapedWithRemainingRecipe.Serializer shapedWithRemainingSerializer;
	@ObjectHolder(SpellTomePageCombineRecipe.SERIALIZER_ID) public static SpecialRecipeSerializer<SpellTomePageCombineRecipe> spellTomePageCombineSerializer;
	@ObjectHolder(RuneCombineRecipe.SERIALIZER_ID) public static SpecialRecipeSerializer<RuneCombineRecipe> runeCombineSerializer;
	
	@SubscribeEvent
	public static void registerSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
		final IForgeRegistry<IRecipeSerializer<?>> registry = event.getRegistry();
		
		registry.register(new BaubleColorRecipe.Serializer().setRegistryName(BaubleColorRecipe.Serializer.ID));
		registry.register(new ShapedWithRemainingRecipe.Serializer().setRegistryName(ShapedWithRemainingRecipe.Serializer.ID));
		
		registry.register(new SpecialRecipeSerializer<SpellTomePageCombineRecipe>(SpellTomePageCombineRecipe::new)
				.setRegistryName(SpellTomePageCombineRecipe.SERIALIZER_ID));
		registry.register(new SpecialRecipeSerializer<RuneCombineRecipe>(RuneCombineRecipe::new)
				.setRegistryName(RuneCombineRecipe.SERIALIZER_ID));
	}
}
