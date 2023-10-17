package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThanosStaff extends SwordItem implements ILoreTagged, ISpellArmor {

	public static final String ID = "thanos_staff";
	private static final String NBT_XP = "absorbed_xp";
	
	public ThanosStaff() {
		super(ItemTier.WOOD, 3, -2.4F, NostrumItems.PropEquipment().maxDamage(500));
		
		this.addPropertyOverride(new ResourceLocation("activated"), new IItemPropertyGetter() {
			@OnlyIn(Dist.CLIENT)
			@Override
			public float call(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
				return entityIn != null && hasFreeCast(stack)
						? 1.0F : 0.0F;
			}
		});
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

        if (equipmentSlot == EquipmentSlotType.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 4, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
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
        return !repair.isEmpty() && NostrumItemTags.Items.CrystalSmall.contains(repair.getItem());
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
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
		tooltip.add(new StringTextComponent("Magic Potency Bonus: 15%"));
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
}
