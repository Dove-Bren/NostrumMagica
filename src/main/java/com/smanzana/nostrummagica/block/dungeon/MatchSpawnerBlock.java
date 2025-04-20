package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.MatchSpawnerTileEntity;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
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
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.MatchSpawnerTileEntityType);
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
				playerIn.sendMessage(new TextComponent("Currently set to " + state.getValue(MOB).getSerializedName()), Util.NIL_UUID);
			} else if (heldItem.getItem() instanceof EssenceItem) {
				Type type = null;
				switch (EssenceItem.findType(heldItem)) {
				case EARTH:
					type = Type.GOLEM_EARTH;
					break;
				case ENDER:
					type = Type.GOLEM_ENDER;
					break;
				case FIRE:
					type = Type.GOLEM_FIRE;
					break;
				case ICE:
					type = Type.GOLEM_ICE;
					break;
				case LIGHTNING:
					type = Type.GOLEM_LIGHTNING;
					break;
				case PHYSICAL:
					type = Type.GOLEM_PHYSICAL;
					break;
				case WIND:
					type = Type.GOLEM_WIND;
					break;
				}
				
				worldIn.setBlockAndUpdate(pos, state.setValue(MOB, type));
			} else if (heldItem.is(NostrumTags.Items.DragonWing)) {
				worldIn.setBlockAndUpdate(pos, state.setValue(MOB, Type.DRAGON_RED));
			} else if (heldItem.getItem() == Items.SUGAR_CANE) {
				worldIn.setBlockAndUpdate(pos, state.setValue(MOB, Type.PLANT_BOSS));
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
				}
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.FAIL;
	}
}
