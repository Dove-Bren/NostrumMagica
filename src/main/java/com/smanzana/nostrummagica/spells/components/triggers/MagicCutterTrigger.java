package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.ISpellSaucerTrigger;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MagicCutterTrigger extends SpellTrigger {
	
	public class MagicCutterTriggerInstance extends SpellTrigger.SpellTriggerInstance implements ISpellSaucerTrigger {

		private World world;
		private Vec3d pos;
		private float pitch;
		private float yaw;
		private boolean piercing;
		private int trips;
		
		public MagicCutterTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw, boolean piercing, int trips) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.piercing = piercing;
			this.trips = trips;
		}
		
		@Override
		public void init(LivingEntity caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3d dir;
			if (caster instanceof MobEntity && ((MobEntity) caster).getAttackTarget() != null) {
				MobEntity ent = (MobEntity) caster  ;
				dir = ent.getAttackTarget().getPositionVector().add(0.0, ent.getHeight() / 2.0, 0.0)
						.subtract(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			} else {
				dir = MagicCutterTrigger.getVectorForRotation(pitch, yaw);
			}
			
			final MagicCutterTriggerInstance self = this;
			
			caster.getServer().runAsync(new Runnable() {

				@Override
				public void run() {
					EntitySpellSaucer projectile = new EntityChakramSpellSaucer(NostrumEntityTypes.chakramSpellSaucer, self, 
							getState().getSelf(),
							world,
							pos.x, pos.y, pos.z,
							dir,
							5.0f, piercing ? PROJECTILE_RANGE/2 : PROJECTILE_RANGE, piercing, trips);
					
//					EntitySpellSaucer projectile = new EntityCyclerSpellSaucer(self,
//							getState().getSelf(),
//							5.0f, 500);
					
					world.addEntity(projectile);
				}
			
			});
		}
		
		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(getState().getOther()), world, Lists.newArrayList(pos), piercing); /// TODO only force split if piercing
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (NostrumMagica.resolveLivingEntity(entity) == null) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), Lists.newArrayList(getState().getOther()), null, null, piercing);
			}
		}
	}

	private static final String TRIGGER_KEY = "cutter";
	private static MagicCutterTrigger instance = null;
	
	public static MagicCutterTrigger instance() {
		if (instance == null)
			instance = new MagicCutterTrigger();
		
		return instance;
	}
	
	private MagicCutterTrigger() {
		super(TRIGGER_KEY);
	}

	private static final double PROJECTILE_RANGE = 50.0;
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		// We use param's flip to indicate whether we should be piercing or not
		boolean piercing = false;
		if (params != null)
			piercing = params.flip;
		int trips = 1;
		if (params != null)
			trips = Math.max(1, (int) params.level);
		
		// Add direction
		pos = new Vec3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MagicCutterTriggerInstance(state, world, pos, pitch, yaw, piercing, trips);
	}

	// Copied from vanilla entity class
	public static final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.SKY_ASH, 1));
	}

	@Override
	public String getDisplayName() {
		return "Mana Cutter";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.SNOWBALL, 1);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] { 1f, 2f, 5f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.REDSTONE_BLOCK),
				new ItemStack(Blocks.OBSIDIAN)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.cutter.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modificaton.cutter.trips", (Object[]) null);
	}
	
}
