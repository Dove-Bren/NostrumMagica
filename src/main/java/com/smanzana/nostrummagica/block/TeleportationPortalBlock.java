package com.smanzana.nostrummagica.block;

import com.smanzana.autodungeons.util.DimensionUtils;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TeleportationPortalTileEntity;
import com.smanzana.nostrummagica.util.Location;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Portal that takes players to different spots in the same dimension
 * @author Skyler
 *
 */
public class TeleportationPortalBlock extends PortalBlock  {
	
	public static final String ID = "teleportation_portal";

	public TeleportationPortalBlock() {
		this(Block.Properties.create(Material.LEAVES)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.setLightLevel((state) -> 14)
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
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
	protected void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn) {
		entityIn.stopRiding();
		entityIn.removePassengers();
		
		if (!entityIn.getPassengers().isEmpty()) {
			return;
		}
		
		if (entityIn.isPassenger()) {
			return;
		}
		
		TileEntity te = worldIn.getTileEntity(portalPos);
		if (te != null && te instanceof TeleportationPortalTileEntity) {
			TeleportationPortalTileEntity ent = (TeleportationPortalTileEntity) te;
			Location target = ent.getTarget();
			if (target != null && DimensionUtils.InDimension(entityIn, target.getDimension())) {
				
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				
					NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entityIn, target.getPos().getX() + .5, target.getPos().getY() + .1, target.getPos().getZ() + .5, null);
					if (!event.isCanceled()) {
						worldIn.getChunk(target.getPos());
						
						entityIn.lastTickPosX = entityIn.prevPosX = target.getPos().getX() + .5;
						entityIn.lastTickPosY = entityIn.prevPosY = target.getPos().getY() + .1;
						entityIn.lastTickPosZ = entityIn.prevPosZ = target.getPos().getZ() + .5;
						if (!worldIn.isRemote) {
						
						
							if (entityIn instanceof ServerPlayerEntity) {
								((ServerPlayerEntity) entityIn).connection.setPlayerLocation(event.getTargetX(), event.getTargetY(), event.getTargetZ(), entityIn.rotationYaw, entityIn.rotationPitch);
							} else {
								entityIn.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
							}
							entityIn.fallDistance = 0;
							//entityIn.velocityChanged = true;
							((ServerWorld) worldIn).updateEntity(entityIn);
								
							// effects, sound, etc.
							double x = event.getTargetX() + .5;
							double y = event.getTargetY() + 1.4;
							double z = event.getTargetZ() + .5;
							NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, x, y, z);
							((ServerWorld) worldIn).spawnParticle(ParticleTypes.DRAGON_BREATH,
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
	protected boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn) {
		return true;
	}
}
