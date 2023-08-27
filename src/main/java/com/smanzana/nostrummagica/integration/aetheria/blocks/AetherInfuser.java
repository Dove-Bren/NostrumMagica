package com.smanzana.nostrummagica.integration.aetheria.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AetherInfuser extends BlockContainer {
	
	public static final String ID = "infuser_multiblk";
	
	private static final PropertyBool MASTER = PropertyBool.create("master");
	
	private static AetherInfuser instance = null;
	public static AetherInfuser instance() {
		if (instance == null)
			instance = new AetherInfuser();
		
		return instance;
	}
	
	public AetherInfuser() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setRegistryName(ID);
		this.setHardness(5.0f);
		this.setResistance(8.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, false));
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (meta != 0) {
			return new AetherInfuserTileEntity();
		}
		
		return null;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isTranslucent(IBlockState state) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(MASTER, meta != 0);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(MASTER) ? 1 : 0;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
	}
	
	public static boolean IsMaster(IBlockState state) {
		return state != null && state.getBlock() instanceof AetherInfuser && state.getValue(MASTER);
	}
	
	public static void SetBlock(World world, BlockPos pos, boolean master) {
		world.setBlockState(pos, instance().getDefaultState().withProperty(MASTER, master));
	}
}
