package com.smanzana.nostrummagica.block;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.tile.AltarTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class AltarBlock extends Block {
	
	public static final String ID = "altar_block";
	protected static final VoxelShape ALTAR_AABB = Block.box(16 * 0.3D, 16 * 0.0D, 16 * 0.3D, 16 * 0.7D, 16 * 0.8D, 16 * 0.7D);
	private static final int TICK_DELAY = 5;
	
	public AltarBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(3.5f, 10f)
				.sound(SoundType.STONE)
				.lightLevel((state) -> (1))
			);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return ALTAR_AABB;
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
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
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AltarTileEntity();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = world.getBlockEntity(pos);
			if (te != null) {
				AltarTileEntity altar = (AltarTileEntity) te;
				if (altar.getItem() != null) {
					InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), altar.getItem());
				}
			}
			
	        world.removeBlockEntity(pos);
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.triggerEvent(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity == null ? false : tileentity.triggerEvent(eventID, eventParam);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!worldIn.getBlockTicks().hasScheduledTick(pos, this)) {
			worldIn.getBlockTicks().scheduleTick(pos, this, TICK_DELAY);
		}
		
		if (!worldIn.isClientSide()) {
			this.tick(oldState, (ServerWorld) worldIn, pos, this.RANDOM);
		}
		
		super.onPlace(state, worldIn, pos, oldState, isMoving);
	}
	
	protected List<ItemEntity> getCapturableItems(World worldIn, BlockPos altarPos) {
		// Copied from HopperTileEntity
		return IHopper.SUCK.toAabbs().stream().flatMap((box) -> {
			return worldIn.getEntitiesOfClass(ItemEntity.class, box.move(altarPos.getX() - .5, altarPos.getY() - .5, altarPos.getZ() - .5),
					EntityPredicates.ENTITY_STILL_ALIVE).stream();
		}).collect(Collectors.toList());
				
				
		//worldIn.getEntitiesWithinAABB(ItemEntity.class, IHopper.COLLECTION_AREA_SHAPE.to.offset(pos).offset(0, 1, 0).expand(0, 1, 0));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getBlockEntity(pos);
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
		
		if (!worldIn.getBlockTicks().hasScheduledTick(pos, this)) {
			worldIn.getBlockTicks().scheduleTick(pos, this, TICK_DELAY);
		}
		
		super.tick(state, worldIn, pos, rand);
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isClientSide()) {
			return ActionResultType.SUCCESS;
		}
		
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te == null)
			return ActionResultType.PASS;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		AltarTileEntity altar = (AltarTileEntity) te;
		if (altar.getItem().isEmpty()) {
			// Accepting items
			if (!heldItem.isEmpty()) {
				altar.setItem(heldItem.split(1));
				return ActionResultType.SUCCESS;
			} else
				return ActionResultType.PASS;
		} else {
			// Has an item
			if (heldItem.isEmpty()) {
				final ItemStack altarItem = altar.getItem();
				if (!playerIn.inventory.add(altarItem)) {
					worldIn.addFreshEntity(
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
				return ActionResultType.SUCCESS;
			} else
				return ActionResultType.FAIL;
		}
		
	}
}
