package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.tile.SorceryPortalTileEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * Portal that takes players to and from the Sorcery dimension
 * @author Skyler
 *
 */
@SuppressWarnings("deprecation")
public class SorceryPortalBlock extends PortalBlock implements EntityBlock  {
	
	public static final String ID = "sorcery_portal";
	
	public SorceryPortalBlock() {
		super(Block.Properties.of(Material.LEAVES)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.lightLevel((state) -> 14)
				);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return isMaster(state);
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
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
	protected void teleportEntity(Level worldIn, BlockPos portalPos, Entity entityIn) {
		entityIn.stopRiding();
		entityIn.ejectPassengers();
		
		if (!entityIn.getPassengers().isEmpty()) {
			return;
		}
		
		if (entityIn.isPassenger()) {
			return;
		}
		
		//entityIn.setPortal(entityIn.getPosition());
		if (!DimensionUtils.IsSorceryDim(worldIn)) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
			if (attr != null) {
				// Find bottom block
				BlockPos bottomBlock = portalPos;
				if (worldIn.getBlockState(portalPos.below()).getBlock() instanceof SorceryPortalBlock) {
					bottomBlock = portalPos.below();
				}
				
				// Try to use a block next to the portal
				BlockPos savedPos = bottomBlock;
				for (BlockPos pos : new BlockPos[]{bottomBlock.north(), bottomBlock.south(), bottomBlock.east(), bottomBlock.west()}) {
					if (worldIn.isEmptyBlock(pos) && worldIn.isEmptyBlock(pos.above())) {
						savedPos = pos;
						break;
					}
				}
				attr.setSorceryPortalLocation(DimensionUtils.GetDimension(entityIn), new BlockPos(savedPos));
			}
			entityIn.changeDimension(entityIn.getServer().getLevel(NostrumDimensions.GetSorceryDimension()), NostrumSorceryDimension.DimensionEntryTeleporter.INSTANCE);
		} else {
			entityIn.changeDimension(entityIn.getServer().getLevel(Level.OVERWORLD), NostrumSorceryDimension.DimensionReturnTeleporter.INSTANCE);
		}
	}
	
	@Override
	protected boolean canTeleport(Level worldIn, BlockPos portalPos, Entity entityIn) {
		return entityIn instanceof Player;
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter worldIn) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
