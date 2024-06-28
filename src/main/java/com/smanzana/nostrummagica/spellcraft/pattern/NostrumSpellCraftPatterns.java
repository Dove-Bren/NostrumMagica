package com.smanzana.nostrummagica.spellcraft.pattern;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spellcraft.modifier.FlatSpellCraftModifier;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumSpellCraftPatterns {

	private static final String ID_MANAFOOT = "manafoot";
	private static final String ID_LIGHTWEIGHT = "lightweight";
	private static final String ID_WOBBLY = "wobbly";
	private static final String ID_SHORTHEAVY = "shortheavy";
	private static final String ID_BARBED = "barbed";
	private static final String ID_TICKTOCK = "ticktock";
	private static final String ID_TOCKTICK = "tocktick";
	
	@ObjectHolder(ID_LIGHTWEIGHT) public static StaticSpellCraftPattern lightweight;
	
	@SubscribeEvent
	public static void registerSpellcraftPatterns(RegistryEvent.Register<SpellCraftPattern> event) {
		final IForgeRegistry<SpellCraftPattern> registry = event.getRegistry();
		
		final ISpellCraftModifier lightButDouble = new FlatSpellCraftModifier.Builder().weight(-1).manaRate(1f).build();
		final ISpellCraftModifier halfMana = new FlatSpellCraftModifier.Builder().manaRate(-.5f).build();
		final ISpellCraftModifier doubleMana = new FlatSpellCraftModifier.Builder().manaRate(1f).build();
		final ISpellCraftModifier zeroMana = new FlatSpellCraftModifier.Builder().manaRate(-1f).build();
		//final ISpellCraftModifier oneWeight = new FlatSpellCraftModifier.Builder().weight(1).build();
		final ISpellCraftModifier twoWeight = new FlatSpellCraftModifier.Builder().weight(2).build();
		final ISpellCraftModifier lightInflict = new FlatSpellCraftModifier.Builder().weight(-1).overrideAlteration(EAlteration.INFLICT).build();
//		final ISpellCraftModifier doublePower = new FlatSpellCraftModifier.Builder().efficiency(1f).build();
//		final ISpellCraftModifier halfPower = new FlatSpellCraftModifier.Builder().efficiency(-.5f).build();
		final ISpellCraftModifier lightButWeak = new FlatSpellCraftModifier.Builder().weight(-1).efficiency(-.25f).build();
		
		registry.register(new StaticSpellCraftPattern(
				null,
				lightButDouble
				).setRegistryName(ID_MANAFOOT));
		
		registry.register(new StaticSpellCraftPattern(
				lightButWeak,
				lightButWeak,
				lightButWeak,
				lightButWeak,
				lightButWeak,
				lightButWeak,
				lightButWeak,
				lightButWeak
				).setRegistryName(ID_LIGHTWEIGHT));
		
		registry.register(new StaticSpellCraftPattern(
				doubleMana,
				null,
				halfMana,
				null,
				doubleMana,
				null,
				halfMana,
				null,
				doubleMana,
				null,
				halfMana,
				null,
				doubleMana,
				null,
				halfMana
				).setRegistryName(ID_WOBBLY));
		
		registry.register(new StaticSpellCraftPattern(
				null,
				twoWeight,
				null,
				zeroMana
				).setRegistryName(ID_SHORTHEAVY));
		
		registry.register(new StaticSpellCraftPattern(
				halfMana,
				null,
				null,
				lightInflict
				).setRegistryName(ID_BARBED));
				
		registry.register(new StaticSpellCraftPattern(
				halfMana,
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build(),
				new FlatSpellCraftModifier.Builder().weight(1).efficiency(-.25f).build()
				).setRegistryName(ID_TICKTOCK));
		
		registry.register(new StaticSpellCraftPattern(
				new FlatSpellCraftModifier.Builder().weight(2).manaRate(1f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.1f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.15f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.2f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.25f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.25f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.25f).build(),
				new FlatSpellCraftModifier.Builder().efficiency(.5f).build()
				).setRegistryName(ID_TOCKTICK));
				
	}
	
}
