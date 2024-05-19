package com.smanzana.nostrummagica.spellcraft.pattern;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spellcraft.modifier.FlatSpellCraftModifier;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumSpellCraftPatterns {

	private static final String ID_LIGHTWEIGHT = "lightweight";
	
	@ObjectHolder(ID_LIGHTWEIGHT) public static StaticSpellCraftPattern lightweight;
	
	@SubscribeEvent
	public static void registerSpellcraftPatterns(RegistryEvent.Register<SpellCraftPattern> event) {
		final IForgeRegistry<SpellCraftPattern> registry = event.getRegistry();
		
		registry.register(new StaticSpellCraftPattern(
				null,
				new FlatSpellCraftModifier.Builder().weight(-1).manaRate(3f).build()
				).setRegistryName(ID_LIGHTWEIGHT));
	}
	
}
