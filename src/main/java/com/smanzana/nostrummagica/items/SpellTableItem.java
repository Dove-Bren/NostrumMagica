package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.SpellTable;
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

public class SpellTableItem extends Item implements ILoreTagged {

	public static void init() {
		
	}
	
	private static SpellTableItem instance = null;
	public static final String ID = "spell_table";

	public static SpellTableItem instance() {
		if (instance == null)
			instance = new SpellTableItem();
	
		return instance;

	}

	public SpellTableItem() {
		super();
		this.setMaxStackSize(64);
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
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

			int i = MathHelper.floor((double)(playerIn.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			EnumFacing enumfacing = EnumFacing.getHorizontal(i + 1);
			BlockPos blockpos = pos.offset(enumfacing);

			if (playerIn.canPlayerEdit(pos, facing, stack) && playerIn.canPlayerEdit(blockpos, facing, stack))
			{
				boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
				boolean flag2 = flag || worldIn.isAirBlock(pos);
				boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);

				if (flag2 && flag3 && worldIn.getBlockState(pos.down()).isOpaqueCube() && worldIn.getBlockState(blockpos.down()).isOpaqueCube())
				{
					IBlockState iblockstate1 = SpellTable.instance().getSlaveState(enumfacing);

					if (worldIn.setBlockState(pos, iblockstate1, 11))
					{
						IBlockState iblockstate2 = SpellTable.instance().getMaster(enumfacing.getOpposite());
						worldIn.setBlockState(blockpos, iblockstate2, 11);
					}

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
		return "nostrum_spell_table";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Table";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Spell Tables are used to create spells.", "Combine spell runes, blank scrolls, and reagents to create Spell Scrolls.", "Spells must begin with a trigger. After that, any triggers or shapes afterwards can be used.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Spell Tables are used to create spells.", "Combine spell runes, blank scrolls, and reagents to create Spell Scrolls.", "Spells must begin with a trigger. After that, any triggers or shapes afterwards can be used.", "Reagents must be slotted into the table in order to be used.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
}
