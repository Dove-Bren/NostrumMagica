package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ManiCrystal;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		WISP_PEBBLE("wisp_pebble"),
		MANA_LEAF("mana_leaf"),
		EVIL_THISTLE("evil_thistle"),
		;
		
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
	
	public NostrumResourceItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(NostrumMagica.MODID, NostrumResourceItem.ID);
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
    @OnlyIn(Dist.CLIENT)
    @Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    	if (this.isInCreativeTab(tab)) {
	    	for (ResourceType type : ResourceType.values()) {
	    		subItems.add(new ItemStack(this, 1, getMetaFromType(type)));
	    	}
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
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		ResourceType type = getTypeFromMeta(stack.getMetadata());
		if (type == null)
			return;
		
		if (I18n.contains(type.getDescKey())) {
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
	public EnumActionResult onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		
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
			if (facing != Direction.UP) {
				if (!worldIn.getBlockState(pos.offset(facing.getOpposite())).isFullBlock()) {
					facing = Direction.UP;
				}
			}

			if (playerIn.canPlayerEdit(pos, facing, stack) && (block.isReplaceable(worldIn, pos) || worldIn.isAirBlock(pos))) {
				IBlockState iblockstate1 = ManiCrystal.instance().getDefaultState()
						.withProperty(ManiCrystal.FACING, facing)
						.withProperty(ManiCrystal.LEVEL, type == ResourceType.CRYSTAL_MEDIUM ? 1 : 0);

				worldIn.setBlockState(pos, iblockstate1, 11);

				SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
				worldIn.playSound((PlayerEntity)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);
				return EnumActionResult.SUCCESS;
			} else {
				return EnumActionResult.FAIL;
			}
		}
	}
}
