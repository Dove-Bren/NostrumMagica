package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class DelayTrigger extends SpellTrigger {
	
	public class DelayTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {

		private int delayTicks;
		
		public DelayTriggerInstance(SpellState state, int delayTicks) {
			super(state);
			this.delayTicks = delayTicks;
		}
		
		@Override
		public void init(LivingEntity caster) {
			// We are instant! Whoo!
			NostrumMagica.playerListener.registerTimer(this, 0, delayTicks);
			
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object unused) {
			// We only registered for time, so don't bother checking
			
			TriggerData data = new TriggerData(
					Lists.newArrayList(this.getState().getSelf()),
					Lists.newArrayList(this.getState().getSelf()),
					null,
					null
					);
			this.trigger(data);
			return true;
		}
	}

	private static final String TRIGGER_KEY = "delay";
	private static DelayTrigger instance = null;
	
	public static DelayTrigger instance() {
		if (instance == null)
			instance = new DelayTrigger();
		
		return instance;
	}
	
	private DelayTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.SKY_ASH, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vector3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new DelayTriggerInstance(state, Math.max(20 * (int) supportedFloats()[0], (int) params.level));
	}

	@Override
	public String getDisplayName() {
		return "Delay";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.CLOCK);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {10f, 30f, 60f, 120f, 300f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.REDSTONE),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.DIAMOND)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.delay.name", (Object[]) null);
	}
	
}
