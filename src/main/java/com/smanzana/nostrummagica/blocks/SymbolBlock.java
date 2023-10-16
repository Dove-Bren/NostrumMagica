package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.tiles.SymbolTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SymbolBlock extends ContainerBlock {
	
	public static final String ID = "symbol_block";
	
	public SymbolBlock() {
		this(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.lightValue(16)
				);
	}
	
	public SymbolBlock(Block.Properties builder) {
		super(builder);
	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//        return false;
//    }
//	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
//		return false;
//	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
	}
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		SymbolTileEntity ent = new SymbolTileEntity(5.0f);
		return ent;
	}
	
//	@Override
//	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
//		super.breakBlock(world, pos, state);
//        world.removeTileEntity(pos);
//	}
	
	public void setInWorld(IWorld world, BlockPos pos, SpellComponentWrapper component) {
		world.setBlockState(pos, this.getDefaultState(), 3);
		SymbolTileEntity te = (SymbolTileEntity) world.getTileEntity(pos);
		te.setComponent(component);
	}
}
