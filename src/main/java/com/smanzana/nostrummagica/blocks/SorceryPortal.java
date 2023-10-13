package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.tiles.SorceryPortalTileEntity;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

/**
 * Portal that takes players to and from the Sorcery dimension
 * @author Skyler
 *
 */
@SuppressWarnings("deprecation")
public class SorceryPortal extends NostrumPortal implements ITileEntityProvider  {
	
	public static final String ID = "sorcery_portal";
	
	public SorceryPortal() {
		super(Block.Properties.create(Material.LEAVES)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.lightValue(14)
				);
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
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
		entityIn.stopRiding();
		entityIn.removePassengers();
		
		if (!entityIn.getPassengers().isEmpty()) {
			return;
		}
		
		if (entityIn.isPassenger()) {
			return;
		}
		
		entityIn.setPortal(entityIn.getPosition());
		if (worldIn.getDimension().getType() != NostrumDimensions.EmptyDimension) {
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
			entityIn.changeDimension(NostrumDimensions.EmptyDimension);
		} else {
			entityIn.changeDimension(DimensionType.OVERWORLD);
		}
	}
	
	@Override
	protected boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn) {
		return entityIn instanceof PlayerEntity;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
