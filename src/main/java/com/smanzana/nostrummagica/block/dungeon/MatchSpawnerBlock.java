package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.MatchSpawnerTileEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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
		
		this.setDefaultState(this.getDefaultState().with(TRIGGERED, false));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(TRIGGERED);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState blockState, BlockState adjacentState, Direction side) {
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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MatchSpawnerTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		if (hand != Hand.MAIN_HAND) {
			return ActionResultType.SUCCESS;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof MatchSpawnerTileEntity)) {
			return ActionResultType.SUCCESS;
		}
		
		MatchSpawnerTileEntity ent = (MatchSpawnerTileEntity) te;
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new StringTextComponent("Currently set to " + state.get(MOB).getString()), Util.DUMMY_UUID);
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
				
				worldIn.setBlockState(pos, state.with(MOB, type));
			} else if (NostrumTags.Items.DragonWing.contains(heldItem.getItem())) {
				worldIn.setBlockState(pos, state.with(MOB, Type.DRAGON_RED));
			} else if (heldItem.getItem() == Items.SUGAR_CANE) {
				worldIn.setBlockState(pos, state.with(MOB, Type.PLANT_BOSS));
			} else if (heldItem.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
				if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.getDimensionKey())) {
					ent.setTriggerPosition(heldPos.getX(), heldPos.getY(), heldPos.getZ());
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
				return ActionResultType.SUCCESS;
			} else if (heldItem.getItem() instanceof EnderEyeItem) {
				BlockPos loc = (ent.getTriggerOffset() == null ? null : ent.getTriggerOffset().toImmutable().add(pos));
				if (loc != null) {
					BlockState atState = worldIn.getBlockState(loc);
					if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
						playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
					} else {
						playerIn.sendMessage(new StringTextComponent("Not pointed at valid triggered block!"), Util.DUMMY_UUID);
					}
				}
			}
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.FAIL;
	}
}
