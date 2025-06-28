package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.util.WorldUtil.IBlockWalker;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.ILaserReactive;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class FogBlock extends Block implements ILaserReactive {
	
	protected static final int MAX_HIDE_COUNT = 7;
	
	// "Block Tick"s left before a hidden fog block will reconstitute into a real fog block 
	protected static final IntegerProperty HIDE_COUNT = IntegerProperty.create("hide_count", 0, MAX_HIDE_COUNT);
	protected static final BooleanProperty LIGHT_SOURCE = BooleanProperty.create("light");

	protected final Supplier<BlockState> deepState;
	protected final Supplier<BlockState> edgeState;
	protected final Supplier<BlockState> hiddenState;
	
	protected FogBlock(Block.Properties props, Supplier<BlockState> deepState, Supplier<BlockState> edgeState, Supplier<BlockState> hiddenState) {
		super(props);
		this.deepState = deepState;
		this.edgeState = edgeState;
		this.hiddenState = hiddenState;
	}
	
	protected static Block.Properties BaseProps() {
		return Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				;
	}
	
	protected boolean isHidden() {
		return false;
	}
	
	/**
	 * Update block state to indicate another 'block tick' interval has transpired where we want to unhide.
	 * Return if block should continue to tick (still hidden)
	 * @param worldIn 
	 * @param pos 
	 * @param state 
	 * @param rand 
	 * @return
	 */
	protected boolean unhideOneStage(LevelAccessor worldIn, BlockPos pos, BlockState state, Random rand) {
		return false;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context); // I don't think I need to change anything here?
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return true;
	}
	
	protected boolean stateSupportsFog(BlockState state, LevelAccessor worldIn, BlockPos check) {
		return (state.getBlock() instanceof FogBlock fog && !fog.isHidden())
				|| (state.canOcclude() && state.isCollisionShapeFullBlock(worldIn, check));
	}
	
	protected BlockState getVisibleFogFor(BlockState state, LevelAccessor worldIn, BlockPos pos) {
		// Count and make sure we're either edge or deep
		final BlockPos[] checks = {
				pos.above(),
				pos.below(),
				pos.north(),
				pos.east(),
				pos.south(),
				pos.west(),
		};
		boolean found = false;
		for (BlockPos check : checks) {
			final BlockState checkState = worldIn.getBlockState(check);
			if (!stateSupportsFog(checkState, worldIn, check)) {
				found = true;
				break;
			}
		}
		
		if (found) {
			// Should be edge
			return this.edgeState.get();
		} else {
			return this.deepState.get();
		}
	}
	
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (isHidden()) {
			return state; 
		}
		return getVisibleFogFor(state, worldIn, currentPos);
	}
	
	protected BlockState makeHiddenState(BlockState state, LevelAccessor world, BlockPos pos, int lightDistance) {
		return this.hiddenState.get()
				.setValue(LIGHT_SOURCE, lightDistance == 0)
				.setValue(HIDE_COUNT, Math.min(MAX_HIDE_COUNT, lightDistance))
				;
	}
	
	public void hideFog(BlockState state, LevelAccessor worldIn, BlockPos pos, int lightDistance) {
		// If not already a hidden block, change to be hidden.
		// Also update tick time based on light distance, starting a timer if none set or updating timer if one is present
		// ... or so I want, but there doesn't appear to be a way to 'remove' a tick for a block. So just ignore that and set some random
		// time (that will already be set hidden onPlace, probably). The tick func will do some checks to try and make it grow from the edges,
		// and lasers will periodically scan and push fog back, so it should be fine.
		FogBlock self;
		final BlockState hiddenState = this.hiddenState.get();
		if (hiddenState != state) {
			state = hiddenState;
			self = (FogBlock) state.getBlock();
			worldIn.setBlock(pos, state, Block.UPDATE_ALL);
			worldIn.scheduleTick(pos, self, 20);
		}
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		if (isHidden()) {
			// If there is another fog block OR a non-empty block nearby, un-hide.
			// Else stay hidden and reschedule
			final BlockPos[] checks = {
					pos.above(),
					pos.below(),
					pos.north(),
					pos.east(),
					pos.south(),
					pos.west(),
			};
			boolean foundFog = false;
			int count = 0;
			for (BlockPos check : checks) {
				final BlockState checkState = worldIn.getBlockState(check);
				if (stateSupportsFog(checkState, worldIn, check)) {
					if (checkState.getBlock() instanceof FogBlock fog && !fog.isHidden()) {
						foundFog = true;
						break;
					}
					count++;
				}
			}
			
			// If fog is found, unhide immediately. Otherwise, chance it based on how many supporting blocks found.
			
			if (foundFog || rand.nextInt(16) < count) {
				if (!this.unhideOneStage(worldIn, pos, state, rand)) {
					return;
				}
			}
			
			worldIn.scheduleTick(pos, this, 20);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (playerIn.isShiftKeyDown()) {
			// Emulate a trigger and spread
			WorldUtil.WalkConnectedBlocks(worldIn, pos, new IBlockWalker() {
				@Override
				public boolean canVisit(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance) {
					return state.getBlock() instanceof FogBlock;
				}

				@Override
				public IBlockWalker.WalkResult walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance, int walkCount, Consumer<BlockPos> addBlock) {
					hideFog(state, worldIn, pos, 16);
					return IBlockWalker.WalkResult.CONTINUE;
				}
			}, 256);
		} else {
			// Normal single block toggle
			hideFog(state, worldIn, pos, 16);
		}
		
		return InteractionResult.SUCCESS;
	}

	@Override
	public LaserHitResult laserPassthroughTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element) {
		this.hideFog(state, level, pos, 0);
		return LaserHitResult.PASSTHROUGH;
	}

	@Override
	public void laserNearbyTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element, int beamDistance) {
		this.hideFog(state, level, pos, beamDistance);
	}
	
	
	
	
	
	
	
	
	
	public static class Deep extends FogBlock {

		public static final String ID = "fog_block";
		
		public Deep() {
			super(BaseProps(), () -> NostrumBlocks.fogBlock.defaultBlockState(),
					() -> NostrumBlocks.fogEdgeBlock.defaultBlockState(),
					() -> NostrumBlocks.fogHiddenBlock.defaultBlockState().setValue(HIDE_COUNT, 7));
		}
		
	}
	
	public static class Edge extends FogBlock {

		public static final String ID = "fog_block_edge";
		
		public Edge() {
			super(BaseProps()
					.noOcclusion(),
				() -> NostrumBlocks.fogBlock.defaultBlockState(),
				() -> NostrumBlocks.fogEdgeBlock.defaultBlockState(),
				() -> NostrumBlocks.fogHiddenBlock.defaultBlockState().setValue(HIDE_COUNT, 7)
			);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public boolean skipRendering(BlockState p_53972_, BlockState p_53973_, Direction p_53974_) { // Copy of HalfTransparentBlock
			return p_53973_.is(this) ? true : super.skipRendering(p_53972_, p_53973_, p_53974_);
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
			return 0.2F;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
			return false;
		}
		
	}
	
	public static class Hidden extends FogBlock {

		public static final String ID = "fog_block_hidden";
		
		public Hidden() {
			super(BaseProps()
					.noOcclusion()
					.lightLevel(s -> s.getValue(LIGHT_SOURCE) ? 15 : 0),
				() -> NostrumBlocks.fogBlock.defaultBlockState(),
				() -> NostrumBlocks.fogEdgeBlock.defaultBlockState(),
				() -> NostrumBlocks.fogHiddenBlock.defaultBlockState().setValue(HIDE_COUNT, MAX_HIDE_COUNT)
			);
			
			this.registerDefaultState(this.defaultBlockState().setValue(HIDE_COUNT, 2));
		}
		
		@Override
		protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
			super.createBlockStateDefinition(builder);
			builder.add(HIDE_COUNT, LIGHT_SOURCE);
		}
		
		@Override
		public RenderShape getRenderShape(BlockState state) {
			return RenderShape.INVISIBLE;
		}
		
		@Override
		public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
			// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
			if (context != CollisionContext.empty() && context instanceof EntityCollisionContext) {
				@Nullable Entity entity = ((EntityCollisionContext) context).getEntity();
				if (entity == null || !(entity instanceof Player) || !((Player) entity).isCreative()) {
					return Shapes.empty();
				}
			}
			
			return Shapes.block();
		}
		
		@Override
		public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
			return Shapes.empty();
		}
		
		@Override
		protected boolean isHidden() {
			return true;
		}
		
		@Override
		public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
			worldIn.scheduleTick(pos, state.getBlock(), 20); // by default, set tick count for 1 second. This is mostly for loading code... which I think will call this?
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
			return 1.0F;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
			return true;
		}
		
		@Override
		protected boolean unhideOneStage(LevelAccessor worldIn, BlockPos pos, BlockState state, Random rand) {
			final int newLeft = state.getValue(HIDE_COUNT) - 1;
			if (newLeft < 0) {
				worldIn.setBlock(pos, this.edgeState.get(), Block.UPDATE_ALL);
				return false; // stop ticking
			} else {
				worldIn.setBlock(pos, state.setValue(HIDE_COUNT, newLeft), Block.UPDATE_ALL);
				return true; // Still hidden, so keep ticking
			}
		}
		
	}

}
