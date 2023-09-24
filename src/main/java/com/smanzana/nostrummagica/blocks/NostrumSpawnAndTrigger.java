package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SpawnerTriggerTileEntity;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumItemTags;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
public class NostrumSpawnAndTrigger extends NostrumSingleSpawner {
	
	public static final String ID = "nostrum_spawner_trigger";
	
	public NostrumSpawnAndTrigger() {
		super();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState blockState, BlockState adjacentState, Direction side) {
		if (NostrumMagica.instance.proxy.getPlayer().isCreative()) {
			return true; // I guess just only in creative?
//			TileEntity te = blockAccess.getTileEntity(pos);
//			if (te != null && te instanceof SpawnerTriggerTileEntity) {
//				SpawnerTriggerTileEntity ent = ((SpawnerTriggerTileEntity) te);
//				return (ent.getSpawnedEntity() == null && ent.getUnlinkedEntID() == null);
//			}
		}
		return false;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SpawnerTriggerTileEntity();
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			return true;
		}
		
		if (hand != Hand.MAIN_HAND) {
			return true;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SpawnerTriggerTileEntity)) {
			return true;
		}
		
		SpawnerTriggerTileEntity ent = (SpawnerTriggerTileEntity) te;
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new StringTextComponent("Currently set to " + state.get(MOB).getName()));
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
			} else if (NostrumItemTags.Items.DragonWing.contains(heldItem.getItem())) {
				worldIn.setBlockState(pos, state.with(MOB, Type.DRAGON_RED));
			} if (heldItem.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
				if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.getDimension().getType().getId()) {
					ent.setTriggerPosition(heldPos.getX(), heldPos.getY(), heldPos.getZ());
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
				return true;
			} else if (heldItem.getItem() instanceof EnderEyeItem) {
				BlockPos loc = (ent.getTriggerOffset() == null ? null : ent.getTriggerOffset().toImmutable().add(pos));
				if (loc != null) {
					BlockState atState = worldIn.getBlockState(loc);
					if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
						playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
					} else {
						playerIn.sendMessage(new StringTextComponent("Not pointed at valid triggered block!"));
					}
				}
			}
			return true;
		}
		
		return false;
	}
}
