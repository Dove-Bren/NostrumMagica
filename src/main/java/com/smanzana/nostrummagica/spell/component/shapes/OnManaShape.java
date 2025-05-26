package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that waits until an entities mana reaches a certain level.
 * @author Skyler
 *
 */
public class OnManaShape extends OnMetricLevelShape {

	public static class ManaShapeInstance extends SpellShapeInstance implements IGenericListener {

		private float amount;
		private boolean onHigh;
		private LivingEntity entity;
		private int duration;
		private boolean expired;
		private final SpellShapeProperties properties;
		private final SpellCharacteristics characteristics;
		
		public ManaShapeInstance(ISpellState state, LivingEntity entity, float amount, boolean higher, int duration, SpellShapeProperties properties, SpellCharacteristics characteristics) {
			super(state);
			this.amount = amount;
			this.onHigh = higher;
			this.entity = entity;
			this.duration = duration;
			
			if (this.amount <= 0f)
				this.amount = .5f;
			if (this.duration <= 0)
				this.duration = 20;
			this.properties = properties;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerMana(this, entity, amount, onHigh);
			NostrumMagica.playerListener.registerTimer(this, 0, 20 * duration);
			
			if (SetTrigger(entity, this)) {
				NostrumMagica.magicEffectProxy.applyOnManaEffect(entity, entity.tickCount, 20 * duration);
			}
		}
		
		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object unused) {
			if (type == Event.MANA) {
				if (!expired) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(this.getState().getSelf()),
							null
							);

					expired = true;
					this.trigger(data);
					NostrumMagica.Proxy.spawnSpellShapeVfx(this.getState().getSelf().level,
							NostrumSpellShapes.OnMana, properties,
							this.getState().getSelf(), null, this.getState().getSelf(), null, characteristics);
					NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_MANA, this.entity);
				}
			} else if (type == Event.TIME) {
				if (!expired) {
					expired = true;
					if (this.entity instanceof Player) {
						Player player = (Player) this.entity;
						player.sendMessage(new TranslatableComponent("modification.damaged_duration.mana"), Util.NIL_UUID);
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_MANA, this.entity);
					}
				}
			}
			
			return true;
		}
	}
	
	private static final Map<UUID, ManaShapeInstance> ActiveMap = new HashMap<>();
	
	private static final boolean SetTrigger(LivingEntity entity, @Nullable ManaShapeInstance trigger) {
		ManaShapeInstance existing = ActiveMap.put(entity.getUUID(), trigger);
		if (existing != null && existing != trigger) {
			existing.expired = true;
		}
		return existing == null || existing != trigger;
	}
	
	private static final String ID = "mana";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1),
			ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	protected OnManaShape(String key) {
		super(key);
	}
	
	public OnManaShape() {
		this(ID);
	}
	
	@Override
	public ManaShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new ManaShapeInstance(state, state.getCaster(),
				getLevel(params), getOnAbove(params), 300, params, characteristics);
	}
	
	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(NostrumItems.essenceIce, 1);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 20;
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}
}
