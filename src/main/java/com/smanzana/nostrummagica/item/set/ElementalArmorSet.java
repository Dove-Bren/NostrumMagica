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
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;

public class MagicArmorSet extends EquipmentSet {
	
	private static final UUID MUTATE_REDUC = UUID.fromString("5fc9f1f2-e6e6-4941-af54-5441e38bf257");
	private static final UUID MUTATE_POTENCY = UUID.fromString("22068d8c-c0f5-4ad7-a44e-1d50ab3e41b6");
	private static final UUID ARMOR_ELYTRA_ID = UUID.fromString("146B0D42-6A18-11EE-8C99-0242AC120002");
	private static final AttributeModifier ARMOR_ELYTRA_MODIFIER = NostrumElytraWrapper
			.MakeHasElytraModifier(ARMOR_ELYTRA_ID, true);
	private static final AttributeModifier ARMOR_NO_ELYTRA_MODIFIER = NostrumElytraWrapper
			.MakeHasElytraModifier(ARMOR_ELYTRA_ID, false);

	protected final EMagicElement element;
	protected final ElementalArmor.Type type;
	protected final List<Multimap<Attribute, AttributeModifier>> setBonuses;
	protected @Nullable Consumer<LivingEntity> fullTickFunc;
	
	public MagicArmorSet(UUID baseID, EMagicElement element, ElementalArmor.Type type, boolean hasFlying, Consumer<LivingEntity> fullTickFunc) {
		this.element = element;
		this.type = type;
		this.fullTickFunc = fullTickFunc;
		
		this.setBonuses = makeSetBonuses(baseID, element, type, hasFlying);
	}
	
	protected final int getFullSetCount() {
		return 4;
	}
	
	protected List<Multimap<Attribute, AttributeModifier>> makeSetBonuses(UUID baseID, EMagicElement element, ElementalArmor.Type type, boolean hasFlying) {
		List<Multimap<Attribute, AttributeModifier>> ret = new ArrayList<>();
		
		final UUID reducID = NetUtils.CombineUUIDs(baseID, MUTATE_REDUC);
		final UUID potencyID = NetUtils.CombineUUIDs(baseID, MUTATE_POTENCY);
		
		for (int i = 0; i < getFullSetCount(); i++) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
			for (EMagicElement targElem : EMagicElement.values()) {
				final double amt = ElementalArmor.CalcMagicSetReductTotal(element, type, i + 1, targElem);
				if (amt == 0) {
					continue;
				}
				
				builder.put(NostrumAttributes.GetReduceAttribute(targElem), 
						new AttributeModifier(reducID, "Magic Reduction (Set)", amt, AttributeModifier.Operation.ADDITION));
			}
			
			final double potency = ElementalArmor.CalcArmorMagicBoostTotal(element, type, i + 1);
			if (potency != 0.0) {
				builder.put(NostrumAttributes.magicPotency, 
						new AttributeModifier(potencyID, "Magic Potency (Set)", potency, AttributeModifier.Operation.ADDITION));
			}
			
			if (i == getFullSetCount() - 1) {
				NostrumElytraWrapper.AddElytraModifier(builder, hasFlying ? ARMOR_ELYTRA_MODIFIER : ARMOR_NO_ELYTRA_MODIFIER);
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
	public void setTick(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems) {
		if (fullTickFunc != null && setItems.size() >= getFullSetCount()) {
			fullTickFunc.accept(entity);
		}
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems) {
		final int idx = Math.min(setBonuses.size() - 1, setItems.size() - 1);
		return setBonuses.get(idx);
	}

	@Override
	public boolean isSetItem(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getItem() instanceof ElementalArmor
				&& ((ElementalArmor) stack.getItem()).getElement() == this.element
				&& ((ElementalArmor) stack.getItem()).getType() == this.type
				;
	}
}
