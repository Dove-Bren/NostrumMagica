package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrumaetheria.api.item.IAetherBurnable;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower.Type;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ReagentItem extends Item implements ILoreTagged, IAetherBurnable {

	public static enum ReagentType implements IStringSerializable {
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

		public String prettyName() {
			String name = this.name();
			String out = "";
			int pos = name.indexOf('_');
			while (pos != -1) {
				out += name.substring(0, 1)
						+ name.substring(1, pos).toLowerCase()
						+ " ";
				name = name.substring(pos + 1);
				pos = name.indexOf('_');
			}
			
			out += name.substring(0, 1)
					+ name.substring(1).toLowerCase();
			
			return out;
		}

		@Override
		public String getName() {
			return name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return getName();
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
    
    public static ReagentType findType(ItemStack reagent) {
    	if (reagent == null || !(reagent.getItem() instanceof ReagentItem))
    		return null;
    	
    	for (ReagentType type : ReagentType.values()) {
    		if (type == getTypeFromMeta(reagent.getMetadata()))
    			return type;
    	}
    	
    	return null;
    }
    
    public static String getNameFromMeta(int meta) {
    	String suffix = "unknown";
		
    	ReagentType type = getTypeFromMeta(meta);
    	if (type != null)
    		suffix = type.getTag();
    	
		return suffix;
    }
    
    public static ReagentType getTypeFromMeta(int meta) {
    	ReagentType ret = null;
    	for (ReagentType type : ReagentType.values()) {
			if (type.getMeta() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
    
    public ItemStack getReagent(ReagentType type, int count) {
    	int meta = type.getMeta();
    	return new ItemStack(this, count, meta);
    }
    
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    	ReagentType type = getTypeFromMeta(stack.getMetadata());
    	
    	if (type == ReagentType.MANDRAKE_ROOT) {
    		// Try to plant as seed. Convenient!
    		return ReagentSeed.mandrake.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    	}
    	
    	if (type == ReagentType.GINSENG) {
	    	return ReagentSeed.ginseng.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    	}
    	
    	if (type == ReagentType.CRYSTABLOOM) {
    		IBlockState state = worldIn.getBlockState(pos);
	        if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && state.getBlock().canSustainPlant(state, worldIn, pos, EnumFacing.UP, NostrumMagicaFlower.instance()) && worldIn.isAirBlock(pos.up())) {
	        	worldIn.setBlockState(pos.up(), NostrumMagicaFlower.instance().getState(Type.CRYSTABLOOM));
	            --stack.stackSize;
	            return EnumActionResult.SUCCESS;
	        } else {
	        	return EnumActionResult.FAIL;
	        }
    	}
    	
    	return EnumActionResult.PASS;
	}

	public static void init() {
		GameRegistry.addShapelessRecipe(instance.getReagent(ReagentType.SPIDER_SILK, 1),
				Items.STRING, Items.STRING, Items.SUGAR);
		
		MinecraftForge.addGrassSeed(instance().getReagent(ReagentType.MANDRAKE_ROOT, 1), 6);
		MinecraftForge.addGrassSeed(instance().getReagent(ReagentType.GINSENG, 1), 5);
	}

	@Override
	public String getLoreKey() {
		return "nostrum_reagent_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magical Reagents";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Reagents are used to create and cast spells, as well as in other magical recipes.", "Reagents can be found all over the world, such as in trees, undead enemies, or hidden in the grass.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Reagents are used to create and cast spells, as well as in other magical recipes.", "Reagents can be found all over the world, such as in trees, undead enemies, or hidden in the grass.", "Reagents can be stored in Reagent Bags or in the Spell Table.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_REAGENTS;
	}

	@Override
	public int getBurnTicks(ItemStack stack) {
		return 100;
	}

	@Override
	public float getAetherYield(ItemStack stack) {
		return 150f;
	}
}
