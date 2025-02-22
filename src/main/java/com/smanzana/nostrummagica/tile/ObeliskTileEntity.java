package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.ObeliskBlock;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionToken;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ObeliskTileEntity extends TileEntity implements ITickableTileEntity {
	
	protected static final TicketType<BlockPos> ObeliskChunkLoaderType = TicketType.create("nostrum_obelisk_chunkloader", Comparator.comparingLong(BlockPos::toLong));
	
	public static class NostrumObeliskTarget {
		private static final String NBT_LEGACY_X = "x";
		private static final String NBT_LEGACY_Y = "y";
		private static final String NBT_LEGACY_Z = "z";
		private static final String NBT_LEGACY_DIMENSION = "dim";
		private static final String NBT_LOC = "location";
		private static final String NBT_TITLE = "title";
		
		private Location loc;
		private String title;
		
		public NostrumObeliskTarget(Location location) {
			this(location, toTitle(location));
		}
		
		public NostrumObeliskTarget(Location location, String title) {
			this.loc = location;
			this.title = title;
		}
		
		private static String toTitle(Location location) {
			final String dim = DimensionUtils.IsOverworld(location.getDimension())
					? ""
					: ("[" + PositionToken.getDimensionName(location.getDimension()) + "]" );
			return "(" + location.getPos().getX() + ", " + location.getPos().getY() + ", " + location.getPos().getZ() + ")" + dim;
		}

		public Location getLocation() {
			return loc;
		}

		public String getTitle() {
			return title;
		}
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putString(NBT_TITLE, title);
			tag.put(NBT_LOC, loc.toNBT());
			
//			tag.putInt(NBT_X, pos.getX());
//			tag.putInt(NBT_Y, pos.getY());
//			tag.putInt(NBT_Z, pos.getZ());
//			tag.putString(NBT_DIMENSION, dimension.getLocation().toString());
			return tag;
		}
		
		public static NostrumObeliskTarget fromNBT(CompoundNBT tag) {
			if (tag.contains(NBT_LOC)) {
				return new NostrumObeliskTarget(Location.FromNBT(tag.getCompound(NBT_LOC)), tag.getString(NBT_TITLE));
			}
			
			// Legacy support
			return new NostrumObeliskTarget(new Location(
					new BlockPos(
							tag.getInt(NBT_LEGACY_X),
							tag.getInt(NBT_LEGACY_Y),
							tag.getInt(NBT_LEGACY_Z)
					),
					DimensionUtils.GetDimKeySafe(tag.getString(NBT_LEGACY_DIMENSION))),
					tag.getString(NBT_TITLE));
		}
	}

	private static final String NBT_MASTER = "master";
	private static final String NBT_TARGETS = "targets";
	private static final String NBT_TARGET_INDEX = "targ_idx";
	//private static final String NBT_TARGET_DIMENSION = "dimension";
	private static final String NBT_CORNER = "corner";
	private static final String NBT_ONESIDE_UPGRADED = "pin_upgrade";
	private static final String NBT_DIMENSION_UPGRADED = "dimension_upgrade";
	
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
	private boolean upgradeOneside; // Upgrade allowing portals with no remote obelisk
	private boolean upgradeDimension; // Upgrade allowing portals to different dimensions
	private Corner corner;
	private int aliveCount;
	
	// Not persisted. Temporary overrides on target. targetOverrideEnds is based off aliveCount.
	private Location targetOverride;
	private int targetOverrideEnd;
	
	private boolean isDestructing;
	
	public ObeliskTileEntity() {
		super(NostrumTileEntities.NostrumObeliskEntityType); //, 0, 2000);
		master = false;
		isDestructing = false;
		targets = new LinkedList<>();
		targetIndex = 0;
		upgradeOneside = false;
		upgradeDimension = false;
		//this.compWrapper.configureInOut(true, false);
	}
	
	public ObeliskTileEntity(boolean master) {
		this();
		this.master = master;
	}
	
	public ObeliskTileEntity(Corner corner) {
		this(false);
		this.corner = corner;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		ListNBT list = new ListNBT();
		
		if (master && targets.size() > 0)
		for (NostrumObeliskTarget target : targets) {
			if (target == null)
				continue;
			
			list.add(target.toNBT());
		}
		
		nbt.put(NBT_TARGETS, list);
		nbt.putInt(NBT_TARGET_INDEX, this.targetIndex);
		nbt.putBoolean(NBT_MASTER, master);
		if (!master && corner != null) {
			nbt.putByte(NBT_CORNER, (byte) corner.ordinal());
		}
		if (master) {
			nbt.putBoolean(NBT_ONESIDE_UPGRADED, upgradeOneside);
			nbt.putBoolean(NBT_DIMENSION_UPGRADED, upgradeDimension);
		}
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		if (nbt == null || !nbt.contains(NBT_MASTER, NBT.TAG_BYTE))
			return;

		this.master = nbt.getBoolean(NBT_MASTER);
		ListNBT list = nbt.getList(NBT_TARGETS, NBT.TAG_COMPOUND);
		if (list != null && list.size() > 0) {
			this.targets = new ArrayList<>(list.size());
			for (int i = 0; i < list.size(); i++) {
				CompoundNBT tag = list.getCompound(i);
				targets.add(NostrumObeliskTarget.fromNBT(tag));
			}
		}
		this.targetIndex = nbt.getInt(NBT_TARGET_INDEX);
		
		if (!master) {
			int ord = nbt.getByte(NBT_CORNER);
			for (Corner c : Corner.values()) {
				if (c.ordinal() == ord)
					this.corner = c;
			}
		} else {
			this.upgradeOneside = nbt.getBoolean(NBT_ONESIDE_UPGRADED);
			this.upgradeDimension = nbt.getBoolean(NBT_DIMENSION_UPGRADED);
		}
	}
	
	public void destroy() {
		if (isDestructing)
			return;
		
		isDestructing = true;
		if (master) {
			// go to all four corners and break all blocks up
			int xs[] = new int[] {-ObeliskBlock.TILE_OFFSETH, -ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH};
			int zs[] = new int[] {-ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH, -ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH};
			for (int i = 0; i < xs.length; i++)
			for (int j = 1; j <= ObeliskBlock.TILE_HEIGHT; j++) { // j starts at one cause the first block is above the base block
				BlockPos bp = pos.add(xs[i], j, zs[i]);
				BlockState state = world.getBlockState(bp);
				if (state.getBlock() instanceof ObeliskBlock) {
					world.destroyBlock(bp, false);
				}
			}

			if (!world.isRemote) {
				NostrumChunkLoader.unforceChunk((ServerWorld) world, ObeliskChunkLoaderType, getPos());
			}
			
			this.deactivatePortal();
			
			world.destroyBlock(pos, false);
		} else {
			int xs[] = new int[] {-ObeliskBlock.TILE_OFFSETH, -ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH};
			int zs[] = new int[] {-ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH, -ObeliskBlock.TILE_OFFSETH, ObeliskBlock.TILE_OFFSETH};
			for (int i = 0; i < xs.length; i++) {
				BlockPos base = pos.add(xs[i], -ObeliskBlock.TILE_OFFSETY, zs[i]);
				TileEntity te = world.getTileEntity(base);
				if (te != null && te instanceof ObeliskTileEntity) {
					ObeliskTileEntity entity = (ObeliskTileEntity) te;
					if (entity.master)
						entity.destroy();
				}
			}
		}
	}
	
	public boolean isMaster() {
		return this.master;
	}
	
	public boolean hasOnesidedUpgrade() {
		return this.upgradeOneside;
	}
	
	public boolean hasDimensionUpgrade() {
		return this.upgradeDimension;
	}
	
	public void addTarget(World world, BlockPos pos) {
		this.addTarget(new Location(world, pos));
	}
	
	public void addTarget(Location location) {
		targets.add(new NostrumObeliskTarget(location));
		forceUpdate();
	}
	
	public void addTarget(World world, BlockPos pos, String title) {
		this.addTarget(new Location(world, pos), title);
	}
	
	public void addTarget(Location location, String title) {
		targets.add(new NostrumObeliskTarget(location, title));
		forceUpdate();
	}
	
	public List<NostrumObeliskTarget> getTargets() {
		return targets;
	}
	
	public static final boolean IsObeliskPos(Location location) {
		final BlockPos pos = location.getPos();
		final World world = ServerLifecycleHooks.getCurrentServer().getWorld(location.getDimension());
		
		if (world.isRemote()) {
			return false; // can't load random worlds or chunks on client
		}
		
		BlockState state = world.getBlockState(pos);
		if (state != null && state.getBlock() instanceof ObeliskBlock && ObeliskBlock.blockIsMaster(state))
			return true;
		
		if (pos.getY() > 0) {
			state = world.getBlockState(pos.down());
			if (state != null && state.getBlock() instanceof ObeliskBlock && ObeliskBlock.blockIsMaster(state))
				return true;
		}
		
		return false;
	}
	
	public boolean canAcceptTarget(Location location) {
		if (DimensionUtils.DimEquals(location.getDimension(), this.world.getDimensionKey())) {
			final BlockPos pos = location.getPos();
			if (pos.equals(this.pos) || pos.equals(this.pos.up())) {
				return false;
			}
		}
		
		if (!this.hasDimensionUpgrade() && !DimensionUtils.DimEquals(location.getDimension(), this.world.getDimensionKey())) {
			return false;
		}
		
		if (!targets.isEmpty())
		for (NostrumObeliskTarget targPos : targets) {
			if (targPos.loc.equals(location))
				return false;
		}
		
		// If not upgraded, make sure destination location is also an obelisk
		if (!this.hasOnesidedUpgrade() && !IsObeliskPos(location)) {
			return false;
		}
		
		return true;
	}
	
	public void setTargetIndex(int index) {
		if (this.targets.size() == 0 || index < 0) {
			return;
		}
		
		this.targetIndex = Math.min(targets.size() - 1, index);
		forceUpdate();
		refreshPortal();
	}
	
	public @Nullable Location getCurrentTarget() {
		if (targetOverride != null) {
			return targetOverride;
		}
		
		if (this.targets.size() == 0) {
			return null;
		}
		
		return targets.get(Math.min(targets.size() - 1, targetIndex)).getLocation();
	}
	
	public boolean hasOverride() {
		return this.targetOverride != null;
	}
	
	public void setOnesidedUpgraded(boolean upgraded) {
		this.upgradeOneside = upgraded;
		forceUpdate();
	}
	
	public void setDimensionUpgraded(boolean upgraded) {
		this.upgradeDimension = upgraded;
		forceUpdate();
	}
	
	protected void deactivatePortal() {
		// Remove portal above us
		world.removeBlock(pos.up(), false);
	}
	
	protected void activatePortal() {
		world.setBlockState(pos.up(), NostrumBlocks.obeliskPortal.getMaster());
		NostrumBlocks.obeliskPortal.createPaired(world, pos.up());
	}
	
	protected void refreshPortal() {
		@Nullable Location target;
		boolean valid;
		if (this.targetOverride != null) {
			valid = true;
			target = this.targetOverride;
		} else {
			target = this.getCurrentTarget();
			valid = (target != null);// && IsValidObelisk(world, target));
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
		return !(state == null || !(state.getBlock() instanceof ObeliskBlock)
				|| !ObeliskBlock.blockIsMaster(state));
	}
	
	public void setOverride(Location override, int durationTicks) {
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
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
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
		
		// Server logic
		if (!world.isRemote()) {
			if (master) {
				if (targetOverride != null && aliveCount >= targetOverrideEnd) {
					targetOverride = null;
				}
				refreshPortal();
				intakeItems();
			}
			
			return;
		}
		
		
		// Client logic
		
		if (!master)
			return;
		
		
		final boolean doSwirl = (this.getCurrentTarget() != null);
		if (doSwirl) {
			final long stepInverval = 2;
			if (aliveCount % stepInverval != 0)
				return;
			
			for (int i = 0; i < 4; i++) {
			
				int step = (int) (aliveCount / stepInverval);
				int maxStep = (int) ((20 / stepInverval) * 4); // 4 second period
				step = step % maxStep;
				float ratio = (float) step / (float) maxStep;
				float angle = (float) (ratio * 2f * Math.PI); // radians
				
				angle += (.125 + ((double) i + 1.0) * .25) * (2.0 * Math.PI);
				
				float radius = (float) ((1f - ratio) * (ObeliskBlock.TILE_OFFSETH * 1.41));
				
				double x, z, y;
				x = Math.cos(angle) * radius;
				z = Math.sin(angle) * radius;
				y = ratio * (-ObeliskBlock.TILE_OFFSETY);
				
				BlockPos master = pos;
				x += master.getX() + .5;
				z += master.getZ() + .5;
				y += pos.getY() + .5 + ObeliskBlock.TILE_OFFSETY;
				world.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, .01, 0, .01);
			}
		}
		
	}
	
