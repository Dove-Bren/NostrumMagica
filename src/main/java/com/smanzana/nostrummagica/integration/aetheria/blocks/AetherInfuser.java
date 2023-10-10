package com.smanzana.nostrummagica.integration.aetheria.blocks;

import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AetherInfuser extends Block {
	
	public static final String ID = "infuser_multiblk";
	
	private static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public AetherInfuser() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(5.0f, 8.0f)
				.sound(SoundType.STONE)
				.noDrops()
				);
		
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockReader worldIn, BlockPos pos, Direction side) {
//		return true;
//	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return state.get(MASTER);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (state.get(MASTER)) {
			return new AetherInfuserTileEntity();
		}
		
		return null;
	}
	
	// TODO need the event stuff from ContainerBlock?
	
//	@Override
//	public TileEntity createNewTileEntity(IBlockReader world) {
//		return new AetherInfuserTileEntity();
//	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return true;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return true;
//	}
//	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public boolean isTranslucent(BlockState state) {
//		return false;
//	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER);
	}
	
//	@Override
//	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
//		super.breakBlock(world, pos, state);
//	}
	
	public static boolean IsMaster(BlockState state) {
		return state != null && state.getBlock() instanceof AetherInfuser && state.get(MASTER);
	}
	
	public static void SetBlock(World world, BlockPos pos, boolean master) {
		world.setBlockState(pos, AetheriaProxy.BlockInfuser.getDefaultState().with(MASTER, master));
	}
}
