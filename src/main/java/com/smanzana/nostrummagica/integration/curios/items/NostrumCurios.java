package com.smanzana.nostrummagica.integration.curios.items;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

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
	
	public static Item.Properties PropBase() {
		return new Item.Properties()
				.group(NostrumMagica.equipmentTab)
				;
	}
	
	public static Item.Properties PropCurio() {
		return PropBase()
				.maxStackSize(1)
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
	}
	
}
