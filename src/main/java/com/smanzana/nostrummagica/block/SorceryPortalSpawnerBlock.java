package com.smanzana.nostrummagica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class SorceryPortalSpawnerBlock extends Block implements ITriggeredBlock {
	
	public static final String ID = "portal_spawner";
	
	public SorceryPortalSpawnerBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(3.0f, 15.0f)
				.sound(SoundType.STONE)
				.noDrops()
				);
	}
	
	protected void deactivatePortal(Level world, BlockPos pos, BlockState state) {
		// Remove portal above us
		world.removeBlock(pos.above(), false);
	}
	
	protected void activatePortal(Level world, BlockPos pos, BlockState state) {
		world.setBlockAndUpdate(pos.above(), NostrumBlocks.sorceryPortal.getMaster());
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		deactivatePortal(world, pos, state);
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
		}
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return (worldIn.isEmptyBlock(pos) && worldIn.isEmptyBlock(pos.above()) && worldIn.isEmptyBlock(pos.above().above()));
	}
	
	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		activatePortal(worldIn, pos, state);
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		BlockState aboveState = world.getBlockState(blockPos.above());
		if (aboveState == null || !(aboveState.getBlock() instanceof SorceryPortalBlock)) {
			this.activatePortal(world, blockPos, state);
		}
	}
}
