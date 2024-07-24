package com.smanzana.nostrummagica.item.set;

import java.util.UUID;
import java.util.function.Consumer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.item.armor.MagicFireArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumEquipmentSets {
	
	private static final String ID_MAGICARMOR_PHYSICAL = "magic_armor.physical";
	private static final String ID_MAGICARMOR_PHYSICAL_NOVICE = ID_MAGICARMOR_PHYSICAL + ".novice";
	private static final String ID_MAGICARMOR_PHYSICAL_ADEPT = ID_MAGICARMOR_PHYSICAL + ".adept";
	private static final String ID_MAGICARMOR_PHYSICAL_MASTER = ID_MAGICARMOR_PHYSICAL + ".master";
	private static final String ID_MAGICARMOR_FIRE = "magic_armor.fire";
	private static final String ID_MAGICARMOR_FIRE_NOVICE = ID_MAGICARMOR_FIRE + ".novice";
	private static final String ID_MAGICARMOR_FIRE_ADEPT = ID_MAGICARMOR_FIRE + ".adept";
	private static final String ID_MAGICARMOR_FIRE_MASTER = ID_MAGICARMOR_FIRE + ".master";
	private static final String ID_MAGICARMOR_ICE = "magic_armor.ice";
	private static final String ID_MAGICARMOR_ICE_NOVICE = ID_MAGICARMOR_ICE + ".novice";
	private static final String ID_MAGICARMOR_ICE_ADEPT = ID_MAGICARMOR_ICE + ".adept";
	private static final String ID_MAGICARMOR_ICE_MASTER = ID_MAGICARMOR_ICE + ".master";
	private static final String ID_MAGICARMOR_EARTH = "magic_armor.earth";
	private static final String ID_MAGICARMOR_EARTH_NOVICE = ID_MAGICARMOR_EARTH + ".novice";
	private static final String ID_MAGICARMOR_EARTH_ADEPT = ID_MAGICARMOR_EARTH + ".adept";
	private static final String ID_MAGICARMOR_EARTH_MASTER = ID_MAGICARMOR_EARTH + ".master";
	private static final String ID_MAGICARMOR_WIND = "magic_armor.wind";
	private static final String ID_MAGICARMOR_WIND_NOVICE = ID_MAGICARMOR_WIND + ".novice";
	private static final String ID_MAGICARMOR_WIND_ADEPT = ID_MAGICARMOR_WIND + ".adept";
	private static final String ID_MAGICARMOR_WIND_MASTER = ID_MAGICARMOR_WIND + ".master";
	private static final String ID_MAGICARMOR_LIGHTNING = "magic_armor.lightning";
	private static final String ID_MAGICARMOR_LIGHTNING_NOVICE = ID_MAGICARMOR_LIGHTNING + ".novice";
	private static final String ID_MAGICARMOR_LIGHTNING_ADEPT = ID_MAGICARMOR_LIGHTNING + ".adept";
	private static final String ID_MAGICARMOR_LIGHTNING_MASTER = ID_MAGICARMOR_LIGHTNING + ".master";
	private static final String ID_MAGICARMOR_ENDER = "magic_armor.ender";
	private static final String ID_MAGICARMOR_ENDER_NOVICE = ID_MAGICARMOR_ENDER + ".novice";
	private static final String ID_MAGICARMOR_ENDER_ADEPT = ID_MAGICARMOR_ENDER + ".adept";
	private static final String ID_MAGICARMOR_ENDER_MASTER = ID_MAGICARMOR_ENDER + ".master";
	
	private static final UUID SET_UUID_PHYSICAL_NOVICE = UUID.fromString("7164440b-20da-4b04-9e3f-491515ff57d8");
	private static final UUID SET_UUID_PHYSICAL_ADEPT = UUID.fromString("2bf205b7-ec52-40d5-bf91-ec94b665139f");
	private static final UUID SET_UUID_PHYSICAL_MASTER = UUID.fromString("2c0e8d42-164b-4cf5-8f3c-7f96035aba81");
	private static final UUID SET_UUID_FIRE_NOVICE = UUID.fromString("0f40ec59-be0b-4496-bb74-743f71b5386d");
	private static final UUID SET_UUID_FIRE_ADEPT = UUID.fromString("1961c447-7c54-44e6-89b2-32502d5e8624");
	private static final UUID SET_UUID_FIRE_MASTER = UUID.fromString("87d9d2b9-ae03-4785-b397-3c6b536da229");
	private static final UUID SET_UUID_ICE_NOVICE = UUID.fromString("dcf072c7-bb6d-4bb9-8901-cd6a59b7e887");
	private static final UUID SET_UUID_ICE_ADEPT = UUID.fromString("8d52c298-d7dd-40c3-81c9-7f33210bf0c4");
	private static final UUID SET_UUID_ICE_MASTER = UUID.fromString("102e3c8a-8364-4214-a8bd-ae57a42d92f0");
	private static final UUID SET_UUID_EARTH_NOVICE = UUID.fromString("c4f52e3e-f272-47bd-acaa-78b0cbce8e44");
	private static final UUID SET_UUID_EARTH_ADEPT = UUID.fromString("3e147769-ca3a-4d2f-a832-b78630def9ee");
	private static final UUID SET_UUID_EARTH_MASTER = UUID.fromString("b6016538-06ed-4e36-b172-a61e2106182f");
	private static final UUID SET_UUID_WIND_NOVICE = UUID.fromString("e0d89528-41db-4d20-9610-51ec81fc8e78");
	private static final UUID SET_UUID_WIND_ADEPT = UUID.fromString("719fe6fa-4b09-443e-80f7-c23ac32cf078");
	private static final UUID SET_UUID_WIND_MASTER = UUID.fromString("a8dd8c28-b6d9-4eb9-b641-d5a87eee3a13");
	private static final UUID SET_UUID_LIGHTNING_NOVICE = UUID.fromString("01760265-afba-44e8-97ad-2a2c1ce4f6a1");
	private static final UUID SET_UUID_LIGHTNING_ADEPT = UUID.fromString("4966aff3-ebb9-48bf-8f83-b59b22506607");
	private static final UUID SET_UUID_LIGHTNING_MASTER = UUID.fromString("451250c7-a34f-4138-ba60-d86469661780");
	private static final UUID SET_UUID_ENDER_NOVICE = UUID.fromString("23ea65fa-34f8-41c7-9865-0744b22209b7");
	private static final UUID SET_UUID_ENDER_ADEPT = UUID.fromString("e0587390-70dc-4f5c-a8fb-43843d23da3d");
	private static final UUID SET_UUID_ENDER_MASTER = UUID.fromString("d1b8b185-1b88-4444-ae01-1d9aea11db80");
	
	@ObjectHolder(ID_MAGICARMOR_PHYSICAL) public static MagicArmorSet physicalMagicArmor;
	@ObjectHolder(ID_MAGICARMOR_FIRE) public static MagicArmorSet fireMagicArmor;
	@ObjectHolder(ID_MAGICARMOR_ICE) public static MagicArmorSet iceMagicArmor;
	@ObjectHolder(ID_MAGICARMOR_EARTH) public static MagicArmorSet earthMagicArmor;
	@ObjectHolder(ID_MAGICARMOR_WIND) public static MagicArmorSet windMagicArmor;
	@ObjectHolder(ID_MAGICARMOR_LIGHTNING) public static MagicArmorSet lightningMagicArmor;
	@ObjectHolder(ID_MAGICARMOR_ENDER) public static MagicArmorSet enderMagicArmor;
	
	@SubscribeEvent
	public static void registerSets(RegistryEvent.Register<EquipmentSet> event) {
		final IForgeRegistry<EquipmentSet> registry = event.getRegistry();
		
		registry.register(makeSet(ID_MAGICARMOR_PHYSICAL_NOVICE, SET_UUID_PHYSICAL_NOVICE, EMagicElement.PHYSICAL, MagicArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_PHYSICAL_ADEPT, SET_UUID_PHYSICAL_ADEPT, EMagicElement.PHYSICAL, MagicArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_PHYSICAL_MASTER, SET_UUID_PHYSICAL_MASTER, EMagicElement.PHYSICAL, MagicArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_FIRE_NOVICE, SET_UUID_FIRE_NOVICE, EMagicElement.FIRE, MagicArmor.Type.NOVICE, false, (e) -> MagicFireArmor.onFullSetTick(e, MagicArmor.Type.NOVICE)));
		registry.register(makeSet(ID_MAGICARMOR_FIRE_ADEPT, SET_UUID_FIRE_ADEPT, EMagicElement.FIRE, MagicArmor.Type.ADEPT, false, (e) -> MagicFireArmor.onFullSetTick(e, MagicArmor.Type.ADEPT)));
		registry.register(makeSet(ID_MAGICARMOR_FIRE_MASTER, SET_UUID_FIRE_MASTER, EMagicElement.FIRE, MagicArmor.Type.MASTER, true, (e) -> MagicFireArmor.onFullSetTick(e, MagicArmor.Type.MASTER)));

		registry.register(makeSet(ID_MAGICARMOR_ICE_NOVICE, SET_UUID_ICE_NOVICE, EMagicElement.ICE, MagicArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ICE_ADEPT, SET_UUID_ICE_ADEPT, EMagicElement.ICE, MagicArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ICE_MASTER, SET_UUID_ICE_MASTER, EMagicElement.ICE, MagicArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_EARTH_NOVICE, SET_UUID_EARTH_NOVICE, EMagicElement.EARTH, MagicArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_EARTH_ADEPT, SET_UUID_EARTH_ADEPT, EMagicElement.EARTH, MagicArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_EARTH_MASTER, SET_UUID_EARTH_MASTER, EMagicElement.EARTH, MagicArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_WIND_NOVICE, SET_UUID_WIND_NOVICE, EMagicElement.WIND, MagicArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_WIND_ADEPT, SET_UUID_WIND_ADEPT, EMagicElement.WIND, MagicArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_WIND_MASTER, SET_UUID_WIND_MASTER, EMagicElement.WIND, MagicArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_LIGHTNING_NOVICE, SET_UUID_LIGHTNING_NOVICE, EMagicElement.LIGHTNING, MagicArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_LIGHTNING_ADEPT, SET_UUID_LIGHTNING_ADEPT, EMagicElement.LIGHTNING, MagicArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_LIGHTNING_MASTER, SET_UUID_LIGHTNING_MASTER, EMagicElement.LIGHTNING, MagicArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_ENDER_NOVICE, SET_UUID_ENDER_NOVICE, EMagicElement.ENDER, MagicArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ENDER_ADEPT, SET_UUID_ENDER_ADEPT, EMagicElement.ENDER, MagicArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ENDER_MASTER, SET_UUID_ENDER_MASTER, EMagicElement.ENDER, MagicArmor.Type.MASTER, true, null));
		
	}
	
	protected static final MagicArmorSet makeSet(String ID, UUID uuid, EMagicElement element, MagicArmor.Type type, boolean flight, Consumer<LivingEntity> tick) {
		MagicArmorSet set = new MagicArmorSet(uuid, element, type, flight, tick);
		set.setRegistryName(NostrumMagica.Loc(ID));
		return set;
	}
	
}
