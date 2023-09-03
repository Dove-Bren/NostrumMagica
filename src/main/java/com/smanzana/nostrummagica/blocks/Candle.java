package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.CandleTileEntity;
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
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Candle extends Block implements ITileEntityProvider {

	public static String ID = "nostrum_candle";
	public static PropertyBool LIT = PropertyBool.create("lit");
	protected static final AxisAlignedBB CANDLE_AABB = new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 0.5D, 0.5625D);
	protected static final AxisAlignedBB CANDLE_E_AABB = new AxisAlignedBB(0.0D, 0.35D, 0.4375D, 0.25D, 0.85D, 0.5625D);
	protected static final AxisAlignedBB CANDLE_N_AABB = new AxisAlignedBB(0.4375D, 0.35D, 0.75D, 0.5625D, 0.85D, 1D);
	protected static final AxisAlignedBB CANDLE_W_AABB = new AxisAlignedBB(0.75D, 0.35D, 0.4375D, 1D, 0.85D, 0.5625D);
	protected static final AxisAlignedBB CANDLE_S_AABB = new AxisAlignedBB(0.4375D, 0.35D, 0D, 0.5625D, 0.85D, 0.25D);
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<Direction>() {
		public boolean apply(@Nullable Direction facing) {
			return facing != Direction.DOWN;
		}
	});
	private static final int TICK_DELAY = 5;
	
	private static Candle instance = null;
	public static Candle instance() {
			if (instance == null)
				instance = new Candle();
			
			return instance;
	}
	
	public Candle() {
		super(Material.CLOTH, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(0.1f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.PLANT);
		
		this.hasTileEntity = true;
		//this.setLightOpacity(16);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(LIT, false));
		this.setTickRandomly(true);
	}
	
	protected boolean canPlaceAt(World worldIn, BlockPos pos, Direction facing) {
		return (worldIn.getBlockState(pos.offset(facing.getOpposite())).isSideSolid(worldIn, pos.offset(facing), facing));
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (Direction enumfacing : FACING.getAllowedValues()) {
			if (canPlaceAt(worldIn, pos, enumfacing)) {
				return true;
			}
		}

        return false;
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return this.getDefaultState().withProperty(FACING, facing);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		Direction facing = state.getValue(FACING);
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
	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
		if (state == null)
			return 0;
		if (!state.getValue(LIT))
			return 0;
		
		return 10;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LIT, FACING);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		boolean lit = ((meta & 0x1) == 1);
		Direction facing = Direction.VALUES[(meta >> 1) & 7];
		return getDefaultState().withProperty(LIT, lit).withProperty(FACING, facing);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return (state.getValue(LIT) ? 1 : 0) | (state.getValue(FACING).ordinal() << 1);
	}
	
//    @Override
//    public void updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
//    	if (state.getValue(LIT)) {
//    		if (rand.nextInt(10) == 0) {
//    			extinguish(worldIn, pos, state);
//    		}
//    	}
//    }
    
    public static void light(World world, BlockPos pos, BlockState state) {
    	if (!state.getValue(LIT)) {
	    	world.setBlockState(pos, state.withProperty(LIT, true));
			
			if (!world.isUpdateScheduled(pos, state.getBlock())) {
				world.scheduleUpdate(pos, state.getBlock(), TICK_DELAY);
			}
    	}
    }
    
    public static void extinguish(World world, BlockPos pos, BlockState state) {
    	extinguish(world, pos, state, false);
    }
    
    public static void extinguish(World world, BlockPos pos, BlockState state, boolean force) {
    	
    	if (world.getTileEntity(pos) != null) {
    		world.removeTileEntity(pos);
    		world.notifyBlockUpdate(pos, state, state, 2);
    	}
    	
    	if (!world.isRemote) {
			NetworkHandler.getSyncChannel().sendToAllAround(new CandleIgniteMessage(world.provider.getDimension(), pos, null),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
		}
    	
    	if (!force && Candle.IsCandleEnhanced(world, pos)) {
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
	public void onBlockAdded(World worldIn, BlockPos pos, BlockState state) {
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		super.breakBlock(world, pos, state);
		
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null)
			return;
		
		world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
		if (null == stateIn || !stateIn.getValue(LIT))
			return;
		
		Direction facing = stateIn.getValue(FACING);
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
	public void updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
		// Check for a reagent item over the candle
		if (state.getValue(LIT) && worldIn.getTileEntity(pos) == null) {
			List<ItemEntity> items = worldIn.getEntitiesWithinAABB(ItemEntity.class, Block.FULL_BLOCK_AABB.offset(pos).expand(0, 1, 0));
			if (items != null && !items.isEmpty()) {
				for (ItemEntity item : items) {
					ItemStack stack = item.getItem();
					if (stack.getItem() instanceof ReagentItem) {
						ReagentType type = ReagentItem.findType(stack.splitStack(1));
						if (type != null) {
							setReagent(worldIn, pos, state, type);
						}
						
						if (stack.getCount() <= 0) {
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {

//		if (worldIn.isRemote)
//			return true;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (!state.getValue(LIT)) {
			if (heldItem.isEmpty())
				return false;
			
			if (heldItem.getItem() instanceof ItemFlintAndSteel) {
				light(worldIn, pos, state);
				heldItem.damageItem(1, playerIn);
				return true;
			}
			
			return false;
		}
		
		// it's lit
		if (heldItem.isEmpty()) {
			// only if mainhand or mainhand is null. Otherwise if offhand is
			// empty, will still put out. Dumb!
			
			if (hand == Hand.MAIN_HAND && (playerIn.getHeldItemMainhand().isEmpty())) {
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
		
		ReagentType type = ReagentItem.findType(heldItem);
		heldItem.splitStack(1);
		
		if (type == null)
			return false;
		
		setReagent(worldIn, pos, state, type);
		
		return true;
	}
	
	public static void setReagent(World world, BlockPos pos, BlockState state, ReagentType type) {
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
		
		candle.setReagentType(type);
		
		if (!world.isRemote) {
			NetworkHandler.getSyncChannel().sendToAllAround(new CandleIgniteMessage(world.provider.getDimension(), pos, type),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
		}
	}
	
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (!canPlaceAt(worldIn, pos, state.getValue(FACING))) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}
	
	public static boolean IsCandleEnhancingBlock(World world, BlockPos pos, BlockState state) {
		return state.getBlock().isFireSource(world, pos, Direction.UP);
	}
	
	public static boolean IsCandleEnhanced(World world, BlockPos candlePos) {
		BlockPos[] positions = {
				candlePos.down(),
				candlePos.down().down(),
			};
			
		for (BlockPos cursor: positions) {
			BlockState state = world.getBlockState(cursor);
			if (state != null && Candle.IsCandleEnhancingBlock(world, cursor, state)) {
				return true;
			}
		}
		
		return false;
	}
	
}