//	private BlockPos getMasterPos() {
//		if (this.corner != null)
//		switch (this.corner) {
//		case NE:
//			return pos.add(-ObeliskBlock.TILE_OFFSETH, 1, -ObeliskBlock.TILE_OFFSETH);
//		case NW:
//			return pos.add(ObeliskBlock.TILE_OFFSETH, 1, -ObeliskBlock.TILE_OFFSETH);
//		case SE:
//			return pos.add(-ObeliskBlock.TILE_OFFSETH, 1, ObeliskBlock.TILE_OFFSETH);
//		case SW:
//			return pos.add(ObeliskBlock.TILE_OFFSETH, 1, ObeliskBlock.TILE_OFFSETH);
//		}
//		return pos;
//	};
	
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
	
	public boolean canAffordTeleport(Location destination) {
		return true;
		//return this.compWrapper.check(getAetherCost(destination));
	}
	
	/**
	 * Attempt to pay any secondary fee for teleportation to the provided location.
	 * This assumes the destination has been checked, etc.
	 * @param destination
	 * @return
	 */
	public boolean deductForTeleport(Location destination) {
		return true;
		//return this.compWrapper.checkAndWithdraw(getAetherCost(destination));
	}
	
	protected AxisAlignedBB getCaptureBB() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos.up());
	}
	
	protected boolean intakeItems() {
		for (ItemEntity entity : world.getEntitiesWithinAABB(ItemEntity.class, getCaptureBB())) {
			// try and pull from the stack
			@Nonnull ItemStack stack = entity.getItem();
			if (canIntakeItem(stack)) {
				this.consumeItem(stack.split(1)); // reduces stack size by 1
				entity.setItem(stack); // Try and force an update
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean isOnesideUpgradeItem(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == NostrumItems.skillEnderPin;
	}
	
	protected boolean isDimensionUpgradeItem(ItemStack stack) {
		return false; // Portals aren't set up for dimensional teleportation yet. So disable this here.
		//return !stack.isEmpty() && stack.getItem() == NostrumItems.seekingGem;
	}
	
	protected boolean canIntakeItem(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		
		final Item item = stack.getItem();
		if (item instanceof PositionToken) {
			final BlockPos storedPos = PositionToken.getBlockPosition(stack);
			final RegistryKey<World> storedDim = PositionToken.getDimension(stack);
			if (canAcceptTarget(new Location(storedPos, storedDim))) {
				return true;
			}
		}
		
		if (!this.hasOnesidedUpgrade() && isOnesideUpgradeItem(stack)) {
			return true;
		}
		
		if (!this.hasDimensionUpgrade() && isDimensionUpgradeItem(stack)) {
			return true;
		}
		
		return false;
	}
	
	protected void consumeItem(ItemStack stack) {
		final Item item = stack.getItem();
		
		if (item instanceof PositionToken) {
			final BlockPos storedPos = PositionToken.getBlockPosition(stack);
			final RegistryKey<World> storedDim = PositionToken.getDimension(stack);
			final Location storedLoc = new Location(storedPos, storedDim);
			
			if (stack.hasDisplayName()) {
				addTarget(storedLoc, stack.getDisplayName().getString());
			} else {
				addTarget(storedLoc);
			}
			NostrumMagicaSounds.SUCCESS_QUEST.play(
					world,
					pos.getX(),
					pos.getY(),
					pos.getZ()
					);
			return;
		}
		
		if (!this.hasOnesidedUpgrade() && isOnesideUpgradeItem(stack)) {
			this.setOnesidedUpgraded(true);
			return;
		}
		
		if (!this.hasDimensionUpgrade() && isDimensionUpgradeItem(stack)) {
			this.setDimensionUpgraded(true);
			return;
		}
	}
}