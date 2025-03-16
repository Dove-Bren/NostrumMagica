package com.smanzana.nostrummagica.spellcraft.pattern;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
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
	
	public String getUnlocalizedName() {
		return "pattern." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath();
	}
	
	public Component getName() {
		return new TranslatableComponent(getUnlocalizedName()).withStyle(ChatFormatting.DARK_GREEN);
	}
	
	public String getUnlocalizedDescription() {
		return getUnlocalizedName() + ".description";
	}
	
	public List<Component> addDescription(List<Component> description) {
		description.addAll(TextUtils.GetTranslatedList(getUnlocalizedDescription()));
		return description;
	}
	
	
	
	
	public static final @Nullable SpellCraftPattern Get(ResourceLocation id) {
		return REGISTRY.getValue(id);
	}
	
	public static final Collection<SpellCraftPattern> GetAll() {
		return REGISTRY.getValues();
	}
	
	@SubscribeEvent
	public static final void CreateRegistry(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<SpellCraftPattern>()
				.setName(NostrumMagica.Loc("spellcraft_pattern"))
				.setType(SpellCraftPattern.class)
				.setMaxID(Integer.MAX_VALUE - 1) // copied from GameData, AKA Forge's registration
				.disableSaving()
			.create();
	}
	
}
