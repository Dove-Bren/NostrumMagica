package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellTableItem;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellTable extends BlockHorizontal {// implements ITileEntityProvider {
	
//	private static class SpellTableEntity extends TileEntity {
//		
//	}

	private static final PropertyBool MASTER = PropertyBool.create("master");
	
	public static final String ID = "spell_table";
	
	private static SpellTable instance = null;
	public static SpellTable instance() {
		if (instance == null)
			instance = new SpellTable();
		
		return instance;
	}
	
	
	public SpellTable() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(15.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 3);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, true)
				.withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean isVisuallyOpaque() {
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
		return new BlockStateContainer(this, MASTER, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
		return getDefaultState().withProperty(FACING, enumfacing)
				.withProperty(MASTER, ((meta >> 2) & 1) == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((state.getValue(MASTER) ? 1 : 0) << 2) | (state.getValue(FACING).getHorizontalIndex());
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		destroy(worldIn, pos, state);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
		destroy(worldIn, pos, null);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(IBlockState state, BlockPos pos) {
		return pos.offset(state.getValue(FACING));
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	public IBlockState getSlaveState(EnumFacing direction) {
		return this.getDefaultState().withProperty(MASTER, false)
				.withProperty(FACING, direction);
	}


	public IBlockState getMaster(EnumFacing enumfacing) {
		return this.getDefaultState().withProperty(MASTER, true)
				.withProperty(FACING, enumfacing);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(MASTER) ? SpellTableItem.instance() : null;
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(SpellTableItem.instance(), 1);
	}
	
}
