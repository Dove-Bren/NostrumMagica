package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A Logic door that doesn't destroy itself, and can be triggered multiple times
 * @author Skyler
 *
 */
public class ToggleLogicDoor extends LogicDoor {

	public static final String ID = "toggle_logic_door";
	
	protected static final BooleanProperty TOGGLED = BooleanProperty.create("toggled");
	
	public ToggleLogicDoor() {
		super();
		
		this.setDefaultState(this.getDefaultState().with(TOGGLED, false));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(TOGGLED);
	}
	
	public boolean isToggled(BlockState state) {
		return state.get(TOGGLED);
	}
	
	public BlockState getUntoggled() {
		return this.getDefaultState().with(TOGGLED, false);
	}
	
	public BlockState getToggled() {
		return this.getDefaultState().with(TOGGLED, true);
	}
	
	protected void toggle(World world, BlockPos pos, BlockState state) {
		final BlockState newState = (isToggled(state) ? getUntoggled() : getToggled());
		this.walkDoor(world, pos, state, (walkPos, walkState) -> {
			world.setBlockState(walkPos, newState, 3);
			return false; // Keep walking
		});
	}
	
	@Override
	public void trigger(World world, BlockPos pos, BlockState state, BlockPos triggerPos) {
		this.toggle(world, pos, state);
	}
	
	@Override
	public boolean isSolid(BlockState state) {
		return !isToggled(state);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (isToggled(state)) {
			// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
			if (context != ISelectionContext.dummy()) {
				if (context.getEntity() == null || !(context.getEntity() instanceof PlayerEntity) || !((PlayerEntity) context.getEntity()).isCreative()) {
					return VoxelShapes.empty();
				}
			}
		}
		
		return super.getShape(state, worldIn, pos, context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (isToggled(state)) {
			return VoxelShapes.empty();
		}
		
		return super.getCollisionShape(state, worldIn, pos, context);
	}
}
