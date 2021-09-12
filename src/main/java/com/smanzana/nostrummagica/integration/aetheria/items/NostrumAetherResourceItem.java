package com.smanzana.nostrummagica.integration.aetheria.items;

import java.util.List;

import com.smanzana.nostrumaetheria.api.item.IAetherBurnable;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Misc. resource items for aether-related progression
 * @author Skyler
 *
 */
public class NostrumAetherResourceItem extends Item implements ILoreTagged, IAetherBurnable {

	public static final String ID = "nostrum_aether_resource";
	
	private static NostrumAetherResourceItem instance = null;
	public static NostrumAetherResourceItem instance() {
		if (instance == null)
			instance = new NostrumAetherResourceItem();
		
		return instance;
	}
	
	public static void init() {
//		GameRegistry.addRecipe(getItem(AetherResourceType.CRYSTAL_SMALL, 1), " MR", "MDM", "RM ",
//				'D', Items.DIAMOND,
//				'M', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
//				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
	}
	
	public NostrumAetherResourceItem() {
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
		
		AetherResourceType type = getTypeFromMeta(i);
		return "item." + type.getUnlocalizedKey();
	}
	
	/**
	 * Returns an itemstack of the specified type
	 * @param type
	 * @param count
	 * @return
	 */
	public static ItemStack getItem(AetherResourceType type, int count) {
		int meta = getMetaFromType(type);
		
		return new ItemStack(instance(), count, meta);
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    	for (AetherResourceType type : AetherResourceType.values()) {
    		subItems.add(new ItemStack(this, 1, getMetaFromType(type)));
    	}
	}
    
    public static int getMetaFromType(AetherResourceType type) {
    	return type.ordinal();
    }
    
    public static AetherResourceType getTypeFromMeta(int meta) {
    	AetherResourceType ret = null;
    	for (AetherResourceType type : AetherResourceType.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
    
    @Override
	public String getLoreKey() {
		return "nostrum_aether_resource";
	}

	@Override
	public String getLoreDisplayName() {
		return "Aether Flowers";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Flowers of mandrake and ginseng that can't be used as reagents... and yet, you can tell there's something magical about them.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Flowers of mandrake and ginseng with high levels of aether.", "These flowers cannot be used as reagents by themselves but produce more aether than regular reagents when burned.");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		AetherResourceType type = getTypeFromMeta(stack.getMetadata());
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
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
//		// Copied from ItemBed (vanilla) with some modifications
//		AetherResourceType type = getTypeFromMeta(stack.getMetadata()); 
//		
//		if (type != AetherResourceType.CRYSTAL_SMALL && type != AetherResourceType.CRYSTAL_MEDIUM) {
//			return EnumActionResult.PASS;
//		}
//		
//		if (worldIn.isRemote) {
//			return EnumActionResult.SUCCESS;
//		} else {
//			IBlockState iblockstate = worldIn.getBlockState(pos);
//			Block block = iblockstate.getBlock();
//
//			if (!block.isReplaceable(worldIn, pos)) {
//				pos = pos.offset(facing);
//			}
//			
//			// If setting on the side of a non-full block, promote to a regular standing one
//			if (facing != EnumFacing.UP) {
//				if (!worldIn.getBlockState(pos.offset(facing.getOpposite())).isFullBlock()) {
//					facing = EnumFacing.UP;
//				}
//			}
//
//			if (playerIn.canPlayerEdit(pos, facing, stack) && (block.isReplaceable(worldIn, pos) || worldIn.isAirBlock(pos))) {
//				IBlockState iblockstate1 = ManiCrystal.instance().getDefaultState()
//						.withProperty(ManiCrystal.FACING, facing)
//						.withProperty(ManiCrystal.LEVEL, type == AetherResourceType.CRYSTAL_MEDIUM ? 1 : 0);
//
//				worldIn.setBlockState(pos, iblockstate1, 11);
//
//				SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
//				worldIn.playSound((EntityPlayer)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
//				--stack.stackSize;
//				return EnumActionResult.SUCCESS;
//			} else {
//				return EnumActionResult.FAIL;
//			}
//		}
		
		return EnumActionResult.PASS;
	}

	@Override
	public int getBurnTicks(ItemStack stack) {
		AetherResourceType type = getTypeFromMeta(stack.getMetadata());
		switch (type) {
		case FLOWER_GINSENG:
		case FLOWER_MANDRAKE:
			return 300;
		}
		
		return 0;
	}

	@Override
	public float getAetherYield(ItemStack stack) {
		AetherResourceType type = getTypeFromMeta(stack.getMetadata());
		switch (type) {
		case FLOWER_GINSENG:
			return 450;
		case FLOWER_MANDRAKE:
			return 350;
		}
		
		return 0;
	}
}
