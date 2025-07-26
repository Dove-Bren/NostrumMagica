package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.capabilities.ILaserReactive;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.ShapeUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Small light-source invisible block that is created by lasers and removed by lasers
 */
public class LaserLight extends Block implements ILaserReactive {
	
	public static final String ID = "laser_light";
	
	protected static final int MAX_HIDE_COUNT = 1;
	protected static final IntegerProperty HIDE_COUNT = IntegerProperty.create("hide_count", 0, MAX_HIDE_COUNT);

	public LaserLight() {
		super(Block.Properties.of(Material.AIR)
				.noCollission()
				.instabreak()
				.lightLevel(s -> 15)
				);
		
		this.registerDefaultState(this.defaultBlockState().setValue(HIDE_COUNT, MAX_HIDE_COUNT));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HIDE_COUNT);
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
				return ShapeUtil.EMPTY_NOCRASH;
			}
		}
		
		return Shapes.block();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
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
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		final int newLevel = state.getValue(HIDE_COUNT) - 1;
		if (newLevel < 0) {
			worldIn.removeBlock(pos, false);
		} else {
			worldIn.setBlockAndUpdate(pos, state.setValue(HIDE_COUNT, newLevel));
		}
	}

	@Override
	public LaserHitResult laserPassthroughTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos,
			EMagicElement element) {
		// refresh light
		final BlockState freshState = this.defaultBlockState();
		if (state != freshState) {
			level.setBlock(pos, freshState, Block.UPDATE_ALL);
		}
		
		return LaserHitResult.PASSTHROUGH;
	}

	@Override
	public void laserNearbyTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element, int beamDistance) {
		; // nearby doesn't refresh light
	}

}
