package com.smanzana.nostrummagica.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

public class HarvestUtil {
	
	private static final Method seedDrops;

	static {
		seedDrops = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod(CropBlock.class, "getBaseSeedId");
	}

	private static Item getCropSeed(Block block) {
		try {
			return (Item) seedDrops.invoke(block);
		}

		catch (Exception e) {
			NostrumMagica.logger.warn("Could not find seed for " + (block == null ? "NULL" : block.toString()));
		}

		return null;
	}

	public static boolean HarvestCrop(Level world, BlockPos pos) {
		List<ItemStack> drops;
		if (world.getBlockState(pos).getBlock() instanceof CropBlock) {
			CropBlock crop = (CropBlock) world.getBlockState(pos).getBlock();
			if (crop.isMaxAge(world.getBlockState(pos))) {
				if (!world.isClientSide) {
					drops = Block.getDrops(world.getBlockState(pos),
							(ServerLevel) world, pos,
							world.getBlockEntity(pos));
					for (int i = 0; i < drops.size(); i++) {
						if (drops.get(i).getItem() != getCropSeed(crop))
							world
									.addFreshEntity(new ItemEntity((Level) world, pos.getX(),
											pos.getY(), pos.getZ(),
											(ItemStack) drops.get(i)));
					}
					for (int i = 0; i < drops.size(); i++) {
						if (drops.stream().distinct().limit(2).count() <= 1 || crop == Blocks.POTATOES
								|| crop == Blocks.CARROTS) {
							drops.remove(0);
							world
									.addFreshEntity(new ItemEntity((Level) world, pos.getX(),
											pos.getY(), pos.getZ(),
											(ItemStack) drops.get(i)));
						}

					}
					world.playSound((Player) null, pos, SoundEvents.CROP_BREAK,
							SoundSource.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
					world.setBlock(pos, crop.defaultBlockState(), 2);

				}
				
				return true;
			}
		}

		if (world.getBlockState(pos).getBlock() instanceof NetherWartBlock) {
			NetherWartBlock nether = (NetherWartBlock) world.getBlockState(pos)
					.getBlock();

			if (world.getBlockState(pos).getValue(NetherWartBlock.AGE) == 3) {
				if (!world.isClientSide) {
					drops = Block.getDrops(world.getBlockState(pos),
							(ServerLevel) world, pos,
							world.getBlockEntity(pos));
					for (int i = 0; i < drops.size(); i++) {
						world
								.addFreshEntity(new ItemEntity((Level) world, pos.getX(),
										pos.getY(), pos.getZ(),
										(ItemStack) drops.get(i)));
					}
					world.playSound((Player) null, pos,
							SoundEvents.NETHER_WART_BREAK, SoundSource.BLOCKS, 1.0F,
							0.8F + world.random.nextFloat() * 0.4F);
					world.setBlock(pos, nether.defaultBlockState(), 2);
				}
				
				return true;
			}

		}
		
		return false;
	}
	
	public static boolean canHarvestCrop(BlockState state) {
		return state.getBlock() instanceof CropBlock
				|| state.getBlock() instanceof NetherWartBlock;
	}
	
	private static boolean isTree(BlockState state) {
		return state != null && BlockTags.LOGS.contains(state.getBlock());
	}
	
	private static boolean isLeaves(BlockState state) {
		return state != null && BlockTags.LEAVES.contains(state.getBlock());
	}
	
	public static boolean canHarvestTree(BlockState state) {
		return isTree(state) || isLeaves(state);
	}
	
	public static interface ITreeWalker {
		/**
		 * Visit a part of the tree.
		 * @param world
		 * @param pos
		 * @param isLeaves
		 * @return true to keep walking or false to stop
		 */
		public boolean visit(Level world, BlockPos pos, int depth, boolean isLeaves);
	}
	
	private static final int MAX_TREE = 200;
	
	public static boolean WalkTree(Level world, BlockPos pos, ITreeWalker walker) {
//		Set<BlockPos> visitted = new HashSet<>();
//		return walkTreeDepthFirst(visitted, world, pos, walker, 1);
		
		return walkTreeBreadthFirst(world, pos, walker);
	}
	
	// Returns if any block was leaves or log.
	// Breadth-first version
	private static boolean walkTreeBreadthFirst(Level world, BlockPos startPos, ITreeWalker walker) {
		final class NextNode {
			public final BlockPos pos;
			public final int depth;
			
			public NextNode(BlockPos pos, int depth) {
				this.pos = pos;
				this.depth = depth;
			}
		}
		
		Set<BlockPos> visitted = new HashSet<>();
		List<NextNode> next = new LinkedList<>();
		
		next.add(new NextNode(startPos, 1));
		boolean walked = false;
		
		while (!next.isEmpty()) {
			NextNode visit = next.remove(0);
			if (visitted.contains(visit.pos)) {
				continue;
			}
			visitted.add(visit.pos);
			
			if (visit.depth > MAX_TREE) {
				continue;
			}
			
			BlockState state = world.getBlockState(visit.pos);
			
			boolean isTrunk = isTree(state);
			boolean isLeaves = isLeaves(state);
			//if (isTrunk || isLeaves) { // ends up chopping nearby trees to easily
			if (isTrunk || (isLeaves && visit.depth <= 3)) {
				if (walker.visit(world, visit.pos, visit.depth, !isTrunk && isLeaves)) {
					next.add(new NextNode(visit.pos.east(), visit.depth+1));
					next.add(new NextNode(visit.pos.west(), visit.depth+1));
					next.add(new NextNode(visit.pos.north(), visit.depth+1));
					next.add(new NextNode(visit.pos.south(), visit.depth+1));
					
					// corners
					next.add(new NextNode(visit.pos.north().east(), visit.depth+1));
					next.add(new NextNode(visit.pos.north().west(), visit.depth+1));
					next.add(new NextNode(visit.pos.south().east(), visit.depth+1));
					next.add(new NextNode(visit.pos.south().west(), visit.depth+1));
					
					BlockPos up = visit.pos.above();
					
					next.add(new NextNode(up, visit.depth+1));
					
					next.add(new NextNode(up.east(), visit.depth+1));
					next.add(new NextNode(up.west(), visit.depth+1));
					next.add(new NextNode(up.north(), visit.depth+1));
					next.add(new NextNode(up.south(), visit.depth+1));
					
					// corners
					next.add(new NextNode(up.north().east(), visit.depth+1));
					next.add(new NextNode(up.north().west(), visit.depth+1));
					next.add(new NextNode(up.south().east(), visit.depth+1));
					next.add(new NextNode(up.south().west(), visit.depth+1));
					
					BlockPos down = visit.pos.below();
					
					next.add(new NextNode(down, visit.depth+1));
					
					next.add(new NextNode(down.east(), visit.depth+1));
					next.add(new NextNode(down.west(), visit.depth+1));
					next.add(new NextNode(down.north(), visit.depth+1));
					next.add(new NextNode(down.south(), visit.depth+1));
					
					// corners
					next.add(new NextNode(down.north().east(), visit.depth+1));
					next.add(new NextNode(down.north().west(), visit.depth+1));
					next.add(new NextNode(down.south().east(), visit.depth+1));
					next.add(new NextNode(down.south().west(), visit.depth+1));
				}
				
				walked = true;
			}
		}
		
		return walked;
	}
	
	// Returns if any block was leaves or log.
	// Depth-first version
//	private static boolean walkTreeDepthFirst(Set<BlockPos> visitted, World world, BlockPos pos, ITreeWalker walker, int depth) {
//		if (visitted.contains(pos)) {
//			return false;
//		}
//		visitted.add(pos);
//		
//		BlockState state = world.getBlockState(pos);
//		
//		boolean isTrunk = isTree(state);
//		boolean isLeaves = isLeaves(state);
//		//if (isTrunk || isLeaves) { // ends up chopping nearby trees to easily
//		if (isTrunk || (isLeaves && depth <= 5)) {
//			// Visit and check if we should keep walking
//			if (walker.visit(world, pos, depth, !isTrunk && isLeaves)) {
//				walkTreeDepthFirst(visitted, world, pos.east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, pos.west(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, pos.north(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, pos.south(), walker, depth+1);
//				
//				// corners
//				walkTreeDepthFirst(visitted, world, pos.north().east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, pos.north().west(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, pos.south().east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, pos.south().west(), walker, depth+1);
//				
//				BlockPos up = pos.up();
//				
//				walkTreeDepthFirst(visitted, world, up, walker, depth+1);
//				
//				walkTreeDepthFirst(visitted, world, up.east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, up.west(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, up.north(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, up.south(), walker, depth+1);
//				
//				// corners
//				walkTreeDepthFirst(visitted, world, up.north().east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, up.north().west(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, up.south().east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, up.south().west(), walker, depth+1);
//				
//				BlockPos down = pos.down();
//				
//				walkTreeDepthFirst(visitted, world, down, walker, depth+1);
//				
//				walkTreeDepthFirst(visitted, world, down.east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, down.west(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, down.north(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, down.south(), walker, depth+1);
//				
//				// corners
//				walkTreeDepthFirst(visitted, world, down.north().east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, down.north().west(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, down.south().east(), walker, depth+1);
//				walkTreeDepthFirst(visitted, world, down.south().west(), walker, depth+1);
//			}
//			
//			return true; // Visitted pos
//		}
//		// else die here ;-;
//		
//		return false;
//	}
	
	public static interface IVeinWalker {
		/**
		 * Visit a part of the vein.
		 * @param world
		 * @param pos
		 * @param state
		 * @return true to keep walking or false to stop
		 */
		public boolean visit(Level world, BlockPos pos, int depth, BlockState state);
		
		public default boolean canVeinMine(Level world, BlockPos pos, BlockState state) {
			return state != null && (
					Tags.Blocks.ORES.contains(state.getBlock())
						|| Tags.Blocks.STONE.contains(state.getBlock())
					);
		}
		
		public default boolean matchesVein(Level world, BlockPos pos, BlockState state, BlockState original) {
			return state.getBlock() == original.getBlock();
		}
	}
	
	private static final int MAX_VEIN = 20;
	
	public static boolean WalkVein(Level world, BlockPos startPos, IVeinWalker walker) {
		return walkVeinBreadthFirst(world, startPos, walker);
	}
	
	private static boolean walkVeinBreadthFirst(Level world, BlockPos startPos, IVeinWalker walker) {
		final class NextNode {
			public final BlockPos pos;
			public final int depth;
			
			public NextNode(BlockPos pos, int depth) {
				this.pos = pos;
				this.depth = depth;
			}
		}
		
		Set<BlockPos> visitted = new HashSet<>();
		List<NextNode> next = new LinkedList<>();
		
		next.add(new NextNode(startPos, 1));
		boolean walked = false;
		// Make sure to capture original state
		final BlockState startingState = world.getBlockState(startPos);
		
		while (!next.isEmpty()) {
			NextNode visit = next.remove(0);
			if (visitted.contains(visit.pos)) {
				continue;
			}
			visitted.add(visit.pos);
			
			if (visitted.size() > MAX_VEIN) {
				continue;
			}
			
			BlockState state = world.getBlockState(visit.pos);
			boolean isVein = walker.canVeinMine(world, visit.pos, state) && walker.matchesVein(world, visit.pos, state, startingState);
			if (isVein) {
				if (walker.visit(world, visit.pos, visit.depth, world.getBlockState(visit.pos))) {
					next.add(new NextNode(visit.pos.east(), visit.depth+1));
					next.add(new NextNode(visit.pos.west(), visit.depth+1));
					next.add(new NextNode(visit.pos.north(), visit.depth+1));
					next.add(new NextNode(visit.pos.south(), visit.depth+1));
					next.add(new NextNode(visit.pos.above(), visit.depth+1));
					next.add(new NextNode(visit.pos.below(), visit.depth+1));
				}
				
				walked = true;
			}
		}
		
		return walked;
	}
	
}
