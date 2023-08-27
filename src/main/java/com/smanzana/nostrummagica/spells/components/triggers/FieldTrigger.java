package com.smanzana.nostrummagica.spells.components.triggers;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FieldTrigger extends TriggerAreaTrigger {
	
	public class FieldTriggerInstance extends TriggerAreaTrigger.TriggerAreaTriggerInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 10) / TICK_RATE; // 20 seconds

		private Vec3d origin;
		private float radius;
		
		public FieldTriggerInstance(SpellState state, World world, Vec3d pos, float radius, boolean continuous) {
			super(state, world, pos, TICK_RATE, NUM_TICKS, radius + .75f, continuous, true);
			this.radius = radius;
			this.origin = pos;
		}
		
		@Override
		public void init(LivingEntity caster) {
			super.init(caster); // Inits listening and stuff
		}
		
		@Override
		protected boolean isInArea(LivingEntity entity) {
			return origin.distanceTo(new Vec3d(entity.posX, origin.y, entity.posZ)) <= radius; // compare against our y for horizontal distance.
			// .75 wiggle room in listener means you can't be way below.
		}

		@Override
		protected boolean isInArea(World world, BlockPos pos) {
			return (Math.abs((Math.floor(origin.x) + .5) - (pos.getX() + .5))
					+ Math.abs(origin.y - pos.getY())
					+ Math.abs((Math.floor(origin.z) + .5) - (pos.getZ() + .5))) < radius;
		}

		@Override
		protected void doEffect() {
			for (int i = 0; i < radius + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						2,
						origin.x,
						origin.y, // technically correct but visually sucky cause 50% will be underground
						origin.z,
						radius,
						20, 0, // lifetime + jitter
						new Vec3d(0, -.025, 0), new Vec3d(0, .05, 0)
						).color(getState().getNextElement().getColor()));
				NostrumParticles.LIGHTNING_STATIC.spawn(world, new SpawnParams(
						2,
						origin.x,
						origin.y, // technically correct but visually sucky cause 50% will be underground
						origin.z,
						radius,
						20, 0, // lifetime + jitter
						new Vec3d(0, -.025, 0), new Vec3d(0, .05, 0)
						).color(getState().getNextElement().getColor()));
			}
		}
	}

	private static final String TRIGGER_KEY = "trigger_field";
	private static FieldTrigger instance = null;
	
	public static FieldTrigger instance() {
		if (instance == null)
			instance = new FieldTrigger();
		
		return instance;
	}
	
	private FieldTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 100;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
				ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1),
				ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new FieldTriggerInstance(state, world, pos,
				Math.max(supportedFloats()[0], params.level), !params.flip);
	}

	@Override
	public String getDisplayName() {
		return "Field";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.DRAGON_BREATH);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {1f, 1.5f, 2f, 3f, 4f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DRAGON_BREATH),
				new ItemStack(Blocks.EMERALD_BLOCK),
				new ItemStack(Blocks.DIAMOND_BLOCK),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1)
				);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.field.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.field.float.name", (Object[]) null);
	}
	
}
