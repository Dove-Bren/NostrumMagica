package com.smanzana.nostrummagica.spells.components.shapes;

import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.utils.DimensionUtils;
import com.smanzana.nostrummagica.utils.Entities;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that can immediately be resolved.
 * For example, touch is resolve instantly.
 * @author Skyler
 *
 */
public class AuraShape extends AreaShape {

	public static class AuraTriggerInstance extends SpellShapeInstance implements IGenericListener {

		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private final LivingEntity origin;
		private final float radius;
		private final World world;
		private final boolean includeAllies;
		private final SpellCharacteristics characteristics;
		
		private int aliveCycles;
		private boolean dead;
		private final Map<LivingEntity, Integer> affected;
		
		public AuraTriggerInstance(SpellState state, World world, LivingEntity entity, float radius, boolean includeAllies, SpellCharacteristics characteristics) {
			super(state);
			this.radius = radius;
			this.origin = entity;
			this.world = world;
			this.includeAllies = includeAllies;
			this.characteristics = characteristics;
			
			dead = false;
			aliveCycles = 0;
			affected = new HashMap<>();
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, TICK_RATE);
			
			doEffect();
		}
		
		protected boolean canAffect(LivingEntity entity) {
			return entity != null
					&& entity.isAlive()
					&& (
							includeAllies
							|| !NostrumMagica.IsSameTeam(entity, origin)
							);
		}
		
		protected boolean isInArea(LivingEntity entity) {
			return origin.getDistance(entity) <= radius;
		}

		protected void doEffect() {
			if (origin != null && origin.isAlive())
			for (int i = 0; i < radius + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						origin.getPosX(),
						origin.getPosY() + (origin.getEyeHeight() / 2), // technically correct but visually sucky cause 50% will be underground
						origin.getPosZ(),
						.1,
						30, 0, // lifetime + jitter
						Vector3d.ZERO, (new Vector3d(.2, .2, .2)).scale(radius / 4)
						).color(characteristics.element.getColor())
						);
				NostrumParticles.LIGHTNING_STATIC.spawn(world, new SpawnParams(
						2,
						origin.getPosX(),
						origin.getPosY() + (origin.getEyeHeight() / 2), // technically correct but visually sucky cause 50% will be underground
						origin.getPosZ(),
						radius,
						20, 0, // lifetime + jitter
						new Vector3d(0, -.025, 0), new Vector3d(0, .05, 0)
						).color(characteristics.element.getColor()));
			}
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object empty) {
			if (dead)
				return true;
			
			if (type == Event.TIME) {
				
				if (!origin.isAlive() || !DimensionUtils.InDimension(origin, world)) {
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
				for (LivingEntity e : Entities.GetEntities((ServerWorld) world, (e) -> {return canAffect(e) && isInArea(e);})) {
					if (visitEntity(e)) {
						TriggerData data = new TriggerData(
								Lists.newArrayList(e),
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
			if (entity == null || !entity.isAlive()) {
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
	
	private static final String ID = "aura";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
			ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
			ReagentItem.CreateStack(ReagentType.CRYSTABLOOM, 1),
			ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	protected AuraShape(String key) {
		super(key);
	}
	
	public AuraShape() {
		this(ID);
	}
	
	@Override
	public SpellShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new AuraTriggerInstance(state, world, state.getSelf(),
				Math.max(supportedFloats()[0], params.level),
				params.flip,
				characteristics);
	}

	@Override
	public String getDisplayName() {
		return "Aura";
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
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
				new ItemStack(NostrumItems.resourceSpriteCore, 1),
				new ItemStack(NostrumItems.crystalMedium, 1)
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

	@Override
	public int getManaCost() {
		return 300;
	}

	@Override
	public int getWeight() {
		return 3;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		// TODO Auto-generated method stub
		return false;
	}
}