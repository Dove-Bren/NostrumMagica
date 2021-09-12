package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class AltarBlock extends Block implements ITileEntityProvider {
	
	public static final String ID = "altar_block";
	protected static final AxisAlignedBB ALTAR_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 0.8D, 0.7D);
	private static final int TICK_DELAY = 5;
	
	private static AltarBlock instance = null;
	public static AltarBlock instance() {
		if (instance == null)
			instance = new AltarBlock();
		
		return instance;
	}
	
	public AltarBlock() {
		super(Material.ROCK, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(3.5f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.hasTileEntity = true;
		this.setLightOpacity(1);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return ALTAR_AABB;
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.SOLID;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		AltarTileEntity ent = new AltarTileEntity();
		
		return ent;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		EntityItem item = new EntityItem(world,
				pos.getX() + .5,
				pos.getY() + .5,
				pos.getZ() + .5,
				new ItemStack(AltarItem.instance()));
		world.spawnEntity(item);
		
		TileEntity te = world.getTileEntity(pos);
		if (te != null) {
			AltarTileEntity altar = (AltarTileEntity) te;
			if (altar.getItem() != null) {
				item = new EntityItem(world,
						pos.getX() + .5,
						pos.getY() + .5,
						pos.getZ() + .5,
						altar.getItem());
				world.spawnEntity(item);
			}
		}
		
        world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return super.getItemDropped(state, rand, fortune);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isUpdateScheduled(pos, this)) {
			worldIn.scheduleUpdate(pos, this, TICK_DELAY);
		}
		
		super.onBlockAdded(worldIn, pos, state);
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te != null && te instanceof AltarTileEntity && ((AltarTileEntity) te).getItem() == null) {
			AltarTileEntity altar = (AltarTileEntity) te;
			List<EntityItem> items = worldIn.getEntitiesWithinAABB(EntityItem.class, Block.FULL_BLOCK_AABB.offset(pos).offset(0, 1, 0).expand(0, 1, 0));
			if (items != null && !items.isEmpty()) {
				EntityItem first = items.get(0);
				ItemStack stack = first.getItem();
				
				altar.setItem(stack.splitStack(1));
				if (stack.getCount() <= 0) {
					first.setDead();
				}
			}
		}
		
		if (!worldIn.isUpdateScheduled(pos, this)) {
			worldIn.scheduleUpdate(pos, this, TICK_DELAY);
		}
		
		super.updateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null)
			return false;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		AltarTileEntity altar = (AltarTileEntity) te;
		if (altar.getItem().isEmpty()) {
			// Accepting items
			if (!heldItem.isEmpty()) {
				altar.setItem(heldItem.splitStack(1));
				return true;
			} else
				return false;
		} else {
			// Has an item
			if (heldItem.isEmpty()) {
				final ItemStack altarItem = altar.getItem();
				if (!playerIn.inventory.addItemStackToInventory(altarItem)) {
					worldIn.spawnEntity(
							new EntityItem(worldIn,
									pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5,
									altar.getItem())
							);
				} else {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.isUnlocked()) {
						if (altarItem.getItem() instanceof ILoreTagged) {
							attr.giveBasicLore((ILoreTagged) altarItem.getItem());
						} else if (altarItem.getItem() instanceof ItemBlock &&
								((ItemBlock) altarItem.getItem()).getBlock() instanceof ILoreTagged) {
							attr.giveBasicLore((ILoreTagged) ((ItemBlock) altarItem.getItem()).getBlock());
						}
					}
				}
				altar.setItem(ItemStack.EMPTY);
				return true;
			} else
				return false;
		}
		
	}
}
