package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChalkBlock extends Block {

	public static final String ID = "nostrum_chalk_block";
	protected static final AxisAlignedBB CHALK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.03125D, 1.0D);
	
	private static ChalkBlock instance = null;
	public static ChalkBlock instance() {
		if (instance == null)
			instance = new ChalkBlock();
		
		return instance;
	}
	
	public ChalkBlock() {
		super(Material.CARPET);
		this.setHardness(.01f);
		this.setLightOpacity(1);
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return CHALK_AABB;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).isFullCube();
	}
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		if (world instanceof World && !((World) world).isRemote) {
			if (!canPlaceBlockAt((World) world, pos)) {
				((World) world).setBlockToAir(pos);
			}
		}
	}
}
