package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	
	public static void init() {
		GameRegistry.registerTileEntity(SorceryPortalTileEntity.class, "sorcery_portal");
	}
	
	public SorceryPortal() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		IBlockState state = this.getStateFromMeta(meta);
		if (isMaster(state)) {
			return new SorceryPortalTileEntity();
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
		System.out.println("Teleport!");
		entityIn.setPortal(entityIn.getPosition());
		if (worldIn.provider.getDimension() != ModConfig.config.sorceryDimensionIndex()) {
			entityIn.changeDimension(ModConfig.config.sorceryDimensionIndex());
		} else {
			entityIn.changeDimension(0);
		}
	}
	
	public static class SorceryPortalTileEntity extends NostrumPortal.NostrumPortalTileEntityBase  {

		@SideOnly(Side.CLIENT)
		@Override
		public int getColor() {
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (NostrumPortal.getRemainingCooldown(player) > 0) {
				return 0x00A00050;
			}
			return 0x00500050;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public float getRotationPeriod() {
			return 3;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public float getOpacity() {
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (NostrumPortal.getRemainingCooldown(player) > 0) {
				return 0.5f;
			}
			return .9f;
		}
		
		
		
	}
	
}