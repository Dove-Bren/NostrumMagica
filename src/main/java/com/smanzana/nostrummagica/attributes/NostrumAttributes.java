package com.smanzana.nostrummagica.attributes;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumAttributes {

	@SubscribeEvent
	public static void registerAttributes(RegistryEvent.Register<Attribute> event) {
		final IForgeRegistry<Attribute> registry = event.getRegistry();
		registry.register(AttributeMagicResist.instance().setRegistryName(AttributeMagicResist.ID));
		registry.register(AttributeMagicPotency.instance().setRegistryName(AttributeMagicPotency.ID));
		registry.register(AttributeManaRegen.instance().setRegistryName(AttributeManaRegen.ID));
		for (EMagicElement elem : EMagicElement.values()) {
			registry.register(AttributeMagicReduction.instance(elem).setRegistryName(AttributeMagicReduction.ID_PREFIX + elem.name().toLowerCase()));
		}
	}
	
	@SubscribeEvent
	public static void onAttributeConstruct(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> type : event.getTypes()) {
			event.add(type, AttributeMagicResist.instance());
			event.add(type, AttributeMagicPotency.instance());
			event.add(type, AttributeManaRegen.instance());
			for (EMagicElement elem : EMagicElement.values()) {
				event.add(type, AttributeMagicReduction.instance(elem));
			}
		}
	}
}
