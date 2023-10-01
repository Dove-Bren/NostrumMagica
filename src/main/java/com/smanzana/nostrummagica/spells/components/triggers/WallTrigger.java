package com.smanzana.nostrummagica.spells.components.triggers;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WallTrigger extends TriggerAreaTrigger {
	
	public class WallTriggerInstance extends TriggerAreaTrigger.TriggerAreaTriggerInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private boolean northsouth;
		private float radius;
		
		private double minX;
		private double minZ;
		private double maxX;
		private double maxZ;
		
		public WallTriggerInstance(SpellState state, World world, Vec3d pos, boolean northsouth, float radius, boolean continuous) {
			super(state, world, new Vec3d(Math.floor(pos.x) + .5, pos.y, Math.floor(pos.z) + .5), TICK_RATE, NUM_TICKS, radius + .75f, continuous, false);
			this.radius = radius;
			this.northsouth = northsouth;
		}
		
		@Override
		public void init(LivingEntity caster) {
			// Figure out bounds
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
			
			super.init(caster); // Inits listening and stuff
		}
		
		@Override
		protected boolean isInArea(LivingEntity entity) {
			return entity.posX >= this.minX && entity.posX <= this.maxX
					&& entity.posZ >= this.minZ && entity.posZ <= this.maxZ
					&& entity.posY >= Math.floor(this.pos.y) && entity.posY <= Math.floor(this.pos.y) + 2;
		}

		@Override
		protected boolean isInArea(World world, BlockPos pos) {
			return false;
		}

		@Override
		protected void doEffect() {
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
						new Vec3d(0, .05, 0), null
						).color(getState().getNextElement().getColor()));
			}
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
				ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
				ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1));
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
		return new ItemStack(Blocks.GLASS);
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
