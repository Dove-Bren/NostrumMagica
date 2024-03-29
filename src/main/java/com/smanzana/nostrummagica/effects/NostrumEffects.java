package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.potion.Effect;
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
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Effect> event) {
    	final IForgeRegistry<Effect> registry = event.getRegistry();

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
    }
}
