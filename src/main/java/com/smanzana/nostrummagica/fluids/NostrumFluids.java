package com.smanzana.nostrummagica.fluids;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.NostrumItems;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFluids {
	
	@ObjectHolder(FluidPoisonWater.ID_BREAKABLE) public static FluidPoisonWater poisonWater;
	@ObjectHolder(FluidPoisonWater.ID_BREAKABLE_FLOWING) public static FluidPoisonWater poisonWaterFlowing;
	@ObjectHolder(FluidPoisonWater.ID_UNBREAKABLE) public static FluidPoisonWater unbreakablePoisonWater;
	@ObjectHolder(FluidPoisonWater.ID_UNBREAKABLE_FLOWING) public static FluidPoisonWater unbreakablePoisonWaterFlowing;
	
	@SubscribeEvent
    public static void registerFluidBlocks(RegistryEvent.Register<Fluid> event) {
		final IForgeRegistry<Fluid> registry = event.getRegistry();

		registry.register(new FluidPoisonWater.Source(false).setRegistryName(FluidPoisonWater.ID_BREAKABLE));
		registry.register(new FluidPoisonWater.Flowing(false).setRegistryName(FluidPoisonWater.ID_BREAKABLE_FLOWING));
		registry.register(new FluidPoisonWater.Source(true).setRegistryName(FluidPoisonWater.ID_UNBREAKABLE));
		registry.register(new FluidPoisonWater.Flowing(true).setRegistryName(FluidPoisonWater.ID_UNBREAKABLE_FLOWING));
    }
    
    @SubscribeEvent
    public static void registerFluidItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	registry.register(new BucketItem(() -> {return NostrumFluids.poisonWater;}, NostrumItems.PropUnstackable())
    			.setRegistryName(FluidPoisonWater.ID_BREAKABLE + "_bucket")
    			);
    	
    	registry.register(new BucketItem(() -> {return NostrumFluids.unbreakablePoisonWater;}, NostrumItems.PropUnstackable())
    			.setRegistryName(FluidPoisonWater.ID_UNBREAKABLE + "_bucket")
    			);
    }
}
