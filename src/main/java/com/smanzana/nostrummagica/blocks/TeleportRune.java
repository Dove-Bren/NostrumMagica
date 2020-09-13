package com.smanzana.nostrummagica.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TeleportRune extends BlockContainer  {
	
	public static final String ID = "teleport_rune";
	protected static final AxisAlignedBB RUNE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
	
	private static TeleportRune instance = null;
	public static TeleportRune instance() {
		if (instance == null) {
			instance = new TeleportRune();
		}
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(TeleportRuneTileEntity.class, "teleport_rune");;
	}
	
	public TeleportRune() {
		super(Material.CIRCUITS, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(5.0f);
		this.setResistance(5.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setTickRandomly(true);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return NULL_AABB;
		//return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return RUNE_AABB;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	protected void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn) {
		TileEntity te = worldIn.getTileEntity(portalPos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		BlockPos offset = ent.getOffset();
		if (offset == null) {
			return;
		}
		
		BlockPos target = portalPos.add(offset);
		entityIn.lastTickPosX = entityIn.prevPosX = target.getX() + .5;
		entityIn.lastTickPosY = entityIn.prevPosY = target.getY() + .005;
		entityIn.lastTickPosZ = entityIn.prevPosZ = target.getZ() + .5;
		
		if (!worldIn.isRemote) {
			entityIn.setPositionAndUpdate(target.getX() + .5, target.getY() + .005, target.getZ() + .5);

			double dx = target.getX() + .5;
			double dy = target.getY() + 1;
			double dz = target.getZ() + .5;
			for (int i = 0; i < 10; i++) {
				
				((WorldServer) worldIn).spawnParticle(EnumParticleTypes.DRAGON_BREATH,
						dx,
						dy,
						dz,
						10,
						.25,
						.6,
						.25,
						.1,
						new int[0]);
			}
			NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, dx, dy, dz);
		}
	}
	
	// Note: Just doing a dumb little static map cause we don't really care to persist portal cooldowns. Just
	// There to be nice to players.
	private static final Map<UUID, Integer> EntityTeleportCharge = new HashMap<>();
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 2;
	public static final int TELEPORT_RANGE = 32;
	
	private static boolean DumbIntegratedGuard = false;
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		Integer charge = EntityTeleportCharge.get(entityIn.getUniqueID());
		if (charge == null) {
			charge = 0;
		}
		
		if (worldIn.isRemote && entityIn == NostrumMagica.proxy.getPlayer() && ((!DumbIntegratedGuard && charge == 0) || (DumbIntegratedGuard && charge == 2))) {
			entityIn.playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
		}
		
		if (!DumbIntegratedGuard) {
			charge += 2;
			DumbIntegratedGuard = true;
		}
		
		if (charge > TELEPORT_CHARGE_TIME * 20) {
			EntityTeleportCharge.put(entityIn.getUniqueID(), -(TELEPORT_CHARGE_TIME * 20));
			if (!worldIn.isRemote) {
				this.teleportEntity(worldIn, pos, entityIn);
			}
		} else {
			EntityTeleportCharge.put(entityIn.getUniqueID(), charge);
			if (worldIn.isRemote && charge >= 0) {
				int count = (charge / 20) / TELEPORT_CHARGE_TIME;
				for (int i = 0; i < count + 1; i++) {
					double dx = pos.getX() + .5;
					double dy = pos.getY() + .5;
					double dz = pos.getZ() + .5;
					
					double mx = .25 * (NostrumMagica.rand.nextFloat() - .5f);
					double my = .5 * (NostrumMagica.rand.nextFloat() - .5f);
					double mz = .25 * (NostrumMagica.rand.nextFloat() - .5f);
					worldIn.spawnParticle(EnumParticleTypes.DRAGON_BREATH, dx + mx, dy, dz + mz, mx / 3, my, mz / 3, new int[0]);
				}
			}
		}
	}
	
	public static void tick() {
		Iterator<UUID> it = EntityTeleportCharge.keySet().iterator();
		while (it.hasNext()) {
			UUID key = it.next();
			Integer charge = EntityTeleportCharge.get(key);
			if (charge != null && charge > 0) {
				charge--;
			}
			
			if (charge == null || charge == 0) {
				it.remove();
			} else {
				EntityTeleportCharge.put(key, charge);
			}
		}
		DumbIntegratedGuard = false;
	}
	
	public static void resetTimers() {
		EntityTeleportCharge.clear();
	}
	
	public static int getRemainingCharge(Entity ent) {
		Integer charge = EntityTeleportCharge.get(ent.getPersistentID());
		return TELEPORT_CHARGE_TIME - (charge == null ? 0 : charge) * 20; 
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		
		if (heldItem == null || !(heldItem.getItem() instanceof PositionCrystal)) {
			return false;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return true;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
		if (heldPos == null) {
			return true;
		}
		
		if (!playerIn.isCreative()) {
			// 1) has to be another teleport rune there, and 2) has to be within X blocks
			if (!NostrumMagica.isBlockLoaded(worldIn, heldPos)) {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.teleportrune.unloaded"));
				return true;
			}
			
			IBlockState targetState = worldIn.getBlockState(heldPos);
			if (targetState == null || !(targetState.getBlock() instanceof TeleportRune)) {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.teleportrune.norune"));
				return true;
			}
			
			int dist = Math.abs(heldPos.getX() - pos.getX())
					+ Math.abs(heldPos.getY() - pos.getY())
					+ Math.abs(heldPos.getZ() - pos.getZ());
			
			if (dist > TELEPORT_RANGE) {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.teleportrune.toofar"));
				return true;
			}
		}
		
		IBlockState targetState = worldIn.getBlockState(heldPos);
		if (targetState != null && targetState.getBlock() instanceof TeleportRune) {
			;
		} else {
			heldPos = heldPos.up();
		}
		
		ent.setTargetPosition(heldPos);
		
		// If creative, can target tele tiles that are pointing to other ones. But, if it's not pointing anywhere, we'll conveniently hook them up.
		// Non-creative placement forces them to be linked to eachother, though.
		boolean shouldLink = true;
		if (playerIn.isCreative()) {
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				shouldLink = (otherEnt.getOffset() == null);
			}
		}
		
		if (shouldLink) {
			BlockPos oldOffset = null;
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				oldOffset = otherEnt.getOffset();
				otherEnt.setTargetPosition(pos);
			}
			
			if (oldOffset != null && !playerIn.isCreative()) {
				// Unlink old one, too!
				otherTE = worldIn.getTileEntity(heldPos.add(oldOffset));
				if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
					TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
					otherEnt.setTargetPosition(null);
				}
			}
		}
		
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TeleportRuneTileEntity();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		if (ent.getOffset() != null) {
			double dx = pos.getX() + .5;
			double dy = pos.getY() + .1;
			double dz = pos.getZ() + .5;
			
			double mx = 1 * (rand.nextFloat() - .5f);
			double mz = 1 * (rand.nextFloat() - .5f);
			
			worldIn.spawnParticle(EnumParticleTypes.PORTAL, dx + mx, dy, dz + mz, mx / 3, 0.0D, mz / 3, new int[0]);
		}
	}
	
	public static class TeleportRuneTileEntity extends TileEntity {
		
		private static final String NBT_OFFSET = "offset";
		
		private BlockPos teleOffset = null;
		
		public TeleportRuneTileEntity() {
			super();
		}
		
		/**
		 * Sets where to teleport to as an offset from the block itself.
		 * OFFSET, not target location. Went ahead and used ints here to make it more obvious.
		 * @param offsetX
		 * @param offsetY
		 * @param offsetZ
		 */
		public void setOffset(int offsetX, int offsetY, int offsetZ) {
			this.teleOffset = new BlockPos(offsetX, offsetY, offsetZ);
			flush();
		}
		
		public void setTargetPosition(BlockPos target) {
			if (target == null) {
				this.teleOffset = null;
			} else {
				this.teleOffset = target.subtract(pos);
			}
			flush();
		}
		
		public @Nullable BlockPos getOffset() {
			return teleOffset;
		}
		
		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
		}

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}
		
		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			super.onDataPacket(net, pkt);
			handleUpdateTag(pkt.getNbtCompound());
		}
		
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			super.writeToNBT(compound);
			
			if (teleOffset != null) {
				compound.setLong(NBT_OFFSET, teleOffset.toLong());
			}
			
			return compound;
		}
		
		public void readFromNBT(NBTTagCompound compound) {
			super.readFromNBT(compound);
			
			teleOffset = null;
			if (compound.hasKey(NBT_OFFSET, NBT.TAG_LONG)) {
				teleOffset = BlockPos.fromLong(compound.getLong(NBT_OFFSET));
			}
		}
		
		protected void flush() {
			if (worldObj != null && !worldObj.isRemote) {
				IBlockState state = worldObj.getBlockState(pos);
				worldObj.notifyBlockUpdate(pos, state, state, 2);
			}
		}
	}
}
