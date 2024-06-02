package com.smanzana.nostrummagica.attribute;

import java.util.function.Function;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EMagicElement;

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
	
	protected static final String ID_REDUCE_PHYSICAL = AttributeMagicReduction.ID_PREFIX + "physical";
	protected static final String ID_REDUCE_EARTH = AttributeMagicReduction.ID_PREFIX + "earth";
	protected static final String ID_REDUCE_ENDER = AttributeMagicReduction.ID_PREFIX + "ender";
	protected static final String ID_REDUCE_FIRE = AttributeMagicReduction.ID_PREFIX + "fire";
	protected static final String ID_REDUCE_ICE = AttributeMagicReduction.ID_PREFIX + "ice";
	protected static final String ID_REDUCE_LIGHTNING = AttributeMagicReduction.ID_PREFIX + "lightning";
	protected static final String ID_REDUCE_WIND = AttributeMagicReduction.ID_PREFIX + "wind";
	
	@ObjectHolder(AttributeMagicPotency.ID) public static AttributeMagicPotency magicPotency;
	@ObjectHolder(AttributeMagicResist.ID) public static AttributeMagicResist magicResist;
	@ObjectHolder(AttributeManaRegen.ID) public static AttributeManaRegen manaRegen;
	@ObjectHolder(ID_REDUCE_PHYSICAL) public static AttributeMagicReduction reducePhysical;
	@ObjectHolder(ID_REDUCE_EARTH) public static AttributeMagicReduction reduceEarth;
	@ObjectHolder(ID_REDUCE_ENDER) public static AttributeMagicReduction reduceEnder;
	@ObjectHolder(ID_REDUCE_FIRE) public static AttributeMagicReduction reduceFire;
	@ObjectHolder(ID_REDUCE_ICE) public static AttributeMagicReduction reduceIce;
	@ObjectHolder(ID_REDUCE_LIGHTNING) public static AttributeMagicReduction reduceLightning;
	@ObjectHolder(ID_REDUCE_WIND) public static AttributeMagicReduction reduceWind;
	
	protected static final String makeName(String base) {
		return "attribute.nostrummagica." + base + ".name";
	}
	
	protected static final <T extends Attribute> void makeAndRegister(final IForgeRegistry<Attribute> registry, Function<String, T> constructor, String ID) {
		T attribute = constructor.apply(makeName(ID));
		attribute.setRegistryName(NostrumMagica.Loc(ID));
		registry.register(attribute);
	}

	@SubscribeEvent
	public static void registerAttributes(RegistryEvent.Register<Attribute> event) {
		final IForgeRegistry<Attribute> registry = event.getRegistry();
		
		makeAndRegister(registry, AttributeMagicResist::new, AttributeMagicResist.ID);
		makeAndRegister(registry, AttributeMagicPotency::new, AttributeMagicPotency.ID);
		makeAndRegister(registry, AttributeManaRegen::new, AttributeManaRegen.ID);
		
		for (EMagicElement elem : EMagicElement.values()) {
			final String ID = AttributeMagicReduction.ID_PREFIX + elem.name().toLowerCase();
			makeAndRegister(registry, (name) -> new AttributeMagicReduction(elem, name), ID);
		}
	}
	
	@SubscribeEvent
	public static void onAttributeConstruct(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> type : event.getTypes()) {
			event.add(type, magicResist);
			event.add(type, magicPotency);
			event.add(type, manaRegen);
			for (EMagicElement elem : EMagicElement.values()) {
				event.add(type, GetReduceAttribute(elem));
			}
		}
	}
	
	public static AttributeMagicReduction GetReduceAttribute(EMagicElement element) {
		switch (element) {
		case EARTH:
			return reduceEarth;
		case ENDER:
			return reduceEnder;
		case FIRE:
			return reduceFire;
		case ICE:
			return reduceIce;
		case LIGHTNING:
			return reduceLightning;
		case PHYSICAL:
			return reducePhysical;
		case WIND:
			return reduceWind;
		}
		
		return reducePhysical;
	}
}
