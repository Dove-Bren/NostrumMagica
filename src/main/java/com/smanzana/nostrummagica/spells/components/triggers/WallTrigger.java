package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WallTrigger extends SpellTrigger {
	
	public class WallTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private World world;
		private Vec3d pos;
		private boolean northsouth;
		private float radius;
		private boolean continuous;
		
		private double minX;
		private double minZ;
		private double maxX;
		private double maxZ;
		
		private int aliveCycles;
		private boolean dead;
		private Map<EntityLivingBase, Integer> affected; // maps to time last effect visited
		
		public WallTriggerInstance(SpellState state, World world, Vec3d pos, boolean northsouth, float radius, boolean continuous) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.radius = radius;
			this.northsouth = northsouth;
			this.continuous = continuous;
			
			dead = false;
			affected = new HashMap<>();
			aliveCycles = 0;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			
			// Wall is 1x2x[radius]
//			final List<BlockPos> blocks = new ArrayList<>();
//			final MutableBlockPos cursor = new MutableBlockPos();
//			cursor.setPos(pos.x, pos.y, pos.z);
//			blocks.add(cursor.toImmutable());
//			for (int i = 0; i < radius; i++) {
//				cursor.move(northsouth ? EnumFacing.NORTH : EnumFacing.EAST);
//				blocks.add(cursor.toImmutable());
//			}
//			cursor.setPos(pos.x, pos.y, pos.z);
//			for (int i = 0; i < radius; i++) {
//				cursor.move(northsouth ? EnumFacing.SOUTH : EnumFacing.WEST);
//				blocks.add(cursor.toImmutable());
//			}
//			
//			NostrumMagica.playerListener.registerPosition(this, world, blocks);
			
			if (this.northsouth) {
				this.minX = Math.floor(pos.x) -.25;
				this.maxX = minX + 1.5;
				this.minZ = Math.floor(pos.z) - radius;
				this.maxZ = minZ + 1 + (radius * 2);
			} else {
				this.minX = Math.floor(pos.x) - radius;
				this.maxX = minX + 1 + (radius * 2);
				this.minZ = Math.floor(pos.z) -.25;
				this.maxZ = minZ + 1.5;
			}
			Vec3d adjustedCenter = new Vec3d(Math.floor(pos.x) + .5, pos.y, Math.floor(pos.z) + .5);
			NostrumMagica.playerListener.registerProximity(this, world, adjustedCenter, radius + .75);
			
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, TICK_RATE);
			
			doEffect();
			
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity, Object empty) {
			if (dead)
				return true;
			
			if (type == Event.TIME) {
				
				doEffect();
				
				aliveCycles++;
				if (aliveCycles >= NUM_TICKS) { // 20 seconds
					this.dead = true;
					return true;
				}
				
				return false;
			}
			
			
			// Else we've already been set. Check if actually inside wall, and then try to trigger
			if (entity.posX >= this.minX && entity.posX <= this.maxX
					&& entity.posZ >= this.minZ && entity.posZ <= this.maxZ
					&& entity.posY >= Math.floor(this.pos.y) && entity.posY <= Math.floor(this.pos.y) + 2) {
				if (visitEntity(entity)) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(entity),
							Lists.newArrayList(this.getState().getSelf()),
							null,
							null
							);
					this.trigger(data, true);
				}
			}
			return false;
		}
		
		protected void doEffect() {
			//int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
			//Vec3d velocity, boolean unused
			
			final double diffX = maxX - minX;
			final double diffZ = maxZ - minZ;
			for (int i = 0; i < radius + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						minX + NostrumMagica.rand.nextFloat() * diffX,
						Math.floor(pos.y),
						minZ + NostrumMagica.rand.nextFloat() * diffZ,
						0, // pos + posjitter
						40, 10, // lifetime + jitter
						new Vec3d(0, .05, 0), false
						).color(getState().getNextElement().getColor()));
			}
			
			
			
//			NostrumMagica.proxy.spawnEffect(world, new SpellComponentWrapper(instance()),
//					null, null, null, this.pos, new SpellComponentWrapper(getState().getNextElement()), false,
//					((northsouth ? 1000f : 0f) + radius) // encode northsouth bit in radius since radius should be small and integral
//					);
		}
		
		/**
		 * Check if entity should experirence effects.
		 * Also tracks time when returned true to slow down effects.
		 * @param entity
		 * @return
		 */
		protected boolean visitEntity(EntityLivingBase entity) {
			if (entity == null) {
				return false;
			}
			
			Integer last = affected.get(entity);
			if (last == null
					|| (continuous && last + 20 < entity.ticksExisted)
					) {
				affected.put(entity, entity.ticksExisted);
				return true;
			}
			return false;
		}
	}

	private static final String TRIGGER_KEY = "trigger_wall";
	private static WallTrigger instance = null;
	
	public static WallTrigger instance() {
		if (instance == null)
			instance = new WallTrigger();
		
		return instance;
	}
	
	private WallTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 50;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
				ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		// Get N/S or E/W from target positions
		final double dz = Math.abs(state.getCaster().posZ - pos.z);
		final double dx = Math.abs(state.getCaster().posX - pos.x);
		final boolean northsouth = dz < dx;
		
		return new WallTriggerInstance(state, world, pos,
				northsouth, Math.max(supportedFloats()[0], params.level), !params.flip);
	}

	@Override
	public String getDisplayName() {
		return "Wall";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Item.getItemFromBlock(Blocks.GLASS));
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {0f, 1f, 2f, 3f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.GLASS),
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.EMERALD)
				);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.wall.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.wall.float.name", (Object[]) null);
	}
	
}
