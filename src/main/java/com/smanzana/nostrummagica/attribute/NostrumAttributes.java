package com.smanzana.nostrummagica.attribute;

import java.util.function.Function;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumAttributes {
	
	protected static final String ID_REDUCE_NEUTRAL = MagicReductionAttribute.ID_PREFIX + "neutral";
	protected static final String ID_REDUCE_EARTH = MagicReductionAttribute.ID_PREFIX + "earth";
	protected static final String ID_REDUCE_ENDER = MagicReductionAttribute.ID_PREFIX + "ender";
	protected static final String ID_REDUCE_FIRE = MagicReductionAttribute.ID_PREFIX + "fire";
	protected static final String ID_REDUCE_ICE = MagicReductionAttribute.ID_PREFIX + "ice";
	protected static final String ID_REDUCE_LIGHTNING = MagicReductionAttribute.ID_PREFIX + "lightning";
	protected static final String ID_REDUCE_WIND = MagicReductionAttribute.ID_PREFIX + "wind";
	
	protected static final String ID_XP_NEUTRAL = ElementXPBonusAttribute.ID_PREFIX + "neutral";
	protected static final String ID_XP_EARTH = ElementXPBonusAttribute.ID_PREFIX + "earth";
	protected static final String ID_XP_ENDER = ElementXPBonusAttribute.ID_PREFIX + "ender";
	protected static final String ID_XP_FIRE = ElementXPBonusAttribute.ID_PREFIX + "fire";
	protected static final String ID_XP_ICE = ElementXPBonusAttribute.ID_PREFIX + "ice";
	protected static final String ID_XP_LIGHTNING = ElementXPBonusAttribute.ID_PREFIX + "lightning";
	protected static final String ID_XP_WIND = ElementXPBonusAttribute.ID_PREFIX + "wind";
	
	@ObjectHolder(MagicPotencyAttribute.ID) public static MagicPotencyAttribute magicPotency;
	@ObjectHolder(MagicResistAttribute.ID) public static MagicResistAttribute magicResist;
	@ObjectHolder(ManaRegenAttribute.ID) public static ManaRegenAttribute manaRegen;
	@ObjectHolder(ID_REDUCE_NEUTRAL) public static MagicReductionAttribute reduceNeutral;
	@ObjectHolder(ID_REDUCE_EARTH) public static MagicReductionAttribute reduceEarth;
	@ObjectHolder(ID_REDUCE_ENDER) public static MagicReductionAttribute reduceEnder;
	@ObjectHolder(ID_REDUCE_FIRE) public static MagicReductionAttribute reduceFire;
	@ObjectHolder(ID_REDUCE_ICE) public static MagicReductionAttribute reduceIce;
	@ObjectHolder(ID_REDUCE_LIGHTNING) public static MagicReductionAttribute reduceLightning;
	@ObjectHolder(ID_REDUCE_WIND) public static MagicReductionAttribute reduceWind;
	@ObjectHolder(AllMagicReductionAttribute.ID) public static AllMagicReductionAttribute reduceAll;
	@ObjectHolder(MagicDamageAttribute.ID) public static MagicDamageAttribute magicDamage;
	@ObjectHolder(ManaCostReductionAttribute.ID) public static ManaCostReductionAttribute manaCost;
	@ObjectHolder(MagicXPBonusAttribute.ID) public static MagicXPBonusAttribute xpBonus;
	@ObjectHolder(ID_XP_NEUTRAL) public static ElementXPBonusAttribute xpNeutral;
	@ObjectHolder(ID_XP_EARTH) public static ElementXPBonusAttribute xpEarth;
	@ObjectHolder(ID_XP_ENDER) public static ElementXPBonusAttribute xpEnder;
	@ObjectHolder(ID_XP_FIRE) public static ElementXPBonusAttribute xpFire;
	@ObjectHolder(ID_XP_ICE) public static ElementXPBonusAttribute xpIce;
	@ObjectHolder(ID_XP_LIGHTNING) public static ElementXPBonusAttribute xpLightning;
	@ObjectHolder(ID_XP_WIND) public static ElementXPBonusAttribute xpWind;
	@ObjectHolder(AllElementXPBonusAttribute.ID) public static AllElementXPBonusAttribute xpAllElements;
	@ObjectHolder(BonusJumpAttribute.ID) public static BonusJumpAttribute bonusJump;
	@ObjectHolder(CastSpeedAttribute.ID) public static CastSpeedAttribute castSpeed; 
	
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
		makeAndRegister(registry, MagicXPBonusAttribute::new, MagicXPBonusAttribute.ID);
		makeAndRegister(registry, AllElementXPBonusAttribute::new, AllElementXPBonusAttribute.ID);
		makeAndRegister(registry, AllMagicReductionAttribute::new, AllMagicReductionAttribute.ID);
		makeAndRegister(registry, BonusJumpAttribute::new, BonusJumpAttribute.ID);
		makeAndRegister(registry, CastSpeedAttribute::new, CastSpeedAttribute.ID);
		
		for (EMagicElement elem : EMagicElement.values()) {
			final String REDUC_ID = MagicReductionAttribute.ID_PREFIX + elem.name().toLowerCase();
			final String XP_ID = ElementXPBonusAttribute.ID_PREFIX + elem.name().toLowerCase();
			makeAndRegister(registry, (name) -> new MagicReductionAttribute(elem, name), REDUC_ID);
			makeAndRegister(registry, (name) -> new ElementXPBonusAttribute(elem, name), XP_ID);
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
			event.add(type, xpBonus);
			event.add(type, xpAllElements);
			event.add(type, reduceAll);
			event.add(type, castSpeed);
			for (EMagicElement elem : EMagicElement.values()) {
				event.add(type, GetReduceAttribute(elem));
				event.add(type, GetXPAttribute(elem));
			}
			
			if (type == EntityType.PLAYER) {
				event.add(type, bonusJump);
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
		case NEUTRAL:
			return reduceNeutral;
		case WIND:
			return reduceWind;
		}
		
		return reduceNeutral;
	}
	
	public static ElementXPBonusAttribute GetXPAttribute(EMagicElement element) {
		switch (element) {
		case EARTH:
			return xpEarth;
		case ENDER:
			return xpEnder;
		case FIRE:
			return xpFire;
		case ICE:
			return xpIce;
		case LIGHTNING:
			return xpLightning;
		case NEUTRAL:
			return xpNeutral;
		case WIND:
			return xpWind;
		}
		
		return xpNeutral;
	}
}
