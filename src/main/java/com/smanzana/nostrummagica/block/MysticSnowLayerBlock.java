package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class MysticSnowLayerBlock extends Block {

	public static final String ID = "mystic_snow_layer";
	
	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	private static final int DECAY_TICKS = 20 * 5;
	
	public MysticSnowLayerBlock() {
		super(Block.Properties.of(Material.TOP_SNOW).strength(0.1F).requiresCorrectToolForDrops().sound(SoundType.SNOW));
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		worldIn.removeBlock(pos, false);
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
    }
	
	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getBlockTicks().scheduleTick(pos, state.getBlock(), DECAY_TICKS);
	}
	
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		//worldIn.getPendingBlockTicks().scheduleTick(currentPos, state.getBlock(), DECAY_TICKS);
		return state;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		final int blizzardPieces = ElementalArmor.GetSetCount(player, EMagicElement.ICE, ElementalArmor.Type.MASTER);
		if (blizzardPieces == 4) {
			player.addItem(new ItemStack(Items.SNOWBALL));
			worldIn.removeBlock(pos, false);
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}

}
