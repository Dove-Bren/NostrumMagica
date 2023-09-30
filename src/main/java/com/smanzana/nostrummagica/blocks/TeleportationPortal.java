package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.TeleportationPortalTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
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
@SuppressWarnings("deprecation")
public class TeleportationPortal extends NostrumPortal implements ITileEntityProvider  {
	
	public static final String ID = "teleportation_portal";

	public TeleportationPortal() {
		this(Block.Properties.create(Material.LEAVES)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.lightValue(14)
				);
	}
	
	public TeleportationPortal(Block.Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean hasTileEntity() {
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
			BlockPos target = ent.getTarget();
			if (target != null) {
				
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				
					worldIn.getChunk(target);
					
					entityIn.lastTickPosX = entityIn.prevPosX = target.getX() + .5;
					entityIn.lastTickPosY = entityIn.prevPosY = target.getY() + .1;
					entityIn.lastTickPosZ = entityIn.prevPosZ = target.getZ() + .5;
					if (!worldIn.isRemote) {
						if (entityIn instanceof ServerPlayerEntity) {
							((ServerPlayerEntity) entityIn).connection.setPlayerLocation(target.getX() + .5, target.getY() + .1, target.getZ() + .5, entityIn.rotationYaw, entityIn.rotationPitch);
						} else {
							entityIn.setPositionAndUpdate(target.getX() + .5, target.getY() + .1, target.getZ() + .5);
						}
						entityIn.fallDistance = 0;
						//entityIn.velocityChanged = true;
						((ServerWorld) worldIn).updateEntity(entityIn);
							
						// effects, sound, etc.
						double x = target.getX() + .5;
						double y = target.getY() + 1.4;
						double z = target.getZ() + .5;
						NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, x, y, z);
						((ServerWorld) worldIn).addParticle(ParticleTypes.DRAGON_BREATH,
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
				}, 1, 0);
			}
		}
	}
	
	@Override
	protected boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn) {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
