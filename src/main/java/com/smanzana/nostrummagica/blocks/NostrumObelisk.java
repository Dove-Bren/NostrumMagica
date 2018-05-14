package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObeliskEntity.Corner;
import com.smanzana.nostrummagica.blocks.NostrumObeliskEntity.NostrumObeliskTarget;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
	
	

	private static final PropertyBool MASTER = PropertyBool.create("master");
	private static final PropertyBool TILE = PropertyBool.create("tile");
	
	public static final int TILE_OFFSETY = 3; // height diff between TE in pillars and master TE
	public static final int TILE_OFFSETH = 3; // horizontal distance between master and pillars
	public static final int TILE_HEIGHT = 4; // Total height of corner pillars
	
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
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state == null)
			return 0;
		if (!state.getValue(TILE))
			return 0;
		
		if (!state.getValue(MASTER))
			return 8;
		
		return 12;
	}
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state == null)
			return 15;
		if (!state.getValue(TILE))
			return 15;
		
		return 0;
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
		if (!ModConfig.config.obeliskReqMagic() && (attr == null || !attr.isUnlocked())) {
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
		for (int i = -TILE_OFFSETH; i <= TILE_OFFSETH; i++)
			for (int j = -TILE_OFFSETH; j <= TILE_OFFSETH; j++) {
				world.setBlockState(center.add(i, 0, j), Blocks.OBSIDIAN.getDefaultState());
			}
		for (int i = -1; i <= 1; i++) {
			int offset = (TILE_OFFSETH + 1);
			world.setBlockState(center.add(i, 0, offset), Blocks.OBSIDIAN.getDefaultState());
			world.setBlockState(center.add(i, 0, -offset), Blocks.OBSIDIAN.getDefaultState());
			world.setBlockState(center.add(offset, 0, i), Blocks.OBSIDIAN.getDefaultState());
			world.setBlockState(center.add(-offset, 0, i), Blocks.OBSIDIAN.getDefaultState());
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
		if (ent.getTargets() == null || ent.getTargets().isEmpty())
			return false;
		
		// Load it?
		state = world.getBlockState(to);
		if (state == null
				|| !(state.getBlock() instanceof NostrumObelisk)
				|| !blockIsMaster(state))
			return false;
		
		for (NostrumObeliskTarget targ : ent.getTargets()) {
			if (targ.getPos().getX() == to.getX()
					&& targ.getPos().getY() == to.getY()
					&& targ.getPos().getZ() == to.getZ())
				return true;
		}
		
		return false;
	}
	
}
