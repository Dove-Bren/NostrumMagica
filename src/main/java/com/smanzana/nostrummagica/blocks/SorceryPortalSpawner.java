package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
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
	
	public static void init() {
		
	}
	
	public SorceryPortalSpawner() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(15.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		// Remove portal above us
		world.setBlockToAir(pos.up());
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		this.destroy(world, pos, state);
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return (worldIn.isAirBlock(pos) && worldIn.isAirBlock(pos.up()) && worldIn.isAirBlock(pos.up().up()));
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.setBlockState(pos.up(), SorceryPortal.instance().getStateForPlacement(worldIn, pos, EnumFacing.UP, 0f, 0f, 0f, 0, null, null));
	}

	@Override
	public void trigger(World world, BlockPos blockPos, IBlockState state, BlockPos triggerPos) {
		IBlockState aboveState = world.getBlockState(blockPos.up());
		SorceryPortal.instance();
		if (aboveState == null || !(aboveState.getBlock() instanceof SorceryPortal)) {
			this.onBlockAdded(world, blockPos, state);
		}
	}
}
