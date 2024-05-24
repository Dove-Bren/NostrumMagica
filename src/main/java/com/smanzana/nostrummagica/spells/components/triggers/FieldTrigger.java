package com.smanzana.nostrummagica.spells.components.triggers;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams.TargetBehavior;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellPartProperties;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class FieldTrigger extends TriggerAreaTrigger {
	
	public class FieldTriggerInstance extends TriggerAreaTrigger.TriggerAreaTriggerInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 10) / TICK_RATE; // 20 seconds

		private Vector3d origin;
		private float radius;
		
		public FieldTriggerInstance(SpellState state, World world, Vector3d pos, float radius, boolean continuous) {
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
			return origin.distanceTo(new Vector3d(entity.getPosX(), origin.y, entity.getPosZ())) <= radius; // compare against our y for horizontal distance.
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
			for (int i = 0; i < radius/2 + 1; i++) {
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						2,
//						origin.x,
//						origin.y, // technically correct but visually sucky cause 50% will be underground
//						origin.z,
//						radius,
//						20, 0, // lifetime + jitter
//						new Vector3d(0, -.025, 0), new Vector3d(0, .05, 0)
//						).color(getState().getNextElement().getColor()));
//				NostrumParticles.LIGHTNING_STATIC.spawn(world, new SpawnParams(
//						2,
//						origin.x,
//						origin.y, // technically correct but visually sucky cause 50% will be underground
//						origin.z,
//						radius,
//						20, 0, // lifetime + jitter
//						new Vector3d(0, -.025, 0), new Vector3d(0, .05, 0)
//						).color(getState().getNextElement().getColor()));
				
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						origin.x,
						origin.y + .5,
						origin.z,
						0,
						60, 10, // lifetime + jitter
						origin
						).color(getState().getNextElement().getColor())
						.setTargetBehavior(TargetBehavior.ORBIT)
						.setOrbitRadius(((NostrumMagica.rand.nextFloat() * .5f) + .5f) * radius));
			}
			
			// Spawn a border one
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					1,
					origin.x,
					origin.y + .5,
					origin.z,
					0,
					60, 10, // lifetime + jitter
					origin
					).color(getState().getNextElement().getColor())
					.setTargetBehavior(TargetBehavior.ORBIT)
					.setOrbitRadius(radius));
			
			
			// Looks very cool
//			final int slices = 10;
//			final double radPerSlice = (Math.PI * 2) / slices;
//			//for (int i = 0; i < slices; i++) {
//			{
//				final double rot = i * radPerSlice;
//				Vector3d borderPos = origin.add(Math.cos(rot) * radius, 0, Math.sin(rot) * radius);
//				
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						1,
//						borderPos.x, borderPos.y + .25, borderPos.z,
//						0,
//						20, 0, // lifetime + jitter
//						new Vector3d(0, .05, 0), Vector3d.ZERO
//						).color(getState().getNextElement().getColor()));
//			}
		}
	}

	private static final String TRIGGER_KEY = "field";
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
				ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
				ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
				ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vector3d pos, float pitch, float yaw,
			SpellPartProperties params) {
		
		// Blindly guess if trigger put us in a wall but above us isn't that t he player
		// wants us up one
		BlockPos blockPos = new BlockPos(pos);
		if (!world.isAirBlock(blockPos) && world.isAirBlock(blockPos.up())) {
			pos = pos.add(0, 1, 0);
		}
		
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
		return new float[] {1.5f, 2f, 2.5f, 3f, 4f};
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
				new ItemStack(NostrumItems.crystalLarge, 1)
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
	
	@Override
	public int getWeight() {
		return 2;
	}

	@Override
	public boolean shouldTrace(SpellPartProperties params) {
		return false;
	}
}
