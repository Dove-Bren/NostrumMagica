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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DamagedTrigger extends SpellTrigger {
	
	public class DamagedTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IMagicListener {

		private EntityLivingBase entity;
		
		public DamagedTriggerInstance(SpellState state, EntityLivingBase entity) {
			super(state);
			this.entity = entity;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			NostrumMagica.playerListener.registerHit(this, entity);
			
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity) {
			// We only registered for time, so don't bother checking
			
			TriggerData data = new TriggerData(
					Lists.newArrayList(this.getState().getSelf()),
					Lists.newArrayList(entity),
					null,
					null
					);
			this.trigger(data);
			return true;
		}
	}

	private static final String TRIGGER_KEY = "trigger_hit";
	private static DamagedTrigger instance = null;
	
	public static DamagedTrigger instance() {
		if (instance == null)
			instance = new DamagedTrigger();
		
		return instance;
	}
	
	private DamagedTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
		
		return list;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new DamagedTriggerInstance(state, state.getSelf());
	}

	@Override
	public String getDisplayName() {
		return "On Damage";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Item.getItemFromBlock(Blocks.CACTUS));
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public ItemStack[] supportedFloatCosts() {
		return null;
	}
	
}
