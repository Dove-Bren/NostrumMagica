package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Logic door that doesn't destroy itself, and can be triggered multiple times
 * @author Skyler
 *
 */
public class ToggleLogicDoor extends LogicDoorBlock {

	public static final String ID = "toggle_logic_door";
	
	protected static final BooleanProperty TOGGLED = BooleanProperty.create("toggled");
	
	public ToggleLogicDoor() {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE)
				.noOcclusion()
				);
		
		this.registerDefaultState(this.defaultBlockState().setValue(TOGGLED, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TOGGLED);
	}
	
	@Override
	public boolean isPartition(BlockState state) {
		return true; // see through door... although? Maybe it makes sense to even have these be room partitions?
		// They just are OFTEN used inside what I consider a room
	}
	
	public boolean isToggled(BlockState state) {
		return state.getValue(TOGGLED);
	}
	
	public BlockState getStateWith(Direction direction, boolean toggled) {
		return this.defaultBlockState().setValue(TOGGLED, toggled).setValue(FACING, direction);
	}
	
	public BlockState getUntoggled(Direction direction) {
		return getStateWith(direction, false);
	}
	
	public BlockState getToggled(Direction direction) {
		return getStateWith(direction, true);
	}
	
	protected void toggle(Level world, BlockPos pos, BlockState state) {
		final BlockState newState = (isToggled(state) ? getUntoggled(state.getValue(FACING)) : getToggled(state.getValue(FACING)));
		this.walkDoor(world, pos, state, (walkPos, walkState) -> {
			world.setBlock(walkPos, newState, 3);
			return false; // Keep walking
		});
	}
	
	@Override
	public void trigger(Level world, BlockPos pos, BlockState state, BlockPos triggerPos) {
		this.toggle(world, pos, state);
	}
	
//	@Override
//	public boolean isSolid(BlockState state) {
//		return !isToggled(state);
//	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (isToggled(state)) {
			// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
			if (context != CollisionContext.empty() && context instanceof EntityCollisionContext) {
				final @Nullable Entity entity = ((EntityCollisionContext) context).getEntity();
				if (entity == null || !(entity instanceof Player) || !((Player) entity).isCreative()) {
					return Shapes.empty();
				}
			}
		}
		
		return super.getShape(state, worldIn, pos, context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (isToggled(state)) {
			return Shapes.empty();
		}
		
		return super.getCollisionShape(state, worldIn, pos, context);
	}
}
