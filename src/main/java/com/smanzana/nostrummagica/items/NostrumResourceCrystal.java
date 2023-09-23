package com.smanzana.nostrummagica.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class NostrumResourceCrystal extends BlockItem implements ILoreTagged {

	public static final String ID_CRYSTAL_SMALL = "crystal_small";
	public static final String ID_CRYSTAL_MEDIUM = "crystal_medium";
	public static final String ID_CRYSTAL_LARGE = "crystal_large";
	
	public NostrumResourceCrystal(@Nonnull Block blockToPlace, Item.Properties properties) {
		super(blockToPlace, properties);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_crystals";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Crystals";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Mani crystals are crystaline stores of power.", "You can already tell they will be useful crafting components!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Mani, Kani, and Vani crystals store varying amounts of power.", "The sheer amount of magic energy that radiates off of each can be felt!", "What's more, the gems seem to attract defending Wisps when placed in the world!");
	}
	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
//		super.addInformation(stack, worldIn, tooltip, flagIn);
//		
//		final String descKey = "item." + this.getRegistryName().getPath() + ".desc";
//		
//		if (I18n.hasKey(descKey)) {
//			tooltip.add(new TranslationTextComponent(descKey));
//		}
//	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	// TODO evaluate
	private int unused;
//	@Override
//	public ActionResultType onItemUse(ItemUseContext context) {
//		final World worldIn = context.getWorld();
//		final @Nonnull ItemStack stack = context.getItem();
//		final BlockPos pos = context.getPos();
//		final Direction facing = context.getFace();
//		final PlayerEntity playerIn = context.getPlayer();
//		
//		if (worldIn.isRemote) {
//			return ActionResultType.SUCCESS;
//		} else {
//			BlockState iblockstate = worldIn.getBlockState(pos);
//			Block block = iblockstate.getBlock();
//
//			if (!iblockstate.isReplaceable(context)) {
//				pos = pos.offset(facing);
//			}
//			
//			// If setting on the side of a non-full block, promote to a regular standing one
//			if (facing != Direction.UP) {
//				if (!worldIn.getBlockState(pos.offset(facing.getOpposite())).isFullBlock()) {
//					facing = Direction.UP;
//				}
//			}
//
//			if (playerIn.canPlayerEdit(pos, facing, stack) && (block.isReplaceable(worldIn, pos) || worldIn.isAirBlock(pos))) {
//				BlockState iblockstate1 = ManiCrystal.instance().getDefaultState()
//						.with(ManiCrystal.FACING, facing)
//						.with(ManiCrystal.LEVEL, type == ResourceType.CRYSTAL_MEDIUM ? 1 : 0);
//
//				worldIn.setBlockState(pos, iblockstate1, 11);
//
//				SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
//				worldIn.playSound((PlayerEntity)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
//				stack.shrink(1);
//				return ActionResultType.SUCCESS;
//			} else {
//				return ActionResultType.FAIL;
//			}
//		}
//	}
}
