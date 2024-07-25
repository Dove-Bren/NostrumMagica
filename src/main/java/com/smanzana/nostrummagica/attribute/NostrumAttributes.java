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
	
	protected static final String ID_REDUCE_PHYSICAL = MagicReductionAttribute.ID_PREFIX + "physical";
	protected static final String ID_REDUCE_EARTH = MagicReductionAttribute.ID_PREFIX + "earth";
	protected static final String ID_REDUCE_ENDER = MagicReductionAttribute.ID_PREFIX + "ender";
	protected static final String ID_REDUCE_FIRE = MagicReductionAttribute.ID_PREFIX + "fire";
	protected static final String ID_REDUCE_ICE = MagicReductionAttribute.ID_PREFIX + "ice";
	protected static final String ID_REDUCE_LIGHTNING = MagicReductionAttribute.ID_PREFIX + "lightning";
	protected static final String ID_REDUCE_WIND = MagicReductionAttribute.ID_PREFIX + "wind";
	
	@ObjectHolder(MagicPotencyAttribute.ID) public static MagicPotencyAttribute magicPotency;
	@ObjectHolder(MagicResistAttribute.ID) public static MagicResistAttribute magicResist;
	@ObjectHolder(ManaRegenAttribute.ID) public static ManaRegenAttribute manaRegen;
	@ObjectHolder(ID_REDUCE_PHYSICAL) public static MagicReductionAttribute reducePhysical;
	@ObjectHolder(ID_REDUCE_EARTH) public static MagicReductionAttribute reduceEarth;
	@ObjectHolder(ID_REDUCE_ENDER) public static MagicReductionAttribute reduceEnder;
	@ObjectHolder(ID_REDUCE_FIRE) public static MagicReductionAttribute reduceFire;
	@ObjectHolder(ID_REDUCE_ICE) public static MagicReductionAttribute reduceIce;
	@ObjectHolder(ID_REDUCE_LIGHTNING) public static MagicReductionAttribute reduceLightning;
	@ObjectHolder(ID_REDUCE_WIND) public static MagicReductionAttribute reduceWind;
	@ObjectHolder(MagicDamageAttribute.ID) public static MagicDamageAttribute magicDamage;
	@ObjectHolder(ManaCostReductionAttribute.ID) public static ManaCostReductionAttribute manaCost;
	
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
		
		makeAndRegister(registry, MagicResistAttribute::new, MagicResistAttribute.ID);
		makeAndRegister(registry, MagicPotencyAttribute::new, MagicPotencyAttribute.ID);
		makeAndRegister(registry, ManaRegenAttribute::new, ManaRegenAttribute.ID);
		makeAndRegister(registry, MagicDamageAttribute::new, MagicDamageAttribute.ID);
		makeAndRegister(registry, ManaCostReductionAttribute::new, ManaCostReductionAttribute.ID);
		
		for (EMagicElement elem : EMagicElement.values()) {
			final String ID = MagicReductionAttribute.ID_PREFIX + elem.name().toLowerCase();
			makeAndRegister(registry, (name) -> new MagicReductionAttribute(elem, name), ID);
		}
	}
	
	@SubscribeEvent
	public static void onAttributeConstruct(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> type : event.getTypes()) {
			event.add(type, magicResist);
			event.add(type, magicPotency);
			event.add(type, manaRegen);
			event.add(type, magicDamage);
			event.add(type, manaCost);
			for (EMagicElement elem : EMagicElement.values()) {
				event.add(type, GetReduceAttribute(elem));
			}
		}
	}
	
	public static MagicReductionAttribute GetReduceAttribute(EMagicElement element) {
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
