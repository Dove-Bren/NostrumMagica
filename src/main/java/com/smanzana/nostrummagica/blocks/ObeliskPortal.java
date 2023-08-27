package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.blocks.tiles.ObeliskPortalTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		IBlockState state = this.getStateFromMeta(meta);
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
		if (entityIn == null || entityIn instanceof EntityItem) {
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn, EnumHand hand, Direction side, float hitX, float hitY, float hitZ) {
		pos = getMaster(state, pos); // find master
		
		IBlockState parentState = worldIn.getBlockState(pos.down());
		if (parentState != null && parentState.getBlock() instanceof NostrumObelisk) {
			parentState.getBlock().onBlockActivated(worldIn, pos.down(), parentState, playerIn, hand, side, hitX, hitY, hitZ);
		}
		
		return true;
	}
}
