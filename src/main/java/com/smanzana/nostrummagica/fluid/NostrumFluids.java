package com.smanzana.nostrummagica.fluid;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumFluids {
	
	private static final String ID_PUREWATER = "pure_water";
	private static final String ID_PUREWATER_FLOWING = ID_PUREWATER + "_flowing";
	
	@ObjectHolder(PoisonWaterFluid.ID_BREAKABLE) public static PoisonWaterFluid poisonWater;
	@ObjectHolder(PoisonWaterFluid.ID_BREAKABLE_FLOWING) public static PoisonWaterFluid poisonWaterFlowing;
	@ObjectHolder(PoisonWaterFluid.ID_UNBREAKABLE) public static PoisonWaterFluid unbreakablePoisonWater;
	@ObjectHolder(PoisonWaterFluid.ID_UNBREAKABLE_FLOWING) public static PoisonWaterFluid unbreakablePoisonWaterFlowing;
	@ObjectHolder(MysticWaterFluid.ID) public static MysticWaterFluid mysticWater;
	@ObjectHolder(ID_PUREWATER) public static FlowingFluid pureWater;
	@ObjectHolder(ID_PUREWATER_FLOWING) public static FlowingFluid pureWaterFlowing;
	
	private static final ForgeFlowingFluid.Properties PROPS_PUREWATER = new ForgeFlowingFluid.Properties(() -> pureWater, () -> pureWaterFlowing,
			FluidAttributes.builder(NostrumMagica.Loc("block/" + ID_PUREWATER), NostrumMagica.Loc("block/" + ID_PUREWATER_FLOWING))
				.color(0xCF8dd1f0).density(15).luminosity(3).viscosity(2).overlay(new ResourceLocation("minecraft:block/water_overlay")).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY))
			.slopeFindDistance(4).levelDecreasePerBlock(1)
			.block(() -> NostrumBlocks.pureWater).bucket(() -> NostrumItems.pureWaterBucket)
			;
	
	@SubscribeEvent
    public static void registerFluidBlocks(RegistryEvent.Register<Fluid> event) {
		final IForgeRegistry<Fluid> registry = event.getRegistry();

		registry.register(new PoisonWaterFluid.Source(false).setRegistryName(PoisonWaterFluid.ID_BREAKABLE));
		registry.register(new PoisonWaterFluid.Flowing(false).setRegistryName(PoisonWaterFluid.ID_BREAKABLE_FLOWING));
		registry.register(new PoisonWaterFluid.Source(true).setRegistryName(PoisonWaterFluid.ID_UNBREAKABLE));
		registry.register(new PoisonWaterFluid.Flowing(true).setRegistryName(PoisonWaterFluid.ID_UNBREAKABLE_FLOWING));
		registry.register(new MysticWaterFluid().setRegistryName(MysticWaterFluid.ID));
		registry.register(new ForgeFlowingFluid.Source(PROPS_PUREWATER).setRegistryName(ID_PUREWATER));
		registry.register(new ForgeFlowingFluid.Flowing(PROPS_PUREWATER).setRegistryName(ID_PUREWATER_FLOWING));
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
    	
    	registry.register(new BucketItem(() -> {return NostrumFluids.pureWater;}, NostrumItems.PropUnstackable())
    			.setRegistryName(ID_PUREWATER + "_bucket")
    			);
    }
}
