package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.item.armor.MagicArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MysticSnowLayerBlock extends Block {

	public static final String ID = "mystic_snow_layer";
	
	private static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	private static final int DECAY_TICKS = 20 * 5;
	
	public MysticSnowLayerBlock() {
		super(Block.Properties.create(Material.SNOW).hardnessAndResistance(0.1F).setRequiresTool().sound(SoundType.SNOW));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		worldIn.removeBlock(pos, false);
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
    }
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), DECAY_TICKS);
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		//worldIn.getPendingBlockTicks().scheduleTick(currentPos, state.getBlock(), DECAY_TICKS);
		return state;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		final int blizzardPieces = MagicArmor.GetSetCount(player, EMagicElement.ICE, MagicArmor.Type.TRUE);
		if (blizzardPieces == 4) {
			player.addItemStackToInventory(new ItemStack(Items.SNOWBALL));
			worldIn.removeBlock(pos, false);
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}

}
