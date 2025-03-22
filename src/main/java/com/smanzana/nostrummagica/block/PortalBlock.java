package com.smanzana.nostrummagica.block;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.WeakHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PortalBlock extends Block implements IPortalBlock, EntityBlock  {
	
	protected static final BooleanProperty MASTER = BooleanProperty.create("master");
	
	public PortalBlock(Block.Properties properties) {
		super(properties.noCollission());
		this.registerDefaultState(this.stateDefinition.any().setValue(MASTER, false));
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }
	
	@Override
	public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MASTER);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		world.removeBlock(getPaired(state, pos), false);
	}
	
	protected static BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.relative(state.getValue(MASTER) ? Direction.UP : Direction.DOWN);
	}
	
	protected static BlockPos getMaster(BlockState state, BlockPos pos) {
		if (!isMaster(state)) {
			pos = getPaired(state, pos);
		}
		
		return pos;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	public BlockState getSlaveState() {
		return this.defaultBlockState().setValue(MASTER, false);
	}


	public BlockState getMaster() {
		return this.defaultBlockState().setValue(MASTER, true);
	}
	
	public static boolean isMaster(BlockState state) {
		return state.getValue(MASTER);
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		if (!worldIn.isEmptyBlock(pos.above()))
			return false;
		
		if (worldIn.getBlockEntity(pos) != null)
			return false;
		
		if (worldIn.getBlockEntity(pos.above()) != null)
			return false;
		
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return getMaster();
	}
	
	public void createPaired(Level worldIn, BlockPos pos) {
		worldIn.setBlockAndUpdate(pos.above(), getSlaveState());
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		createPaired(worldIn, pos);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (!isMaster(stateIn)) {
			return;
		}
		
		if (rand.nextFloat() < .01f) {
			worldIn.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, NostrumMagicaSounds.PORTAL.getEvent(), SoundSource.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
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
	
	protected abstract boolean canTeleport(Level worldIn, BlockPos portalPos, Entity entityIn);
	
	protected abstract void teleportEntity(Level worldIn, BlockPos portalPos, Entity entityIn);
	
	@Override
	public boolean attemptTeleport(Level world, BlockPos pos, BlockState state, Entity entity) {
		teleportEntity(world, pos, entity);
		if (entity instanceof Player) {
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
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		// This func is called many (4) times each frame (per logical side) for portals.
		// First is during entity updates on the player, which happens for both blocks since both are collided.
		// Second is when handling player movement.
		if (!worldIn.isClientSide() && ServerFrameEntities.contains(entityIn)) {
			return;
		}
		
		if (worldIn.isClientSide() && ClientTeleportTickMark) {
			return;
		}
		
		if (!this.canTeleport(worldIn, pos, entityIn)) {
			return;
		}
		
		boolean doTeleport = false;
		final int maxChargeTicks = TELEPORT_CHARGE_TIME * 20;
		final int cooldownTicks = -(TELEPORT_CHARGE_TIME * 5 * 20);
		if (worldIn.isClientSide()) {
			// Clients manage their own entity.
			if (entityIn == NostrumMagica.instance.proxy.getPlayer()) {
				if (ClientTeleportCharge == 0) {
					// First frame of charging
					entityIn.playSound(SoundEvents.PORTAL_TRIGGER, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
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
			if (!(entityIn instanceof Player)) {
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
				it.discard();
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
	
	public static abstract class NostrumPortalTileEntityBase extends BlockEntity {
		
		public NostrumPortalTileEntityBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
			super(tileEntityTypeIn, pos, state);
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
