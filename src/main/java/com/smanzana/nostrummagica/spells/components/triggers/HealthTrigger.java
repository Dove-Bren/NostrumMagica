package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IMagicListener;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HealthTrigger extends SpellTrigger {
	
	public class HealthTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IMagicListener {

		private float amount;
		private boolean onHigh;
		private EntityLivingBase entity;
		
		public HealthTriggerInstance(SpellState state, EntityLivingBase entity, float amount, boolean higher) {
			super(state);
			this.amount = amount;
			this.onHigh = higher;
			this.entity = entity;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			NostrumMagica.playerListener.registerHealth(this, entity, amount, onHigh);
			
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity) {
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

	private static final String TRIGGER_KEY = "trigger_health";
	private static HealthTrigger instance = null;
	
	public static HealthTrigger instance() {
		if (instance == null)
			instance = new HealthTrigger();
		
		return instance;
	}
	
	private HealthTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.GINSENG, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
		
		return list;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new HealthTriggerInstance(state, state.getCaster(), params.level, params.flip);
	}
	
}
