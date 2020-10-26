package com.smanzana.nostrummagica.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class NostrumPortal extends Block  {
	
	protected static final PropertyBool MASTER = PropertyBool.create("master");
	
	public NostrumPortal() {
		super(Material.LEAVES, MapColor.OBSIDIAN);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setTickRandomly(true);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, false));
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
		return 14;
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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(MASTER, (meta & 1) == 1);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(MASTER) ? 1 : 0;
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	protected static BlockPos getPaired(IBlockState state, BlockPos pos) {
		return pos.offset(state.getValue(MASTER) ? EnumFacing.UP : EnumFacing.DOWN);
	}
	
	protected static BlockPos getMaster(IBlockState state, BlockPos pos) {
		if (!isMaster(state)) {
			pos = getPaired(state, pos);
		}
		
		return pos;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	public IBlockState getSlaveState() {
		return this.getDefaultState().withProperty(MASTER, false);
	}


	public IBlockState getMaster() {
		return this.getDefaultState().withProperty(MASTER, true);
	}
	
	public static boolean isMaster(IBlockState state) {
		return state.getValue(MASTER);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		this.destroy(world, pos, state);
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		if (!worldIn.isAirBlock(pos.up()))
			return false;
		
		if (worldIn.getTileEntity(pos) != null)
			return false;
		
		if (worldIn.getTileEntity(pos.up()) != null)
			return false;
		
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return getMaster();
	}
	
	public void createPaired(World worldIn, BlockPos pos) {
		worldIn.setBlockState(pos.up(), getSlaveState());
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		createPaired(worldIn, pos);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!isMaster(stateIn)) {
			return;
		}
		
		if (rand.nextFloat() < .01f) {
			worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, NostrumMagicaSounds.PORTAL.getEvent(), SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
		}
		
		// Create particles
		for (int i = 0; i < 5; i++) {
	    	final float horAngle = rand.nextFloat() * (float) (2 * Math.PI);
	    	final float verAngle = (rand.nextFloat()) * (float) (2 * Math.PI);
	    	final float dist = rand.nextFloat() + 2f;
	    	
	    	final double dx = Math.cos(horAngle) * dist;
	    	final double dz = Math.sin(horAngle) * dist;
	    	final double dy = Math.sin(verAngle) * dist;
	    	final double mx = rand.nextFloat() - .5;
	    	final double mz = rand.nextFloat() - .5;
	    	
	    	worldIn.spawnParticle(EnumParticleTypes.SUSPENDED_DEPTH,
	    			pos.getX() + .5 + dx, pos.getY() + 1 + dy, pos.getZ() + .5 + dz,
	    			mx, 0, mz,
	    			new int[0]);
		}

        
    	for (int i = 0; i < 5; i++) {
        	final float horAngle = rand.nextFloat() * (float) (2 * Math.PI);
        	final float verAngle = (rand.nextFloat()) * (float) (2 * Math.PI);
        	final float dist = 1f;
        	
        	final double dx = Math.cos(horAngle) * dist;
	    	final double dz = Math.sin(horAngle) * dist;
	    	final double dy = Math.sin(verAngle) * dist;
	    	final double mx = rand.nextFloat() - .5;
	    	final double mz = rand.nextFloat() - .5;
	    	
	    	worldIn.spawnParticle(EnumParticleTypes.SPELL_WITCH,
	    			pos.getX() + .5 + dx, pos.getY() + 1 + dy, pos.getZ() + .5 + dz,
	    			mx, 0, mz,
	    			new int[0]);
    	}
	}
	
	protected abstract boolean canTeleport(World worldIn, BlockPos portalPos, Entity entityIn);
	
	protected abstract void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn);
	
	// Note: Just doing a dumb little static map cause we don't really care to persist portal cooldowns. Just
	// There to be nice to players.
	private static final Map<UUID, Integer> EntityTeleportCharge = new HashMap<>();
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 3;
	
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
		
		
		if (charge > TELEPORT_CHARGE_TIME * 20 && this.canTeleport(worldIn, pos, entityIn)) {
			EntityTeleportCharge.put(entityIn.getUniqueID(), -(TELEPORT_CHARGE_TIME * 5 * 20));
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
			if (charge != null) {
				if (charge > 0) {
					charge--;
				} else if (charge < 0) {
					charge++;
				}
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
	
	public static int getCooldownTime(Entity ent) {
		Integer charge = EntityTeleportCharge.get(ent.getPersistentID());
		return (charge == null || charge >= 0 ? 0 : -charge);
	}
	
//	// Note: Just doing a dumb little static map cause we don't really care to persist portal cooldowns. Just
//	// There to be nice to players.
//	private static final Map<UUID, Long> EntityTeleportTimes = new HashMap<>();
//	
//	// How long entities must wait before they can teleport again after using a portal, in seconds.
//	public static final int TELEPORT_COOLDOWN = 10;
//	
//	@Override
//	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
//
//		if (!worldIn.isRemote) {
//			// Check if player teleported too recently
//			Long lastTime = EntityTeleportTimes.get(entityIn.getPersistentID());
//			final long now = System.currentTimeMillis();
//			if (lastTime != null && (now - lastTime) < (1000 * TELEPORT_COOLDOWN)) {
//				// Teleported too recently
//				return;
//			}
//			
//			// Get master block
//			pos = getMaster(state, pos);
//			
//			if (canTeleport(worldIn, pos, entityIn)) {
//				EntityTeleportTimes.put(entityIn.getPersistentID(), now);
//				this.teleportEntity(worldIn, pos, entityIn);
//			}
//		}
//	}
//	
//	public static void resetTimers() {
//		EntityTeleportTimes.clear();
//	}
//	
//	public static int getRemainingCooldown(Entity ent) {
//		Long lastTime = EntityTeleportTimes.get(ent.getPersistentID());
//		return lastTime == null ? 0 : ((1000 * TELEPORT_COOLDOWN) - (int) (System.currentTimeMillis() - lastTime)); 
//	}
	
	public static abstract class NostrumPortalTileEntityBase extends TileEntity {
		
		/**
		 * Return color the portal should be rendered as. Only 3 least-sig bytes used as 0RGB.
		 * @return
		 */
		@SideOnly(Side.CLIENT)
		public abstract int getColor();
		
		/**
		 * How long a full rotation period should take to perform. 0 or less means no rotating animation.
		 * This is in seconds.
		 * @return
		 */
		@SideOnly(Side.CLIENT)
		public abstract float getRotationPeriod();
		
		/**
		 * Opacity of the portal. This is expresses as 0 to 1.
		 * @return
		 */
		@SideOnly(Side.CLIENT)
		public abstract float getOpacity();
		
	}
	
}
