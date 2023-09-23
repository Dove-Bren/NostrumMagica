package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SorceryPortalSpawner extends Block implements ITriggeredBlock {
	
	public static final String ID = "portal_spawner";
	
	private static SorceryPortalSpawner instance = null;
	public static SorceryPortalSpawner instance() {
		if (instance == null)
			instance = new SorceryPortalSpawner();
		
		return instance;
	}
	
	public SorceryPortalSpawner() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(15.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
	}
	
	protected void deactivatePortal(World world, BlockPos pos, BlockState state) {
		// Remove portal above us
		world.setBlockToAir(pos.up());
	}
	
	protected void activatePortal(World world, BlockPos pos, BlockState state) {
		world.setBlockState(pos.up(), SorceryPortal.instance().getStateForPlacement(world, pos, Direction.UP, 0f, 0f, 0f, 0, null, null));
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		deactivatePortal(world, pos, state);
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
		this.destroy(world, pos, state);
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return (worldIn.isAirBlock(pos) && worldIn.isAirBlock(pos.up()) && worldIn.isAirBlock(pos.up().up()));
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		activatePortal(worldIn, pos, state);
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		BlockState aboveState = world.getBlockState(blockPos.up());
		SorceryPortal.instance();
		if (aboveState == null || !(aboveState.getBlock() instanceof SorceryPortal)) {
			this.activatePortal(world, blockPos, state);
		}
	}
}
