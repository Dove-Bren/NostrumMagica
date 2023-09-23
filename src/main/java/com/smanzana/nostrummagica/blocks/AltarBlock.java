package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AltarBlock extends ContainerBlock {
	
	public static final String ID = "altar_block";
	protected static final VoxelShape ALTAR_AABB = Block.makeCuboidShape(0.3D, 0.0D, 0.3D, 0.7D, 0.8D, 0.7D);
	private static final int TICK_DELAY = 5;
	
	public AltarBlock() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(3.5f, 10f)
				.sound(SoundType.STONE)
				.lightValue((1))
			);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return ALTAR_AABB;
	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//        return false;
//    }
	
//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
//		return BlockFaceShape.SOLID;
//	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
	}
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new AltarTileEntity();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null) {
				AltarTileEntity altar = (AltarTileEntity) te;
				if (altar.getItem() != null) {
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), altar.getItem());
				}
			}
			
	        world.removeTileEntity(pos);
		}
		super.onReplaced(state, world, pos, newState, isMoving);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, TICK_DELAY);
		}
		
		this.tick(oldState, worldIn, pos, this.RANDOM);
		
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
	}
	
	protected List<ItemEntity> getCapturableItems(World worldIn, BlockPos altarPos) {
		// Copied from HopperTileEntity
		return IHopper.COLLECTION_AREA_SHAPE.toBoundingBoxList().stream().flatMap((box) -> {
			return worldIn.getEntitiesWithinAABB(ItemEntity.class, box.offset(altarPos.getX() - .5, altarPos.getY() - .5, altarPos.getZ() - .5),
					EntityPredicates.IS_ALIVE).stream();
		}).collect(Collectors.toList());
				
				
		//worldIn.getEntitiesWithinAABB(ItemEntity.class, IHopper.COLLECTION_AREA_SHAPE.to.offset(pos).offset(0, 1, 0).expand(0, 1, 0));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te != null && te instanceof AltarTileEntity && ((AltarTileEntity) te).getItem().isEmpty()) {
			AltarTileEntity altar = (AltarTileEntity) te;
			List<ItemEntity> items = getCapturableItems(worldIn, pos);
			if (items != null && !items.isEmpty()) {
				ItemEntity first = items.get(0);
				ItemStack stack = first.getItem();
				
				altar.setItem(stack.split(1));
				if (stack.getCount() <= 0) {
					first.remove();
				}
			}
		}
		
		if (!worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, TICK_DELAY);
		}
		
		super.tick(state, worldIn, pos, rand);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null)
			return false;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		AltarTileEntity altar = (AltarTileEntity) te;
		if (altar.getItem().isEmpty()) {
			// Accepting items
			if (!heldItem.isEmpty()) {
				altar.setItem(heldItem.split(1));
				return true;
			} else
				return false;
		} else {
			// Has an item
			if (heldItem.isEmpty()) {
				final ItemStack altarItem = altar.getItem();
				if (!playerIn.inventory.addItemStackToInventory(altarItem)) {
					worldIn.addEntity(
							new ItemEntity(worldIn,
									pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5,
									altar.getItem())
							);
				} else {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.isUnlocked()) {
						if (altarItem.getItem() instanceof ILoreTagged) {
							attr.giveBasicLore((ILoreTagged) altarItem.getItem());
						} else if (altarItem.getItem() instanceof BlockItem &&
								((BlockItem) altarItem.getItem()).getBlock() instanceof ILoreTagged) {
							attr.giveBasicLore((ILoreTagged) ((BlockItem) altarItem.getItem()).getBlock());
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
