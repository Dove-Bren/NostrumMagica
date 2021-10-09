package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NostrumRoseItem extends Item implements ILoreTagged {

	public static enum RoseType {
		BLOOD("rose_blood"),
		ELDRICH("rose_eldrich"),
		PALE("rose_pale");
		
		private String key;
		
		private RoseType(String key) {
			this.key = key;
		}
		
		public String getUnlocalizedKey() {
			return key;
		}
	}
	
	public static final String ID = "RoseItem";
	
	private static NostrumRoseItem instance = null;
	public static NostrumRoseItem instance() {
		if (instance == null)
			instance = new NostrumRoseItem();
		
		return instance;
	}
	
	public NostrumRoseItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(NostrumMagica.MODID, ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(16);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		RoseType type = getTypeFromMeta(i);
		return "item." + type.getUnlocalizedKey();
	}
	
	public static RoseType getTypeFromMeta(int meta) {
		RoseType ret = null;
    	for (RoseType type : RoseType.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
	
	public static ItemStack getItem(RoseType type, int count) {
		int meta = getMetaFromType(type);
		
		return new ItemStack(instance(), count, meta);
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
	    	for (RoseType type: RoseType.values()) {
	    		subItems.add(new ItemStack(this, 1, getMetaFromType(type)));
	    	}
		}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_rose_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mystic Roses";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("It appears that some golems carry roses full of power... But what are they for?");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Some golems carry Mystic Roses.", "The roses can be used to created items that increase your abilities as a mage.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static int getMetaFromType(RoseType type) {
		if (type == null)
			return 0;
		
		return type.ordinal();
	}
	
}
