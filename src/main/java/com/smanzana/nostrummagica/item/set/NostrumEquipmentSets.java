package com.smanzana.nostrummagica.item.set;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMultimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.item.armor.ElementalFireArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
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
	private static final String ID_MAGEARMOR = "magearmor";
	private static final String ID_KOID = "koid";
	private static final String ID_GOLEM = "golem";
	
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
	private static final UUID SET_UUID_MAGE = UUID.fromString("bd077649-e7dc-406f-874d-98fbc1404c6b");
	private static final UUID SET_UUID_KOID = UUID.fromString("e168a2bd-1f68-48f6-b5d4-5b6eafb3243e");
	private static final UUID SET_UUID_GOLEM = UUID.fromString("724dccca-56bd-48f3-ab8f-5b6653a275c5");
	
	@ObjectHolder(ID_MAGICARMOR_PHYSICAL_NOVICE) public static ElementalArmorSet physicalNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_PHYSICAL_ADEPT) public static ElementalArmorSet physicalAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_PHYSICAL_MASTER) public static ElementalArmorSet physicalMasterArmor;
	@ObjectHolder(ID_MAGICARMOR_FIRE_NOVICE) public static ElementalArmorSet fireNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_FIRE_ADEPT) public static ElementalArmorSet fireAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_FIRE_MASTER) public static ElementalArmorSet fireMasterArmor;
	@ObjectHolder(ID_MAGICARMOR_ICE_NOVICE) public static ElementalArmorSet iceNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_ICE_ADEPT) public static ElementalArmorSet iceAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_ICE_MASTER) public static ElementalArmorSet iceMasterArmor;
	@ObjectHolder(ID_MAGICARMOR_EARTH_NOVICE) public static ElementalArmorSet earthNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_EARTH_ADEPT) public static ElementalArmorSet earthAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_EARTH_MASTER) public static ElementalArmorSet earthMasterArmor;
	@ObjectHolder(ID_MAGICARMOR_WIND_NOVICE) public static ElementalArmorSet windNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_WIND_ADEPT) public static ElementalArmorSet windAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_WIND_MASTER) public static ElementalArmorSet windMasterArmor;
	@ObjectHolder(ID_MAGICARMOR_LIGHTNING_NOVICE) public static ElementalArmorSet lightningNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_LIGHTNING_ADEPT) public static ElementalArmorSet lightningAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_LIGHTNING_MASTER) public static ElementalArmorSet lightningMasterArmor;
	@ObjectHolder(ID_MAGICARMOR_ENDER_NOVICE) public static ElementalArmorSet enderNoviceArmor;
	@ObjectHolder(ID_MAGICARMOR_ENDER_ADEPT) public static ElementalArmorSet enderAdeptArmor;
	@ObjectHolder(ID_MAGICARMOR_ENDER_MASTER) public static ElementalArmorSet enderMasterArmor;
	@ObjectHolder(ID_MAGEARMOR) public static BasicEquipmentSet mageArmor;
	@ObjectHolder(ID_KOID) public static BasicEquipmentSet koidSet;
	@ObjectHolder(ID_GOLEM) public static BasicEquipmentSet golem;
	
	@SubscribeEvent
	public static void registerSets(RegistryEvent.Register<EquipmentSet> event) {
		final IForgeRegistry<EquipmentSet> registry = event.getRegistry();
		
		registry.register(makeSet(ID_MAGICARMOR_PHYSICAL_NOVICE, SET_UUID_PHYSICAL_NOVICE, EMagicElement.PHYSICAL, ElementalArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_PHYSICAL_ADEPT, SET_UUID_PHYSICAL_ADEPT, EMagicElement.PHYSICAL, ElementalArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_PHYSICAL_MASTER, SET_UUID_PHYSICAL_MASTER, EMagicElement.PHYSICAL, ElementalArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_FIRE_NOVICE, SET_UUID_FIRE_NOVICE, EMagicElement.FIRE, ElementalArmor.Type.NOVICE, false, (e) -> ElementalFireArmor.onFullSetTick(e, ElementalArmor.Type.NOVICE)));
		registry.register(makeSet(ID_MAGICARMOR_FIRE_ADEPT, SET_UUID_FIRE_ADEPT, EMagicElement.FIRE, ElementalArmor.Type.ADEPT, false, (e) -> ElementalFireArmor.onFullSetTick(e, ElementalArmor.Type.ADEPT)));
		registry.register(makeSet(ID_MAGICARMOR_FIRE_MASTER, SET_UUID_FIRE_MASTER, EMagicElement.FIRE, ElementalArmor.Type.MASTER, true, (e) -> ElementalFireArmor.onFullSetTick(e, ElementalArmor.Type.MASTER)));

		registry.register(makeSet(ID_MAGICARMOR_ICE_NOVICE, SET_UUID_ICE_NOVICE, EMagicElement.ICE, ElementalArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ICE_ADEPT, SET_UUID_ICE_ADEPT, EMagicElement.ICE, ElementalArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ICE_MASTER, SET_UUID_ICE_MASTER, EMagicElement.ICE, ElementalArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_EARTH_NOVICE, SET_UUID_EARTH_NOVICE, EMagicElement.EARTH, ElementalArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_EARTH_ADEPT, SET_UUID_EARTH_ADEPT, EMagicElement.EARTH, ElementalArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_EARTH_MASTER, SET_UUID_EARTH_MASTER, EMagicElement.EARTH, ElementalArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_WIND_NOVICE, SET_UUID_WIND_NOVICE, EMagicElement.WIND, ElementalArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_WIND_ADEPT, SET_UUID_WIND_ADEPT, EMagicElement.WIND, ElementalArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_WIND_MASTER, SET_UUID_WIND_MASTER, EMagicElement.WIND, ElementalArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_LIGHTNING_NOVICE, SET_UUID_LIGHTNING_NOVICE, EMagicElement.LIGHTNING, ElementalArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_LIGHTNING_ADEPT, SET_UUID_LIGHTNING_ADEPT, EMagicElement.LIGHTNING, ElementalArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_LIGHTNING_MASTER, SET_UUID_LIGHTNING_MASTER, EMagicElement.LIGHTNING, ElementalArmor.Type.MASTER, true, null));

		registry.register(makeSet(ID_MAGICARMOR_ENDER_NOVICE, SET_UUID_ENDER_NOVICE, EMagicElement.ENDER, ElementalArmor.Type.NOVICE, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ENDER_ADEPT, SET_UUID_ENDER_ADEPT, EMagicElement.ENDER, ElementalArmor.Type.ADEPT, false, null));
		registry.register(makeSet(ID_MAGICARMOR_ENDER_MASTER, SET_UUID_ENDER_MASTER, EMagicElement.ENDER, ElementalArmor.Type.MASTER, true, null));
		
		registry.register(MageArmorSet.Build(SET_UUID_MAGE).setRegistryName(NostrumMagica.Loc(ID_MAGEARMOR)));

		{
		@SuppressWarnings("unchecked")
		Supplier<Item>[] items = new Supplier[]{
				() -> NostrumItems.koidHelm,
				() -> NostrumCurios.neckKoid,
				() -> NostrumCurios.ringKoid,
			};
			registry.register(new BasicEquipmentSet.Builder(items)
				.addEmptyBonus()
				.addBonus(ImmutableMultimap.of(
						NostrumAttributes.magicDamage, new AttributeModifier(SET_UUID_KOID, "Koid Set", 10.0, AttributeModifier.Operation.ADDITION),
						NostrumAttributes.manaRegen, new AttributeModifier(SET_UUID_KOID, "Koid Set", 25.0, AttributeModifier.Operation.ADDITION)
						))
				.addLastBonus()
				.build().setRegistryName(NostrumMagica.Loc(ID_KOID)));
		}

		{
			@SuppressWarnings("unchecked")
			Supplier<Item>[] items = new Supplier[]{
					() -> NostrumItems.thanoPendant,
					() -> NostrumCurios.beltGolem,
					() -> NostrumCurios.ringGolem,
				};
			registry.register(new BasicEquipmentSet.Builder(items)
				.addEmptyBonus()
				.addBonus(ImmutableMultimap.of(
						NostrumAttributes.reduceAll, new AttributeModifier(SET_UUID_GOLEM, "Golem Set", 0.25, AttributeModifier.Operation.ADDITION)
						))
				.addBonus(ImmutableMultimap.of(
						NostrumAttributes.reduceAll, new AttributeModifier(SET_UUID_GOLEM, "Golem Set", 0.5, AttributeModifier.Operation.ADDITION),
						NostrumAttributes.xpAllElements, new AttributeModifier(SET_UUID_GOLEM, "Golem Set", 20.0, AttributeModifier.Operation.ADDITION)
						))
				.build().setRegistryName(NostrumMagica.Loc(ID_GOLEM)));
		}
		
	}
	
	protected static final ElementalArmorSet makeSet(String ID, UUID uuid, EMagicElement element, ElementalArmor.Type type, boolean flight, Consumer<LivingEntity> tick) {
		ElementalArmorSet set = new ElementalArmorSet(uuid, element, type, flight, tick);
		set.setRegistryName(NostrumMagica.Loc(ID));
		return set;
	}
	
}
