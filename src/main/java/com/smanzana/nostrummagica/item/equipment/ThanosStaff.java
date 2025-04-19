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

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThanosStaff extends SwordItem implements ILoreTagged, ISpellEquipment {

	public static final String ID = "thanos_staff";
	private static final String NBT_XP = "absorbed_xp";
	
	protected static UUID THANOSTAFF_POTENCY_UUID = UUID.fromString("d46057a6-872d-45d5-9d09-9cb1f0daf62e");
	
	public ThanosStaff() {
		super(Tiers.WOOD, 3, -2.4F, NostrumItems.PropEquipment().durability(500));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);
        
        if (equipmentSlot == EquipmentSlot.MAINHAND)
        {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 4, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
        }
        
        if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND) {
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
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return !repair.isEmpty() && repair.is(NostrumTags.Items.CrystalSmall);
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// We provide -5% reagent cost, +15% potency
		summary.addReagentCost(-.05f);
		summary.addEfficiency(.15f);
		ItemStacks.damageItem(stack, caster, caster.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);

		if (summary.getReagentCost() <= 0) {
			return;
		}
		
		if (hasFreeCast(stack)) {
			summary.addReagentCost(-1f);
			if (!(caster instanceof Player) || !((Player) caster).isCreative()) {
				removeFreeCast(stack);
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent("Reagent Cost Discount: 5%"));
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
		
		CompoundTag nbt = staff.getTag();
		return nbt.getByte(NBT_XP);
	}
	
	private static void setXP(ItemStack staff, byte xp) {
		if (staff.isEmpty())
			return;
		
		CompoundTag nbt = staff.getTag();
		if (nbt == null)
			nbt = new CompoundTag();
		
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
	public static final float ModelActivated(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		return entityIn != null && hasFreeCast(stack)
				? 1.0F : 0.0F;
	}
}
