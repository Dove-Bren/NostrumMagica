package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.tiles.TriggeredMatchSpawnerTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A match spawner, but doesn't start until triggered
 * @author Skyler
 *
 */
public class NostrumTriggeredMatchSpawner extends NostrumMatchSpawner implements ITriggeredBlock {
	
	public static final String ID = "nostrum_match_spawner_triggered";
	
	public NostrumTriggeredMatchSpawner() {
		super();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TriggeredMatchSpawnerTileEntity();
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		 TileEntity te = world.getTileEntity(blockPos);
		 if (te == null || !(te instanceof TriggeredMatchSpawnerTileEntity)) {
			 return;
		 }
		 
		 ((TriggeredMatchSpawnerTileEntity) te).triggerSpawn();
	}
}
