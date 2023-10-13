package com.smanzana.nostrummagica.world.dimension;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dimension.NostrumEmptyDimension.EmptyDimensionFactory;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ObjectHolder;

@EventBusSubscriber(modid = NostrumMagica.MODID, bus = EventBusSubscriber.Bus.FORGE) // for RegisterDimensionsEvent
@ObjectHolder(NostrumMagica.MODID)
public class NostrumDimensions {

	@ObjectHolder(NostrumEmptyDimension.TYPE_ID) public static DimensionType EmptyDimension;
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onRegisterDim(@Nonnull final RegisterDimensionsEvent event) {
		if(!DimensionManager.getRegistry().containsKey(new ResourceLocation(NostrumMagica.MODID, NostrumEmptyDimension.TYPE_ID)))
		{
			DimensionManager.registerDimension(new ResourceLocation(NostrumMagica.MODID, NostrumEmptyDimension.TYPE_ID), NostrumDimensionWrappers.EmptyDimensionWrapper,
					null, false);
		}
		else
		{
			; // already registered
		}
	}
	
	@EventBusSubscriber(modid = NostrumMagica.MODID, bus = EventBusSubscriber.Bus.MOD) // for ModDimension registry event
	protected static class NostrumDimensionWrappers {
		
		private static final String EMPTY_DIM_WRAPPER_ID = NostrumEmptyDimension.TYPE_ID + "_moddim";
		
		@ObjectHolder(EMPTY_DIM_WRAPPER_ID) public static EmptyDimensionFactory EmptyDimensionWrapper;
		
		@SubscribeEvent
		public static void onRegisterDimType(@Nonnull final RegistryEvent.Register<ModDimension> event) {
			event.getRegistry().register(new EmptyDimensionFactory().setRegistryName(EMPTY_DIM_WRAPPER_ID));
		}

	}
	
}
