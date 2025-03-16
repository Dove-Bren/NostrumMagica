package com.smanzana.nostrummagica.block.dungeon;

import java.awt.Color;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A regular ol block that can be stood on, except it disappears when triggered
 * @author Skyler
 *
 */
public class TogglePlatformBlock extends Block implements ITriggeredBlock {
	
	public static final String ID = "toggle_platform";

	public static final BooleanProperty ON = BooleanProperty.create("on");
	
	public TogglePlatformBlock() {
		super(Block.Properties.of(Material.GLASS)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.noDrops()
				);
		
		this.registerDefaultState(this.defaultBlockState().setValue(ON, true));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ON);
	}
	
	public void setEnabled(Level world, BlockPos pos, BlockState state, boolean enabled) {
		world.setBlock(pos, state.setValue(ON, enabled), 3);
	}
	
	public boolean isEnabled(BlockState state) {
		return state.getValue(ON);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return isEnabled(state)
				&& adjacentBlockState.getBlock() instanceof TogglePlatformBlock
				&& ((TogglePlatformBlock) adjacentBlockState.getBlock()).isEnabled(adjacentBlockState);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (!isEnabled(state) && context != CollisionContext.empty()) {
			if (context.getEntity() == null || !(context.getEntity() instanceof Player) || !((Player) context.getEntity()).isCreative()) {
				return Shapes.empty();
			}
		}
		
		return Shapes.block();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (!isEnabled(state)) {
			return Shapes.empty();
		}
		
		return super.getCollisionShape(state, worldIn, pos, context);
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1.0F;
	}
	
	protected void toggle(Level world, BlockPos pos, BlockState state) {
		setEnabled(world, pos, state, !isEnabled(state));
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (playerIn.isShiftKeyDown()) {
			// Emulate a trigger and spread
			trigger(worldIn, pos, state, new BlockPos(0, 0, 0));
		} else {
			// Normal single block toggle
			toggle(worldIn, pos, state);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	protected void triggerInternal(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		toggle(world, blockPos, state);
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		if (!world.isClientSide()) {
			WorldUtil.WalkConnectedBlocks(world, blockPos, new IBlockWalker() {
				@Override
				public boolean canVisit(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance) {
					return state.getBlock() == TogglePlatformBlock.this && !pos.equals(triggerPos);
				}

				@Override
				public boolean walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance, int walkCount) {
					((TogglePlatformBlock) state.getBlock()).triggerInternal((Level) world, pos, state, triggerPos);
					return false;
				}
			}, 256);
		}
	}
	
	public static final int MakePlatformColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
		return Color.HSBtoRGB((float) ((double)(System.currentTimeMillis() % 6000L) / 6000.0), 1f, 1f);
	}
}
