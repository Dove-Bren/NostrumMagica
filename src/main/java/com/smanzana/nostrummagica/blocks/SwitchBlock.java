package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity.SwitchTriggerType;
import com.smanzana.nostrummagica.utils.DimensionUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Houses a switch that has to be interacted iwth in order to proc other mechanisms
 * @author Skyler
 *
 */
public class SwitchBlock extends Block {
	
	protected static final VoxelShape SWITCH_BLOCK_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16D, 3.2D, 16D);

	public static final String ID = "switch_block";
	
	public SwitchBlock() {
		super(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.setLightLevel((state) -> 8)
				);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SWITCH_BLOCK_AABB;
	}
	
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SwitchBlockTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote || !playerIn.isCreative()) {
			return ActionResultType.PASS;
		}
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.getDimensionKey())) {
				TileEntity te = worldIn.getTileEntity(pos);
				if (te != null) {
					SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
					ent.offsetTo(heldPos);
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return ActionResultType.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof EnderEyeItem) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				BlockPos loc = ent.getOffset().toImmutable().add(pos);
				BlockState atState = worldIn.getBlockState(loc);
				if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
					playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
				} else {
					playerIn.sendMessage(new StringTextComponent("Not pointed at valid triggered block!"), Util.DUMMY_UUID);
				}
			}
			return ActionResultType.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof SwordItem) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setHitType(ent.getSwitchHitType() == SwitchBlockTileEntity.SwitchHitType.ANY ? SwitchBlockTileEntity.SwitchHitType.MAGIC : SwitchBlockTileEntity.SwitchHitType.ANY);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return ActionResultType.SUCCESS;
		} else if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setTriggerType(SwitchTriggerType.ONE_TIME);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return ActionResultType.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.CLOCK) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				if (ent.getSwitchTriggerType() == SwitchTriggerType.TIMED) {
					ent.setCooldownTicks(ent.getTotalCooldownTicks() + 10);
				} else {
					ent.setTriggerType(SwitchTriggerType.TIMED);
					ent.setCooldownTicks(20);
				}
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return ActionResultType.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.LEVER) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setTriggerType(SwitchTriggerType.REPEATABLE);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}
	
}
