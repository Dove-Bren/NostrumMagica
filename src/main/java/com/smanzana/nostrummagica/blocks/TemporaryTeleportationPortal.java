package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Teleportation Portal with a finite lifetime
 * @author Skyler
 *
 */
public class TemporaryTeleportationPortal extends TeleportationPortal  {
	
	public static final String ID = "limited_teleportation_portal";
	
	private static TemporaryTeleportationPortal instance = null;
	public static TemporaryTeleportationPortal instance() {
		if (instance == null)
			instance = new TemporaryTeleportationPortal();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(TemporaryPortalTileEntity.class, "limited_teleportation_portal");
	}
	
	public TemporaryTeleportationPortal() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if (worldIn.isRemote) {
			IBlockState state = this.getStateFromMeta(meta);
			if (isMaster(state)) {
				return new TemporaryPortalTileEntity();
			}
		}
		
		return null;
	}
	
	protected static void spawnPortal(World worldIn, BlockPos portalMaster, BlockPos target, int duration) {
		TemporaryPortalTileEntity te = new TemporaryPortalTileEntity(target, worldIn.getTotalWorldTime() + duration);
		worldIn.setTileEntity(portalMaster, te);
	}
	
	public static void spawn(World world, BlockPos at, BlockPos target, int duration) {
		IBlockState state = instance().getMaster();
		world.setBlockState(at, state);
		instance().createPaired(world, at);
		
		spawnPortal(world, at, target, duration);
	}
	
	public static class TemporaryPortalTileEntity extends TeleportationPortalTileEntity implements ITickable  {

		private long endticks;
		
		public TemporaryPortalTileEntity() {
			super();
		}
		
		public TemporaryPortalTileEntity(BlockPos target, long endticks) {
			super(target);
			this.endticks = endticks;
			this.markDirty();
		}
		
		@Override
		public void update() {
			if (worldObj == null || worldObj.isRemote) {
				return;
			}
			
			if (worldObj.getTotalWorldTime() >= this.endticks) {
				worldObj.setBlockToAir(pos);
			}
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public int getColor() {
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (NostrumPortal.getRemainingCooldown(player) > 0) {
				return 0x00400000;
			}
			return 0x003030FF;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public float getRotationPeriod() {
			return 2;
		}

		@SideOnly(Side.CLIENT)
		@Override
		public float getOpacity() {
			float opacity = .9f;
			
			if (worldObj != null) {
				final long now =  worldObj.getTotalWorldTime();
				final long FadeTicks = 20 * 5;
				final long left = Math.max(0, endticks - now);
				if (left < FadeTicks) {
					opacity *= ((double) left / (double) FadeTicks);
				}
			}
			
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (NostrumPortal.getRemainingCooldown(player) > 0) {
				opacity *= 0.5f;
			}
			
			return opacity;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound compound) {
			super.readFromNBT(compound);
			
			endticks = compound.getLong("EXPIRE");
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setLong("EXPIRE", endticks);
			
			return nbt;
		}
	}
}
