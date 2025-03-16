package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TeleportationPortalTileEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Location;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * Portal that takes players to different spots in the same dimension
 * @author Skyler
 *
 */
public class TeleportationPortalBlock extends PortalBlock  {
	
	public static final String ID = "teleportation_portal";

	public TeleportationPortalBlock() {
		this(Block.Properties.of(Material.LEAVES)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.lightLevel((state) -> 14)
				);
	}
	
	public TeleportationPortalBlock(Block.Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		if (isMaster(state)) {
			return new TeleportationPortalTileEntity();
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
		
		BlockEntity te = worldIn.getBlockEntity(portalPos);
		if (te != null && te instanceof TeleportationPortalTileEntity) {
			TeleportationPortalTileEntity ent = (TeleportationPortalTileEntity) te;
			Location target = ent.getTarget();
			if (target != null && DimensionUtils.InDimension(entityIn, target.getDimension())) {
				
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				
					NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entityIn, target.getPos().getX() + .5, target.getPos().getY() + .1, target.getPos().getZ() + .5, null);
					if (!event.isCanceled()) {
						worldIn.getChunk(target.getPos());
						
						entityIn.xOld = entityIn.xo = target.getPos().getX() + .5;
						entityIn.yOld = entityIn.yo = target.getPos().getY() + .1;
						entityIn.zOld = entityIn.zo = target.getPos().getZ() + .5;
						if (!worldIn.isClientSide) {
						
						
							if (entityIn instanceof ServerPlayer) {
								((ServerPlayer) entityIn).connection.teleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), entityIn.yRot, entityIn.xRot);
							} else {
								entityIn.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
							}
							entityIn.fallDistance = 0;
							//entityIn.velocityChanged = true;
							((ServerLevel) worldIn).tickNonPassenger(entityIn);
								
							// effects, sound, etc.
							double x = event.getTargetX() + .5;
							double y = event.getTargetY() + 1.4;
							double z = event.getTargetZ() + .5;
							NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, x, y, z);
							((ServerLevel) worldIn).sendParticles(ParticleTypes.DRAGON_BREATH,
									x,
									y,
									z,
									50, // TODO count??
									.3,
									.5,
									.3,
									.2
									);
						}
						return true;
					}  else {
						return false;
					}
				}, 1, 0);
			}
		}
	}
	
	@Override
	protected boolean canTeleport(Level worldIn, BlockPos portalPos, Entity entityIn) {
		return true;
	}
}
