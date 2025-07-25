package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumEffects {

	@ObjectHolder(FamiliarEffect.ID) public static FamiliarEffect familiar;
	@ObjectHolder(FrostbiteEffect.ID) public static FrostbiteEffect frostbite;
	@ObjectHolder(LightningAttackEffect.ID) public static LightningAttackEffect lightningAttack;
	@ObjectHolder(LightningChargeEffect.ID) public static LightningChargeEffect lightningCharge;
	@ObjectHolder(MagicBoostEffect.ID) public static MagicBoostEffect magicBoost;
	@ObjectHolder(MagicBuffEffect.ID) public static MagicBuffEffect magicBuff;
	@ObjectHolder(MagicResistEffect.ID) public static MagicResistEffect magicResist;
	@ObjectHolder(MagicShieldEffect.ID) public static MagicShieldEffect magicShield;
	@ObjectHolder(ManaRegenEffect.ID) public static ManaRegenEffect manaRegen;
	@ObjectHolder(NaturesBlessingEffect.ID) public static NaturesBlessingEffect naturesBlessing;
	@ObjectHolder(NostrumTransformationEffect.ID) public static NostrumTransformationEffect nostrumTransformation;
	@ObjectHolder(PhysicalShieldEffect.ID) public static PhysicalShieldEffect physicalShield;
	@ObjectHolder(RootedEffect.ID) public static RootedEffect rooted;
	@ObjectHolder(RendEffect.ID) public static RendEffect rend;
	@ObjectHolder(MagicRendEffect.ID) public static MagicRendEffect magicRend;
	@ObjectHolder(SublimationEffect.ID) public static SublimationEffect sublimation;
	@ObjectHolder(HealResistEffect.ID) public static HealResistEffect healResist;
	@ObjectHolder(FastFallEffect.ID) public static FastFallEffect fastFall;
	@ObjectHolder(DisruptionEffect.ID) public static DisruptionEffect disruption;
	@ObjectHolder(RendStrikeEffect.ID) public static RendStrikeEffect rendStrike;
	@ObjectHolder(SteelSkinEffect.ID) public static SteelSkinEffect steelSkin;
	@ObjectHolder(SoulDrainEffect.ID) public static SoulDrainEffect soulDrain;
	@ObjectHolder(SoulVampireEffect.ID) public static SoulVampireEffect soulVampire;
	@ObjectHolder(LootLuckEffect.ID) public static LootLuckEffect lootLuck;
	@ObjectHolder(ElementalEnchantEffect.ID_EARTH) public static ElementalEnchantEffect enchantEarth;
	@ObjectHolder(ElementalEnchantEffect.ID_ENDER) public static ElementalEnchantEffect enchantEnder;
	@ObjectHolder(ElementalEnchantEffect.ID_FIRE) public static ElementalEnchantEffect enchantFire;
	@ObjectHolder(ElementalEnchantEffect.ID_ICE) public static ElementalEnchantEffect enchantIce;
	@ObjectHolder(ElementalEnchantEffect.ID_LIGHTNING) public static ElementalEnchantEffect enchantLightning;
	@ObjectHolder(ElementalEnchantEffect.ID_PHYSICAL) public static ElementalEnchantEffect enchantPhysical;
	@ObjectHolder(ElementalEnchantEffect.ID_WIND) public static ElementalEnchantEffect enchantWind;
	@ObjectHolder(ElementalSpellBoostEffect.ID_EARTH) public static ElementalSpellBoostEffect spellBoostEarth;
	@ObjectHolder(ElementalSpellBoostEffect.ID_ENDER) public static ElementalSpellBoostEffect spellBoostEnder;
	@ObjectHolder(ElementalSpellBoostEffect.ID_FIRE) public static ElementalSpellBoostEffect spellBoostFire;
	@ObjectHolder(ElementalSpellBoostEffect.ID_ICE) public static ElementalSpellBoostEffect spellBoostIce;
	@ObjectHolder(ElementalSpellBoostEffect.ID_LIGHTNING) public static ElementalSpellBoostEffect spellBoostLightning;
	@ObjectHolder(ElementalSpellBoostEffect.ID_PHYSICAL) public static ElementalSpellBoostEffect spellBoostPhysical;
	@ObjectHolder(ElementalSpellBoostEffect.ID_WIND) public static ElementalSpellBoostEffect spellBoostWind;
	@ObjectHolder(MysticWaterEffect.ID) public static MysticWaterEffect mysticWater;
	@ObjectHolder(MysticAirEffect.ID) public static MysticAirEffect mysticAir;
	@ObjectHolder(CursedFireEffect.ID) public static CursedFireEffect cursedFire;
	@ObjectHolder(ImmobilizeEffect.ID) public static ImmobilizeEffect immobilize;
	@ObjectHolder(MagicWeaknessEffect.ID) public static MagicWeaknessEffect magicWeakness;
	@ObjectHolder(MobBlindnessEffect.ID) public static MobBlindnessEffect mobBlindness;
	@ObjectHolder(BonusJumpEffect.ID) public static BonusJumpEffect bonusJump;
	@ObjectHolder(SwiftCastEffect.ID_INSTANT) public static SwiftCastEffect swiftCast;
	@ObjectHolder(SwiftCastEffect.ID_LASTING) public static SwiftCastEffect lastingSwiftCast;
	@ObjectHolder(SwiftSwimEffect.ID) public static SwiftSwimEffect swiftSwim;
	@ObjectHolder(CarapaceEffect.ID) public static CarapaceEffect carapace;
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<MobEffect> event) {
    	final IForgeRegistry<MobEffect> registry = event.getRegistry();

    	registry.register(new FamiliarEffect().setRegistryName(FamiliarEffect.ID));
    	registry.register(new FrostbiteEffect().setRegistryName(FrostbiteEffect.ID));
    	registry.register(new LightningAttackEffect().setRegistryName(LightningAttackEffect.ID));
    	registry.register(new LightningChargeEffect().setRegistryName(LightningChargeEffect.ID));
    	registry.register(new MagicBoostEffect().setRegistryName(MagicBoostEffect.ID));
    	registry.register(new MagicBuffEffect().setRegistryName(MagicBuffEffect.ID));
    	registry.register(new MagicResistEffect().setRegistryName(MagicResistEffect.ID));
    	registry.register(new MagicShieldEffect().setRegistryName(MagicShieldEffect.ID));
    	registry.register(new ManaRegenEffect().setRegistryName(ManaRegenEffect.ID));
    	registry.register(new NaturesBlessingEffect().setRegistryName(NaturesBlessingEffect.ID));
    	registry.register(new NostrumTransformationEffect().setRegistryName(NostrumTransformationEffect.ID));
    	registry.register(new PhysicalShieldEffect().setRegistryName(PhysicalShieldEffect.ID));
    	registry.register(new RootedEffect().setRegistryName(RootedEffect.ID));
    	registry.register(new RendEffect().setRegistryName(RendEffect.ID));
    	registry.register(new MagicRendEffect().setRegistryName(MagicRendEffect.ID));
    	registry.register(new SublimationEffect().setRegistryName(SublimationEffect.ID));
    	registry.register(new HealResistEffect().setRegistryName(HealResistEffect.ID));
    	registry.register(new FastFallEffect().setRegistryName(FastFallEffect.ID));
    	registry.register(new DisruptionEffect().setRegistryName(DisruptionEffect.ID));
    	registry.register(new RendStrikeEffect().setRegistryName(RendStrikeEffect.ID));
    	registry.register(new SteelSkinEffect().setRegistryName(SteelSkinEffect.ID));
    	registry.register(new SoulDrainEffect().setRegistryName(SoulDrainEffect.ID));
    	registry.register(new SoulVampireEffect().setRegistryName(SoulVampireEffect.ID));
    	registry.register(new LootLuckEffect().setRegistryName(LootLuckEffect.ID));
    	registry.register(new ElementalEnchantEffect(EMagicElement.EARTH).setRegistryName(ElementalEnchantEffect.ID_EARTH));
    	registry.register(new ElementalEnchantEffect(EMagicElement.ENDER).setRegistryName(ElementalEnchantEffect.ID_ENDER));
    	registry.register(new ElementalEnchantEffect(EMagicElement.FIRE).setRegistryName(ElementalEnchantEffect.ID_FIRE));
    	registry.register(new ElementalEnchantEffect(EMagicElement.ICE).setRegistryName(ElementalEnchantEffect.ID_ICE));
    	registry.register(new ElementalEnchantEffect(EMagicElement.LIGHTNING).setRegistryName(ElementalEnchantEffect.ID_LIGHTNING));
    	registry.register(new ElementalEnchantEffect(EMagicElement.PHYSICAL).setRegistryName(ElementalEnchantEffect.ID_PHYSICAL));
    	registry.register(new ElementalEnchantEffect(EMagicElement.WIND).setRegistryName(ElementalEnchantEffect.ID_WIND));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.EARTH).setRegistryName(ElementalSpellBoostEffect.ID_EARTH));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.ENDER).setRegistryName(ElementalSpellBoostEffect.ID_ENDER));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.FIRE).setRegistryName(ElementalSpellBoostEffect.ID_FIRE));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.ICE).setRegistryName(ElementalSpellBoostEffect.ID_ICE));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.LIGHTNING).setRegistryName(ElementalSpellBoostEffect.ID_LIGHTNING));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.PHYSICAL).setRegistryName(ElementalSpellBoostEffect.ID_PHYSICAL));
    	registry.register(new ElementalSpellBoostEffect(EMagicElement.WIND).setRegistryName(ElementalSpellBoostEffect.ID_WIND));
    	registry.register(new MysticWaterEffect().setRegistryName(MysticWaterEffect.ID));
    	registry.register(new MysticAirEffect().setRegistryName(MysticAirEffect.ID));
    	registry.register(new CursedFireEffect().setRegistryName(CursedFireEffect.ID));
    	registry.register(new ImmobilizeEffect().setRegistryName(ImmobilizeEffect.ID));
    	registry.register(new MagicWeaknessEffect().setRegistryName(MagicWeaknessEffect.ID));
    	registry.register(new MobBlindnessEffect().setRegistryName(MobBlindnessEffect.ID));
    	registry.register(new BonusJumpEffect().setRegistryName(BonusJumpEffect.ID));
    	registry.register(new SwiftCastEffect(true).setRegistryName(SwiftCastEffect.ID_INSTANT));
    	registry.register(new SwiftCastEffect(false).setRegistryName(SwiftCastEffect.ID_LASTING));
    	registry.register(new SwiftSwimEffect().setRegistryName(SwiftSwimEffect.ID));
    	registry.register(new CarapaceEffect().setRegistryName(CarapaceEffect.ID));
    }
}
