package com.smanzana.nostrummagica.items;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThanosStaff extends ItemSword implements ILoreTagged, ISpellArmor {

	public static String ID = "thanos_staff";
	private static final String NBT_XP = "absorbed_xp";
	
	private static ThanosStaff instance = null;

	public static ThanosStaff instance() {
		if (instance == null)
			instance = new ThanosStaff();
	
		return instance;

	}

	public ThanosStaff() {
		super(ToolMaterial.WOOD);
		this.setMaxDamage(500);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ID);
		this.setRegistryName(NostrumMagica.MODID, ID);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 4, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
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
        return !repair.isEmpty() && repair.getItem() == NostrumResourceItem.instance()
        		&& NostrumResourceItem.getTypeFromMeta(repair.getMetadata()) == ResourceType.CRYSTAL_SMALL;
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// We provide -5% reagent cost, +15% potency
		summary.addReagentCost(-.05f);
		summary.addEfficiency(.15f);
		stack.damageItem(1, caster);

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
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add("Magic Potency Bonus: 15%");
		tooltip.add("Reagent Cost Discount: 5%");
	}
	
	public static boolean hasFreeCast(ItemStack staff) {
		if (staff.isEmpty() || !staff.hasTagCompound())
			return false;
		int xp = getXP(staff);
		return xp >= 10;
	}
	
	public static void removeFreeCast(ItemStack staff) {
		if (staff.isEmpty() || !staff.hasTagCompound())
			return;
		
		setXP(staff, (byte) 0);
	}
	
	public static int getXP(ItemStack staff) {
		if (staff.isEmpty() || !staff.hasTagCompound())
			return 0;
		
		CompoundNBT nbt = staff.getTagCompound();
		return nbt.getByte(NBT_XP);
	}
	
	private static void setXP(ItemStack staff, byte xp) {
		if (staff.isEmpty())
			return;
		
		CompoundNBT nbt = staff.getTagCompound();
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.setByte(NBT_XP, xp);
		staff.setTagCompound(nbt);
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
	public static class ModelMesher implements ItemMeshDefinition {

		@Override
		public ModelResourceLocation getModelLocation(ItemStack stack) {
			String suffix = "";
			
			if (hasFreeCast(stack))
				suffix = "_activated";
			
			return new ModelResourceLocation(
					new ResourceLocation(NostrumMagica.MODID, ID + suffix),
					"inventory");
		}
	}

}
