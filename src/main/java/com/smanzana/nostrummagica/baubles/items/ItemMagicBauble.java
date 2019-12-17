package com.smanzana.nostrummagica.baubles.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface="baubles.api.IBauble", modid="Baubles")
public class ItemMagicBauble extends Item implements ILoreTagged, ISpellArmor, IBauble {

	public static enum ItemType {
		RIBBON_SMALL("ribbon_small"),
		RIBBON_MEDIUM("ribbon_medium"),
		RIBBON_LARGE("ribbon_large"),
		RIBBON_FIERCE("ribbon_fierce"),
		RIBBON_KIND("ribbon_kind"),
		BELT_LIGHTNING("belt_lightning"),
		BELT_ENDER("belt_ender"),
		RING_GOLD("ring_gold"),
		RING_GOLD_TRUE("ring_gold_true"),
		RING_GOLD_CORRUPTED("ring_gold_corrupted"),
		RING_SILVER("ring_silver"),
		RING_SILVER_TRUE("ring_silver_true"),
		RING_SILVER_CORRUPTED("ring_silver_corrupted"),
		TRINKET_FLOAT_GUARD("float_guard");
		
		private String key;
		
		private ItemType(String key) {
			this.key = key;
		}
		
		public String getUnlocalizedKey() {
			return key;
		}
		
		private String getDescKey() {
			return "item." + key + ".desc";
		}
	}
	
	public static int getMetaFromType(ItemType type) {
    	return type.ordinal();
    }
    
