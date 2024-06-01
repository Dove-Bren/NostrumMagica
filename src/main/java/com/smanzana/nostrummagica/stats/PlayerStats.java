package com.smanzana.nostrummagica.stats;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.utils.NetUtils;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;

public class PlayerStats {

	// Mana
	private int manaSpentTotal; // Total mana spent ever
	private int maxReservedMana; // Largest amount of mana reserved at once
	
	// Spells
	private int spellsCast; // Total number of spell casts
	private int uniqueSpellsCast; // Number of different spells cast
	private int totalSpellWeight; // Sum of the weight of all spells cast
	
	// Damage received
	private float damageReceivedTotal; // Total damage received, including non-magical
	private float magicDamageReceivedTotal; // Total magic damage received
	private final Map<EMagicElement, Float> elementalDamageReceived; // Total damage received per element
	
	// Damage Dealt
	private float magicDamageDealtTotal; // Total magic damage dealt
	private final Map<EMagicElement, Float> elementalDamageDealt; // Damage dealt per element
	private float maxSpellDamageDealt; // Max damage done in a single spell
	
	// Kills
	private final Map<ResourceLocation, Integer> entityKills; // Generic kills per entity type
	private final Map<EMagicElement, Integer> elementalKills; // Kills per elementally-attuned entity
	private final Map<EMagicElement, Integer> killsWithElement; // Kills using the provided element;
	private int killsWithMagic; // Kills with any type of magic
	
	public PlayerStats() {
		this.elementalDamageReceived = new EnumMap<>(EMagicElement.class);
		this.elementalDamageDealt = new EnumMap<>(EMagicElement.class);
		this.entityKills = new HashMap<>();
		this.elementalKills = new EnumMap<>(EMagicElement.class);
		this.killsWithElement = new EnumMap<>(EMagicElement.class);
		
	}

	public int getManaSpentTotal() {
		return manaSpentTotal;
	}

	public int getMaxReservedMana() {
		return maxReservedMana;
	}

	public int getSpellsCast() {
		return spellsCast;
	}

	public int getUniqueSpellsCast() {
		return uniqueSpellsCast;
	}

	public int getTotalSpellWeight() {
		return totalSpellWeight;
	}

	public float getDamageReceivedTotal() {
		return damageReceivedTotal;
	}

	public float getMagicDamageReceivedTotal() {
		return magicDamageReceivedTotal;
	}

	public float getElementalDamageReceived(EMagicElement element) {
		return elementalDamageReceived.computeIfAbsent(element, (elem) -> 0f);
	}

	public float getMagicDamageDealtTotal() {
		return magicDamageDealtTotal;
	}

	/**
	 * Max damage done in a single spell
	 * @return
	 */
	public float getMaxSpellDamageDealt() {
		return maxSpellDamageDealt;
	}

	public float getElementalDamageDealt(EMagicElement element) {
		return elementalDamageDealt.computeIfAbsent(element, (elem) -> 0f);
	}

	public int getEntityKills(EntityType<?> entityType) {
		ResourceLocation name = entityType.getRegistryName();
		return entityKills.computeIfAbsent(name, (k) -> 0);
	}

	public int getElementalKills(EMagicElement element) {
		return elementalKills.computeIfAbsent(element, (e) -> 0);
	}

	public int getKillsWithElement(EMagicElement element) {
		return killsWithElement.computeIfAbsent(element, (e) -> 0);
	}

	public int getKillsWithMagic() {
		return killsWithMagic;
	}

	public PlayerStats addManaSpent(int mana) {
		this.manaSpentTotal += mana;
		return this;
	}

	public PlayerStats recordReservedMana(int reservedMana) {
		if (reservedMana > this.maxReservedMana) {
			this.maxReservedMana = reservedMana;
		}
		return this;
	}

	public PlayerStats addSpellsCast(int spellsCast) {
		this.spellsCast += spellsCast;
		return this;
	}

	public PlayerStats addUniqueSpellsCast(int uniqueSpellsCast) {
		this.uniqueSpellsCast += uniqueSpellsCast;
		return this;
	}

