package com.smanzana.nostrummagica.blocks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk.NostrumObeliskEntity.Corner;
import com.smanzana.nostrummagica.blocks.NostrumObelisk.NostrumObeliskEntity.NostrumObeliskTarget;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.world.NostrumChunkLoader;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Obelisk block. If tile, has tile entity (and is master or slave).
 * If not tile, regular block.
 * 
 * Structure is 4 pillars that surround a central block. When blocks are broken,
 * They use their tile and master status to determine where they should check to
 * remove the structure.
 * 
 * @author Skyler
 *
 */
public class NostrumObelisk extends Block implements ITileEntityProvider {
	
	public static class NostrumObeliskEntity extends TileEntity implements ITickable {
		
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
					targets.add(new NostrumObeliskTarget(new BlockPos(
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
				int xs[] = new int[] {-TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH, TILE_OFFSETH};
				int zs[] = new int[] {-TILE_OFFSETH, TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH};
				for (int i = 0; i < xs.length; i++)
				for (int j = 1; j <= TILE_HEIGHT; j++) { // j starts at one cause the first block is above the base block
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
				int xs[] = new int[] {-TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH, TILE_OFFSETH};
				int zs[] = new int[] {-TILE_OFFSETH, TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH};
				for (int i = 0; i < xs.length; i++) {
					BlockPos base = pos.add(xs[i], -TILE_OFFSETY, zs[i]);
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
			dirty();
		}
		
		public void addTarget(BlockPos pos, String title) {
			targets.add(new NostrumObeliskTarget(pos, title));
			dirty();
		}
		
		public List<NostrumObeliskTarget> getTargets() {
			return targets;
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
			
			float radius = (float) ((1f - ratio) * (TILE_OFFSETH * 1.25));
			
			double x, z, y;
			x = Math.cos(angle) * radius;
			z = Math.sin(angle) * radius;
			y = ratio * (-TILE_OFFSETY);
			
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
				return pos.add(-TILE_OFFSETH, 1, -TILE_OFFSETH);
			case NW:
				return pos.add(TILE_OFFSETH, 1, -TILE_OFFSETH);
			case SE:
				return pos.add(-TILE_OFFSETH, 1, TILE_OFFSETH);
			case SW:
				return pos.add(TILE_OFFSETH, 1, TILE_OFFSETH);
			}
			return pos;
		}
		
	}

	private static final PropertyBool MASTER = PropertyBool.create("master");
	private static final PropertyBool TILE = PropertyBool.create("tile");
	
	private static final int TILE_OFFSETY = 3; // height diff between TE in pillars and master TE
	private static final int TILE_OFFSETH = 3; // horizontal distance between master and pillars
	private static final int TILE_HEIGHT = 4; // Total height of corner pillars
	
	public static final String ID = "nostrum_obelisk";
	
	private static NostrumObelisk instance = null;
	public static NostrumObelisk instance() {
		if (instance == null)
			instance = new NostrumObelisk();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(NostrumObeliskEntity.class, "nostrum_obelisk");
	}
	
	public NostrumObelisk() {
		super(Material.ROCK, MapColor.STONE);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 2);
		
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(MASTER, false)
				.withProperty(TILE, false));
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(TILE) && !state.getValue(MASTER)) {
			return new AxisAlignedBB(0.3D, 0.3D, 0.3D, 0.7D, 0.7D, 0.7D);
		} else {
			return super.getBoundingBox(state, source, pos);
		}
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return !state.getValue(TILE) || state.getValue(MASTER);
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER, TILE);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(MASTER, (meta & 0x1) == 1)
				.withProperty(TILE, ((meta >> 1) & 0x1) == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = 0x0;
		if (state.getValue(TILE))
			meta = 0x2;
		if (state.getValue(MASTER))
			meta |= 0x1;
		
		return meta;
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		//destroy(worldIn, pos, state);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
		//destroy(worldIn, pos, null);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (world.isRemote)
			return;
		
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		// If we're a tile, get TE and call destroy
		// Else, search up and down to find a tile
		// If none are found, exit; we'll be destroyed
		if (state.getValue(TILE)) {
			TileEntity ent = world.getTileEntity(pos);
			if (ent == null || !(ent instanceof NostrumObeliskEntity))
				return;
			
			NostrumObeliskEntity te = (NostrumObeliskEntity) ent;
			te.destroy();
		} else {
			// search up and down for a TE
			// This sort of sucks, because it'll be done for every block in the pillar
			// as they are destroyed.
			// I decided to go ahead and do it instead of doing something like storing
			// whether we've been 'destroyed' in the blockstate
			// (tile = 0, master = 1 for example)
			// just cause this is rarely called and it's not THAT many checks
			for (int i = -(TILE_HEIGHT - 1); i < TILE_HEIGHT; i++) {
				BlockPos bp = pos.add(0, i, 0);
				TileEntity ent = world.getTileEntity(bp);
				if (ent == null || !(ent instanceof NostrumObeliskEntity))
					continue;
				
				NostrumObeliskEntity te = (NostrumObeliskEntity) ent;
				te.destroy();
				break;
			}
		}
		
		// We will be destroyed
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.SOLID;
	}
	
