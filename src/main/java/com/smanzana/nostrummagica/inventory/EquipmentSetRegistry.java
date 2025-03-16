package com.smanzana.nostrummagica.inventory;

import java.util.Collection;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.set.EquipmentSet;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
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

	public static final ResourceKey<Registry<EquipmentSet>> KEY_REG_ITEMSETS = ResourceKey.createRegistryKey(new ResourceLocation(NostrumMagica.MODID, "item_sets"));
	
	private static IForgeRegistry<EquipmentSet> REGISTRY;
	
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<EquipmentSet>().setName(KEY_REG_ITEMSETS.location()).setType(EquipmentSet.class).setMaxID(Integer.MAX_VALUE - 1)
			.disableSaving().create();
	}
	
	public static Collection<EquipmentSet> GetAllSets() {
		return REGISTRY.getValues();
	}
	
}
