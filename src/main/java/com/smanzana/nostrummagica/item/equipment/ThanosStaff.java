package com.smanzana.nostrummagica.item.equipment;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThanosStaff extends SwordItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "thanos_staff";
	private static final String NBT_XP = "absorbed_xp";
	
	protected static UUID THANOSTAFF_POTENCY_UUID = UUID.fromString("d46057a6-872d-45d5-9d09-9cb1f0daf62e");
	
	public ThanosStaff() {
		super(ItemTier.WOOD, 3, -2.4F, NostrumItems.PropEquipment().maxDamage(500));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);
        
        if (equipmentSlot == EquipmentSlotType.MAINHAND)
        {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 4, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
        }
        
        if (equipmentSlot == EquipmentSlotType.MAINHAND || equipmentSlot == EquipmentSlotType.OFFHAND) {
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(THANOSTAFF_POTENCY_UUID, "Potency modifier", 15, AttributeModifier.Operation.ADDITION));
		}

        return builder.build();
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_thanos_staff";
	}

	@Override
	public String getLoreDisplayName() {
		return "Staves of Thanos";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("This staff gives less in the way of bonuses and discounts than a regular Mage Staff.", "The pendant at the end, however, is certainly important...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("The Staff of Thanos gives a modest bonus to potency and discount to reagent cost.", "Like a pendent of Thanos, it will absorb energy and provide a totally free cast after a time.", "The staff can only hold one charge.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return !repair.isEmpty() && NostrumTags.Items.CrystalSmall.contains(repair.getItem());
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// We provide -5% reagent cost, +15% potency
		summary.addReagentCost(-.05f);
		summary.addEfficiency(.15f);
		ItemStacks.damageItem(stack, caster, caster.getHeldItemMainhand() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);

		if (summary.getReagentCost() <= 0) {
			return;
		}
		
		if (hasFreeCast(stack)) {
			summary.addReagentCost(-1f);
			if (!(caster instanceof PlayerEntity) || !((PlayerEntity) caster).isCreative()) {
				removeFreeCast(stack);
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Reagent Cost Discount: 5%"));
	}
	
	public static boolean hasFreeCast(ItemStack staff) {
		if (staff.isEmpty() || !staff.hasTag())
			return false;
		int xp = getXP(staff);
		return xp >= 10;
	}
	
	public static void removeFreeCast(ItemStack staff) {
		if (staff.isEmpty() || !staff.hasTag())
			return;
		
		setXP(staff, (byte) 0);
	}
	
	public static int getXP(ItemStack staff) {
		if (staff.isEmpty() || !staff.hasTag())
			return 0;
		
		CompoundNBT nbt = staff.getTag();
		return nbt.getByte(NBT_XP);
	}
	
	private static void setXP(ItemStack staff, byte xp) {
		if (staff.isEmpty())
			return;
		
		CompoundNBT nbt = staff.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.putByte(NBT_XP, xp);
		staff.setTag(nbt);
	}
	
	public static int addXP(ItemStack staff, int xp) {
		if (staff.isEmpty())
			return xp;
		
		int inStaff = getXP(staff);
		int space = 10 - inStaff;
		int remaining;
		if (space >= xp) {
			inStaff += xp;
			remaining = 0;
		} else {
			inStaff = 10;
			remaining = xp - space;
		}
		
		setXP(staff, (byte) inStaff);
		
		return remaining;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelActivated(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		return entityIn != null && hasFreeCast(stack)
				? 1.0F : 0.0F;
	}
}
