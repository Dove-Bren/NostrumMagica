package com.smanzana.nostrummagica.block;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PortalBlock extends Block  {
	
	protected static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public PortalBlock(Block.Properties properties) {
		super(properties.doesNotBlockMovement());
		this.setDefaultState(this.stateContainer.getBaseState().with(MASTER, false));
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return false;
    }
	
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public VoxelShape getShape(BlockState blockState, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.fullCube();
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		world.removeBlock(getPaired(state, pos), false);
	}
	
	protected static BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.offset(state.get(MASTER) ? Direction.UP : Direction.DOWN);
	}
	
	protected static BlockPos getMaster(BlockState state, BlockPos pos) {
		if (!isMaster(state)) {
			pos = getPaired(state, pos);
		}
		
		return pos;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	public BlockState getSlaveState() {
		return this.getDefaultState().with(MASTER, false);
	}


	public BlockState getMaster() {
		return this.getDefaultState().with(MASTER, true);
	}
	
	public static boolean isMaster(BlockState state) {
		return state.get(MASTER);
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeTileEntity(pos);
		}
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		if (!worldIn.isAirBlock(pos.up()))
			return false;
		
		if (worldIn.getTileEntity(pos) != null)
			return false;
		
		if (worldIn.getTileEntity(pos.up()) != null)
			return false;
		
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getMaster();
	}
	
	public void createPaired(World worldIn, BlockPos pos) {
		worldIn.setBlockState(pos.up(), getSlaveState());
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		createPaired(worldIn, pos);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
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
	    	
	    	worldIn.addParticle(ParticleTypes.MYCELIUM,
	    			pos.getX() + .5 + dx, pos.getY() + 1 + dy, pos.getZ() + .5 + dz,
	    			mx, 0, mz);
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
	    	
	    	worldIn.addParticle(ParticleTypes.WITCH,
	    			pos.getX() + .5 + dx, pos.getY() + 1 + dy, pos.getZ() + .5 + dz,
	    			mx, 0, mz);
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
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		Integer charge = EntityTeleportCharge.get(entityIn.getUniqueID());
		if (charge == null) {
			charge = 0;
		}
		
		if (worldIn.isRemote && entityIn == NostrumMagica.instance.proxy.getPlayer() && ((!DumbIntegratedGuard && charge == 0) || (DumbIntegratedGuard && charge == 2))) {
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
					worldIn.addParticle(ParticleTypes.DRAGON_BREATH, dx + mx, dy, dz + mz, mx / 3, my, mz / 3);
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
		Integer charge = EntityTeleportCharge.get(ent.getUniqueID());
		return TELEPORT_CHARGE_TIME - (charge == null ? 0 : charge) * 20; 
	}
	
	public static int getCooldownTime(Entity ent) {
		Integer charge = EntityTeleportCharge.get(ent.getUniqueID());
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
//	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, BlockState state, Entity entityIn) {
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
		
		public NostrumPortalTileEntityBase(TileEntityType<?> tileEntityTypeIn) {
			super(tileEntityTypeIn);
		}

		/**
		 * Return color the portal should be rendered as. Only 3 least-sig bytes used as 0RGB.
		 * @return
		 */
		@OnlyIn(Dist.CLIENT)
		public abstract int getColor();
		
		/**
		 * How long a full rotation period should take to perform. 0 or less means no rotating animation.
		 * This is in seconds.
		 * @return
		 */
		@OnlyIn(Dist.CLIENT)
		public abstract float getRotationPeriod();
		
		/**
		 * Opacity of the portal. This is expresses as 0 to 1.
		 * @return
		 */
		@OnlyIn(Dist.CLIENT)
		public abstract float getOpacity();
		
	}
	
}