	public static boolean blockIsMaster(IBlockState state) {
		return state.getValue(TILE) && state.getValue(MASTER);
	}
	
	public IBlockState getMasterState() {
		return this.getDefaultState().withProperty(MASTER, true)
				.withProperty(TILE, true);
	}


	public IBlockState getTileState() {
		return this.getDefaultState().withProperty(MASTER, false)
				.withProperty(TILE, true);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		IBlockState state = this.getStateFromMeta(meta);
		if (state.getValue(TILE))
			return new NostrumObeliskEntity(state.getValue(MASTER));
		
		return null;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (state.getValue(MASTER) == false) {
			return false;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr == null || !attr.isUnlocked()) {
			if (worldIn.isRemote) {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.obelisk.nomagic"));
			}
			return false;
		}
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.obeliskID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, worldIn, pos, rand);
		if (Minecraft.getSystemTime() % 500 != 0)
			return;
		System.out.println("tick");
		if (state.getValue(TILE)) {
			
			
			if (state.getValue(MASTER)) {
				
			} else {
//				((WorldServer) worldIn).spawnParticle(EnumParticleTypes.DRAGON_BREATH,
//						pos.getX() + 2, pos.getY() + .5, pos.getZ() + .5, 1,
//						.1, .1, .1, .1, new int[0]);
				
			}
		} 
	}
	
	public static boolean spawnObelisk(World world, BlockPos center) {
		IBlockState state = world.getBlockState(center);
		if (state == null || state.getBlockHardness(world, center) > 2.0f)
			return false;
		
		int xs[] = new int[] {-TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH, TILE_OFFSETH};
		int zs[] = new int[] {-TILE_OFFSETH, TILE_OFFSETH, -TILE_OFFSETH, TILE_OFFSETH};
		Corner corners[] = new Corner[] {Corner.SW, Corner.NW, Corner.SE, Corner.NE};
		for (int i = 0; i < xs.length; i++) {
			if (!checkPillar(world, center.add(xs[i], 1, zs[i])))
				return false;
		}
		
		for (int i = 0; i < xs.length; i++) {
			spawnPillar(world, center.add(xs[i], 1, zs[i]), corners[i]);
		}
		world.setBlockState(center, instance().getMasterState());
		world.setTileEntity(center, new NostrumObeliskEntity(true));
		
		((NostrumObeliskEntity) world.getTileEntity(center)).init();
		
		return true;
	}
	
	private static boolean checkPillar(World world, BlockPos center) {
		for (int i = 0; i < TILE_HEIGHT; i++) {
			if (!world.isAirBlock(center.add(0, i, 0)))
				return false;
		}
		
		return true;
	}

	private static void spawnPillar(World world, BlockPos center, Corner corner) {
		for (int i = 0; i < TILE_HEIGHT; i++) {
			BlockPos pos = center.add(0, i, 0);
			if (i == TILE_OFFSETY - 1) {
				world.setBlockState(pos, instance().getTileState());
				world.setTileEntity(pos, new NostrumObeliskEntity(corner));
			} else {
				world.setBlockState(pos, instance().getDefaultState());
			}
		}
	}

	public static boolean isValidTarget(World world, BlockPos from, BlockPos to) {
		IBlockState state = world.getBlockState(from);
		if (state == null
				|| !(state.getBlock() instanceof NostrumObelisk)
				|| !blockIsMaster(state))
			return false;
		
		TileEntity te = world.getTileEntity(from);
		if (te == null || !(te instanceof NostrumObeliskEntity))
			return false;
		
		NostrumObeliskEntity ent = (NostrumObeliskEntity) te;
		if (ent.targets == null || ent.targets.isEmpty())
			return false;
		
		// Load it?
		state = world.getBlockState(to);
		if (state == null
				|| !(state.getBlock() instanceof NostrumObelisk)
				|| !blockIsMaster(state))
			return false;
		
		for (NostrumObeliskTarget targ : ent.targets) {
			if (targ.pos.getX() == to.getX()
					&& targ.pos.getY() == to.getY()
					&& targ.pos.getZ() == to.getZ())
				return true;
		}
		
		return false;
	}
	
}
