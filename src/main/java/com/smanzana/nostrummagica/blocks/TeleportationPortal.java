package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.TeleportationPortalTileEntity;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Portal that takes players to different spots in the same dimension
 * @author Skyler
 *
 */
public class TeleportationPortal extends NostrumPortal implements ITileEntityProvider  {
	
	public static final String ID = "teleportation_portal";
	
	private static TeleportationPortal instance = null;
	public static TeleportationPortal instance() {
		if (instance == null)
			instance = new TeleportationPortal();
		
		return instance;
	}
	
	public TeleportationPortal() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		IBlockState state = this.getStateFromMeta(meta);
		if (isMaster(state)) {
			return new TeleportationPortalTileEntity();
		}
		
		return null;
	}
	
//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
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
		
		TileEntity te = worldIn.getTileEntity(portalPos);
		if (te != null && te instanceof TeleportationPortalTileEntity) {
			TeleportationPortalTileEntity ent = (TeleportationPortalTileEntity) te;
			BlockPos target = ent.getTarget();
			if (target != null) {
				
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				
					worldIn.getChunkFromBlockCoords(target);
					
					entityIn.lastTickPosX = entityIn.prevPosX = target.getX() + .5;
					entityIn.lastTickPosY = entityIn.prevPosY = target.getY() + .1;
					entityIn.lastTickPosZ = entityIn.prevPosZ = target.getZ() + .5;
					if (!worldIn.isRemote) {
						if (entityIn instanceof EntityPlayerMP) {
							((EntityPlayerMP) entityIn).connection.setPlayerLocation(target.getX() + .5, target.getY() + .1, target.getZ() + .5, entityIn.rotationYaw, entityIn.rotationPitch);
						} else {
							entityIn.setPositionAndUpdate(target.getX() + .5, target.getY() + .1, target.getZ() + .5);
						}
						entityIn.fallDistance = 0;
						//entityIn.velocityChanged = true;
						worldIn.updateEntityWithOptionalForce(entityIn, true);
							
						// effects, sound, etc.
						double x = target.getX() + .5;
						double y = target.getY() + 1.4;
						double z = target.getZ() + .5;
						NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, x, y, z);
						((WorldServer) worldIn).spawnParticle(EnumParticleTypes.DRAGON_BREATH,
								x,
								y,
								z,
								50,
								.3,
								.5,
								.3,
								.2,
								new int[0]);
						
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
	
}
