package com.smanzana.nostrummagica.rituals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.rituals.RitualRecipe.RitualMatchInfo;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RitualRegistry {
	
	private static RitualRegistry instance;
	public static RitualRegistry instance() {
		if (instance == null)
			instance = new RitualRegistry();
		
		return instance;
	}
	
	private static IForgeRegistry<RitualRecipe> REGISTRY;
	
	@SubscribeEvent
	public static void onRegistryCreate(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<RitualRecipe>()
				.setName(new ResourceLocation(NostrumMagica.MODID, "rituals"))
				.setType(RitualRecipe.class)
				.setMaxID(Integer.MAX_VALUE - 1) // copied from GameData, AKA Forge's registration
				//.addCallback(new NamespacedWrapper.Factory<>()) // not sure what this is
				.disableSaving()
				.create();
	}
	
	//private List<RitualRecipe> knownRituals;
	private Set<IRitualListener> ritualListeners;
	
	private RitualRegistry() {
		//knownRituals = new LinkedList<>();
		ritualListeners = new HashSet<>();
	}

	// Use the registry event instead
	@Deprecated
	public void addRitual(RitualRecipe ritual) {
		//knownRituals.add(ritual);
	}
	
	public void addRitualListener(IRitualListener listener) {
		ritualListeners.add(listener);
	}
	
	public void unregisterListener(IRitualListener listener) {
		ritualListeners.remove(listener);
	}
	
	/**
	 * Checks the world surrounding the player for any valid ritual setups
	 * that match
	 * @param world
	 * @param pos
	 * @param player
	 * @param incantation
	 * @return
	 */
	public static boolean attemptRitual(World world, BlockPos pos, PlayerEntity player, EMagicElement element) {
		// All rituals have a candle or an altar in the center
		// For all candles and altars around the player, check if recipes match
		BlockState state = world.getBlockState(pos);
		if (state == null ||
			(!(state.getBlock() instanceof Candle) && !(state.getBlock() instanceof AltarBlock)))
			return false;
		
		// else it's an altar or a candle
		for (RitualRecipe ritual : REGISTRY) {
			final RitualMatchInfo result = ritual.matches(player, world, pos, element);
			if (result.matched) {
					if (ritual.perform(world, player, pos)) {
					
					if (!instance().ritualListeners.isEmpty()) {
						for (IRitualListener listener : instance().ritualListeners) {
							listener.onRitualPerformed(ritual, world, player, pos);
						}
					}
					
					NostrumMagicaSounds.AMBIENT_WOOSH2.play(world,
							pos.getX(), pos.getY(), pos.getZ());
	
					NostrumMagica.instance.proxy.playRitualEffect(world, pos, result.element == null ? EMagicElement.PHYSICAL : result.element,
							result.center, result.extras, result.reagents, result.output);
					
					return true;
				}
			}
		}
		
		return false;
	}

	public List<RitualRecipe> getRegisteredRituals() {
		return new ArrayList<RitualRecipe>(REGISTRY.getValues());
	}
}
