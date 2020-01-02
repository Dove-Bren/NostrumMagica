package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Candle extends Block implements ITileEntityProvider {

	public static String ID = "nostrum_candle";
	public static PropertyBool LIT = PropertyBool.create("lit");
	protected static final AxisAlignedBB CANDLE_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D);
	
	private static Candle instance = null;
	public static Candle instance() {
			if (instance == null)
				instance = new Candle();
			
			return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(CandleTileEntity.class, "nostrum_candle_te");
		
		GameRegistry.addShapedRecipe(new ItemStack(instance()),
				"W",
				"F",
				'W', ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1),
				'F', Items.ROTTEN_FLESH);
		GameRegistry.addShapedRecipe(new ItemStack(instance()),
				"W",
				"F",
				'W', Items.STRING,
				'F', Items.ROTTEN_FLESH);
	}
	
	public Candle() {
		super(Material.BARRIER, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(1.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.PLANT);
		
		this.isBlockContainer = true;
		//this.setLightOpacity(16);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(LIT, false));
		this.setTickRandomly(true);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).isFullBlock();
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return CANDLE_AABB;
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state == null)
			return 0;
		if (!state.getValue(LIT))
			return 0;
		
		return 10;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LIT);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean lit = ((meta & 0x1) == 1);
		return getDefaultState().withProperty(LIT, lit);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(LIT) ? 1 : 0);
	}
	
//    @Override
//    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
//    	if (state.getValue(LIT)) {
//    		if (rand.nextInt(10) == 0) {
//    			extinguish(worldIn, pos, state);
//    		}
//    	}
//    }
    
    public static void light(World world, BlockPos pos, IBlockState state) {
    	world.setBlockState(pos, state.withProperty(LIT, true));
    }
    
    public static void extinguish(World world, BlockPos pos, IBlockState state) {
    	extinguish(world, pos, state, false);
    }
    
    public static void extinguish(World world, BlockPos pos, IBlockState state, boolean force) {
    	
    	if (world.getTileEntity(pos) != null)
    		world.removeTileEntity(pos);
    	
    	if (!force && world.getBlockState(pos.add(0, -1, 0)).getBlock()
				.isFireSource(world, pos.add(0, -1, 0), EnumFacing.UP))
			return;
    	
    	
    	world.setBlockState(pos, state.withProperty(LIT, false));
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
		
		// We don't create when the block is placed.
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null)
			return;
		
		world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
		if (null == stateIn || !stateIn.getValue(LIT))
			return;
		
		double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.6D;
        double d2 = (double)pos.getZ() + 0.5D;

        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

//		if (worldIn.isRemote)
//			return true;
		
		if (!state.getValue(LIT)) {
			if (heldItem == null)
				return false;
			
			if (heldItem.getItem() instanceof ItemFlintAndSteel) {
				light(worldIn, pos, state);
				heldItem.damageItem(1, playerIn);
				return true;
			}
			
			return false;
		}
		
		// it's lit
		if (heldItem == null) {
			// only if mainhand or mainhand is null. Otherwise if offhand is
			// empty, will still put out. Dumb!
			
			if (hand == EnumHand.MAIN_HAND && (playerIn.getHeldItemMainhand() == null)) {
				// putting it out
				extinguish(worldIn, pos, state, true);
				return true;
			}
			
			return false;
		}

		if (!(heldItem.getItem() instanceof ReagentItem))
			return false;
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te != null)
			return false;
		
		heldItem.stackSize--;
		
		ReagentType type = ReagentItem.findType(heldItem);
		
		if (type == null)
			return false;
		
		CandleTileEntity candle = new CandleTileEntity(type);
		worldIn.setTileEntity(pos, candle);
		candle.dirty();
		
		return true;
	}
	
	public static class CandleTileEntity extends TileEntity implements ITickable {
		
		private static final String NBT_TYPE = "type";
		private static Random rand = new Random();
		private ReagentType type;
		private int lifeTicks;
		
		public CandleTileEntity(ReagentType type) {
			this();
			this.type = type;
		}
		
		public CandleTileEntity() {
			this.lifeTicks = (20 * 15) + CandleTileEntity.rand.nextInt(20*30);
		}
		
		public ReagentType getType() {
			return type;
		}
		
		private ReagentType parseType(String serial) {
			for (ReagentType type : ReagentType.values()) {
				if (type.name().equalsIgnoreCase(serial))
					return type;
			}
			
			return null;
		}
		
		private String serializeType(ReagentType type) {
			if (type == null)
				return "null";
			return type.name().toLowerCase();
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setString(NBT_TYPE, serializeType(type));
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null || !nbt.hasKey(NBT_TYPE, NBT.TAG_STRING))
				return;
			
			this.type = parseType(nbt.getString(NBT_TYPE));
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
		
		private void dirty() {
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
			worldObj.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			markDirty();
		}
		
		@Override
		public void update() {
			this.lifeTicks = Math.max(-1, this.lifeTicks-1);
			
			if (this.lifeTicks == 0) {
				IBlockState state = worldObj.getBlockState(this.pos);
				if (state == null)
					return;
				
				extinguish(worldObj, this.pos, state, false);
			}
		}
	}
	
}
