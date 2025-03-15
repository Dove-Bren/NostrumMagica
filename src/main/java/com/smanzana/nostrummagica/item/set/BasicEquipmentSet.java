package com.smanzana.nostrummagica.item.set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.inventory.EquipmentSlotKey;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.inventory.PlayerInventorySlotKey;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;

public class BasicEquipmentSet extends EquipmentSet {

	private final List<Supplier<Item>> setItemSuppliers;
	private final Set<Item> setItemsComputed;
	protected final List<Multimap<Attribute, AttributeModifier>> setBonuses;
	
	protected boolean requireUniqueItems = true;
	
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
	
	protected int getUniqueItemCount(Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems) {
		if (requireUniqueItems) {
			return setItems.values().stream().filter(i -> !i.isEmpty()).map(i -> i.getItem()).collect(Collectors.toSet()).size();
		} else {
			return setItems.size();
		}
	}
	
	protected boolean itemIsWeaponOrTool(Item item) {
		return item instanceof SwordItem
				|| item instanceof ToolItem
				|| item instanceof BowItem
				;
	}
	
	@Override
	public boolean isSetItemValid(ItemStack stack, IInventorySlotKey<? extends LivingEntity> slot, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> existingItems) {
		if (stack.isEmpty()) {
			return false;
		}
		
		Item item = stack.getItem();
		
		// If unique items are required, check that first
		if (requireUniqueItems && existingItems.values().stream().filter(i -> !i.isEmpty()).map(s -> s.getItem()).filter(i -> i == item).findAny().isPresent()) {
			return false;
		}
		
		// If item is armor, make sure it's in right slot
		if (item instanceof ArmorItem) {
			if (slot instanceof EquipmentSlotKey) {
				return ((EquipmentSlotKey) slot).getSlotType() == ((ArmorItem) item).getSlot();
			}
			// else just guess? Could hardcode inventory numbers here in case it's a PlayerInventorySlotKey?
			return false;
		}
		
		// If slot type is equipment slot, always accept non-hands (since this isn't armor so it can't specify).
		// If it's a tool or weapon, also accept hands.
		if (slot instanceof EquipmentSlotKey) {
			return itemIsWeaponOrTool(stack.getItem())
					|| ((EquipmentSlotKey) slot).getSlotType().getType() == EquipmentSlotType.Group.ARMOR;
		}
		
		// Not just blindly guess if that if it's not a PlayerInventorySlotKey, it's probably something special like
		// a curio slot or something
		return (!(slot instanceof PlayerInventorySlotKey));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems) {
		final int idx = Math.min(setBonuses.size() - 1, getUniqueItemCount(setItems) - 1);
		return setBonuses.get(idx);
	}

	@Override
	public boolean isSetItem(ItemStack stack) {
		return !stack.isEmpty() && getSetItems().contains(stack.getItem());
	}

	@Override
	public void setTick(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems) {
		;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getFullSetBonuses() {
		return setBonuses.get(getFullSetCount() - 1);
	}

	@Override
	public int getFullSetCount() {
		return getSetItems().size();
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
