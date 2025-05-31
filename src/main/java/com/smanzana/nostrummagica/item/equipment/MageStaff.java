package com.smanzana.nostrummagica.item.equipment;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.ISpellEquipment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MageStaff extends SwordItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "mage_staff";
	
	protected static UUID MAGESTAFF_POTENCY_UUID = UUID.fromString("3c262e7c-237c-48fa-aaf7-7b4be23affb3");
	
	public MageStaff() {
		super(Tiers.WOOD, 3, -2.4F, NostrumItems.PropEquipment().durability(200));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);

		if (equipmentSlot == EquipmentSlot.MAINHAND) {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 3, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
		}
		
		if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(MAGESTAFF_POTENCY_UUID, "Potency modifier", 20, AttributeModifier.Operation.ADDITION));
		}

        return builder.build();
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_mage_staff";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mage Staves";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By combining regular with with Mani Crystals, you've constructed a staff!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By combining regular with with Mani Crystals, you've constructed a staff!", "The staff providese a bonus to spell potency and a discount to reagent cost!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return repair.is(NostrumTags.Items.CrystalSmall);
		}
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% reagent cost, +20% potency
		summary.addReagentCost(-.1f);
		//summary.addEfficiency(.2f);
		ItemStacks.damageItem(stack, caster, caster.getItemInHand(InteractionHand.MAIN_HAND) == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		//tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add(new TextComponent("Reagent Cost Discount: 10%"));
	}

}
