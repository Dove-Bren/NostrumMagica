package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.CandleIgniteMessage;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Candle extends Block implements ITileEntityProvider {

	public static String ID = "nostrum_candle";
	public static PropertyBool LIT = PropertyBool.create("lit");
	protected static final AxisAlignedBB CANDLE_AABB = new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 0.5D, 0.5625D);
	protected static final AxisAlignedBB CANDLE_E_AABB = new AxisAlignedBB(0.0D, 0.35D, 0.4375D, 0.25D, 0.85D, 0.5625D);
	protected static final AxisAlignedBB CANDLE_N_AABB = new AxisAlignedBB(0.4375D, 0.35D, 0.75D, 0.5625D, 0.85D, 1D);
	protected static final AxisAlignedBB CANDLE_W_AABB = new AxisAlignedBB(0.75D, 0.35D, 0.4375D, 1D, 0.85D, 0.5625D);
	protected static final AxisAlignedBB CANDLE_S_AABB = new AxisAlignedBB(0.4375D, 0.35D, 0D, 0.5625D, 0.85D, 0.25D);
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>() {
		public boolean apply(@Nullable EnumFacing facing) {
			return facing != EnumFacing.DOWN;
		}
	});
	private static final int TICK_DELAY = 5;
	
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
	
	protected boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing facing) {
		return (worldIn.getBlockState(pos.offset(facing.getOpposite())).isSideSolid(worldIn, pos.offset(facing), facing));
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing enumfacing : FACING.getAllowedValues()) {
			if (canPlaceAt(worldIn, pos, enumfacing)) {
				return true;
			}
		}

        return false;
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return this.getDefaultState().withProperty(FACING, facing);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing facing = state.getValue(FACING);
		switch (facing) {
		case EAST:
			return CANDLE_E_AABB;
		case NORTH:
			return CANDLE_N_AABB;
		case SOUTH:
			return CANDLE_S_AABB;
		case WEST:
			return CANDLE_W_AABB;
		case UP:
		case DOWN:
		default:
			return CANDLE_AABB;
		}
		
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
		return new BlockStateContainer(this, LIT, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean lit = ((meta & 0x1) == 1);
		EnumFacing facing = EnumFacing.VALUES[(meta >> 1) & 7];
		return getDefaultState().withProperty(LIT, lit).withProperty(FACING, facing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(LIT) ? 1 : 0) | (state.getValue(FACING).ordinal() << 1);
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
    	if (!state.getValue(LIT)) {
	    	world.setBlockState(pos, state.withProperty(LIT, true));
			
			if (!world.isUpdateScheduled(pos, state.getBlock())) {
				world.scheduleUpdate(pos, state.getBlock(), TICK_DELAY);
			}
    	}
    }
    
    public static void extinguish(World world, BlockPos pos, IBlockState state) {
    	extinguish(world, pos, state, false);
    }
    
    public static void extinguish(World world, BlockPos pos, IBlockState state, boolean force) {
    	
    	if (world.getTileEntity(pos) != null) {
    		world.removeTileEntity(pos);
    		world.notifyBlockUpdate(pos, state, state, 2);
    	}
    	
    	if (!world.isRemote) {
			NetworkHandler.getSyncChannel().sendToAllAround(new CandleIgniteMessage(world.provider.getDimension(), pos, null),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
		}
    	
    	if (!force && world.getBlockState(pos.add(0, -1, 0)).getBlock()
				.isFireSource(world, pos.add(0, -1, 0), EnumFacing.UP)) {
			
			if (!world.isUpdateScheduled(pos, state.getBlock())) {
				world.scheduleUpdate(pos, state.getBlock(), TICK_DELAY);
			}
			return;
    	}
    	
    	if (!force && world.getBlockState(pos.add(0, -2, 0)).getBlock()
				.isFireSource(world, pos.add(0, -2, 0), EnumFacing.UP)) {
			
			if (!world.isUpdateScheduled(pos, state.getBlock())) {
				world.scheduleUpdate(pos, state.getBlock(), TICK_DELAY);
			}
			return;
    	}
    	
    	world.setBlockState(pos, state.withProperty(LIT, false));
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
		
		// We don't create when the block is placed.
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
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
		
		EnumFacing facing = stateIn.getValue(FACING);
		double d0 = (double)pos.getX() + 0.5D;
		double d1 = (double)pos.getY() + 0.6D;
		double d2 = (double)pos.getZ() + 0.5D;
		
		final double hangOffset = .3;
		
		switch (facing) {
		case EAST:
			d0 -= hangOffset;
			d1 += .35;
	        break;
		case NORTH:
			d2 += hangOffset;
			d1 += .35;
	        break;
		case SOUTH:
	        d2 -= hangOffset;
			d1 += .35;
	        break;
		case WEST:
			d0 += hangOffset;
			d1 += .35;
	        break;
		case UP:
		case DOWN:
		default:
	        break;
		}
		
        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
        worldIn.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		// Check for a reagent item over the candle
		if (state.getValue(LIT) && worldIn.getTileEntity(pos) == null) {
			List<EntityItem> items = worldIn.getEntitiesWithinAABB(EntityItem.class, Block.FULL_BLOCK_AABB.offset(pos).expand(1, 1, 1));
			if (items != null && !items.isEmpty()) {
				for (EntityItem item : items) {
					ItemStack stack = item.getEntityItem();
					if (stack.getItem() instanceof ReagentItem) {
						ReagentType type = ReagentItem.findType(stack.splitStack(1));
						if (type != null) {
							setReagent(worldIn, pos, state, type);
						}
						
						if (stack.stackSize <= 0) {
							item.setDead();
						}
						
						break;
					}
				}
			}
			
			if (!worldIn.isUpdateScheduled(pos, this)) {
				worldIn.scheduleUpdate(pos, this, TICK_DELAY);
			}
		}
		
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
		
		setReagent(worldIn, pos, state, type);
		
		return true;
	}
	
	public static void setReagent(World world, BlockPos pos, IBlockState state, ReagentType type) {
		if (world.isRemote && type == null) {
			extinguish(world, pos, state, false);
			return;
		}
		
		light(world, pos, state);
		
		CandleTileEntity candle = null;
		if (world.getTileEntity(pos) != null) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof CandleTileEntity) {
				candle = (CandleTileEntity) te;
			} else {
				world.removeTileEntity(pos);
			}
		}
		
		if (candle == null) {
			candle = new CandleTileEntity(type);
			world.setTileEntity(pos, candle);
		}
		
		candle.type = type;
		candle.dirty();
		
		if (!world.isRemote) {
			NetworkHandler.getSyncChannel().sendToAllAround(new CandleIgniteMessage(world.provider.getDimension(), pos, type),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
		}
	}
	
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (!canPlaceAt(worldIn, pos, state.getValue(FACING))) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
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
			
			if (this.lifeTicks == 0 && !worldObj.isRemote) {
				IBlockState state = worldObj.getBlockState(this.pos);
				if (state == null)
					return;
				
				extinguish(worldObj, this.pos, state, false);
			}
		}
	}
	
}
