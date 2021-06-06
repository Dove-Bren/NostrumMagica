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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ProximityTrigger extends SpellTrigger {
	
	public class ProximityTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {

		private World world;
		private Vec3d pos;
		private boolean set;
		private float range;
		private boolean dead;
		
		public ProximityTriggerInstance(SpellState state, World world,
				Vec3d pos, float range) {
			super(state);
			this.world = world;
			this.set = false;
			this.pos = pos;
			this.range = range;
			dead = false;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			NostrumMagica.playerListener.registerTimer(this, 20, 100);
			
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity, Object empty) {
			// We first wait 20 ticks to allow people to move around.
			if (type == Event.TIME) {
				if (dead)
					return true;
				
				NostrumMagica.proxy.spawnEffect(world, new SpellComponentWrapper(instance()),
						null, null, null, this.pos, new SpellComponentWrapper(getState().getNextElement()), false, range);
				if (!set) {
					// Trap is now set!
					set = true;
					NostrumMagica.playerListener.registerProximity(this, world, pos, range / 2f);
					return false;
				}
				
				return false;
			}
			
			
			// Else we've already been set. Just BOOM
			TriggerData data = new TriggerData(
					Lists.newArrayList(entity),
					Lists.newArrayList(this.getState().getSelf()),
					null,
					null
					);
			this.trigger(data);
			this.dead = true;
			return true;
		}
	}

	private static final String TRIGGER_KEY = "trigger_proximity";
	private static ProximityTrigger instance = null;
	
	public static ProximityTrigger instance() {
		if (instance == null)
			instance = new ProximityTrigger();
		
		return instance;
	}
	
	private ProximityTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1));
		
		return list;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new ProximityTriggerInstance(state, world, pos,
				Math.max(supportedFloats()[0], params.level));
	}

	@Override
	public String getDisplayName() {
		return "Proximity";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Item.getItemFromBlock(Blocks.TRIPWIRE_HOOK));
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {1f, 2f, 3f, 5f};
	}

	public static ItemStack[] costs = null;
	@Override
	public ItemStack[] supportedFloatCosts() {
		if (costs == null) {
			costs = new ItemStack[] {
				null,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.DIAMOND),
			};
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.proximity.name", (Object[]) null);
	}
	
}
