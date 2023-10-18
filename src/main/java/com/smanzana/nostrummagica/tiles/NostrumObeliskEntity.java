package com.smanzana.nostrummagica.tiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumObeliskEntity extends TileEntity implements ITickableTileEntity {
	
	protected static final TicketType<BlockPos> ObeliskChunkLoaderType = TicketType.create("nostrum_obelisk_chunkloader", Comparator.comparingLong(BlockPos::toLong));
	
	public static class NostrumObeliskTarget {
		private BlockPos pos;
		private String title;
		
		public NostrumObeliskTarget(BlockPos pos) {
			this(pos, toTitle(pos));
		}
		
		public NostrumObeliskTarget(BlockPos pos, String title) {
			this.pos = pos;
			this.title = title;
		}
		
		private static String toTitle(BlockPos pos) {
			return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
		}

		public BlockPos getPos() {
			return pos;
		}

		public String getTitle() {
			return title;
		}
	}

	private static final String NBT_MASTER = "master";
	private static final String NBT_TARGETS = "targets";
	private static final String NBT_TARGET_X = "x";
	private static final String NBT_TARGET_Y = "y";
	private static final String NBT_TARGET_Z = "z";
	private static final String NBT_TARGET_TITLE = "title";
	//private static final String NBT_TARGET_DIMENSION = "dimension";
	private static final String NBT_CORNER = "corner";
	
	/**
	 * If master, destroy in all 4 corners
	 * If not master, search for master by offset in all corners.
	 * Destroy ALL masters found, not just the first one.
	 */
	public static enum Corner {
		NE(0),
		NW(1),
		SW(2),
		SE(3);
		
		private int offset;
		private Corner(int offset) {
			this.offset = offset;
		}
		
		public int getOffset() {
			return offset;
		}
	}
	
	//private static final float AetherPerBlock = 1f / 2f;
	
	private boolean master;
	private List<NostrumObeliskTarget> targets;
	private int targetIndex;
	private Corner corner;
	private int aliveCount;
	
	// Not persisted. Temporary overrides on target. targetOverrideEnds is based off aliveCount.
	private BlockPos targetOverride;
	private int targetOverrideEnd;
	
	private boolean isDestructing;
	
	public NostrumObeliskEntity() {
		super(NostrumTileEntities.NostrumObeliskEntityType); //, 0, 2000);
		master = false;
		isDestructing = false;
		targets = new LinkedList<>();
		targetIndex = 0;
		//this.compWrapper.configureInOut(true, false);
	}
	
	public NostrumObeliskEntity(boolean master) {
		this();
		this.master = master;
	}
	
	public NostrumObeliskEntity(Corner corner) {
		this(false);
		this.corner = corner;
	}
	
	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		//this.compWrapper.setAutoFill(!world.isRemote && this.isMaster());
		//aetherHandler.setAutoFill(!world.isRemote);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		ListNBT list = new ListNBT();
		
		if (master && targets.size() > 0)
		for (NostrumObeliskTarget target : targets) {
			if (target == null)
				continue;
			
			CompoundNBT tag = new CompoundNBT();
			tag.putInt(NBT_TARGET_X, target.pos.getX());
			tag.putInt(NBT_TARGET_Y, target.pos.getY());
			tag.putInt(NBT_TARGET_Z, target.pos.getZ());
			tag.putString(NBT_TARGET_TITLE, target.title);
			//tag.putInt(NBT_TARGET_DIMENSION, target.dimension);
			
			list.add(tag);
		}
		
		nbt.put(NBT_TARGETS, list);
		nbt.putBoolean(NBT_MASTER, master);
		if (!master && corner != null) {
			nbt.putByte(NBT_CORNER, (byte) corner.ordinal());
		}
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		if (nbt == null || !nbt.contains(NBT_MASTER, NBT.TAG_BYTE))
			return;

		this.master = nbt.getBoolean(NBT_MASTER);
		ListNBT list = nbt.getList(NBT_TARGETS, NBT.TAG_COMPOUND);
		if (list != null && list.size() > 0) {
			this.targets = new ArrayList<>(list.size());
			for (int i = 0; i < list.size(); i++) {
				CompoundNBT tag = list.getCompound(i);
				targets.add(new NostrumObeliskTarget(
						//tag.getInt(NBT_TARGET_DIMENSION),
						new BlockPos(
								tag.getInt(NBT_TARGET_X),
								tag.getInt(NBT_TARGET_Y),
								tag.getInt(NBT_TARGET_Z)
						),
						tag.getString(NBT_TARGET_TITLE)));
			}
		}
		
		if (!master) {
			int ord = nbt.getByte(NBT_CORNER);
			for (Corner c : Corner.values()) {
				if (c.ordinal() == ord)
					this.corner = c;
			}
		}
	}
	
	public void destroy() {
		if (isDestructing)
			return;
		
		isDestructing = true;
		if (master) {
			// go to all four corners and break all blocks up
			int xs[] = new int[] {-NostrumObelisk.TILE_OFFSETH, -NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH};
			int zs[] = new int[] {-NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH, -NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH};
			for (int i = 0; i < xs.length; i++)
			for (int j = 1; j <= NostrumObelisk.TILE_HEIGHT; j++) { // j starts at one cause the first block is above the base block
				BlockPos bp = pos.add(xs[i], j, zs[i]);
				BlockState state = world.getBlockState(bp);
				if (state.getBlock() instanceof NostrumObelisk) {
					world.destroyBlock(bp, false);
				}
			}

			if (!world.isRemote) {
				NostrumChunkLoader.unforceChunk((ServerWorld) world, ObeliskChunkLoaderType, getPos());
			}
			
			this.deactivatePortal();
			
			world.destroyBlock(pos, false);
		} else {
			int xs[] = new int[] {-NostrumObelisk.TILE_OFFSETH, -NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH};
			int zs[] = new int[] {-NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH, -NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH};
			for (int i = 0; i < xs.length; i++) {
				BlockPos base = pos.add(xs[i], -NostrumObelisk.TILE_OFFSETY, zs[i]);
				TileEntity te = world.getTileEntity(base);
				if (te != null && te instanceof NostrumObeliskEntity) {
					NostrumObeliskEntity entity = (NostrumObeliskEntity) te;
					if (entity.master)
						entity.destroy();
				}
			}
		}
	}
	
	public boolean isMaster() {
		return this.master;
	}
	
	public void addTarget(BlockPos pos) {
		targets.add(new NostrumObeliskTarget(pos));
		forceUpdate();
	}
	
	public void addTarget(BlockPos pos, String title) {
		targets.add(new NostrumObeliskTarget(pos, title));
		forceUpdate();
	}
	
	public List<NostrumObeliskTarget> getTargets() {
		return targets;
	}
	
	public boolean canAcceptTarget(BlockPos pos) {
		if (pos.equals(this.pos))
			return false;
		
		if (!targets.isEmpty())
		for (NostrumObeliskTarget targPos : targets) {
			if (pos.equals(targPos.pos))
				return false;
		}
		
		if (!this.world.isRemote) {
			BlockState state = world.getBlockState(pos);
			if (state == null || !(state.getBlock() instanceof NostrumObelisk)
					|| !NostrumObelisk.blockIsMaster(state))
				return false;
		}
		
		return true;
	}
	
	public void setTargetIndex(int index) {
		if (this.targets.size() == 0 || index < 0) {
			return;
		}
		
		this.targetIndex = Math.min(targets.size() - 1, index);
		refreshPortal();
	}
	
	public @Nullable BlockPos getCurrentTarget() {
		if (targetOverride != null) {
			return targetOverride;
		}
		
		if (this.targets.size() == 0) {
			return null;
		}
		
		return targets.get(Math.min(targets.size() - 1, targetIndex)).getPos();
	}
	
	public boolean hasOverride() {
		return this.targetOverride != null;
	}
	
	protected void deactivatePortal() {
		// Remove portal above us
		world.removeBlock(pos.up(), false);
	}
	
	protected void activatePortal() {
		world.setBlockState(pos.up(), NostrumBlocks.obeliskPortal.getDefaultState());
		NostrumBlocks.obeliskPortal.createPaired(world, pos.up());
	}
	
	protected void refreshPortal() {
		@Nullable BlockPos target;
		boolean valid;
		if (this.targetOverride != null) {
			valid = true;
			target = this.targetOverride;
		} else {
			target = this.getCurrentTarget();
			valid = (target != null && IsValidObelisk(world, target));
		}
		
		if (valid) {
			// Make sure there's a portal
			activatePortal();
		} else {
			deactivatePortal();
		}
	}
	
	public static boolean IsValidObelisk(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return !(state == null || !(state.getBlock() instanceof NostrumObelisk)
				|| !NostrumObelisk.blockIsMaster(state));
	}
	
	public void setOverride(BlockPos override, int durationTicks) {
		this.targetOverride = override;
		this.targetOverrideEnd = this.aliveCount + durationTicks;
		this.refreshPortal();
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	// Registers this TE as a chunk loader. Gets a ticket and forces the chunk.
	// Relies on already being placed in the world
	public void init() {
		if (world.isRemote)
			return;
		
		NostrumChunkLoader.forceChunk((ServerWorld) world, ObeliskChunkLoaderType, getPos());
	}
	
	private void forceUpdate() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}

	@Override
	public void tick() {
		//super.tick();
		

		aliveCount++;
		
		if (!world.isRemote && targetOverride != null && aliveCount >= targetOverrideEnd) {
			targetOverride = null;
			refreshPortal();
		}
		
		if (!world.isRemote)
			return;
		if (corner == null || master)
			return;
		
		
		final long stepInverval = 2;
		if (aliveCount % stepInverval != 0)
			return;
		
		int step = (int) (aliveCount / stepInverval);
		int maxStep = (int) ((20 / stepInverval) * 4); // 4 second period
		step = step % maxStep;
		float ratio = (float) step / (float) maxStep;
		float angle = (float) (ratio * 2f * Math.PI); // radians
		
		angle += ((double) corner.getOffset() + 1.0) * .25 * (2.0 * Math.PI);
		
		float radius = (float) ((1f - ratio) * (NostrumObelisk.TILE_OFFSETH * 1.25));
		
		double x, z, y;
		x = Math.cos(angle) * radius;
		z = Math.sin(angle) * radius;
		y = ratio * (-NostrumObelisk.TILE_OFFSETY);
		
		BlockPos master = getMasterPos();
		x += master.getX() + .5;
		z += master.getZ() + .5;
		y += pos.getY() + .5;
		world.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, .01, 0, .01);
		
	}
	
	private BlockPos getMasterPos() {
		if (this.corner != null)
		switch (this.corner) {
		case NE:
			return pos.add(-NostrumObelisk.TILE_OFFSETH, 1, -NostrumObelisk.TILE_OFFSETH);
		case NW:
			return pos.add(NostrumObelisk.TILE_OFFSETH, 1, -NostrumObelisk.TILE_OFFSETH);
		case SE:
			return pos.add(-NostrumObelisk.TILE_OFFSETH, 1, NostrumObelisk.TILE_OFFSETH);
		case SW:
			return pos.add(NostrumObelisk.TILE_OFFSETH, 1, NostrumObelisk.TILE_OFFSETH);
		}
		return pos;
	};
	
//	protected int getAetherCost(BlockPos destination) {
//		if (this.targetOverride != null && destination == this.targetOverride) {
//			// No cost for overrides
//			return 0;
//		}
//		
//		double dist = (Math.abs(this.pos.getX() - destination.getX())
//						 + Math.abs(this.pos.getY() - destination.getY())
//						 + Math.abs(this.pos.getZ() - destination.getZ()));
//		//double dist = Math.sqrt(this.pos.distanceSq(destination));
//		
//		return (int) Math.round(AetherPerBlock * dist);
//	}
	
	public boolean canAffordTeleport(BlockPos destination) {
		return true;
		//return this.compWrapper.check(getAetherCost(destination));
	}
	
	/**
	 * Attempt to pay any secondary fee for teleportation to the provided location.
	 * This assumes the destination has been checked, etc.
	 * @param destination
	 * @return
	 */
	public boolean deductForTeleport(BlockPos destination) {
		return true;
		//return this.compWrapper.checkAndWithdraw(getAetherCost(destination));
	}
}