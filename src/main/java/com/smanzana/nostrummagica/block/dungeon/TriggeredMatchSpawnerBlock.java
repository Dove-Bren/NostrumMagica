package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.tile.TriggeredMatchSpawnerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredMatchSpawnerTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.TriggeredMatchSpawnerTileEntityType);
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
