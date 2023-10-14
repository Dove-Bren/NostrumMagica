package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.client.gui.MirrorGui;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.DistExecutor;

public class NostrumMirrorBlock extends HorizontalBlock {
	
	public static final String ID = "mirror_block";
	protected static final VoxelShape MIRROR_AABB_EW = Block.makeCuboidShape(0.4D, 0.0D, 0.1D, 0.6D, 1.05D, 0.9D);
	protected static final VoxelShape MIRROR_AABB_NS = Block.makeCuboidShape(0.1D, 0.0D, 0.4D, 0.9D, 1.05D, 0.6D);
	
	public NostrumMirrorBlock() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(4f, 20f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(0)
				.lightValue(4)
				);
	}
	
	// todo ??
	@Override
	public boolean isSolid(BlockState state) {
		return true;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
//	@Override
//	public boolean isVisuallyOpaque() {
//		return false;
//	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//        return false;
//    }
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		if (worldIn.isRemote()) {
			DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> {
				Minecraft.getInstance().displayGuiScreen(new MirrorGui(player));
				return 0;
			});
		}
		
		return true;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		if (state.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
}
