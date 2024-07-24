package com.smanzana.nostrummagica.item.set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BasicEquipmentSet extends EquipmentSet {

	private final List<Supplier<Item>> setItemSuppliers;
	private final Set<Item> setItemsComputed;
	protected final List<Multimap<Attribute, AttributeModifier>> setBonuses;
	
	private boolean computedItems;
	
	protected BasicEquipmentSet(List<Supplier<Item>> setItems, List<Multimap<Attribute, AttributeModifier>> setBonuses) {
		this.setItemSuppliers = setItems;
		this.setBonuses = setBonuses;
		this.setItemsComputed = new HashSet<>(); // empty any needs computing
	}
	
	protected Set<Item> getSetItems() {
		if (!computedItems) {
			computedItems = true;
			for (Supplier<Item> supp : setItemSuppliers) {
				setItemsComputed.add(supp.get());
			}
		}
		return this.setItemsComputed;
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems) {
		final int idx = Math.min(setBonuses.size() - 1, setItems.size() - 1);
		return setBonuses.get(idx);
	}

	@Override
	public boolean isSetItem(ItemStack stack) {
		return !stack.isEmpty() && getSetItems().contains(stack.getItem());
	}

	@Override
	public void setTick(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems) {
		;
	}
	
	public int getSetItemCount() {
		return getSetItems().size();
	}
	
	public int getCurrentSetCount(LivingEntity entity) {
		return NostrumMagica.itemSetListener.getSetCount(entity, this);
	}
	
	public static class Builder {
		
		protected final List<Supplier<Item>> items;
		protected final List<Multimap<Attribute, AttributeModifier>> bonuses;
		
		@SafeVarargs
		public Builder(Supplier<Item> ... items) {
			this.items = ImmutableList.copyOf(items);
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
