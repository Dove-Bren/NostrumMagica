package com.smanzana.nostrummagica.blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Teleportation Portal with a finite lifetime
 * @author Skyler
 *
 */
public class TemporaryTeleportationPortal extends TeleportationPortal  {
	
	public static final String ID = "limited_teleportation_portal";
	
	private static TemporaryTeleportationPortal instance = null;
	public static TemporaryTeleportationPortal instance() {
		if (instance == null)
			instance = new TemporaryTeleportationPortal();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(TemporaryPortalTileEntity.class, "limited_teleportation_portal");
	}
	
	public TemporaryTeleportationPortal() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (worldIn.isRemote) {
			IBlockState state = this.getStateFromMeta(meta);
			if (isMaster(state)) {
				return new TemporaryPortalTileEntity();
			}
		}
		
		return null;
	}
	
	protected static void spawnPortal(World worldIn, BlockPos portalMaster, BlockPos target, int duration) {
		TemporaryPortalTileEntity te = new TemporaryPortalTileEntity(target, worldIn.getTotalWorldTime() + duration);
		worldIn.setTileEntity(portalMaster, te);
	}
	
	public static void spawn(World world, BlockPos at, BlockPos target, int duration) {
		IBlockState state = instance().getMaster();
		world.setBlockState(at, state);
		instance().createPaired(world, at);
		
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
					IBlockState state = world.getBlockState(loc);
					if (!state.getBlock().isReplaceable(world, loc)) {
						pass = false;
						break;
					}
				}
			}
			
			if (!pass) {
				continue;
			}
			
			// Check that it's on ground
			IBlockState ground = world.getBlockState(loc.down());
			if (!ground.getMaterial().blocksMovement()) {
			//if (!ground.isSideSolid(world, loc.down(), EnumFacing.UP)) {
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
	
	public static class TemporaryPortalTileEntity extends TeleportationPortalTileEntity implements ITickable  {

		private long endticks;
		
		public TemporaryPortalTileEntity() {
			super();
		}
		
		public TemporaryPortalTileEntity(BlockPos target, long endticks) {
			super(target);
			this.endticks = endticks;
			this.markDirty();
		}
		
		@Override
		public void update() {
			if (worldObj == null || worldObj.isRemote) {
				return;
			}
			
			if (worldObj.getTotalWorldTime() >= this.endticks) {
				worldObj.setBlockToAir(pos);
			}
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public int getColor() {
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (NostrumPortal.getRemainingCooldown(player) > 0) {
				return 0x00400000;
			}
			return 0x003030FF;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public float getRotationPeriod() {
			return 2;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public float getOpacity() {
			float opacity = .9f;
			
			if (worldObj != null) {
				final long now =  worldObj.getTotalWorldTime();
				final long FadeTicks = 20 * 5;
				final long left = Math.max(0, endticks - now);
				if (left < FadeTicks) {
					opacity *= ((double) left / (double) FadeTicks);
				}
			}
			
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (NostrumPortal.getRemainingCooldown(player) > 0) {
				opacity *= 0.5f;
			}
			
			return opacity;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound compound) {
			super.readFromNBT(compound);
			
			endticks = compound.getLong("EXPIRE");
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setLong("EXPIRE", endticks);
			
			return nbt;
		}
	}
}
