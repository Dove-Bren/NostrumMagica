package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.MandrakeRoot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
		
		return this.getUnlocalizedName() + "." + suffix;
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
		
    	ReagentType type = getTypeFromMeta(meta);
    	if (type != null)
    		suffix = type.getTag();
    	
		return suffix;
    }
    
    public ReagentType getTypeFromMeta(int meta) {
    	ReagentType ret = null;
    	for (ReagentType type : ReagentType.values()) {
			if (type.getMeta() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
    
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	ReagentType type = getTypeFromMeta(stack.getMetadata());
    	
    	if (type == ReagentType.MANDRAKE_ROOT) {
	    	IBlockState state = worldIn.getBlockState(pos);
	        if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && state.getBlock().canSustainPlant(state, worldIn, pos, EnumFacing.UP, MandrakeRoot.instance()) && worldIn.isAirBlock(pos.up())) {
	        	worldIn.setBlockState(pos.up(), MandrakeRoot.instance().getDefaultState());
	            --stack.stackSize;
	            return EnumActionResult.SUCCESS;
	        } else {
	        	return EnumActionResult.FAIL;
	        }
    	}
    	
    	return EnumActionResult.PASS;
	}
}
