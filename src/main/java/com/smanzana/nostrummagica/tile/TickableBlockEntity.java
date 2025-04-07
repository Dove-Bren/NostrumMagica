package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Just a convenience rerouter for ticking method, since tickers are now block side.
 * Used same name as 1.16.5 ticking interface
 */
public interface TickableBlockEntity {

	public void tick();
	
	public static <T extends TickableBlockEntity> void Tick(Level world, BlockPos pos, BlockState state, T entity) {
		entity.tick();
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <E extends BlockEntity & TickableBlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> worldType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> tickFunc) {
		return worldType == expectedType ? (BlockEntityTicker<A>)tickFunc : null;
	}
	
	public static <E extends BlockEntity & TickableBlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> worldType, BlockEntityType<E> expectedType) {
		return createTickerHelper(worldType, expectedType, TickableBlockEntity::Tick);
	}
	
}
