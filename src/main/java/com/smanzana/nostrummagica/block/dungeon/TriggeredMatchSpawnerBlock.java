package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.tile.TriggeredMatchSpawnerTileEntity;

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
public class TriggeredMatchSpawnerBlock extends MatchSpawnerBlock implements ITriggeredBlock {
	
	public static final String ID = "nostrum_match_spawner_triggered";
	
	public TriggeredMatchSpawnerBlock() {
		super();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TriggeredMatchSpawnerTileEntity();
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		 TileEntity te = world.getBlockEntity(blockPos);
		 if (te == null || !(te instanceof TriggeredMatchSpawnerTileEntity)) {
			 return;
		 }
		 
		 ((TriggeredMatchSpawnerTileEntity) te).triggerSpawn();
	}
}
