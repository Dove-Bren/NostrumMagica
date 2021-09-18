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

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MageStaff extends ItemSword implements ILoreTagged, ISpellArmor {

	public static String ID = "mage_staff";
	
	private static MageStaff instance = null;

	public static MageStaff instance() {
		if (instance == null)
			instance = new MageStaff();
	
		return instance;

	}

	public MageStaff() {
		super(ToolMaterial.WOOD);
		this.setMaxDamage(200);
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
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
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
        return !repair.isEmpty() && repair.getItem() == NostrumResourceItem.instance()
        		&& NostrumResourceItem.getTypeFromMeta(repair.getMetadata()) == ResourceType.CRYSTAL_SMALL;
    }

	@Override
	public void apply(EntityLivingBase caster, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% reagent cost, +20% potency
		summary.addReagentCost(-.1f);
		summary.addEfficiency(.2f);
		stack.damageItem(1, caster);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add("Reagent Cost Discount: 10%");
	}

}
