package com.smanzana.nostrummagica.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class FireBlockEvent extends Event {
	
	protected final Level level;
	protected final BlockPos fireSource;
	
	protected FireBlockEvent(Level level, BlockPos fireSource) {
		this.level = level;
		this.fireSource = fireSource;
	}
	
	public Level getLevel() {
		return level;
	}

	public BlockPos getFireSource() {
		return fireSource;
	}

	@Cancelable
	public static class FireSpreadAttemptEvent extends FireBlockEvent {

		protected final BlockPos spreadPos;

		public FireSpreadAttemptEvent(Level level, BlockPos fireSource, BlockPos spreadPos) {
			super(level, fireSource);
			this.spreadPos = spreadPos;
		}
		
		public BlockPos getSpreadPosition() {
			return spreadPos;
		}
	}

	public static class FireCheckOddsEvent extends FireBlockEvent {

		private int odds = -1;
		
		public FireCheckOddsEvent(Level level, BlockPos checkPos) {
			super(level, checkPos);
		}
		
		public BlockPos getCheckPosition() {
			return this.getFireSource();
		}
		
		public void setOdds(int odds) {
			this.odds = odds;
		}
		
		public int getOdds() {
			return odds;
		}
		
		public boolean hasOddsOverride() {
			return odds >= 0;
		}
	}

}
