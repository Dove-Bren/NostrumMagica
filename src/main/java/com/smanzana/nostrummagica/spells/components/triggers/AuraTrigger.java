package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AuraTrigger extends TriggerAreaTrigger {
	
	public class AuraTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private LivingEntity origin;
		private float radius;
		private World world;
		private boolean includeAllies;
		
		private int aliveCycles;
		private boolean dead;
		private Map<LivingEntity, Integer> affected;
		
		public AuraTriggerInstance(SpellState state, World world, LivingEntity entity, float radius, boolean includeAllies) {
			super(state);
			this.radius = radius;
			this.origin = entity;
			this.world = world;
			this.includeAllies = includeAllies;
			
			dead = false;
			aliveCycles = 0;
			affected = new HashMap<>();
		}
		
		@Override
		public void init(LivingEntity caster) {
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, TICK_RATE);
			
			doEffect();
		}
		
		protected boolean canAffect(LivingEntity entity) {
			return entity != null
					&& !entity.isDead
					&& (
							includeAllies
							|| !NostrumMagica.IsSameTeam(entity, origin)
							);
		}
		
		protected boolean isInArea(LivingEntity entity) {
			return origin.getDistance(entity) <= radius;
		}

		protected void doEffect() {
			if (origin != null && !origin.isDead)
			for (int i = 0; i < radius + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						origin.posX,
						origin.posY + (origin.getEyeHeight() / 2), // technically correct but visually sucky cause 50% will be underground
						origin.posZ,
						.1,
						30, 0, // lifetime + jitter
						Vec3d.ZERO, (new Vec3d(.2, .2, .2)).scale(radius / 4)
						).color(getState().getNextElement().getColor())
						);
				NostrumParticles.LIGHTNING_STATIC.spawn(world, new SpawnParams(
						2,
						origin.posX,
						origin.posY + (origin.getEyeHeight() / 2), // technically correct but visually sucky cause 50% will be underground
						origin.posZ,
						radius,
						20, 0, // lifetime + jitter
						new Vec3d(0, -.025, 0), new Vec3d(0, .05, 0)
						).color(getState().getNextElement().getColor()));
			}
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object empty) {
			if (dead)
				return true;
			
			if (type == Event.TIME) {
				
				if (origin.isDead || origin.world.provider.getDimension() != world.provider.getDimension()) {
					this.dead = true;
					return true;
				}
				
				doEffect();
				
				aliveCycles++;
				if (aliveCycles >= NUM_TICKS) { // 20 seconds
					this.dead = true;
					return true;
				}
				
				// Check all entities in the world
				for (LivingEntity e : world.getEntities(LivingEntity.class, (e) -> {return canAffect(e) && isInArea(e);})) {
					if (visitEntity(e)) {
						TriggerData data = new TriggerData(
								Lists.newArrayList(e),
								Lists.newArrayList(this.getState().getSelf()),
								null,
								null
								);
						this.trigger(data, true);
					}
				}
				
				return false;
			}
			
			return false;
		}
		
		/**
		 * Check if entity should experirence effects.
		 * Also tracks time when returned true to slow down effects.
		 * @param entity
		 * @return
		 */
		protected boolean visitEntity(LivingEntity entity) {
			if (entity == null || entity.isDead) {
				return false;
			}
			
			Integer last = affected.get(entity);
			if (last == null
					|| (last + 40 < entity.ticksExisted)
					) {
				affected.put(entity, entity.ticksExisted);
				return true;
			}
			return false;
		}
	}

	private static final String TRIGGER_KEY = "trigger_aura";
	private static AuraTrigger instance = null;
	
	public static AuraTrigger instance() {
		if (instance == null)
			instance = new AuraTrigger();
		
		return instance;
	}
	
	private AuraTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 300;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1),
				ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
				ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1),
				ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new AuraTriggerInstance(state, world, state.getSelf(),
				Math.max(supportedFloats()[0], params.level),
				params.flip);
	}

	@Override
	public String getDisplayName() {
		return "Aura";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.GUNPOWDER);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {2f, 3f, 5f, 10f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DRAGON_BREATH),
				NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)
				);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.aura.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.aura.float.name", (Object[]) null);
	}
	
}
