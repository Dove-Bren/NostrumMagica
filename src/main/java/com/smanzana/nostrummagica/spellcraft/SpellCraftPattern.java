package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * A 'pattern' applied to a spell crafting attempt on a spell table.
 * The pattern is made up of modifiers to individual runes when creating the final spell.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public abstract class SpellCraftPattern extends ForgeRegistryEntry<SpellCraftPattern> {

	protected static IForgeRegistry<SpellCraftPattern> REGISTRY;
	
	public SpellCraftPattern() {
		
	}
	
	public abstract boolean hasModifier(SpellCraftContext context, int slot);
	
	public abstract @Nullable ISpellCraftModifier getModifier(SpellCraftContext context, int slot);
	
	@OnlyIn(Dist.CLIENT)
	public abstract void drawPatternIcon(MatrixStack matrixStackIn, SpellCraftContext context, int width, int height, float red, float green, float blue, float alpha);
	
	@SubscribeEvent
	public static void CreateRegistry(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<SpellCraftPattern>()
				.setName(NostrumMagica.Loc("spellcraft_pattern"))
				.setType(SpellCraftPattern.class)
				.setMaxID(Integer.MAX_VALUE - 1) // copied from GameData, AKA Forge's registration
				.disableSaving()
			.create();
	}
	
}
