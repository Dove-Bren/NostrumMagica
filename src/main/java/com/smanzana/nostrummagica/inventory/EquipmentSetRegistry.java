package com.smanzana.nostrummagica.inventory;

import java.util.Collection;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.set.EquipmentSet;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * Registry for item sets
 */
@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EquipmentSetRegistry {

	public static final RegistryKey<Registry<EquipmentSet>> KEY_REG_ITEMSETS = RegistryKey.getOrCreateRootKey(new ResourceLocation(NostrumMagica.MODID, "item_sets"));
	
	private static IForgeRegistry<EquipmentSet> REGISTRY;
	
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<EquipmentSet>().setName(KEY_REG_ITEMSETS.getLocation()).setType(EquipmentSet.class).setMaxID(Integer.MAX_VALUE - 1)
			.disableSaving().create();
	}
	
	public static Collection<EquipmentSet> GetAllSets() {
		return REGISTRY.getValues();
	}
	
}
