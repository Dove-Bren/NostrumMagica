package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MagicWall extends Block {

	public static final String ID = "magic_wall";
	private static final PropertyInteger DECAY = PropertyInteger.create("decay", 0, 3);
	private static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 2);
	
	private static MagicWall instance = null;
	public static MagicWall instance() {
		if (instance == null)
			instance = new MagicWall();
		
		return instance;
	}
	
	
	public MagicWall() {
		super(Material.PLANTS, MapColor.EMERALD);
		this.setUnlocalizedName(ID);
		this.setHardness(0.01f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GLASS);
		this.setLightOpacity(2);
		this.setTickRandomly(true);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(DECAY, 0)
				.withProperty(LEVEL, 0));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
//	@Override
//	public boolean isVisuallyOpaque() {
//		return false;
//	}
	
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
		return new BlockStateContainer(this, DECAY, LEVEL);
	}
	
	/**
	 * Returns a state that's 0 decay but at the appropriate level
	 * @param level
	 * @return
	 */
	public IBlockState getState(int level) {
		return getDefaultState().withProperty(DECAY, 0)
				.withProperty(LEVEL, Math.max(Math.min(2, level - 1), 0));
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(DECAY, meta & 0x3)
				.withProperty(LEVEL, Math.min(2, (meta >> 2) & 0x3));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(LEVEL) << 2) | (state.getValue(DECAY));
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, 0);
	}
	
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
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
        		|| block == instance());
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			int decay = state.getValue(DECAY) + 1;
			if (decay >= 4) {
				worldIn.setBlockToAir(pos);
			} else {
				worldIn.setBlockState(pos, state.withProperty(DECAY, decay));
			}
    }
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		
		int level = state.getValue(LEVEL);
		
		if (level <= 0
				|| (level >= 2 && !(entityIn instanceof EntityPlayer))
				|| (level == 1 && !(entityIn instanceof EntityItem))) {
			super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
		
    }

}
