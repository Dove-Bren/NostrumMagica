package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FoodTrigger extends SpellTrigger {
	
	public class FoodTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {

		private int amount;
		private boolean onHigh;
		private EntityLivingBase entity;
		
		public FoodTriggerInstance(SpellState state, EntityLivingBase entity, int amount, boolean higher) {
			super(state);
			this.amount = amount;
			this.onHigh = higher;
			this.entity = entity;
			
			if (this.amount <= 0)
				this.amount = 10;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			if (entity instanceof EntityPlayer)
				NostrumMagica.playerListener.registerFood(this, (EntityPlayer) entity, amount, onHigh);
			else
				NostrumMagica.playerListener.registerTimer(this, 20, 0);
			
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity, Object unused) {
			
			EntityLivingBase self = this.getState().getSelf();
			TriggerData data = new TriggerData(
					Lists.newArrayList(self),
					Lists.newArrayList(self),
					null,
					null
					);
			this.trigger(data);
			
			NostrumMagica.proxy.spawnEffect(self.worldObj,
					new SpellComponentWrapper(instance()),
					self, null, self, null, null, false, 0);
			
			return true;
		}
	}

	private static final String TRIGGER_KEY = "trigger_food";
	private static FoodTrigger instance = null;
	
	public static FoodTrigger instance() {
		if (instance == null)
			instance = new FoodTrigger();
		
		return instance;
	}
	
	private FoodTrigger() {
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
		list.add(ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1));
		
		return list;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new FoodTriggerInstance(state, state.getCaster(),
				Math.max((int) supportedFloats()[0], (int) params.level), params.flip);
	}

	@Override
	public String getDisplayName() {
		return "Food Level";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.GOLDEN_CARROT);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {.5f, .2f, .8f, 1f};
	}

	public static ItemStack[] costs = null;
	@Override
	public ItemStack[] supportedFloatCosts() {
		if (costs == null) {
			costs = new ItemStack[] {
				null,
				new ItemStack(Blocks.TRIPWIRE_HOOK),
				new ItemStack(Items.REPEATER),
				new ItemStack(Items.ENDER_PEARL),
			};
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.level.flip", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.food.name", (Object[]) null);
	}
	
}
