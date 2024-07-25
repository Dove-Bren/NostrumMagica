package com.smanzana.nostrummagica.item.set;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;

public class MageArmorSet extends BasicEquipmentSet {
	
	protected MageArmorSet(List<Supplier<Item>> setItems, List<Multimap<Attribute, AttributeModifier>> setBonuses) {
		super(setItems, setBonuses);
	}
	
	protected static class Builder extends BasicEquipmentSet.Builder {
		
		@SafeVarargs
		public Builder(Supplier<Item> ... items) {
			super(items);
		}
		
		@Override
		public MageArmorSet build() {
			return new MageArmorSet(this.items, this.bonuses);
		}
	}
	
	public static MageArmorSet Build(UUID modifierID) {
		@SuppressWarnings("unchecked")
		Supplier<Item>[] items = new Supplier[]{
				() -> NostrumItems.mageArmorHelm,
				() -> NostrumItems.mageArmorChest,
				() -> NostrumItems.mageArmorLegs,
				() -> NostrumItems.mageArmorFeet,
				() -> NostrumCurios.ringCorruptedSilver,
				() -> NostrumCurios.ringCorruptedGold
			};
		return (MageArmorSet) (new Builder(items)
				.addBonus(MakeBonus(1, modifierID))
				.addBonus(MakeBonus(2, modifierID))
				.addBonus(MakeBonus(3, modifierID))
				.addBonus(MakeBonus(4, modifierID))
				.addBonus(MakeBonus(5, modifierID))
				.addBonus(MakeBonus(6, modifierID))
				.build());
	}
	
	protected static final Multimap<Attribute, AttributeModifier> MakeBonus(int pieces, UUID modifierID) {
		pieces = Math.min(6, Math.max(0, pieces));
		
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		
		final double potency = new int[]{0, 5, 5, 10, 10, 20}[pieces-1];
		final double xpBoost = new int[]{0, 0, 5, 5, 10, 15}[pieces-1];
		final double reduc = new float[]{0, 0, .5f, .5f, 1f, 1f}[pieces-1];
		final double regen = new float[]{0, 5, 5, 10, 20, 30}[pieces-1];
		
		if (potency != 0) {
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(modifierID,
				"Magic Potency (Mage Set)", (double) potency, AttributeModifier.Operation.ADDITION));
		}
		
		if (xpBoost != 0) {
			builder.put(NostrumAttributes.xpBonus, new AttributeModifier(modifierID,
				"XP Boost (Mage Set)", (double) xpBoost, AttributeModifier.Operation.ADDITION));
		}
		
		if (regen != 0) {
			builder.put(NostrumAttributes.manaRegen, new AttributeModifier(modifierID,
				"Mana Regen (Mage Set)", (double) regen, AttributeModifier.Operation.ADDITION));
		}
		
		if (reduc != 0) {
			for (EMagicElement elem : EMagicElement.values()) {
				builder.put(NostrumAttributes.GetReduceAttribute(elem), new AttributeModifier(modifierID,
					"Magic Reduction (Mage Set)", (double) reduc, AttributeModifier.Operation.ADDITION));
			}
		}
		
		return builder.build();
	}
}
