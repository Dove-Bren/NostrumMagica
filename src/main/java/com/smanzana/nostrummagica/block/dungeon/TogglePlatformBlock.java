package com.smanzana.nostrummagica.block.dungeon;

import java.awt.Color;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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
		super(Block.Properties.create(Material.GLASS)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.sound(SoundType.GLASS)
				.notSolid()
				.noDrops()
				);
		
		this.setDefaultState(this.getDefaultState().with(ON, true));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(ON);
	}
	
	public void setEnabled(World world, BlockPos pos, BlockState state, boolean enabled) {
		world.setBlockState(pos, state.with(ON, enabled), 3);
	}
	
	public boolean isEnabled(BlockState state) {
		return state.get(ON);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return isEnabled(state)
				&& adjacentBlockState.getBlock() instanceof TogglePlatformBlock
				&& ((TogglePlatformBlock) adjacentBlockState.getBlock()).isEnabled(adjacentBlockState);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (!isEnabled(state) && context != ISelectionContext.dummy()) {
			if (context.getEntity() == null || !(context.getEntity() instanceof PlayerEntity) || !((PlayerEntity) context.getEntity()).isCreative()) {
				return VoxelShapes.empty();
			}
		}
		
		return VoxelShapes.fullCube();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (!isEnabled(state)) {
			return VoxelShapes.empty();
		}
		
		return super.getCollisionShape(state, worldIn, pos, context);
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return true;
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 1.0F;
	}
	
	protected void toggle(World world, BlockPos pos, BlockState state) {
		setEnabled(world, pos, state, !isEnabled(state));
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!playerIn.isCreative()) {
			return ActionResultType.PASS;
		}
		
		if (hand != Hand.MAIN_HAND) {
			return ActionResultType.PASS;
		}
		
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		if (playerIn.isSneaking()) {
			// Emulate a trigger and spread
			trigger(worldIn, pos, state, new BlockPos(0, 0, 0));
		} else {
			// Normal single block toggle
			toggle(worldIn, pos, state);
		}
		
		return ActionResultType.SUCCESS;
	}
	
	protected void triggerInternal(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		toggle(world, blockPos, state);
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		if (!world.isRemote()) {
			WorldUtil.WalkConnectedBlocks(world, blockPos, new IBlockWalker() {
				@Override
				public boolean canVisit(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance) {
					return state.getBlock() == TogglePlatformBlock.this && !pos.equals(triggerPos);
				}

				@Override
				public boolean walk(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance, int walkCount) {
					((TogglePlatformBlock) state.getBlock()).triggerInternal((World) world, pos, state, triggerPos);
					return false;
				}
			}, 256);
		}
	}
	
	public static final int MakePlatformColor(BlockState state, IBlockDisplayReader world, BlockPos pos, int tintIndex) {
		return Color.HSBtoRGB((float) ((double)(System.currentTimeMillis() % 6000L) / 6000.0), 1f, 1f);
	}
}
