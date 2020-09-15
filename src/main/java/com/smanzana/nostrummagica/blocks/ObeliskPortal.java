package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ObeliskPortal extends TeleportationPortal {
	
	public static final String ID = "obelisk_portal";
	
	private static ObeliskPortal instance = null;
	public static ObeliskPortal instance() {
		if (instance == null)
			instance = new ObeliskPortal();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(ObeliskPortalTileEntity.class, "obelisk_portal");
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
	protected boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn) {
		// Specifically disallow EntityItems so that we can stuck suck up position crystals
		if (entityIn == null || entityIn instanceof EntityItem) {
			return false;
		}
		
		// Check if the obelisk can afford it.
		TileEntity te = worldIn.getTileEntity(portalPos.down());
		if (te != null && te instanceof NostrumObeliskEntity) {
			BlockPos target =  ((NostrumObeliskEntity) te).getCurrentTarget();
			if (target != null) {
				return ((NostrumObeliskEntity) te).deductForTeleport(target);
			}
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		pos = getMaster(state, pos); // find master
		
		IBlockState parentState = worldIn.getBlockState(pos.down());
		if (parentState != null && parentState.getBlock() instanceof NostrumObelisk) {
			parentState.getBlock().onBlockActivated(worldIn, pos.down(), parentState, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		}
		
		return true;
	}
	
	public static class ObeliskPortalTileEntity extends TeleportationPortalTileEntity {
		
		@Override
		public @Nullable BlockPos getTarget() {
			// Defer to obelisk below us
			TileEntity te = worldObj.getTileEntity(pos.down());
			if (te != null && te instanceof NostrumObeliskEntity) {
				BlockPos target = ((NostrumObeliskEntity) te).getCurrentTarget();
				if (target != null) {
					target = target.up(); // we 'target' the actual obelisk but don't want to tele them there!
				}
				return target;
			}
			
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public int getColor() {
			TileEntity te = worldObj.getTileEntity(pos.down());
			if (te != null && te instanceof NostrumObeliskEntity && ((NostrumObeliskEntity) te).hasOverride()) {
				return 0x0000FF50;
			}
			return 0x004000FF;
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
