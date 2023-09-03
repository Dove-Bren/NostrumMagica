package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.items.MirrorItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;

public class NostrumMirrorBlock extends HorizontalBlock {
	
	public static final String ID = "mirror_block";
	protected static final AxisAlignedBB MIRROR_AABB_EW = new AxisAlignedBB(0.4D, 0.0D, 0.1D, 0.6D, 1.05D, 0.9D);
	protected static final AxisAlignedBB MIRROR_AABB_NS = new AxisAlignedBB(0.1D, 0.0D, 0.4D, 0.9D, 1.05D, 0.6D);
	
	private static NostrumMirrorBlock instance = null;
	public static NostrumMirrorBlock instance() {
		if (instance == null)
			instance = new NostrumMirrorBlock();
		
		return instance;
	}
	
	public NostrumMirrorBlock() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(4.0f);
		this.setResistance(20.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 2);
		this.setLightLevel(.4f);
		this.setLightOpacity(0);
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
		return MirrorItem.instance();
	}
	
	@Override
	public int damageDropped(BlockState state) {
		return 0;
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
		return new ItemStack(MirrorItem.instance());
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
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
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(BlockState state) {
        return false;
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		Direction enumfacing = Direction.getHorizontal(meta);
		return getDefaultState().withProperty(FACING, enumfacing);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.mirrorID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		
		return true;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
}
