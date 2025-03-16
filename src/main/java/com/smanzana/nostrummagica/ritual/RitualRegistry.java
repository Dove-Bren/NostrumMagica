package com.smanzana.nostrummagica.ritual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class RitualRegistry {
	
	private static RitualRegistry instance;
	public static RitualRegistry instance() {
		if (instance == null)
			instance = new RitualRegistry();
		
		return instance;
	}
	
//	@SubscribeEvent
//	public static void onRegistryCreate(RegistryEvent.NewRegistry event) {
//		REGISTRY = new RegistryBuilder<RitualRecipe>()
//				.setName(new ResourceLocation(NostrumMagica.MODID, "rituals"))
//				.setType(RitualRecipe.class)
//				.setMaxID(Integer.MAX_VALUE - 1) // copied from GameData, AKA Forge's registration
//				//.addCallback(new NamespacedWrapper.Factory<>()) // not sure what this is
//				.disableSaving()
//				.create();
//	}
	
	private final List<RitualRecipe> ritualRegistry;
	private final Set<IRitualListener> ritualListeners;
	
	private RitualRegistry() {
		ritualRegistry = new ArrayList<>();
		ritualListeners = new HashSet<>();
	}

	public void register(RitualRecipe ritual) {
		ritualRegistry.add(ritual);
	}
	
	public void addRitualListener(IRitualListener listener) {
		ritualListeners.add(listener);
	}
	
	public void unregisterListener(IRitualListener listener) {
		ritualListeners.remove(listener);
	}
	
	public void fireRitualPerformed(RitualRecipe ritual, Level world, Player player, BlockPos pos) {
		for (IRitualListener listener : RitualRegistry.instance().ritualListeners) {
			listener.onRitualPerformed(ritual, world, player, pos);
		}
	}

	public List<RitualRecipe> getRegisteredRituals() {
		return new ArrayList<RitualRecipe>(ritualRegistry);
	}
	
	protected void clear() {
		this.ritualRegistry.clear();
	}
	
	public static class RitualRegisterEvent extends Event {
		public final RitualRegistry registry;
		
		protected RitualRegisterEvent(RitualRegistry registry) {
			this.registry = registry;
		}
	}
	
	public final void reloadRituals() {
		this.clear();
		MinecraftForge.EVENT_BUS.post(new RitualRegisterEvent(this));
	}
}
