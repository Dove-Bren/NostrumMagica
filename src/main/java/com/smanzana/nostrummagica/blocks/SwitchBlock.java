package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Houses a switch that has to be interacted iwth in order to proc other mechanisms
 * @author Skyler
 *
 */
public class SwitchBlock extends Block {
	
	protected static final AxisAlignedBB SWITCH_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1D, 0.2D, 1D);

	public static String ID = "switch_block";
	
	private static SwitchBlock instance = null;
	public static SwitchBlock instance() {
			if (instance == null)
				instance = new SwitchBlock();
			
			return instance;
	}
	
	public SwitchBlock() {
		super(Material.BARRIER, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 8;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return SWITCH_BLOCK_AABB;
	}
	
//	@Override
//	public boolean isVisuallyOpaque() {
//		return false;
//	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new SwitchBlockTileEntity();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote || !playerIn.isCreative()) {
			return false;
		}
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.provider.getDimension()) {
				TileEntity te = worldIn.getTileEntity(pos);
				if (te != null) {
					SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
					ent.offsetTo(heldPos);
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return true;
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof ItemEnderEye) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				BlockPos loc = ent.getOffset().toImmutable().add(pos);
				IBlockState atState = worldIn.getBlockState(loc);
				if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
					playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
				} else {
					playerIn.sendMessage(new TextComponentString("Not pointed at valid triggered block!"));
				}
			}
		} else if (heldItem.isEmpty() && hand == EnumHand.MAIN_HAND) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setType(ent.getType() == SwitchBlockTileEntity.SwitchType.ANY ? SwitchBlockTileEntity.SwitchType.MAGIC : SwitchBlockTileEntity.SwitchType.ANY);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
		
		return false;
	}
	
}