	public PlayerStats addTotalSpellWeight(int spellWeight) {
		this.totalSpellWeight += spellWeight;
		return this;
	}

	protected PlayerStats addDamageReceived(float damageReceived) {
		this.damageReceivedTotal += damageReceived;
		return this;
	}

	protected PlayerStats addMagicDamageReceived(float magicDamageReceived) {
		this.magicDamageReceivedTotal += magicDamageReceived;
		return this;
	}

	protected PlayerStats addElementalDamageReceived(EMagicElement element, float amount) {
		this.elementalDamageReceived.merge(element, amount, Float::sum);
		return this;
	}
	
	public PlayerStats addDamageReceived(float amount, @Nullable EMagicElement magicElement) {
		this.addDamageReceived(amount);
		if (magicElement != null) {
			this.addMagicDamageReceived(amount);
			this.addElementalDamageReceived(magicElement, amount);
		}
		return this;
	}

	protected PlayerStats addMagicDamageDealt(float magicDamageDealt) {
		this.magicDamageDealtTotal += magicDamageDealt;
		return this;
	}

	protected PlayerStats addElementalDamageDealt(EMagicElement element, float amount) {
		this.elementalDamageDealt.merge(element, amount, Float::sum);
		return this;
	}
	
	public PlayerStats addMagicDamageDealt(float amount, @Nonnull EMagicElement element) {
		this.addMagicDamageDealt(amount);
		this.addElementalDamageDealt(element, amount);
		return this;
	}

	public PlayerStats recordSpellDamageDealt(float spellDamage) {
		if (spellDamage > this.maxSpellDamageDealt) {
			this.maxSpellDamageDealt = spellDamage;
		}
		return this;
	}

	public PlayerStats addEntityKills(EntityType<?> entityType, int count) {
		this.entityKills.merge(entityType.getRegistryName(), count, Integer::sum);
		return this;
	}

	public PlayerStats addElementalKills(@Nonnull EMagicElement element, int count) {
		this.elementalKills.merge(element, count, Integer::sum);
		return this;
	}

	public PlayerStats addKillsWithElement(@Nonnull EMagicElement element, int count) {
		this.killsWithElement.merge(element, count, Integer::sum);
		return this;
	}

	public PlayerStats addKillsWithMagic(int count) {
		this.killsWithMagic += count;
		return this;
	}
	
	private static final String NBT_MANA_SPENTTOTAL = "mana_spenttotal";
	private static final String NBT_MANA_MAXRESERVED = "mana_maxreserved";
	
	private static final String NBT_SPELLS_CAST = "spells_cast";
	private static final String NBT_SPELLS_UNIQUECAST = "spells_uniquecast";
	private static final String NBT_SPELLS_WEIGHTTOTAL = "spells_weighttotal";
	
	private static final String NBT_DAMRECV_TOTAL = "damagerecv_total";
	private static final String NBT_DAMRECV_MAGICTOTAL = "damagerecv_magictotal";
	private static final String NBT_DAMRECV_ELEMENTAL = "damagerecv_elemental";
	
	private static final String NBT_DAMDEALT_MAGICTOTAL = "damagedealt_magictotal";
	private static final String NBT_DAMDEALT_ELEMENTAL = "damagedealt_elemental";
	private static final String NBT_DAMDEALT_MAXSPELL = "damagedealt_maxspell";
	
	private static final String NBT_KILLS_ENTITY = "kills";
	private static final String NBT_KILLS_ELEMANTALS = "kills_elementals";
	private static final String NBT_KILLS_WITHELEMENT = "kills_withelement";
	private static final String NBT_KILLS_WITHMAGIC = "kills_withmagic";
	