    public static ItemType getTypeFromMeta(int meta) {
    	ItemType ret = null;
    	for (ItemType type : ItemType.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
	
	public static String ID = "ribbon";
	
	public static void init() {
		instance().setUnlocalizedName(ID);
		
		GameRegistry.addRecipe(new ItemStack(instance), " W ", "WCW", " W ",
				'W', Blocks.WOOL, 
				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
	}
	
	private static ItemMagicBauble instance = null;

	public static ItemMagicBauble instance() {
		if (instance == null)
			instance = new ItemMagicBauble();
	
		return instance;

	}
	
	public static ItemStack getItem(ItemType type, int count) {
		int meta = getMetaFromType(type);
		
		return new ItemStack(instance(), count, meta);
	}
	

	public ItemMagicBauble() {
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		ItemType type = getTypeFromMeta(i);
		return "item." + type.getUnlocalizedKey();
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (ItemType type : ItemType.values()) {
			subItems.add(new ItemStack(itemIn, 1, getMetaFromType(type)));
		}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_baubles";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Baubles";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By imbuing raw materials with magical reagents, you've discovered a way to created small baubles that enhance your magical powers!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By imbuing raw materials with magical reagents, you've discovered a way to created small baubles that enhance your magical powers!", "Cloth and precious metals seem to be especially willing to be imbued. While leather hasn't proven the same, you're sure you can affix a crystal or two to make it work!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack == null)
			return;
		
		ItemType type = getTypeFromMeta(stack.getMetadata());
		if (type == null)
			return;
		
		if (I18n.hasKey(type.getDescKey())) {
			// Format with placeholders for blue and red formatting
			String translation = I18n.format(type.getDescKey(), TextFormatting.GRAY, TextFormatting.BLUE, TextFormatting.DARK_RED);
			if (translation.trim().isEmpty())
				return;
			String lines[] = translation.split("\\|");
			for (String line : lines) {
				tooltip.add(line);
			}
		}
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public BaubleType getBaubleType(ItemStack itemstack) {
		BaubleType btype = BaubleType.RING;
		ItemType type = getTypeFromMeta(itemstack.getMetadata());
		switch (type) {
		case BELT_ENDER:
		case BELT_LIGHTNING:
			btype = BaubleType.BELT;
			break;
		case RIBBON_LARGE:
		case RIBBON_MEDIUM:
		case RIBBON_SMALL:
		case RIBBON_FIERCE:
		case RIBBON_KIND:
			btype = BaubleType.AMULET;
			break;
		case RING_GOLD:
		case RING_GOLD_CORRUPTED:
		case RING_GOLD_TRUE:
		case RING_SILVER:
		case RING_SILVER_CORRUPTED:
		case RING_SILVER_TRUE:
			btype = BaubleType.RING;
			break;
		case TRINKET_FLOAT_GUARD:
			btype = BaubleType.TRINKET;
			break;
		}
		
		return btype;
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		ItemType type = getTypeFromMeta(itemstack.getMetadata());
		switch (type) {
		case RIBBON_LARGE:
			attr.addManaBonus(600);
			break;
		case RIBBON_MEDIUM:
			attr.addManaBonus(250);
			break;
		case RIBBON_SMALL:
			attr.addManaBonus(100);
			break;
		case RIBBON_FIERCE:
			attr.addManaBonus(1000);
			attr.addManaRegenModifier(-.75f);
			break;
		case RIBBON_KIND:
			attr.addManaRegenModifier(1.5f);
			break;
		case BELT_ENDER:
			attr.addManaCostModifer(-0.01f);
			break;
		case BELT_LIGHTNING:
			attr.addManaRegenModifier(0.10f);
			break;
		case RING_GOLD:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_GOLD_CORRUPTED:
			; // Potency Handled on-cast. Not an attribute thing. Skip.
			attr.addManaCostModifer(-0.02f);
			break;
		case RING_GOLD_TRUE:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_SILVER:
			attr.addManaCostModifer(-0.025f);
			break;
		case RING_SILVER_CORRUPTED:
			attr.addManaCostModifer(-0.04f);
			; // Potency handled on-cast
			break;
		case RING_SILVER_TRUE:
			attr.addManaCostModifer(-0.05f);
			break;
		case TRINKET_FLOAT_GUARD:
			; // Checked upon floating
			break;
		}
		
	}
	
	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	@Override
	@Optional.Method(modid="Baubles")
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {	
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		ItemType type = getTypeFromMeta(itemstack.getMetadata());
		switch (type) {
		case RIBBON_LARGE:
			attr.addManaBonus(-600);
			break;
		case RIBBON_MEDIUM:
			attr.addManaBonus(-250);
			break;
		case RIBBON_SMALL:
			attr.addManaBonus(-100);
			break;
		case RIBBON_FIERCE:
			attr.addManaBonus(-1000);
			attr.addManaRegenModifier(.75f);
			break;
		case RIBBON_KIND:
			attr.addManaRegenModifier(-1.5f);
			break;
		case BELT_ENDER:
			attr.addManaCostModifer(0.01f);
			break;
		case BELT_LIGHTNING:
			attr.addManaRegenModifier(-0.10f);
			break;
		case RING_GOLD:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_GOLD_CORRUPTED:
			; // Potency Handled on-cast. Not an attribute thing. Skip.
			attr.addManaCostModifer(0.02f);
			break;
		case RING_GOLD_TRUE:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_SILVER:
			attr.addManaCostModifer(0.025f);
			break;
		case RING_SILVER_CORRUPTED:
			attr.addManaCostModifer(0.04f);
			; // Potency handled on-cast
			break;
		case RING_SILVER_TRUE:
			attr.addManaCostModifer(0.05f);
			break;
		case TRINKET_FLOAT_GUARD:
			; // Checked upon floating
			break;
		}
	}

	/**
	 * can this bauble be placed in a bauble slot
	 */
	@Override
	@Optional.Method(modid="Baubles")
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr != null && attr.isUnlocked();
	}

	@Override
	public void apply(EntityLivingBase caster, SpellCastSummary summary, ItemStack stack) {
		if (stack == null) {
			return;
		}
		
		if (!(stack.getItem() instanceof ItemMagicBauble)) {
			return;
		}
		
		ItemType type = getTypeFromMeta(stack.getMetadata());
		switch (type) {
		case BELT_ENDER:
		case BELT_LIGHTNING:
		case RIBBON_FIERCE:
		case RIBBON_KIND:
		case RIBBON_LARGE:
		case RIBBON_MEDIUM:
		case RIBBON_SMALL:
		case RING_SILVER:
		case RING_SILVER_TRUE:
		case TRINKET_FLOAT_GUARD:
			; // Nothing to do
			break;
		case RING_GOLD:
			// Increase potency by 12.5%
			summary.addEfficiency(.125f);
			break;
		case RING_GOLD_CORRUPTED:
			summary.addEfficiency(.20f);
			break;
		case RING_GOLD_TRUE:
			summary.addEfficiency(.25f);
			break;
		case RING_SILVER_CORRUPTED:
			summary.addEfficiency(.10f);
			break;
		}
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (stack == null) {
			return;
		}
		
		if (!(stack.getItem() instanceof ItemMagicBauble)) {
			return;
		}
		
		ItemType type = getTypeFromMeta(stack.getMetadata());
		switch (type) {
		case BELT_ENDER:
		case BELT_LIGHTNING:
		case RIBBON_FIERCE:
		case RIBBON_KIND:
		case RIBBON_LARGE:
		case RIBBON_MEDIUM:
		case RIBBON_SMALL:
		case RING_SILVER:
		case RING_SILVER_TRUE:
		case RING_GOLD:
		case RING_GOLD_CORRUPTED:
		case RING_GOLD_TRUE:
		case RING_SILVER_CORRUPTED:
			break;
		case TRINKET_FLOAT_GUARD:
			player.removePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
		}
	}

}
