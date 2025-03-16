package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.tile.TriggeredMatchSpawnerTileEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

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
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new TriggeredMatchSpawnerTileEntity();
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		 BlockEntity te = world.getBlockEntity(blockPos);
		 if (te == null || !(te instanceof TriggeredMatchSpawnerTileEntity)) {
			 return;
		 }
		 
		 ((TriggeredMatchSpawnerTileEntity) te).triggerSpawn();
	}
}
