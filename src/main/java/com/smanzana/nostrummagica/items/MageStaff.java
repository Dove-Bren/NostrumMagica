package com.smanzana.nostrummagica.items;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MageStaff extends SwordItem implements ILoreTagged, ISpellArmor {

	public static final String ID = "mage_staff";
	
	protected static UUID MAGESTAFF_POTENCY_UUID = UUID.fromString("3c262e7c-237c-48fa-aaf7-7b4be23affb3");
	
	public MageStaff() {
		super(ItemTier.WOOD, 3, -2.4F, NostrumItems.PropEquipment().maxDamage(200));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);

		if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
		}
		
		if (equipmentSlot == EquipmentSlotType.MAINHAND || equipmentSlot == EquipmentSlotType.OFFHAND) {
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
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.CrystalSmall.contains(repair.getItem());
		}
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% reagent cost, +20% potency
		summary.addReagentCost(-.1f);
		//summary.addEfficiency(.2f);
		ItemStacks.damageItem(stack, caster, caster.getHeldItem(Hand.MAIN_HAND) == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		//tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add(new StringTextComponent("Reagent Cost Discount: 10%"));
	}

}
