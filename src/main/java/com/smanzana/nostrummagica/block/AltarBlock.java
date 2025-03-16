package com.smanzana.nostrummagica.block;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.tile.AltarTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

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
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ALTAR_AABB;
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
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
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new AltarTileEntity();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity te = world.getBlockEntity(pos);
			if (te != null) {
				AltarTileEntity altar = (AltarTileEntity) te;
				if (altar.getItem() != null) {
					Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), altar.getItem());
				}
			}
			
	        world.removeBlockEntity(pos);
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int eventID, int eventParam) {
		super.triggerEvent(state, worldIn, pos, eventID, eventParam);
		BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity == null ? false : tileentity.triggerEvent(eventID, eventParam);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!worldIn.getBlockTicks().hasScheduledTick(pos, this)) {
			worldIn.getBlockTicks().scheduleTick(pos, this, TICK_DELAY);
		}
		
		if (!worldIn.isClientSide()) {
			this.tick(oldState, (ServerLevel) worldIn, pos, this.RANDOM);
		}
		
		super.onPlace(state, worldIn, pos, oldState, isMoving);
	}
	
	protected List<ItemEntity> getCapturableItems(Level worldIn, BlockPos altarPos) {
		// Copied from HopperTileEntity
		return Hopper.SUCK.toAabbs().stream().flatMap((box) -> {
			return worldIn.getEntitiesOfClass(ItemEntity.class, box.move(altarPos.getX() - .5, altarPos.getY() - .5, altarPos.getZ() - .5),
					EntitySelector.ENTITY_STILL_ALIVE).stream();
		}).collect(Collectors.toList());
				
				
		//worldIn.getEntitiesWithinAABB(ItemEntity.class, IHopper.COLLECTION_AREA_SHAPE.to.offset(pos).offset(0, 1, 0).expand(0, 1, 0));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		BlockEntity te = worldIn.getBlockEntity(pos);
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null)
			return InteractionResult.PASS;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		AltarTileEntity altar = (AltarTileEntity) te;
		if (altar.getItem().isEmpty()) {
			// Accepting items
			if (!heldItem.isEmpty()) {
				altar.setItem(heldItem.split(1));
				return InteractionResult.SUCCESS;
			} else
				return InteractionResult.PASS;
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
				return InteractionResult.SUCCESS;
			} else
				return InteractionResult.FAIL;
		}
		
	}
}
