package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.item.BlockItem;

public class MirrorItem extends BlockItem implements ILoreTagged {

	public static final String ID = "mirror_item";

	public MirrorItem() {
		super(NostrumBlocks.mirrorBlock, NostrumItems.PropBase());
	}
	
//	@Override
//	public ActionResultType onItemUse(ItemUseContext context) {
//		
//		// Copied from ItemBed (vanilla) with some modifications
//		
//		final World worldIn = context.getWorld();
//		final Direction facing = context.getFace();
//		final PlayerEntity playerIn = context.getPlayer();
//		final BlockPos pos = context.getPos();
//		
//		
//		if (worldIn.isRemote)
//		{
//			return ActionResultType.SUCCESS;
//		}
//		else if (facing != Direction.UP)
//		{
//			return ActionResultType.FAIL;
//		}
//		else
//		{
//			BlockState iblockstate = worldIn.getBlockState(pos);
//			Block block = iblockstate.getBlock();
//			boolean flag = block.isReplaceable(iblockstate, context);
//			final ItemStack stack = context.getItem();
//
//			if (!flag)
//			{
//				pos = pos.up();
//			}
//
//			int i = MathHelper.floor((double)(playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
//			Direction enumfacing = Direction.byHorizontalIndex(i + 2);
//			//BlockPos blockpos = pos.offset(enumfacing);
//
//			if (playerIn.canPlayerEdit(pos, facing, stack))
//			{
//				//boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
//				boolean flag2 = flag || worldIn.isAirBlock(pos);
//				//boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);
//
//				if (flag2 && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), Direction.UP))
//				{
//					BlockState iblockstate1 = NostrumMirrorBlock.instance().getDefaultState().with(NostrumMirrorBlock.FACING, enumfacing);
//
//					worldIn.setBlockState(pos, iblockstate1, 11);
//
//					SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
//					worldIn.playSound((PlayerEntity)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
//					stack.shrink(1);
//					return ActionResultType.SUCCESS;
//				}
//				else
//				{
//					return ActionResultType.FAIL;
//				}
//			}
//			else
//			{
//				return ActionResultType.FAIL;
//			}
//		}
//	}

	@Override
	public String getLoreKey() {
		return "nostrum_mirror";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Mirror";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("The magic mirror allows you to view and increase your magic attributes.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("The magic mirror allows you to view and increase your magic attributes.", "Use it to spend points you earn as you level up.", "Level up by casting new types of spells.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_BLOCKS;
	}
}