	public @Nonnull CompoundNBT toNBT(@Nullable CompoundNBT nbt) {
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putInt(NBT_MANA_SPENTTOTAL, this.getManaSpentTotal());
		nbt.putInt(NBT_MANA_MAXRESERVED, this.getMaxReservedMana());
		
		nbt.putInt(NBT_SPELLS_CAST, this.getSpellsCast());
		nbt.putInt(NBT_SPELLS_UNIQUECAST, this.getUniqueSpellsCast());
		nbt.putInt(NBT_SPELLS_WEIGHTTOTAL, this.getTotalSpellWeight());
		
		nbt.putFloat(NBT_DAMRECV_TOTAL, this.getDamageReceivedTotal());
		nbt.putFloat(NBT_DAMRECV_MAGICTOTAL, this.getMagicDamageReceivedTotal());
		{
			CompoundNBT tag = NetUtils.ToNBT(this.elementalDamageReceived, FloatNBT::valueOf);
			nbt.put(NBT_DAMRECV_ELEMENTAL, tag);
		}
		
		nbt.putFloat(NBT_DAMDEALT_MAGICTOTAL, this.getMagicDamageDealtTotal());
		{
			CompoundNBT tag = NetUtils.ToNBT(this.elementalDamageDealt, FloatNBT::valueOf);
			nbt.put(NBT_DAMDEALT_ELEMENTAL, tag);
		}
		nbt.putFloat(NBT_DAMDEALT_MAXSPELL, this.getMaxSpellDamageDealt());
		
		CompoundNBT tag;
		tag = NetUtils.ToNBT(this.entityKills, ResourceLocation::toString, IntNBT::valueOf);
		nbt.put(NBT_KILLS_ENTITY, tag);
		tag = NetUtils.ToNBT(this.elementalKills, IntNBT::valueOf);
		nbt.put(NBT_KILLS_ELEMANTALS, tag);
		tag = NetUtils.ToNBT(this.killsWithElement, IntNBT::valueOf);
		nbt.put(NBT_KILLS_WITHELEMENT, tag);
		nbt.putInt(NBT_KILLS_WITHMAGIC, this.getKillsWithMagic());
		
		
		return nbt;
	}
	
	public static final PlayerStats FromNBT(@Nonnull CompoundNBT nbt) {
		PlayerStats stats = new PlayerStats();
		
		stats.manaSpentTotal = nbt.getInt(NBT_MANA_SPENTTOTAL);
		stats.maxReservedMana = nbt.getInt(NBT_MANA_MAXRESERVED);
		
		stats.spellsCast = nbt.getInt(NBT_SPELLS_CAST);
		stats.uniqueSpellsCast = nbt.getInt(NBT_SPELLS_UNIQUECAST);
		stats.totalSpellWeight = nbt.getInt(NBT_SPELLS_WEIGHTTOTAL);
		
		stats.damageReceivedTotal = nbt.getFloat(NBT_DAMRECV_TOTAL);
		stats.magicDamageReceivedTotal = nbt.getFloat(NBT_DAMRECV_MAGICTOTAL);
		{
			CompoundNBT tag = nbt.getCompound(NBT_DAMRECV_ELEMENTAL);
			NetUtils.FromNBT(stats.elementalDamageReceived, EMagicElement.class, tag, (n) -> ((FloatNBT) n).getFloat());
		}
		
		stats.magicDamageDealtTotal = nbt.getFloat(NBT_DAMDEALT_MAGICTOTAL);
		{
			CompoundNBT tag = nbt.getCompound(NBT_DAMDEALT_ELEMENTAL);
			NetUtils.FromNBT(stats.elementalDamageDealt, EMagicElement.class, tag, (n) -> ((FloatNBT) n).getFloat());
		}
		stats.maxSpellDamageDealt = nbt.getFloat(NBT_DAMDEALT_MAXSPELL);
		
		CompoundNBT tag;
		tag = nbt.getCompound(NBT_KILLS_ENTITY);
		NetUtils.FromNBT(stats.entityKills, tag, ResourceLocation::new, (n) -> ((IntNBT) n).getInt());
		tag = nbt.getCompound(NBT_KILLS_ELEMANTALS);
		NetUtils.FromNBT(stats.elementalKills, EMagicElement.class, tag, (n) -> ((IntNBT) n).getInt());
		tag = nbt.getCompound(NBT_KILLS_WITHELEMENT);
		NetUtils.FromNBT(stats.killsWithElement, EMagicElement.class, tag, (n) -> ((IntNBT) n).getInt());
		stats.killsWithMagic = nbt.getInt(NBT_KILLS_WITHMAGIC);
		
		return stats;
	}
	
}
