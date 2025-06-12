package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.MatchSpawnerTileEntity;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Like the single spawner, but spawns and then (once the entity is dead) triggers a triggerable block
 * @author Skyler
 *
 */
public class MatchSpawnerBlock extends SingleSpawnerBlock {
	
	public static final String ID = "nostrum_spawner_trigger";

	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");
	
	public MatchSpawnerBlock() {
		super();
		
		this.registerDefaultState(this.defaultBlockState().setValue(TRIGGERED, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TRIGGERED);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState blockState, BlockState adjacentState, Direction side) {
//		if (!NostrumMagica.instance.proxy.getPlayer().isCreative()) {
//			return true; // I guess just only in creative?
////			TileEntity te = blockAccess.getTileEntity(pos);
////			if (te != null && te instanceof SpawnerTriggerTileEntity) {
////				SpawnerTriggerTileEntity ent = ((SpawnerTriggerTileEntity) te);
////				return (ent.getSpawnedEntity() == null && ent.getUnlinkedEntID() == null);
////			}
//		}
		return false;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MatchSpawnerTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.MatchSpawner);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof MatchSpawnerTileEntity)) {
			return InteractionResult.SUCCESS;
		}
		
		MatchSpawnerTileEntity ent = (MatchSpawnerTileEntity) te;
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getItemInHand(hand);
			if (heldItem.isEmpty()) {
				if (ent.getTriggerOffset() != null) {
					NostrumParticles.GLOW_TRAIL.spawn(worldIn, new SpawnParams(1, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
							0, 300, 0, new TargetLocation(Vec3.atCenterOf(pos.offset(ent.getTriggerOffset())))
							).setTargetBehavior(new ParticleTargetBehavior().joinMode(true)).color(1f, .8f, 1f, .3f));
				}
				// let super run to display stored entity return InteractionResult.SUCCESS;
			} else if (heldItem.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
				if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.dimension())) {
					ent.setTriggerPosition(heldPos.getX(), heldPos.getY(), heldPos.getZ());
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
				return InteractionResult.SUCCESS;
			} else if (heldItem.getItem() instanceof EnderEyeItem) {
				BlockPos loc = (ent.getTriggerOffset() == null ? null : ent.getTriggerOffset().immutable().offset(pos));
				if (loc != null) {
					BlockState atState = worldIn.getBlockState(loc);
					if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
						playerIn.teleportTo(loc.getX(), loc.getY(), loc.getZ());
					} else {
						playerIn.sendMessage(new TextComponent("Not pointed at valid triggered block!"), Util.NIL_UUID);
					}
					return InteractionResult.SUCCESS;
				}
			}
			
		}
		
		return super.use(state, worldIn, pos, playerIn, hand, hit);
	}
}
