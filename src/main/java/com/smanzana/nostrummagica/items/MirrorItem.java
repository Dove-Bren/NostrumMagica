package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MirrorItem extends Item implements ILoreTagged {

	private static MirrorItem instance = null;
	public static final String ID = "mirror_item";

	public static MirrorItem instance() {
		if (instance == null)
			instance = new MirrorItem();
	
		return instance;

	}

	public MirrorItem() {
		super();
		this.setMaxStackSize(64);
		this.setUnlocalizedName(ID);
		this.setRegistryName(NostrumMagica.MODID, MirrorItem.ID);
		this.setMaxDamage(0);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		
		// Copied from ItemBed (vanilla) with some modifications
		
		if (worldIn.isRemote)
		{
			return EnumActionResult.SUCCESS;
		}
		else if (facing != EnumFacing.UP)
		{
			return EnumActionResult.FAIL;
		}
		else
		{
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();
			boolean flag = block.isReplaceable(worldIn, pos);
			final ItemStack stack = playerIn.getHeldItem(hand);

			if (!flag)
			{
				pos = pos.up();
			}

			int i = MathHelper.floor((double)(playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			EnumFacing enumfacing = EnumFacing.getHorizontal(i + 2);
			//BlockPos blockpos = pos.offset(enumfacing);

			if (playerIn.canPlayerEdit(pos, facing, stack))
			{
				//boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
				boolean flag2 = flag || worldIn.isAirBlock(pos);
				//boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);

				if (flag2 && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP))
				{
					IBlockState iblockstate1 = NostrumMirrorBlock.instance().getDefaultState().withProperty(NostrumMirrorBlock.FACING, enumfacing);

					worldIn.setBlockState(pos, iblockstate1, 11);

					SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
					worldIn.playSound((EntityPlayer)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					stack.shrink(1);
					return EnumActionResult.SUCCESS;
				}
				else
				{
					return EnumActionResult.FAIL;
				}
			}
			else
			{
				return EnumActionResult.FAIL;
			}
		}
	}

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
