package com.smanzana.nostrummagica.stats;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PlayerStat {
	
	//////  Mana   ///////
	//////////////////////
	
	public static final PlayerStat ManaSpentTotal = new PlayerStat(NostrumMagica.Loc("mana.totalspent")); // Total mana spent ever
	public static final PlayerStat MaxReservedMana = new PlayerStat(NostrumMagica.Loc("mana.maxreserved")); // Largest amount of mana reserved at once
	
	
	//////////////////////
	////// Spells  ///////
	//////////////////////
	
	// Total number of spell casts
	public static final PlayerStat SpellsCast = new PlayerStat(NostrumMagica.Loc("spells.totalcast")); 
	// Number of different spells cast
	public static final PlayerStat UniqueSpellsCast = new PlayerStat(NostrumMagica.Loc("spells.uniquecast"));
	// Sum of the weight of all spells cast
	public static final PlayerStat TotalSpellWeight = new PlayerStat(NostrumMagica.Loc("spells.totalweight")); 
	

	//////////////////////
	// Damage received  //
	//////////////////////
	
	// Total damage received, including non-magical
	public static final PlayerStat DamageReceivedTotal = new PlayerStat(NostrumMagica.Loc("dmgrecv.total"));
	// Total magic damage received
	public static final PlayerStat MagicDamageReceivedTotal = new PlayerStat(NostrumMagica.Loc("dmgrecv.totalmagic"));
	// Total damage received per element
	private static final Map<EMagicElement, PlayerStat> ElementalDamageReceivedMap = new EnumMap<>(EMagicElement.class);
	public static final PlayerStat ElementalDamageReceived(EMagicElement element) {return ElementalDamageReceivedMap.computeIfAbsent(element, (e) -> new ElementalPlayerStat(e, NostrumMagica.Loc("dmgrecv.elemental")));}
	

	//////////////////////
	///  Damage Dealt   ///
	//////////////////////
	
	// Total magic damage dealt
	public static final PlayerStat MagicDamageDealtTotal = new PlayerStat(NostrumMagica.Loc("dmgdealt.totalmagic")); 
	// Damage dealt per element
	private static final Map<EMagicElement, PlayerStat> ElementalDamageDealtMap = new EnumMap<>(EMagicElement.class);
	public static final PlayerStat ElementalDamgeDealt(EMagicElement element) {return ElementalDamageDealtMap.computeIfAbsent(element, (e) -> new ElementalPlayerStat(e, NostrumMagica.Loc("dmgdealt.elemental")));}
	// Max damage done in a single spell
	public static final PlayerStat MaxSpellDamageDealt = new PlayerStat(NostrumMagica.Loc("dmgdealt.maxspell")); 
	

	//////////////////////
	/////    Kills   /////
	//////////////////////
	
	// Generic kills per entity type
	private static final Map<EntityType<?>, PlayerStat> EntityKills = new HashMap<>();
	public static final PlayerStat EntityKills(EntityType<?> type) {return EntityKills.computeIfAbsent(type, (t) -> new EntityTypeStat(t, NostrumMagica.Loc("kills.entity")));}
	// Kills per elementally-attuned entity
	private static final Map<EMagicElement, PlayerStat> ElementalKillsMap = new EnumMap<>(EMagicElement.class);
	public static final PlayerStat ElementalKills(EMagicElement element) {return ElementalKillsMap.computeIfAbsent(element, (e) -> new ElementalPlayerStat(e, NostrumMagica.Loc("kills.elementalskilled")));}
	// Kills using the provided element;
	private static final Map<EMagicElement, PlayerStat> KillsWithElementMap = new EnumMap<>(EMagicElement.class);
	public static final PlayerStat KillsWithElement(EMagicElement element) {return KillsWithElementMap.computeIfAbsent(element, (e) -> new ElementalPlayerStat(e, NostrumMagica.Loc("kills.withelement")));}
	// Kills with any type of magic
	public static final PlayerStat KillsWithMagic = new PlayerStat(NostrumMagica.Loc("kills.totalwithmagic")); 

	private final ResourceLocation id;
	private final TextComponent name;
	
	public PlayerStat(ResourceLocation id) {
		this.id = id;
		this.name = new TranslationTextComponent("stat." + this.id.getNamespace() + "." + this.id.getPath()); 
	}
	
	public @Nonnull ResourceLocation getID() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof PlayerStat && getID().equals(((PlayerStat) o).getID());
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() * 1773 + 97;
	}

	public TextComponent getName() {
		return name;
	}
	
	public static class ElementalPlayerStat extends PlayerStat {
		public ElementalPlayerStat(EMagicElement element, ResourceLocation idBase) {
			super(new ResourceLocation(idBase.getNamespace(), idBase.getPath() + "_" + element.name().toLowerCase()));
		}
	}
	
	public static class EntityTypeStat extends PlayerStat {
		private final TextComponent name;
		public EntityTypeStat(EntityType<?> type, ResourceLocation idBase) {
			super(new ResourceLocation(idBase.getNamespace(), idBase.getPath() + "." + type.getRegistryName().getNamespace() + "." + type.getRegistryName().getPath()));
			this.name = (TextComponent) ((TextComponent) type.getName()).append(new StringTextComponent(" ")).append(new TranslationTextComponent("stat." + idBase.getNamespace() + "." + idBase.getPath())); 
		}

		@Override
		public TextComponent getName() {
			return name;
		}
	}
	
}
