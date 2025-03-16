package com.smanzana.nostrummagica.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Represents:
 * 	- Midnight Iris
 *  - Crystabloom
 *  
 *  Randomly generated plants
 * @author Skyler
 *
 */
public class MagicaFlowerBlock extends BushBlock {
	
	public static enum Type {
		MIDNIGHT_IRIS(ReagentType.BLACK_PEARL),
		CRYSTABLOOM(ReagentType.CRYSTABLOOM);
		
		public ReagentType reagentType;
		
		private Type(ReagentType type) {
			reagentType = type;
		}
		
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	public static final String ID_MIDNIGHT_IRIS = "midnight_iris";
	public static final String ID_CRYSTABLOOM = "crystabloom_flower";
	
	protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
	
	public final Type type;
	
	public MagicaFlowerBlock(Type type) {
		super(Block.Properties.of(Material.PLANT)
				.sound(SoundType.GRASS)
				.randomTicks()
				.noCollission()
				);
		this.type = type;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
////        switch (state.get(TYPE)) {
////		case CRYSTABLOOM:
////		case MIDNIGHT_IRIS:
////			return ReagentItem.instance();
////        }
////        
////        // fall through
////        return null;
//		
//		return ReagentItem.instance();
//    }
//	
//	@Override
//	public int quantityDropped(BlockState state, int fortune, Random random) {
//		int count = 1;
//		
//		if (state.get(TYPE) == Type.MIDNIGHT_IRIS) {
//			count = 1 + fortune + random.nextInt(2);
//		}
//        
//        return count;
//	}
	
//	@Override
//	public int damageDropped(BlockState state) {
//		return getReagentMetaFromType(state.get(TYPE));
//	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		if (random.nextBoolean()) {
			// Check if we're on crystadirt and maybe spread
			BlockPos groundPos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
			BlockState ground = worldIn.getBlockState(groundPos);
			
			if (ground != null && ground.getBlock() instanceof MagicDirtBlock) {
				// Spread!
				BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
				boolean affected = false;
				
				cursor.set(groundPos.getX(), groundPos.getY(), groundPos.getZ());
				if (this.mayPlaceOn(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isEmptyBlock(cursor)) {
						affected = true;
						worldIn.setBlockAndUpdate(cursor, this.defaultBlockState());
					}
				}
				
				cursor.set(groundPos.getX() - 1, groundPos.getY(), groundPos.getZ());
				if (this.mayPlaceOn(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isEmptyBlock(cursor)) {
						affected = true;
						worldIn.setBlockAndUpdate(cursor, this.defaultBlockState());
					}
				}
				
				cursor.set(groundPos.getX() + 1, groundPos.getY(), groundPos.getZ());
				if (this.mayPlaceOn(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isEmptyBlock(cursor)) {
						affected = true;
						worldIn.setBlockAndUpdate(cursor, this.defaultBlockState());
					}
				}
				
				cursor.set(groundPos.getX(), groundPos.getY(), groundPos.getZ() - 1);
				if (this.mayPlaceOn(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isEmptyBlock(cursor)) {
						affected = true;
						worldIn.setBlockAndUpdate(cursor, this.defaultBlockState());
					}
				}
				
				cursor.set(groundPos.getX(), groundPos.getY(), groundPos.getZ() + 1);
				if (this.mayPlaceOn(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isEmptyBlock(cursor)) {
						affected = true;
						worldIn.setBlockAndUpdate(cursor, this.defaultBlockState());
					}
				}
				
				if (affected) {
					worldIn.setBlockAndUpdate(groundPos, Blocks.DIRT.defaultBlockState());
				}
			}
		}
		
		if (worldIn.isClientSide) {
			
		}
	}
	
	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
		boolean ret = super.mayPlaceOn(state, worldIn, pos);
		if (!ret && state.getBlock() instanceof MagicDirtBlock) {
			ret = true;
		}
		
		return ret;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		
		if (rand.nextBoolean()) {
			final int color;
			if (type == Type.MIDNIGHT_IRIS) {
				color = 0x4D601099;
			} else {
				//color = 0xFFF5FF3D;
				if (rand.nextBoolean()) {
					color = 0x4D86C3DA;
				} else {
					color = 0x4DD87F9E;
				}
			}
			
			NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
					1,
					pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .5, 40, 0,
					new Vec3(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	public @Nullable Item getReagentItem() {
		return ReagentItem.GetItem(this.type.reagentType);
	}
}
