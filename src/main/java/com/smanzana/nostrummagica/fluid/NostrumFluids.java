package com.smanzana.nostrummagica.fluid;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFluids {
	
	@ObjectHolder(PoisonWaterFluid.ID_BREAKABLE) public static PoisonWaterFluid poisonWater;
	@ObjectHolder(PoisonWaterFluid.ID_BREAKABLE_FLOWING) public static PoisonWaterFluid poisonWaterFlowing;
	@ObjectHolder(PoisonWaterFluid.ID_UNBREAKABLE) public static PoisonWaterFluid unbreakablePoisonWater;
	@ObjectHolder(PoisonWaterFluid.ID_UNBREAKABLE_FLOWING) public static PoisonWaterFluid unbreakablePoisonWaterFlowing;
	@ObjectHolder(MysticWaterFluid.ID) public static MysticWaterFluid mysticWater;
	
	@SubscribeEvent
    public static void registerFluidBlocks(RegistryEvent.Register<Fluid> event) {
		final IForgeRegistry<Fluid> registry = event.getRegistry();

		registry.register(new PoisonWaterFluid.Source(false).setRegistryName(PoisonWaterFluid.ID_BREAKABLE));
		registry.register(new PoisonWaterFluid.Flowing(false).setRegistryName(PoisonWaterFluid.ID_BREAKABLE_FLOWING));
		registry.register(new PoisonWaterFluid.Source(true).setRegistryName(PoisonWaterFluid.ID_UNBREAKABLE));
		registry.register(new PoisonWaterFluid.Flowing(true).setRegistryName(PoisonWaterFluid.ID_UNBREAKABLE_FLOWING));
		registry.register(new MysticWaterFluid().setRegistryName(MysticWaterFluid.ID));
    }
    
    @SubscribeEvent
    public static void registerFluidItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	registry.register(new BucketItem(() -> {return NostrumFluids.poisonWater;}, NostrumItems.PropUnstackable())
    			.setRegistryName(PoisonWaterFluid.ID_BREAKABLE + "_bucket")
    			);
    	
    	registry.register(new BucketItem(() -> {return NostrumFluids.unbreakablePoisonWater;}, NostrumItems.PropUnstackable())
    			.setRegistryName(PoisonWaterFluid.ID_UNBREAKABLE + "_bucket")
    			);
    }
}
