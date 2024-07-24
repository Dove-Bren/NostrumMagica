package com.smanzana.nostrummagica.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BasicEquipmentSet extends EquipmentSet {

	private final Set<Item> setItems;
	private final List<Multimap<Attribute, AttributeModifier>> setBonuses;
	
	protected BasicEquipmentSet(Set<Item> setItems, List<Multimap<Attribute, AttributeModifier>> setBonuses) {
		this.setItems = setItems;
		this.setBonuses = setBonuses;
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems) {
		final int idx = Math.min(setBonuses.size() - 1, setItems.size() - 1);
		return setBonuses.get(idx);
	}

	@Override
	public boolean isSetItem(ItemStack stack) {
		return !stack.isEmpty() && setItems.contains(stack.getItem());
	}

	@Override
	public void setTick(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems) {
		;
	}
	
	public static class Builder {
		
		private final Set<Item> items;
		private final List<Multimap<Attribute, AttributeModifier>> bonuses;
		
		public Builder(Item ... items) {
			this.items = Sets.newHashSet(items);
			this.bonuses = new ArrayList<>();
		}
		
		
		@SuppressWarnings("unchecked")
		protected <T extends Builder> T addBonusInternal(Multimap<Attribute, AttributeModifier> bonus) {
			if (bonuses.size() >= items.size()) {
				throw new IllegalStateException("Cannot add more set count bonuses, as there are already more than the total number of pieces");
			}
			bonuses.add(bonus);
			return (T) this;
		}
		
		public <T extends Builder> T addBonus(Multimap<Attribute, AttributeModifier> bonus) {
			return addBonusInternal(bonus);
		}
		
		public <T extends Builder> T  addEmptyBonus() {
			return addBonusInternal(ImmutableMultimap.of());
		}
		
		public <T extends Builder> T  addLastBonus() {
			return addBonusInternal(bonuses.get(bonuses.size() - 1));
		}
		
		public BasicEquipmentSet build() {
			return new BasicEquipmentSet(items, bonuses);
		}
	}
}
