package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ReagentItem extends Item {

	public static enum ReagentType {
		// Do not rearrange.
		MANDRAKE_ROOT("mandrake_root"),
		SPIDER_SILK("spider_silk"),
		BLACK_PEARL("black_pearl"),
		SKY_ASH("sky_ash"),
		GINSENG("ginseng"),
		GRAVE_DUST("grave_dust"),
		CRYSTABLOOM("crystabloom"),
		MANI_DUST("mani_dust");
		
		private String tag;
		private int meta;
		
		private ReagentType(String tag) {
			this.tag = tag;
			this.meta = ordinal();
		}
		
		public String getTag() {
			return tag;
		}
		
		public int getMeta() {
			return meta;
		}
	}
	
	public static final String ID = "nostrum_reagent";
	
	private static ReagentItem instance = null;
	public static ReagentItem instance() {
		if (instance == null)
			instance = new ReagentItem();
		
		return instance;
	}
	
	public ReagentItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(64);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = getNameFromMeta(i);
		
		return ID + "." + suffix;
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (ReagentType type : ReagentType.values()) {
    		subItems.add(new ItemStack(itemIn, 1, type.getMeta()));
    	}
	}
    
    public String getNameFromMeta(int meta) {
    	String suffix = "unknown";
		for (ReagentType type : ReagentType.values()) {
			if (type.getMeta() == meta) {
				suffix = type.getTag();
			}
		}
		return suffix;
    }
}
