package com.smanzana.nostrummagica.block;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.tile.TemporaryPortalTileEntity;
import com.smanzana.nostrummagica.util.Location;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * Teleportation Portal with a finite lifetime
 * @author Skyler
 *
 */
public class TemporaryTeleportationPortalBlock extends TeleportationPortalBlock  {
	
	public static final String ID = "limited_teleportation_portal";
	
	public TemporaryTeleportationPortalBlock() {
		super(Block.Properties.of(Material.LEAVES)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.lightLevel((state) -> 14)
				);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		if (isMaster(state)) {
			return new TemporaryPortalTileEntity();
		}
		
		return null;
	}
	
	protected static void spawnPortal(Level worldIn, BlockPos portalMaster, Location target, int duration) {
		TemporaryPortalTileEntity te = new TemporaryPortalTileEntity(target, worldIn.getGameTime() + duration);
		worldIn.setBlockEntity(portalMaster, te);
	}
	
	public static void spawn(Level world, BlockPos at, Location target, int duration) {
		BlockState state = NostrumBlocks.temporaryTeleportationPortal.getMaster();
		world.setBlockAndUpdate(at, state);
		NostrumBlocks.temporaryTeleportationPortal.createPaired(world, at);
		
		spawnPortal(world, at, target, duration);
	}
	
	public static BlockPos spawnNearby(Level world, BlockPos center, double radius, boolean centerValid, Location target, int duration) {
		// Find a spot to place it!
		List<BlockPos> next = new LinkedList<>();
		Set<BlockPos> seen = new HashSet<>();
		
		if (centerValid) {
			// Try center and grow from there
			next.add(center);
			seen.add(center.above());
		} else {
			// avoid center location by unrolling surrounding blocks
			seen.add(center);
			seen.add(center.above());
			next.add(center.north());
			next.add(center.west());
			next.add(center.east());
			next.add(center.south());
		}
		
		BlockPos found = null;
		while (!next.isEmpty()) {
			BlockPos loc = next.remove(0);
			seen.add(loc);
			
			int lDist = Math.abs(loc.getX() - center.getX()) + Math.abs(loc.getY() - center.getY()) + Math.abs(loc.getZ() - center.getZ());
			
			// Less than here so the last visited are the exact border
			if (lDist < radius) {
				for (BlockPos pos : new BlockPos[]{loc.above(), loc.below(), loc.north(), loc.south(), loc.east(), loc.west()}) {
					if (!seen.contains(pos) && !next.contains(pos)) {
						next.add(pos);
					}
				}
			}

			if (!centerValid && lDist <= 1) {
				continue;
			}
			
			boolean pass = true;
			for (BlockPos pos : new BlockPos[]{loc, loc.above()}) {
				if (!world.isEmptyBlock(pos)) {
					BlockState state = world.getBlockState(loc);
					if (!state.getMaterial().isReplaceable()) {
						pass = false;
						break;
					}
				}
			}
			
			if (!pass) {
				continue;
			}
			
			// Check that it's on ground
			BlockState ground = world.getBlockState(loc.below());
			if (!ground.getMaterial().blocksMotion()) {
			//if (!ground.isSideSolid(world, loc.down(), Direction.UP)) {
				continue;
			}
			
			// Success. Use this position!
			found = loc;
			break;
		}
		
		if (found != null) {
			spawn(world, found, target, duration);
		}
		return found;
	}
}
