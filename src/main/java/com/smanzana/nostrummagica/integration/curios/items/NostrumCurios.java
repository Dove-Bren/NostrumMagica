package com.smanzana.nostrummagica.integration.curios.items;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumCurios {

	@ObjectHolder(NostrumCurio.ID_RIBBON_SMALL) public static NostrumCurio smallRibbon;
	@ObjectHolder(NostrumCurio.ID_RIBBON_MEDIUM) public static NostrumCurio mediumRibbon;
	@ObjectHolder(NostrumCurio.ID_RIBBON_LARGE) public static NostrumCurio largeRibbon;
	@ObjectHolder(NostrumCurio.ID_RIBBON_FIERCE) public static NostrumCurio fierceRibbon;
	@ObjectHolder(NostrumCurio.ID_RIBBON_KIND) public static NostrumCurio kindRibbon;
	@ObjectHolder(NostrumCurio.ID_BELT_LIGHTNING) public static NostrumCurio lightningBelt;
	@ObjectHolder(NostrumCurio.ID_BELT_ENDER) public static NostrumCurio enderBelt;
	@ObjectHolder(NostrumCurio.ID_RING_GOLD) public static NostrumCurio ringGold;
	@ObjectHolder(NostrumCurio.ID_RING_GOLD_TRUE) public static NostrumCurio ringTrueGold;
	@ObjectHolder(NostrumCurio.ID_RING_GOLD_CORRUPTED) public static NostrumCurio ringCorruptedGold;
	@ObjectHolder(NostrumCurio.ID_RING_SILVER) public static NostrumCurio ringSilver;
	@ObjectHolder(NostrumCurio.ID_RING_SILVER_TRUE) public static NostrumCurio ringTrueSilver;
	@ObjectHolder(NostrumCurio.ID_RING_SILVER_CORRUPTED) public static NostrumCurio ringCorruptedSilver;
	@ObjectHolder(NostrumCurio.ID_RING_MYSTIC) public static NostrumCurio ringMystic;
	@ObjectHolder(NostrumCurio.ID_RING_MAGE) public static NostrumCurio ringMage;
	@ObjectHolder(NostrumCurio.ID_RING_KOID) public static NostrumCurio ringKoid;
	@ObjectHolder(NostrumCurio.ID_RING_GOLEM) public static NostrumCurio ringGolem;
	@ObjectHolder(NostrumCurio.ID_NECK_KOID) public static NostrumCurio neckKoid;
	@ObjectHolder(NostrumCurio.ID_BELT_GOLEM) public static NostrumCurio beltGolem;
	@ObjectHolder(FloatGuardItem.ID) public static FloatGuardItem floatGuard;
	@ObjectHolder(DragonWingPendantItem.ID) public static DragonWingPendantItem dragonWingPendant;
	
	private static final UUID attrIDSmallRibbon = UUID.fromString("E7811342-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDMediumRibbon = UUID.fromString("E78115E0-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDLargeRibbon = UUID.fromString("E7811824-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDFierceRibbon = UUID.fromString("E7811964-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDKindRibbon = UUID.fromString("E7811A86-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDLightningBelt = UUID.fromString("E7811DEC-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDEnderBelt = UUID.fromString("E781212A-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDRingGold = UUID.fromString("E7812256-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDRingTrueGold = UUID.fromString("E781236E-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDRingCorruptedGold = UUID.fromString("E7812486-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDRingSilver = UUID.fromString("E78127C4-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDRingTrueSilver = UUID.fromString("E78129EA-711C-11EE-B962-0242AC120002");
	private static final UUID attrIDRingCorruptedSilver = UUID.fromString("3E862EE8-711D-11EE-B962-0242AC120002");
	private static final UUID attrIDRingMystic = UUID.fromString("527667bf-2244-4057-a480-85fb28489e6b");
	private static final UUID attrIDRingMage = UUID.fromString("4e823af7-2faa-4e4e-9ee7-b1cf1240cf04");
	private static final UUID attrIDRingKoid = UUID.fromString("fea6bb8e-c94d-4336-a369-0fa8d9532d26");
	private static final UUID attrIDRingGolem = UUID.fromString("fd3f32c6-5bca-46cc-8aea-cd8b755942b0");
	private static final UUID attrIDNeckKoid = UUID.fromString("56c51fc2-63eb-427f-bbdf-2f1129e8f251");
	private static final UUID attrIDBeltGolem = UUID.fromString("cbdf56ed-85f2-41b1-b050-25a482bf1663");
	
	public static Item.Properties PropBase() {
		return new Item.Properties()
				.tab(NostrumMagica.equipmentTab)
				;
	}
	
	public static Item.Properties PropCurio() {
		return PropBase()
				.stacksTo(1)
				;
	}
	
	public NostrumCurios() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_SMALL)
				.attrID(attrIDSmallRibbon)
				.requiresMagic()
				.manaBonus(50)
				.setRegistryName(NostrumCurio.ID_RIBBON_SMALL));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_MEDIUM)
				.attrID(attrIDMediumRibbon)
				.requiresMagic()
				.manaBonus(100)
				.setRegistryName(NostrumCurio.ID_RIBBON_MEDIUM));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_LARGE)
				.attrID(attrIDLargeRibbon)
				.requiresMagic()
				.manaBonus(200)
				.setRegistryName(NostrumCurio.ID_RIBBON_LARGE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_FIERCE)
				.attrID(attrIDFierceRibbon)
				.requiresMagic()
				.manaBonus(350)
				.manaRegenModifier(-.75f)
				.setRegistryName(NostrumCurio.ID_RIBBON_FIERCE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_KIND)
				.attrID(attrIDKindRibbon)
				.requiresMagic()
				.manaRegenModifier(1.5f)
				.setRegistryName(NostrumCurio.ID_RIBBON_KIND));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_BELT_LIGHTNING)
				.attrID(attrIDLightningBelt)
				.requiresMagic()
				.manaRegenModifier(.1f)
				.setRegistryName(NostrumCurio.ID_BELT_LIGHTNING));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_BELT_ENDER)
				.attrID(attrIDEnderBelt)
				.requiresMagic()
				.manaCostModifier(-.01f)
				.setRegistryName(NostrumCurio.ID_BELT_ENDER));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLD)
				.attrID(attrIDRingGold)
				.requiresMagic()
				.castEfficiency(.125f)
				.setRegistryName(NostrumCurio.ID_RING_GOLD));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLD_TRUE)
				.attrID(attrIDRingTrueGold)
				.requiresMagic()
				.castEfficiency(.25f)
				.setRegistryName(NostrumCurio.ID_RING_GOLD_TRUE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLD_CORRUPTED)
				.attrID(attrIDRingCorruptedGold)
				.requiresMagic()
				.manaCostModifier(-.02f)
				.castEfficiency(.20f)
				.setRegistryName(NostrumCurio.ID_RING_GOLD_CORRUPTED));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_SILVER)
				.attrID(attrIDRingSilver)
				.requiresMagic()
				.manaCostModifier(-.025f)
				.setRegistryName(NostrumCurio.ID_RING_SILVER));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_SILVER_TRUE)
				.attrID(attrIDRingTrueSilver)
				.requiresMagic()
				.manaCostModifier(-.05f)
				.setRegistryName(NostrumCurio.ID_RING_SILVER_TRUE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_SILVER_CORRUPTED)
				.attrID(attrIDRingCorruptedSilver)
				.requiresMagic()
				.manaCostModifier(-.04f)
				.castEfficiency(.10f)
				.setRegistryName(NostrumCurio.ID_RING_SILVER_CORRUPTED));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_MYSTIC)
				.attrID(attrIDRingMystic)
				.requiresMagic()
				.attribute(() -> NostrumAttributes.xpAllElements, new AttributeModifier(attrIDRingMystic, "Mystic Ring", 10.0, AttributeModifier.Operation.ADDITION))
				.setRegistryName(NostrumCurio.ID_RING_MYSTIC)
				);
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_MAGE)
				.attrID(attrIDRingMage)
				.requiresMagic()
				.manaRegenModifier(.25f)
				.setRegistryName(NostrumCurio.ID_RING_MAGE)
				);
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_KOID)
				.attrID(attrIDRingKoid)
				.requiresMagic()
				.attribute(() -> NostrumAttributes.magicDamage, new AttributeModifier(attrIDRingKoid, "Koid Ring", 10.0, AttributeModifier.Operation.ADDITION))
				.manaRegenModifier(.20f)
				.setRegistryName(NostrumCurio.ID_RING_KOID)
				);
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_NECK_KOID)
				.attrID(attrIDNeckKoid)
				.requiresMagic()
				.attribute(() -> NostrumAttributes.magicDamage, new AttributeModifier(attrIDNeckKoid, "Koid Necklace", 15.0, AttributeModifier.Operation.ADDITION))
				.manaBonus(25)
				.setRegistryName(NostrumCurio.ID_NECK_KOID)
				);
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLEM)
				.attrID(attrIDRingGolem)
				.requiresMagic()
				.attribute(() -> NostrumAttributes.reduceAll, new AttributeModifier(attrIDRingGolem, "Golem Ring", 0.25, AttributeModifier.Operation.ADDITION))
				.attribute(() -> Attributes.MOVEMENT_SPEED, new AttributeModifier(attrIDRingGolem, "Golem Ring", -0.05, AttributeModifier.Operation.MULTIPLY_BASE))
				.setRegistryName(NostrumCurio.ID_RING_GOLEM)
				);
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_BELT_GOLEM)
				.attrID(attrIDBeltGolem)
				.requiresMagic()
				.attribute(() -> NostrumAttributes.reduceAll, new AttributeModifier(attrIDBeltGolem, "Golem Belt", 0.25, AttributeModifier.Operation.ADDITION))
				.attribute(() -> Attributes.MOVEMENT_SPEED, new AttributeModifier(attrIDBeltGolem, "Golem Belt", -0.075, AttributeModifier.Operation.MULTIPLY_BASE))
				.attribute(() -> Attributes.ATTACK_DAMAGE, new AttributeModifier(attrIDBeltGolem, "Golem Belt", 1, AttributeModifier.Operation.ADDITION))
				.setRegistryName(NostrumCurio.ID_BELT_GOLEM)
				);
		
		registry.register(new FloatGuardItem().setRegistryName(FloatGuardItem.ID));
		registry.register(new DragonWingPendantItem().setRegistryName(DragonWingPendantItem.ID));
	}

	public static void registerLore() {
		LoreRegistry.instance().register(smallRibbon);
		LoreRegistry.instance().register(mediumRibbon);
		LoreRegistry.instance().register(largeRibbon);
		LoreRegistry.instance().register(fierceRibbon);
		LoreRegistry.instance().register(kindRibbon);
		LoreRegistry.instance().register(lightningBelt);
		LoreRegistry.instance().register(enderBelt);
		LoreRegistry.instance().register(ringGold);
		LoreRegistry.instance().register(ringTrueGold);
		LoreRegistry.instance().register(ringCorruptedGold);
		LoreRegistry.instance().register(ringSilver);
		LoreRegistry.instance().register(ringTrueSilver);
		LoreRegistry.instance().register(ringCorruptedSilver);
		LoreRegistry.instance().register(floatGuard);
		LoreRegistry.instance().register(dragonWingPendant);
		LoreRegistry.instance().register(ringMystic);
		LoreRegistry.instance().register(ringMage);
	}
	
}
