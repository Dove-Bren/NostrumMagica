package com.smanzana.nostrummagica.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CursedIce extends Block {

	public static final String ID = "cursed_ice";
	private static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 2);
	
	private static CursedIce instance = null;
	public static CursedIce instance() {
		if (instance == null)
			instance = new CursedIce();
		
		return instance;
	}
	
	
	public CursedIce() {
		super(Material.ICE, MapColor.ICE);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GLASS);
		this.setLightOpacity(14);
		this.setTickRandomly(true);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LEVEL);
	}
	
	/**
	 * Returns a state that's 0 decay but at the appropriate level
	 * @param level
	 * @return
	 */
	public IBlockState getState(int level) {
		return getDefaultState().withProperty(LEVEL, Math.max(Math.min(2, level - 1), 0));
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(LEVEL, Math.min(2, meta & 0x3));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(LEVEL));
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, 0);
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
	
	@Override
	public boolean isFullCube(IBlockState state) {
        return false;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
		Block block = iblockstate.getBlock();
		
		return !(block == Blocks.GLASS || block == Blocks.STAINED_GLASS
				|| block == Blocks.ICE || block == instance());
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		int level = state.getValue(LEVEL);
		
		// Don't grow is in Sorcery dim
		if (worldIn.provider.getDimension() == ModConfig.config.sorceryDimensionIndex()) {
			return;
		}
		
		if (NostrumMagica.rand.nextFloat() <= 0.2f * (float) (level + 1)) {
			List<BlockPos> targets = Lists.newArrayList(pos.add(1, 0, 0),
									pos.add(0, 0, 1),
									pos.add(-1, 0, 0),
									pos.add(0, 0, -1),
									pos.add(0, 1, 0),
									pos.add(0, -1, 0));
			Collections.shuffle(targets);
			
			for (BlockPos target : targets)
			if (!worldIn.isAirBlock(target)) {
				IBlockState bs = worldIn.getBlockState(target);
				Block b = bs.getBlock();
				if (!(b instanceof BlockIce) && !(b instanceof CursedIce)) {
					if (bs.getBlockHardness(worldIn, target) >= 0.0f &&
							bs.getBlockHardness(worldIn, target) <= Math.pow(2.0f, level)) {
						worldIn.setBlockState(target, Blocks.ICE.getDefaultState());
						return;
					}
					
				} else if (b instanceof BlockIce) {
					// It's ice. Convert to cursed ice
					worldIn.setBlockState(target, getDefaultState());
					return;
				}
			}
		}
    }
	
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		
		if (!worldIn.isRemote) {
			int amp = 0;
			if (worldIn.getBlockState(pos).getValue(LEVEL) == 2)
				amp = 1;
			
			if (entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).getActivePotionEffect(MagicResistPotion.instance()) == null) {
				EntityLivingBase living = (EntityLivingBase) entityIn;
				living.addPotionEffect(new PotionEffect(FrostbitePotion.instance(),
						45, amp));
			}
		}
		
		super.onEntityWalk(worldIn, pos, entityIn);
    }

}
