package com.smanzana.nostrummagica.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class SorceryPortalSpawner extends Block implements ITriggeredBlock {
	
	public static final String ID = "portal_spawner";
	
	public SorceryPortalSpawner() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(3.0f, 15.0f)
				.sound(SoundType.STONE)
				.noDrops()
				);
	}
	
	protected void deactivatePortal(World world, BlockPos pos, BlockState state) {
		// Remove portal above us
		world.removeBlock(pos.up(), false);
	}
	
	protected void activatePortal(World world, BlockPos pos, BlockState state) {
		world.setBlockState(pos.up(), NostrumBlocks.sorceryPortal.getMaster());
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		deactivatePortal(world, pos, state);
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
		}
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return (worldIn.isAirBlock(pos) && worldIn.isAirBlock(pos.up()) && worldIn.isAirBlock(pos.up().up()));
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		activatePortal(worldIn, pos, state);
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		BlockState aboveState = world.getBlockState(blockPos.up());
		if (aboveState == null || !(aboveState.getBlock() instanceof SorceryPortal)) {
			this.activatePortal(world, blockPos, state);
		}
	}
}
