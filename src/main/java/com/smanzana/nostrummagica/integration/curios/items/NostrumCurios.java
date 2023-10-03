package com.smanzana.nostrummagica.integration.curios.items;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
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
	@ObjectHolder(ShieldRingItem.ID_SMALL) public static @Nullable ShieldRingItem ringShieldSmall; // Requires Aether
	@ObjectHolder(ShieldRingItem.ID_LARGE) public static @Nullable ShieldRingItem ringShieldLarge; // Requires Aether
	@ObjectHolder(EludeCloakItem.ID) public static @Nullable EludeCloakItem eludeCape; // Requires Aether
	@ObjectHolder(DragonWingPendantItem.ID) public static DragonWingPendantItem dragonWingPendant;
	@ObjectHolder(AetherCloakItem.ID) public static @Nullable Item aetherCloak; // Requires Aether
	
	public static Item.Properties PropBase() {
		return new Item.Properties()
				.group(NostrumMagica.creativeTab)
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
				.requiresMagic()
				.manaBonus(100)
				.setRegistryName(NostrumCurio.ID_RIBBON_SMALL));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_MEDIUM)
				.requiresMagic()
				.manaBonus(250)
				.setRegistryName(NostrumCurio.ID_RIBBON_MEDIUM));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_LARGE)
				.requiresMagic()
				.manaBonus(600)
				.setRegistryName(NostrumCurio.ID_RIBBON_LARGE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_FIERCE)
				.requiresMagic()
				.manaBonus(1000)
				.manaRegenModifier(-.75f)
				.setRegistryName(NostrumCurio.ID_RIBBON_FIERCE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RIBBON_KIND)
				.requiresMagic()
				.manaRegenModifier(1.5f)
				.setRegistryName(NostrumCurio.ID_RIBBON_KIND));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_BELT_LIGHTNING)
				.requiresMagic()
				.manaRegenModifier(.1f)
				.setRegistryName(NostrumCurio.ID_BELT_LIGHTNING));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_BELT_ENDER)
				.requiresMagic()
				.manaCostModifier(-.01f)
				.setRegistryName(NostrumCurio.ID_BELT_ENDER));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLD)
				.requiresMagic()
				.castEfficiency(.125f)
				.setRegistryName(NostrumCurio.ID_RING_GOLD));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLD_TRUE)
				.requiresMagic()
				.castEfficiency(.25f)
				.setRegistryName(NostrumCurio.ID_RING_GOLD_TRUE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_GOLD_CORRUPTED)
				.requiresMagic()
				.manaCostModifier(-.02f)
				.castEfficiency(.20f)
				.setRegistryName(NostrumCurio.ID_RING_GOLD_CORRUPTED));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_SILVER)
				.requiresMagic()
				.manaCostModifier(-.025f)
				.setRegistryName(NostrumCurio.ID_RING_SILVER));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_SILVER_TRUE)
				.requiresMagic()
				.manaCostModifier(-.05f)
				.setRegistryName(NostrumCurio.ID_RING_SILVER_TRUE));
		
		registry.register(new NostrumCurio(PropCurio(), NostrumCurio.ID_RING_SILVER_CORRUPTED)
				.requiresMagic()
				.manaCostModifier(-.04f)
				.castEfficiency(.10f)
				.setRegistryName(NostrumCurio.ID_RING_SILVER_CORRUPTED));
		
		registry.register(new FloatGuardItem().setRegistryName(FloatGuardItem.ID));
		registry.register(new DragonWingPendantItem().setRegistryName(DragonWingPendantItem.ID));
		
		if (NostrumMagica.instance.aetheria.isEnabled()) {
			registry.register(new ShieldRingItem(2, ShieldRingItem.ID_SMALL).setRegistryName(ShieldRingItem.ID_SMALL));
			registry.register(new ShieldRingItem(4, ShieldRingItem.ID_LARGE).setRegistryName(ShieldRingItem.ID_LARGE));
			registry.register(new EludeCloakItem().setRegistryName(EludeCloakItem.ID));
			registry.register(new AetherCloakItem().setRegistryName(AetherCloakItem.ID));
		}
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
		
		if (NostrumMagica.instance.aetheria.isEnabled()) {
			LoreRegistry.instance().register(ringShieldSmall);
			LoreRegistry.instance().register(ringShieldLarge);
			LoreRegistry.instance().register(eludeCape);
			LoreRegistry.instance().register((ILoreTagged) aetherCloak); // Requires Aether
		}
	}
	
}
