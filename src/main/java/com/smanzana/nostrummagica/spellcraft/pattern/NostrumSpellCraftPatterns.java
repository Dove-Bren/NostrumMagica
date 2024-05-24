package com.smanzana.nostrummagica.spellcraft.pattern;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spellcraft.modifier.FlatSpellCraftModifier;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumSpellCraftPatterns {

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
		
		final ISpellCraftModifier lightButTriple = new FlatSpellCraftModifier.Builder().weight(-1).manaRate(2f).build();
		final ISpellCraftModifier halfMana = new FlatSpellCraftModifier.Builder().manaRate(-.5f).build();
		final ISpellCraftModifier doubleMana = new FlatSpellCraftModifier.Builder().manaRate(1f).build();
		final ISpellCraftModifier zeroMana = new FlatSpellCraftModifier.Builder().manaRate(-1f).build();
		//final ISpellCraftModifier oneWeight = new FlatSpellCraftModifier.Builder().weight(1).build();
		final ISpellCraftModifier twoWeight = new FlatSpellCraftModifier.Builder().weight(2).build();
		final ISpellCraftModifier lightInflict = new FlatSpellCraftModifier.Builder().weight(-1).overrideAlteration(EAlteration.INFLICT).build();
		
		registry.register(new StaticSpellCraftPattern(
				null,
				lightButTriple
				).setRegistryName(ID_LIGHTWEIGHT));
		
		registry.register(new StaticSpellCraftPattern(
				doubleMana,
				halfMana,
				doubleMana,
				halfMana,
				doubleMana,
				halfMana,
				doubleMana,
				halfMana
				).setRegistryName(ID_WOBBLY));
		
		registry.register(new StaticSpellCraftPattern(
				null,
				twoWeight,
				zeroMana
				).setRegistryName(ID_SHORTHEAVY));
		
		registry.register(new StaticSpellCraftPattern(
				null,
				lightInflict,
				halfMana
				).setRegistryName(ID_BARBED));
				
		registry.register(new StaticSpellCraftPattern(
				halfMana,
				new FlatSpellCraftModifier.Builder().weight(-1).overrideShape(SingleShape.instance()).overrideAlteration(EAlteration.RESIST).build(),
				new FlatSpellCraftModifier.Builder().weight(-1).overrideShape(SingleShape.instance()).overrideAlteration(EAlteration.RUIN).build()
				).setRegistryName(ID_TICKTOCK));
		
		registry.register(new StaticSpellCraftPattern(
				halfMana,
				new FlatSpellCraftModifier.Builder().weight(-1).overrideShape(SingleShape.instance()).overrideAlteration(EAlteration.CORRUPT).build(),
				new FlatSpellCraftModifier.Builder().weight(-1).overrideShape(SingleShape.instance()).overrideAlteration(EAlteration.GROWTH).build()
				).setRegistryName(ID_TOCKTICK));
				
	}
	
}
