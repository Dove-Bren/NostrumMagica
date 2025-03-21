package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.tile.ObeliskPortalTileEntity;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class ObeliskPortal extends TeleportationPortalBlock {
	
	public static final String ID = "obelisk_portal";
	
	public ObeliskPortal() {
		super(Block.Properties.of(Material.LEAVES)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.lightLevel((state) -> 14)
				.noOcclusion()
				);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (isMaster(state)) {
			return new ObeliskPortalTileEntity();
		}
		
		return null;
	}
	
	@Override
	protected void teleportEntity(Level worldIn, BlockPos portalPos, Entity entityIn) {
		BlockEntity te = worldIn.getBlockEntity(portalPos.below());
		if (te != null && te instanceof ObeliskTileEntity) {
			ObeliskTileEntity ent = (ObeliskTileEntity) te;
			if (ent.deductForTeleport(ent.getCurrentTarget())) {
				super.teleportEntity(worldIn, portalPos, entityIn);
			} else {
				if (entityIn instanceof Player) {
					((Player) entityIn).sendMessage(new TranslatableComponent("info.obelisk.aetherfail"), Util.NIL_UUID);
				}
			}
		}
	}
	
	@Override
	protected boolean canTeleport(Level worldIn, BlockPos portalPos, Entity entityIn) {
		// Specifically disallow EntityItems so that we can stuck suck up position crystals
		if (entityIn == null || entityIn instanceof ItemEntity) {
			return false;
		}
		
		return true;
		
//		// Check if the obelisk can afford it.
//		TileEntity te = worldIn.getTileEntity(portalPos.down());
//		if (te != null && te instanceof NostrumObeliskEntity) {
//			BlockPos target =  ((NostrumObeliskEntity) te).getCurrentTarget();
//			if (target != null) {
//				return ((NostrumObeliskEntity) te).canAffordTeleport(target);
//			}
//		}
//		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		pos = getMaster(state, pos); // find master
		
		BlockState parentState = worldIn.getBlockState(pos.below());
		if (parentState != null && parentState.getBlock() instanceof ObeliskBlock) {
			parentState.getBlock().use(parentState, worldIn, pos.below(), player, handIn, hit);
		}
		
		return InteractionResult.SUCCESS;
	}
}
