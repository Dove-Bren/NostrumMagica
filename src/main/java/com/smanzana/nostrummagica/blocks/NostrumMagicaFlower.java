package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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
public class NostrumMagicaFlower extends BushBlock {
	
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
	
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
	
	public final Type type;
	
	public NostrumMagicaFlower(Type type) {
		super(Block.Properties.create(Material.PLANTS)
				.sound(SoundType.PLANT)
				.tickRandomly()
				.doesNotBlockMovement()
				);
		this.type = type;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
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
	public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (random.nextBoolean()) {
			// Check if we're on crystadirt and maybe spread
			BlockPos groundPos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
			BlockState ground = worldIn.getBlockState(groundPos);
			
			if (ground != null && ground.getBlock() instanceof MagicDirt) {
				// Spread!
				MutableBlockPos cursor = new MutableBlockPos();
				boolean affected = false;
				
				cursor.setPos(groundPos.getX(), groundPos.getY(), groundPos.getZ());
				if (this.isValidGround(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isAirBlock(cursor)) {
						affected = true;
						worldIn.setBlockState(cursor, this.getDefaultState());
					}
				}
				
				cursor.setPos(groundPos.getX() - 1, groundPos.getY(), groundPos.getZ());
				if (this.isValidGround(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isAirBlock(cursor)) {
						affected = true;
						worldIn.setBlockState(cursor, this.getDefaultState());
					}
				}
				
				cursor.setPos(groundPos.getX() + 1, groundPos.getY(), groundPos.getZ());
				if (this.isValidGround(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isAirBlock(cursor)) {
						affected = true;
						worldIn.setBlockState(cursor, this.getDefaultState());
					}
				}
				
				cursor.setPos(groundPos.getX(), groundPos.getY(), groundPos.getZ() - 1);
				if (this.isValidGround(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isAirBlock(cursor)) {
						affected = true;
						worldIn.setBlockState(cursor, this.getDefaultState());
					}
				}
				
				cursor.setPos(groundPos.getX(), groundPos.getY(), groundPos.getZ() + 1);
				if (this.isValidGround(worldIn.getBlockState(cursor), worldIn, cursor)) {
					cursor.setY(cursor.getY() + 1);
					if (worldIn.isAirBlock(cursor)) {
						affected = true;
						worldIn.setBlockState(cursor, this.getDefaultState());
					}
				}
				
				if (affected) {
					worldIn.setBlockState(groundPos, Blocks.DIRT.getDefaultState());
				}
			}
		}
		
		if (worldIn.isRemote) {
			
		}
	}
	
	@Override
	protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
		boolean ret = super.isValidGround(state, worldIn, pos);
		if (!ret && state.getBlock() instanceof MagicDirt) {
			ret = true;
		}
		
		return ret;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
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
					pos.getX(), pos.getY(), pos.getZ(), 1, 40, 0,
					new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	public @Nullable Item getReagentItem() {
		return ReagentItem.GetItem(this.type.reagentType);
	}
}
