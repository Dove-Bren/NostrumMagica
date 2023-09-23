package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.blocks.tiles.ObeliskPortalTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ObeliskPortal extends TeleportationPortal {
	
	public static final String ID = "obelisk_portal";
	
	private static ObeliskPortal instance = null;
	public static ObeliskPortal instance() {
		if (instance == null)
			instance = new ObeliskPortal();
		
		return instance;
	}
	
	public ObeliskPortal() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		BlockState state = this.getStateFromMeta(meta);
		if (isMaster(state)) {
			return new ObeliskPortalTileEntity();
		}
		
		return null;
	}
	
	@Override
	protected void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn) {
		TileEntity te = worldIn.getTileEntity(portalPos.down());
		if (te != null && te instanceof NostrumObeliskEntity) {
			NostrumObeliskEntity ent = (NostrumObeliskEntity) te;
			if (ent.deductForTeleport(ent.getCurrentTarget())) {
				super.teleportEntity(worldIn, portalPos, entityIn);
			} else {
				if (entityIn instanceof PlayerEntity) {
					((PlayerEntity) entityIn).sendMessage(new TranslationTextComponent("info.obelisk.aetherfail"));
				}
			}
		}
	}
	
	@Override
	protected boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn) {
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
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		pos = getMaster(state, pos); // find master
		
		BlockState parentState = worldIn.getBlockState(pos.down());
		if (parentState != null && parentState.getBlock() instanceof NostrumObelisk) {
			parentState.getBlock().onBlockActivated(worldIn, pos.down(), parentState, playerIn, hand, side, hitX, hitY, hitZ);
		}
		
		return true;
	}
}
