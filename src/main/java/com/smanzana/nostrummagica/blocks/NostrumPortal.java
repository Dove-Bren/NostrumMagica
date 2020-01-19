package com.smanzana.nostrummagica.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
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
	
	private BlockPos getPaired(IBlockState state, BlockPos pos) {
		return pos.offset(state.getValue(MASTER) ? EnumFacing.UP : EnumFacing.DOWN);
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
	
	public boolean isMaster(IBlockState state) {
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
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		
		worldIn.setBlockState(pos.up(), getSlaveState());
	}
	
	protected abstract void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn);
	
	// Note: Just doing a dumb little static map cause we don't really care to persist portal cooldowns. Just
	// There to be nice to players.
	private static final Map<UUID, Long> EntityTeleportTimes = new HashMap<>();
	
	// How long entities must wait before they can teleport again after using a portal, in seconds.
	public static final int TELEPORT_COOLDOWN = 10;
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		// Check if player teleported too recently
		if (worldIn.isRemote) {
			return;
		}
		
		Long lastTime = EntityTeleportTimes.get(entityIn.getPersistentID());
		final long now = worldIn.getTotalWorldTime();
		if (lastTime != null && (now - lastTime) < (20 * TELEPORT_COOLDOWN)) {
			// Teleported too recently
			return;
		}
		
		EntityTeleportTimes.put(entityIn.getPersistentID(), now);
		this.teleportEntity(worldIn, pos, entityIn);
	}
	
	public static void resetTimers() {
		EntityTeleportTimes.clear();
	}
	
	public static int getRemainingCooldown(Entity ent) {
		Long lastTime = EntityTeleportTimes.get(ent.getPersistentID());
		return lastTime == null ? 0 : ((20 * TELEPORT_COOLDOWN) - (int) (ent.worldObj.getTotalWorldTime() - lastTime)); 
	}
	
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
