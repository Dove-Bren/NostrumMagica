package com.smanzana.nostrummagica.blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.tiles.TemporaryPortalTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Teleportation Portal with a finite lifetime
 * @author Skyler
 *
 */
public class TemporaryTeleportationPortal extends TeleportationPortal  {
	
	public static final String ID = "limited_teleportation_portal";
	
	public TemporaryTeleportationPortal() {
		super(Block.Properties.create(Material.LEAVES)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.setLightLevel((state) -> 14)
				);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (isMaster(state)) {
			return new TemporaryPortalTileEntity();
		}
		
		return null;
	}
	
	protected static void spawnPortal(World worldIn, BlockPos portalMaster, BlockPos target, int duration) {
		TemporaryPortalTileEntity te = new TemporaryPortalTileEntity(target, worldIn.getGameTime() + duration);
		worldIn.setTileEntity(portalMaster, te);
	}
	
	public static void spawn(World world, BlockPos at, BlockPos target, int duration) {
		BlockState state = NostrumBlocks.temporaryTeleportationPortal.getMaster();
		world.setBlockState(at, state);
		NostrumBlocks.temporaryTeleportationPortal.createPaired(world, at);
		
		spawnPortal(world, at, target, duration);
	}
	
	public static BlockPos spawnNearby(World world, BlockPos center, double radius, boolean centerValid, BlockPos target, int duration) {
		// Find a spot to place it!
		List<BlockPos> next = new LinkedList<>();
		Set<BlockPos> seen = new HashSet<>();
		
		if (centerValid) {
			// Try center and grow from there
			next.add(center);
			seen.add(center.up());
		} else {
			// avoid center location by unrolling surrounding blocks
			seen.add(center);
			seen.add(center.up());
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
				for (BlockPos pos : new BlockPos[]{loc.up(), loc.down(), loc.north(), loc.south(), loc.east(), loc.west()}) {
					if (!seen.contains(pos) && !next.contains(pos)) {
						next.add(pos);
					}
				}
			}

			if (!centerValid && lDist <= 1) {
				continue;
			}
			
			boolean pass = true;
			for (BlockPos pos : new BlockPos[]{loc, loc.up()}) {
				if (!world.isAirBlock(pos)) {
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
			BlockState ground = world.getBlockState(loc.down());
			if (!ground.getMaterial().blocksMovement()) {
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
