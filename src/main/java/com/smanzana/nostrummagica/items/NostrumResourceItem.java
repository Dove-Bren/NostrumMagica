package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ManiCrystal;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Misc. resource items for delayed progression
 * @author Skyler
 *
 */
public class NostrumResourceItem extends Item implements ILoreTagged {

	public static enum ResourceType {
		TOKEN("token"),
		CRYSTAL_SMALL("crystal_small"),
		CRYSTAL_MEDIUM("crystal_medium"),
		CRYSTAL_LARGE("crystal_large"),
		PENDANT_LEFT("pendant_left"),
		PENDANT_RIGHT("pendant_right"),
		SLAB_FIERCE("slab_fierce"),
		SLAB_KIND("slab_kind"),
		SLAB_BALANCED("slab_balanced"),
		SPRITE_CORE("sprite_core"),
		ENDER_BRISTLE("ender_bristle"),
		WISP_PEBBLE("wisp_pebble");
		
		private String key;
		
		private ResourceType(String key) {
			this.key = key;
		}
		
		public String getUnlocalizedKey() {
			return key;
		}
		
		private String getDescKey() {
			return "item." + key + ".desc";
		}
	}
	
	public static final String ID = "nostrum_resource";
	
	private static NostrumResourceItem instance = null;
	public static NostrumResourceItem instance() {
		if (instance == null)
			instance = new NostrumResourceItem();
		
		return instance;
	}
	
	public static void init() {
		// Only thing with regular crafting recipe is small crystal
		
		GameRegistry.addRecipe(getItem(ResourceType.CRYSTAL_SMALL, 1), " MR", "MDM", "RM ",
				'D', Items.DIAMOND,
				'M', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
	}
	
	public NostrumResourceItem() {
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
		
		ResourceType type = getTypeFromMeta(i);
		return "item." + type.getUnlocalizedKey();
	}
	
	/**
	 * Returns an itemstack of the specified type
	 * @param type
	 * @param count
	 * @return
	 */
	public static ItemStack getItem(ResourceType type, int count) {
		int meta = getMetaFromType(type);
		
		return new ItemStack(instance(), count, meta);
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (ResourceType type : ResourceType.values()) {
    		subItems.add(new ItemStack(itemIn, 1, getMetaFromType(type)));
    	}
	}
    
    public static int getMetaFromType(ResourceType type) {
    	return type.ordinal();
    }
    
    public static ResourceType getTypeFromMeta(int meta) {
    	ResourceType ret = null;
    	for (ResourceType type : ResourceType.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
    
    @Override
	public String getLoreKey() {
		return "nostrum_resource";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Resources";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("There are many crafted resources in Nostrum Magica.", "Each is a little different, but you can't help but feel as if they are all somehow connected...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Nostrum Magica adds a handful of unique crafted resources.", "These resources are used in rituals and special crafts.");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack == null)
			return;
		
		ResourceType type = getTypeFromMeta(stack.getMetadata());
		if (type == null)
			return;
		
		if (I18n.hasKey(type.getDescKey())) {
			String translation = I18n.format(type.getDescKey(), new Object[0]);
			if (translation.trim().isEmpty())
				return;
			tooltip.add(translation);
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		// Copied from ItemBed (vanilla) with some modifications
		ResourceType type = getTypeFromMeta(stack.getMetadata()); 
		
		if (type != ResourceType.CRYSTAL_SMALL && type != ResourceType.CRYSTAL_MEDIUM) {
			return EnumActionResult.PASS;
		}
		
		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		} else {
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if (!block.isReplaceable(worldIn, pos)) {
				pos = pos.offset(facing);
			}
			
			// If setting on the side of a non-full block, promote to a regular standing one
			if (facing != EnumFacing.UP) {
				if (!worldIn.getBlockState(pos.offset(facing.getOpposite())).isFullBlock()) {
					facing = EnumFacing.UP;
				}
			}

			if (playerIn.canPlayerEdit(pos, facing, stack) && (block.isReplaceable(worldIn, pos) || worldIn.isAirBlock(pos))) {
				IBlockState iblockstate1 = ManiCrystal.instance().getDefaultState()
						.withProperty(ManiCrystal.FACING, facing)
						.withProperty(ManiCrystal.LEVEL, type == ResourceType.CRYSTAL_MEDIUM ? 1 : 0);

				worldIn.setBlockState(pos, iblockstate1, 11);

				SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
				worldIn.playSound((EntityPlayer)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				--stack.stackSize;
				return EnumActionResult.SUCCESS;
			} else {
				return EnumActionResult.FAIL;
			}
		}
	}
}
