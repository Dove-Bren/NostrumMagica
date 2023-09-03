package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SorceryPortalTileEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Portal that takes players to and from the Sorcery dimension
 * @author Skyler
 *
 */
public class SorceryPortal extends NostrumPortal implements ITileEntityProvider  {
	
	public static final String ID = "sorcery_portal";
	
	private static SorceryPortal instance = null;
	public static SorceryPortal instance() {
		if (instance == null)
			instance = new SorceryPortal();
		
		return instance;
	}
	
	public SorceryPortal() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		BlockState state = this.getStateFromMeta(meta);
		if (isMaster(state)) {
			return new SorceryPortalTileEntity();
		}
		
		return null;
	}
	
//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
//		super.eventReceived(state, worldIn, pos, id, param);
//        TileEntity tileentity = worldIn.getTileEntity(pos);
//        return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
//	}
	
	@Override
	protected void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn) {
		entityIn.dismountRidingEntity();
		entityIn.removePassengers();
		
		if (!entityIn.getPassengers().isEmpty()) {
			return;
		}
		
		if (entityIn.isRiding()) {
			return;
		}
		
		entityIn.setPortal(entityIn.getPosition());
		if (worldIn.provider.getDimension() != ModConfig.config.sorceryDimensionIndex()) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
			if (attr != null) {
				// Find bottom block
				BlockPos bottomBlock = portalPos;
				if (worldIn.getBlockState(portalPos.down()).getBlock() instanceof SorceryPortal) {
					bottomBlock = portalPos.down();
				}
				
				// Try to use a block next to the portal
				BlockPos savedPos = bottomBlock;
				for (BlockPos pos : new BlockPos[]{bottomBlock.north(), bottomBlock.south(), bottomBlock.east(), bottomBlock.west()}) {
					if (worldIn.isAirBlock(pos) && worldIn.isAirBlock(pos.up())) {
						savedPos = pos;
						break;
					}
				}
				attr.setSorceryPortalLocation(entityIn.dimension, new BlockPos(savedPos));
			}
			entityIn.changeDimension(ModConfig.config.sorceryDimensionIndex());
		} else {
			entityIn.changeDimension(0);
		}
	}
	
	@Override
	protected boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn) {
		return entityIn instanceof PlayerEntity;
	}
	
}
