package com.smanzana.nostrummagica.blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumaetheria.api.blocks.AetherTickingTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumObeliskEntity extends AetherTickingTileEntity {
	
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

	private static final String NBT_TICKET_POS = "obelisk_pos";
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
	protected static enum Corner {
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
	
	private static final float AetherPerBlock = 1f / 2f;
	
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
		super(0, 2000);
		master = false;
		isDestructing = false;
		targets = new LinkedList<>();
		targetIndex = 0;
		this.compWrapper.configureInOut(true, false);
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
	public void setWorldObj(World worldObj) {
		super.setWorldObj(worldObj);
		this.compWrapper.setAutoFill(!worldObj.isRemote);
		//aetherHandler.setAutoFill(!worldObj.isRemote);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		NBTTagList list = new NBTTagList();
		
		if (master && targets.size() > 0)
		for (NostrumObeliskTarget target : targets) {
			if (target == null)
				continue;
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger(NBT_TARGET_X, target.pos.getX());
			tag.setInteger(NBT_TARGET_Y, target.pos.getY());
			tag.setInteger(NBT_TARGET_Z, target.pos.getZ());
			tag.setString(NBT_TARGET_TITLE, target.title);
			//tag.setInteger(NBT_TARGET_DIMENSION, target.dimension);
			
			list.appendTag(tag);
		}
		
		nbt.setTag(NBT_TARGETS, list);
		nbt.setBoolean(NBT_MASTER, master);
		if (!master && corner != null) {
			nbt.setByte(NBT_CORNER, (byte) corner.ordinal());
		}
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null || !nbt.hasKey(NBT_MASTER, NBT.TAG_BYTE))
			return;

		this.master = nbt.getBoolean(NBT_MASTER);
		NBTTagList list = nbt.getTagList(NBT_TARGETS, NBT.TAG_COMPOUND);
		if (list != null && list.tagCount() > 0) {
			this.targets = new ArrayList<>(list.tagCount());
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				targets.add(new NostrumObeliskTarget(
						//tag.getInteger(NBT_TARGET_DIMENSION),
						new BlockPos(
								tag.getInteger(NBT_TARGET_X),
								tag.getInteger(NBT_TARGET_Y),
								tag.getInteger(NBT_TARGET_Z)
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
				IBlockState state = worldObj.getBlockState(bp);
				if (state.getBlock() instanceof NostrumObelisk) {
					worldObj.destroyBlock(bp, false);
				}
			}

			if (!worldObj.isRemote) {
				Ticket ticket = NostrumChunkLoader.instance().pullTicket(genTicketKey());
				if (ticket != null) {
					ForgeChunkManager.releaseTicket(ticket);
				}
			}
			
			worldObj.destroyBlock(pos, false);
		} else {
			int xs[] = new int[] {-NostrumObelisk.TILE_OFFSETH, -NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH};
			int zs[] = new int[] {-NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH, -NostrumObelisk.TILE_OFFSETH, NostrumObelisk.TILE_OFFSETH};
			for (int i = 0; i < xs.length; i++) {
				BlockPos base = pos.add(xs[i], -NostrumObelisk.TILE_OFFSETY, zs[i]);
				TileEntity te = worldObj.getTileEntity(base);
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
		
		if (!this.worldObj.isRemote) {
			IBlockState state = worldObj.getBlockState(pos);
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
		worldObj.setBlockToAir(pos.up());
	}
	
	protected void activatePortal() {
		worldObj.setBlockState(pos.up(), ObeliskPortal.instance().getStateForPlacement(worldObj, pos, EnumFacing.UP, 0f, 0f, 0f, 0, null, null));
		ObeliskPortal.instance().createPaired(worldObj, pos.up());
	}
	
	protected void refreshPortal() {
		@Nullable BlockPos target;
		boolean valid;
		if (this.targetOverride != null) {
			valid = true;
			target = this.targetOverride;
		} else {
			target = this.getCurrentTarget();
			valid = (target != null && IsValidObelisk(worldObj, target));
		}
		
		if (valid) {
			// Make sure there's a portal
			activatePortal();
		} else {
			deactivatePortal();
		}
	}
	
	public static boolean IsValidObelisk(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return !(state == null || !(state.getBlock() instanceof NostrumObelisk)
				|| !NostrumObelisk.blockIsMaster(state));
	}
	
	public void setOverride(BlockPos override, int durationTicks) {
		this.targetOverride = override;
		this.targetOverrideEnd = this.aliveCount + durationTicks;
		this.refreshPortal();
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	// Registers this TE as a chunk loader. Gets a ticket and forces the chunk.
	// Relies on already being placed in the world
	public void init() {
		if (worldObj.isRemote)
			return;
		
		Ticket chunkTicket = ForgeChunkManager.requestTicket(NostrumMagica.instance, worldObj, Type.NORMAL);
		chunkTicket.getModData().setTag(NBT_TICKET_POS, NBTUtil.createPosTag(pos));
		ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(pos));
		NostrumChunkLoader.instance().addTicket(genTicketKey(), chunkTicket);
	}
	
	private String genTicketKey() {
		return "nostrum_obelisk_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
	}
	
	private void forceUpdate() {
		worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
		markDirty();
	}

	@Override
	public void update() {
		super.update();
		
		if (!worldObj.isRemote)
			return;
		if (corner == null || master)
			return;
		
		aliveCount++;
		
		if (targetOverride != null && aliveCount >= targetOverrideEnd) {
			targetOverride = null;
			refreshPortal();
		}
		
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
		worldObj.spawnParticle(EnumParticleTypes.DRAGON_BREATH, x, y, z, .01, 0, .01, new int[0]);
		
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
	
	/**
	 * Attempt to pay any secondary fee for teleportation to the provided location.
	 * This assumes the destination has been checked, etc.
	 * @param destination
	 * @return
	 */
	public boolean deductForTeleport(BlockPos destination) {
		double dist = (Math.abs(this.pos.getX() - destination.getX())
						 + Math.abs(this.pos.getY() - destination.getY())
						 + Math.abs(this.pos.getZ() - destination.getZ()));
		//double dist = Math.sqrt(this.pos.distanceSq(destination));
		
		int aetherCost = (int) Math.round(AetherPerBlock * dist);
		
		return this.compWrapper.checkAndWithdraw(aetherCost);
	}
}