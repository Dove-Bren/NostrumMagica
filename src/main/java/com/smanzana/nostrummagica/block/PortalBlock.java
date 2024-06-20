package com.smanzana.nostrummagica.block;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.WeakHashSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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

public abstract class PortalBlock extends Block implements IPortalBlock  {
	
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
	
	@Override
	public boolean attemptTeleport(World world, BlockPos pos, BlockState state, Entity entity) {
		teleportEntity(world, pos, entity);
		if (entity instanceof PlayerEntity) {
			ServerEntityTeleportCharge.put(entity, -(TELEPORT_CHARGE_TIME * 5 * 20));
		}
		return true;
	}
	
	// Counter only used on client side to charge up and disable teleporting.
	// Goes from -N to X.
	// Starts at 0, and charges when entity is in contact with the portal. When it hits X (max charge time),
	// entity is teleported, and set to a negative number as a portal 'cooldown' before it can charge again.
	private static int ClientTeleportCharge = 0;
	private static boolean ClientTeleportTickMark = false; 
	
	// Server-side charge counter for entities.
	// For players, this isn't used except to check that player's aren't attempting to teleport too frequently.
	// For non-players, this acts like ClientTeleportCharge does.
	private static final Map<Entity, Integer> ServerEntityTeleportCharge = new WeakHashMap<>();
	private static final Set<Entity> ServerFrameEntities = new WeakHashSet<>();
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 3;
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		// This func is called many (4) times each frame (per logical side) for portals.
		// First is during entity updates on the player, which happens for both blocks since both are collided.
		// Second is when handling player movement.
		if (!worldIn.isRemote() && ServerFrameEntities.contains(entityIn)) {
			return;
		}
		
		if (worldIn.isRemote() && ClientTeleportTickMark) {
			return;
		}
		
		if (!this.canTeleport(worldIn, pos, entityIn)) {
			return;
		}
		
		boolean doTeleport = false;
		final int maxChargeTicks = TELEPORT_CHARGE_TIME * 20;
		final int cooldownTicks = -(TELEPORT_CHARGE_TIME * 5 * 20);
		if (worldIn.isRemote()) {
			// Clients manage their own entity.
			if (entityIn == NostrumMagica.instance.proxy.getPlayer()) {
				if (ClientTeleportCharge == 0) {
					// First frame of charging
					entityIn.playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
				}
				ClientTeleportCharge += 2; // + 2 because we decrement each tick, too
				if (ClientTeleportCharge >= maxChargeTicks) {
					ClientTeleportCharge = cooldownTicks;
					doTeleport = true;
				} else {
					int count = (ClientTeleportCharge / 20) / TELEPORT_CHARGE_TIME;
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
				ClientTeleportTickMark = true;
			}
		} else {
			// Servers don't manage player counting
			if (!(entityIn instanceof PlayerEntity)) {
				int charge = ServerEntityTeleportCharge.getOrDefault(entityIn, 0) + 2; // + 2 because we decrement each tick, too
				if (charge >= maxChargeTicks) {
					doTeleport = true;
					charge = cooldownTicks;
				}
				ServerEntityTeleportCharge.put(entityIn, charge);
			}
			ServerFrameEntities.add(entityIn);
		}
		
		if (doTeleport) {
			NostrumMagica.instance.proxy.attemptBlockTeleport(entityIn, pos);
		}
	}
	
	public static void clientTick() {
		if (ClientTeleportCharge < 0) {
			ClientTeleportCharge++;
		} else if (ClientTeleportCharge > 0) {
			ClientTeleportCharge--;
		}
		ClientTeleportTickMark = false;
	}
	
	public static void serverTick() {
		ServerFrameEntities.clear();
		if (ServerEntityTeleportCharge.isEmpty()) {
			return;
		}
		
		Iterator<Entity> it = ServerEntityTeleportCharge.keySet().iterator();
		while (it.hasNext()) {
			Entity ent = it.next();
			Integer charge = ServerEntityTeleportCharge.get(ent);
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
				ServerEntityTeleportCharge.put(ent, charge);
			}
		}
	}
	
	public static void resetTimers() {
		ServerEntityTeleportCharge.clear();
		ClientTeleportCharge = 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static int getRemainingCharge(Entity ent) {
		Integer charge = ClientTeleportCharge;
		return TELEPORT_CHARGE_TIME - (charge == null ? 0 : charge) * 20; 
	}

	@OnlyIn(Dist.CLIENT)
	public static int getCooldownTime(Entity ent) {
		Integer charge = ClientTeleportCharge;
		return (charge == null || charge >= 0 ? 0 : -charge);
	}
	
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
