package com.smanzana.nostrummagica.blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumObeliskEntity extends TileEntity implements ITickable {
	
	public static class NostrumObeliskTarget {
		private int dimension;
		private BlockPos pos;
		private String title;
		
		public NostrumObeliskTarget(int dimension, BlockPos pos) {
			this(dimension, pos, toTitle(pos));
		}
		
		public NostrumObeliskTarget(int dimension, BlockPos pos, String title) {
			this.pos = pos;
			this.title = title;
			this.dimension = dimension;
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
		
		public int getDimension() {
			return this.dimension;
		}
	}

	private static final String NBT_TICKET_POS = "obelisk_pos";
	private static final String NBT_MASTER = "master";
	private static final String NBT_TARGETS = "targets";
	private static final String NBT_TARGET_X = "x";
	private static final String NBT_TARGET_Y = "y";
	private static final String NBT_TARGET_Z = "z";
	private static final String NBT_TARGET_TITLE = "title";
	private static final String NBT_TARGET_DIMENSION = "dimension";
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
	
	private boolean master;
	private List<NostrumObeliskTarget> targets;
	private Corner corner;
	private int aliveCount;
	
	private boolean isDestructing;
	
	public NostrumObeliskEntity() {
		master = false;
		isDestructing = false;
		targets = new LinkedList<>();
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
			tag.setInteger(NBT_TARGET_DIMENSION, target.dimension);
			
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
						tag.getInteger(NBT_TARGET_DIMENSION),
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
	
	public void addTarget(int dimension, BlockPos pos) {
		targets.add(new NostrumObeliskTarget(dimension, pos));
		dirty();
	}
	
	public void addTarget(int dimension, BlockPos pos, String title) {
		targets.add(new NostrumObeliskTarget(dimension, pos, title));
		dirty();
	}
	
	public List<NostrumObeliskTarget> getTargets() {
		return targets;
	}
	
	public boolean canAcceptTarget(int dimension, BlockPos pos) {
		if (pos.equals(this.pos))
			return false;
		
		if (!targets.isEmpty())
		for (NostrumObeliskTarget targPos : targets) {
			if (targPos.dimension == dimension && pos.equals(targPos.pos))
				return false;
		}
		
		if (!this.worldObj.isRemote) {
			World world = DimensionManager.getWorld(dimension);
			if (world == null)
				return false;
			
			IBlockState state = world.getBlockState(pos);
			if (state == null || !(state.getBlock() instanceof NostrumObelisk)
					|| !NostrumObelisk.blockIsMaster(state))
				return false;
		}
		
		return true;
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
	
	private void dirty() {
		worldObj.markBlockRangeForRenderUpdate(pos, pos);
		worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
		worldObj.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
		markDirty();
	}

	@Override
	public void update() {
		if (!worldObj.isRemote)
			return;
		if (corner == null || master)
			return;
		
		aliveCount++;
		
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
	}
	
}