package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.ISpellSaucerTrigger;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class MagicCyclerTrigger extends SpellTrigger {
	
	public class MagicCyclerTriggerInstance extends SpellTrigger.SpellTriggerInstance implements ISpellSaucerTrigger {

		private World world;
		private Vec3d pos;
		private boolean onBlocks;
		private float duration;
		
		public MagicCyclerTriggerInstance(SpellState state, World world, Vec3d pos, boolean onBlocks, float duration) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.onBlocks = onBlocks;
			this.duration = duration;
		}
		
		@Override
		public void init(LivingEntity caster) {
			
			final MagicCyclerTriggerInstance self = this;
			
			caster.getServer().runAsync(new Runnable() {

				@Override
				public void run() {
					EntitySpellSaucer projectile = new EntityCyclerSpellSaucer(self,
							getState().getSelf(),
							5.0f, (int) duration * 20, onBlocks);
					
					world.addEntity(projectile);
			
				}
			
			});
		}
		
		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(getState().getOther()), world, Lists.newArrayList(pos)); /// TODO only force split if piercing
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), Lists.newArrayList(getState().getOther()), null, null);
			}
		}
	}

	private static final String TRIGGER_KEY = "trigger_vortex_blade";
	private static MagicCyclerTrigger instance = null;
	
	public static MagicCyclerTrigger instance() {
		if (instance == null)
			instance = new MagicCyclerTrigger();
		
		return instance;
	}
	
	private MagicCyclerTrigger() {
		super(TRIGGER_KEY);
	}

	@Override
	public int getManaCost() {
		return 25;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		// We use param's flip to indicate whether we should interact with blocks
		boolean onBlocks = false;
		if (params != null)
			onBlocks = params.flip;
		
		// Float param is duration param
		float duration = this.supportedFloats()[0];
		if (params != null && params.level != 0f)
			duration = params.level;
			
		
		// Add direction
		pos = new Vec3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MagicCyclerTriggerInstance(state, world, pos, onBlocks, duration);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.instance().getReagent(ReagentType.GINSENG, 1),
				ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
	}

	@Override
	public String getDisplayName() {
		return "Mana Cycle";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.COMPASS, 1, OreDictionary.WILDCARD_VALUE);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[]{
				10,
				20,
				50,
		};
	}
	
	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.COAL),
				new ItemStack(Blocks.COAL_BLOCK)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.vortex_blade.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.vortex_blade.float.name", (Object[]) null);
	}
	
}
