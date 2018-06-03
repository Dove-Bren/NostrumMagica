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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MageStaff extends ItemSword implements ILoreTagged, ISpellArmor {

	public static String ID = "mage_staff";
	
	public static void init() {
		instance().setUnlocalizedName("mage_staff");
		
		GameRegistry.addRecipe(new ItemStack(instance), " WW", " WC", "W  ",
				'W', Blocks.PLANKS, 
				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
	}
	
	private static MageStaff instance = null;

	public static MageStaff instance() {
		if (instance == null)
			instance = new MageStaff();
	
		return instance;

	}

	public MageStaff() {
		super(ToolMaterial.WOOD);
		this.setMaxDamage(100);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
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
        return repair != null && repair.getItem() == NostrumResourceItem.instance()
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
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add("Reagent Cost Discount: 10%");
	}

}
