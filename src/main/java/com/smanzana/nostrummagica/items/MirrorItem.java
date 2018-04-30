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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class MirrorItem extends Item implements ILoreTagged {

	public static void init() {
		GameRegistry.addRecipe(new ItemStack(instance), "RQR", "QGQ", "SSS",
				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE),
				'Q', Items.QUARTZ,
				'G', Blocks.GLASS,
				'S', Blocks.STONE);
	}
	
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
		this.setMaxDamage(0);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	@SuppressWarnings("deprecation")
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
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

			if (!flag)
			{
				pos = pos.up();
			}

			int i = MathHelper.floor_double((double)(playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			EnumFacing enumfacing = EnumFacing.getHorizontal(i + 2);
			//BlockPos blockpos = pos.offset(enumfacing);

			if (playerIn.canPlayerEdit(pos, facing, stack))
			{
				//boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
				boolean flag2 = flag || worldIn.isAirBlock(pos);
				//boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);

				if (flag2 && worldIn.getBlockState(pos.down()).isFullyOpaque())
				{
					IBlockState iblockstate1 = NostrumMirrorBlock.instance().getDefaultState().withProperty(NostrumMirrorBlock.FACING, enumfacing);

					worldIn.setBlockState(pos, iblockstate1, 11);

					SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, playerIn);
					worldIn.playSound((EntityPlayer)null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					--stack.stackSize;
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
