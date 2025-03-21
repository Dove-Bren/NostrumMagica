package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity.SwitchTriggerType;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Houses a switch that has to be interacted iwth in order to proc other mechanisms
 * @author Skyler
 *
 */
public class SwitchBlock extends Block implements EntityBlock {
	
	protected static final VoxelShape SWITCH_BLOCK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16D, 3.2D, 16D);

	public static final String ID = "switch_block";
	
	public SwitchBlock() {
		super(Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.lightLevel((state) -> 8)
				.noOcclusion()
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SWITCH_BLOCK_AABB;
	}
	
	@Override
	public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SwitchBlockTileEntity();
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide || !playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.dimension())) {
				BlockEntity te = worldIn.getBlockEntity(pos);
				if (te != null) {
					SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
					ent.offsetTo(heldPos);
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return InteractionResult.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof EnderEyeItem) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				BlockPos loc = ent.getOffset().immutable().offset(pos);
				BlockState atState = worldIn.getBlockState(loc);
				if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
					playerIn.teleportTo(loc.getX(), loc.getY(), loc.getZ());
				} else {
					playerIn.sendMessage(new TextComponent("Not pointed at valid triggered block!"), Util.NIL_UUID);
				}
			}
			return InteractionResult.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof SwordItem) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setHitType(ent.getSwitchHitType() == SwitchBlockTileEntity.SwitchHitType.ANY ? SwitchBlockTileEntity.SwitchHitType.MAGIC : SwitchBlockTileEntity.SwitchHitType.ANY);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return InteractionResult.SUCCESS;
		} else if (heldItem.isEmpty() && hand == InteractionHand.MAIN_HAND) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setTriggerType(SwitchTriggerType.ONE_TIME);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return InteractionResult.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.CLOCK) {
			BlockEntity te = worldIn.getBlockEntity(pos);
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
			return InteractionResult.SUCCESS;
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.LEVER) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setTriggerType(SwitchTriggerType.REPEATABLE);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
	
}
