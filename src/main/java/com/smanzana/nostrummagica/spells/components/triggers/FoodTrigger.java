package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class FoodTrigger extends SpellTrigger {
	
	public class FoodTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {

		private int amount;
		private boolean onHigh;
		private LivingEntity entity;
		private int duration;
		private boolean expired;
		
		public FoodTriggerInstance(SpellState state, LivingEntity entity, int amount, boolean higher, int duration) {
			super(state);
			this.amount = amount;
			this.onHigh = higher;
			this.entity = entity;
			this.duration = duration;
			
			if (this.amount <= 0)
				this.amount = 10;
			if (this.duration <= 0)
				this.duration = 20;
		}
		
		@Override
		public void init(LivingEntity caster) {
			if (entity instanceof PlayerEntity) {
				NostrumMagica.playerListener.registerFood(this, (PlayerEntity) entity, amount, onHigh);
				NostrumMagica.playerListener.registerTimer(this, 0, 20 * duration);
			} else {
				NostrumMagica.playerListener.registerTimer(this, 20, 0);
			}
			
			
			if (SetTrigger(entity, this)) {
				NostrumMagica.magicEffectProxy.applyOnFoodEffect(entity, entity.ticksExisted, 20 * duration);
			}
			
		}

		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object unused) {
			if (type == Event.FOOD || (type == Event.TIME && !(this.entity instanceof PlayerEntity))) {
				if (!expired) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(this.getState().getSelf()),
							Lists.newArrayList(this.getState().getSelf()),
							null,
							null
							);
					
					this.entity.world.getServer().runAsync(() -> {
						this.trigger(data);
						NostrumMagica.instance.proxy.spawnEffect(this.getState().getSelf().world,
								new SpellComponentWrapper(instance()),
								this.getState().getSelf(), null, this.getState().getSelf(), null, null, false, 0);
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_FOOD, this.entity);
					});
					expired = true;
				}
			} else if (type == Event.TIME) {
				if (!expired) {
					expired = true;
					if (this.entity instanceof PlayerEntity) {
						PlayerEntity player = (PlayerEntity) this.entity;
						player.sendMessage(new TranslationTextComponent("modification.damaged_duration.health"));
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_FOOD, this.entity);
					}
				}
			}
			
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
	
	private static final Map<UUID, FoodTriggerInstance> ActiveMap = new HashMap<>();
	
	private static final boolean SetTrigger(LivingEntity entity, @Nullable FoodTriggerInstance trigger) {
		FoodTriggerInstance existing = ActiveMap.put(entity.getUniqueID(), trigger);
		if (existing != null && existing != trigger) {
			existing.expired = true;
		}
		return existing == null || existing != trigger;
	}
	
	private FoodTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.GINSENG, 1),
				ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new FoodTriggerInstance(state, state.getCaster(),
				Math.max((int) supportedFloats()[0], (int) params.level), params.flip, 300);
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

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.TRIPWIRE_HOOK),
				new ItemStack(Items.REPEATER),
				new ItemStack(Items.ENDER_PEARL)
			);
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
