package com.smanzana.nostrummagica.item.set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.autodungeons.util.NetUtils;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.integration.caelus.NostrumElytraWrapper;
import com.smanzana.nostrummagica.inventory.EquipmentSlotKey;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ElementalArmorSet extends EquipmentSet {
	
	private static final UUID MUTATE_REDUC = UUID.fromString("5fc9f1f2-e6e6-4941-af54-5441e38bf257");
	private static final UUID MUTATE_POTENCY = UUID.fromString("22068d8c-c0f5-4ad7-a44e-1d50ab3e41b6");
	private static final UUID ARMOR_ELYTRA_ID = UUID.fromString("146B0D42-6A18-11EE-8C99-0242AC120002");
	private static final AttributeModifier ARMOR_ELYTRA_MODIFIER = NostrumElytraWrapper
			.MakeHasElytraModifier(ARMOR_ELYTRA_ID, true);
	private static final AttributeModifier ARMOR_NO_ELYTRA_MODIFIER = NostrumElytraWrapper
			.MakeHasElytraModifier(ARMOR_ELYTRA_ID, false);
	private static final UUID ARMOR_JUMP_ID = UUID.fromString("6dafa458-54e0-47e9-b055-65542c6cfde2");

	protected final EMagicElement element;
	protected final ElementalArmor.Type type;
	protected final List<Multimap<Attribute, AttributeModifier>> setBonuses;
	protected @Nullable Consumer<LivingEntity> fullTickFunc;
	
	public ElementalArmorSet(UUID baseID, EMagicElement element, ElementalArmor.Type type, boolean hasFlying, Consumer<LivingEntity> fullTickFunc) {
		this.element = element;
		this.type = type;
		this.fullTickFunc = fullTickFunc;
		
		this.setBonuses = makeSetBonuses(baseID, element, type, hasFlying);
	}
	
	@Override
	public int getFullSetCount() {
		return 4;
	}
	
	protected List<Multimap<Attribute, AttributeModifier>> makeSetBonuses(UUID baseID, EMagicElement element, ElementalArmor.Type type, boolean hasFlying) {
		List<Multimap<Attribute, AttributeModifier>> ret = new ArrayList<>();
		
		final UUID reducID = NetUtils.CombineUUIDs(baseID, MUTATE_REDUC);
		final UUID potencyID = NetUtils.CombineUUIDs(baseID, MUTATE_POTENCY);
		final UUID xpID = NetUtils.CombineUUIDs(baseID, MUTATE_POTENCY);
		
		for (int i = 0; i < getFullSetCount(); i++) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
			for (EMagicElement targElem : EMagicElement.values()) {
				final double amt = ElementalArmor.CalcMagicSetReductTotal(element, type, i + 1, targElem);
				if (amt == 0) {
					continue;
				}
				
				builder.put(NostrumAttributes.GetReduceAttribute(targElem), 
						new AttributeModifier(reducID, "Spell Damage Reduction (Set)", amt, AttributeModifier.Operation.ADDITION));
			}
			
			final double totalAll = ElementalArmor.CalcMagicSetReductTotal(element, type, 4, null);
			if (totalAll != 0) {
				builder.put(NostrumAttributes.reduceAll, 
						new AttributeModifier(reducID, "Spell Damage Reduction (Set)", totalAll, AttributeModifier.Operation.ADDITION));
			}
			
			final double potency = ElementalArmor.CalcArmorMagicBoostTotal(element, type, i + 1);
			if (potency != 0.0) {
				builder.put(NostrumAttributes.magicPotency, 
						new AttributeModifier(potencyID, "Magic Potency (Set)", potency, AttributeModifier.Operation.ADDITION));
			}
			
			if (i == getFullSetCount() - 1) {
				NostrumElytraWrapper.AddElytraModifier(builder, hasFlying ? ARMOR_ELYTRA_MODIFIER : ARMOR_NO_ELYTRA_MODIFIER);
				builder.put(NostrumAttributes.GetXPAttribute(element), new AttributeModifier(xpID, "Elemental XP Bonus (Set)", 100, AttributeModifier.Operation.ADDITION));
				
				if (element == EMagicElement.WIND) {
					builder.put(NostrumAttributes.bonusJump, new AttributeModifier(ARMOR_JUMP_ID, "Storm Armor Bonus Jump (Set)", 1, AttributeModifier.Operation.ADDITION));
				}
			}
			
			ret.add(builder.build());
		}
		
		return ret;
	}
	
	protected static final Map<EMagicElement, Double> makeReducMap(EMagicElement element, ElementalArmor.Type type) {
		Map<EMagicElement, Double> vals = new HashMap<>(); // not enum because we don't want entries for typs that aren't represented
		for (EMagicElement targElem : EMagicElement.values()) {
			final double total = ElementalArmor.CalcMagicSetReductTotal(element, type, 4, targElem);
			if (total != 0) {
				vals.put(targElem, total);
			}
		}
		return vals;
	}

	@Override
	public void setTick(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems) {
		if (fullTickFunc != null && setItems.size() >= getFullSetCount()) {
			fullTickFunc.accept(entity);
		}
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems) {
		final int idx = Math.min(setBonuses.size() - 1, setItems.size() - 1);
		return setBonuses.get(idx);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getFullSetBonuses() {
		return setBonuses.get(getFullSetCount() - 1);
	}

	@Override
	public boolean isSetItem(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof ElementalArmor
				&& ((ElementalArmor) stack.getItem()).getElement() == this.element
				&& ((ElementalArmor) stack.getItem()).getType() == this.type
				;
	}
	
	@Override
	public boolean isSetItemValid(ItemStack stack, IInventorySlotKey<? extends LivingEntity> slot, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> existingItems) {
		if (stack.isEmpty()) {
			return false;
		}
		
		Item item = stack.getItem();
		if (slot instanceof EquipmentSlotKey) {
			return ((EquipmentSlotKey) slot).getSlotType() == ((ArmorItem) item).getEquipmentSlot();
		}
		// else just guess? Could hardcode inventory numbers here
		return false;
	}
	
	@Override
	public List<ITextComponent> getExtraBonuses(int setCount) {
		return super.getExtraBonuses(setCount);
	}
}
