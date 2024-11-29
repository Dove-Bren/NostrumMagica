package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.IntSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that waits for something to damage the entity it's applied to.
 * Sets the thing that attacked as the new spell target.
 * @author Skyler
 *
 */
public class OnDamageShape extends SpellShape {

	public static class OnDamageShapeInstance extends SpellShapeInstance implements IGenericListener {

		private LivingEntity entity;
		private int duration;
		private boolean affectCaster;
		private boolean expired;
		
		public OnDamageShapeInstance(ISpellState state, LivingEntity entity, int duration, boolean affectCaster, SpellCharacteristics characteristics) {
			super(state);
			this.entity = entity;
			this.duration = duration == 0 ? 20 : duration;
			this.expired = false;
			this.affectCaster = affectCaster;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerHit(this, entity);
			NostrumMagica.playerListener.registerTimer(this, 0, 20 * duration);
			
			if (SetTrigger(entity, this)) {
				NostrumMagica.magicEffectProxy.applyOnHitEffect(entity, entity.ticksExisted, 20 * duration);
			}
		}
		
		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object unused) {
			if (type == Event.DAMAGED) {
				if (!expired) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(affectCaster ? getState().getCaster() : entity),
							null
							);
					
					this.trigger(data);
					NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_DAMAGE, this.entity);
					
					expired = true;
				}
			} else if (type == Event.TIME) {
				if (!expired) {
					expired = true;
					if (this.entity instanceof PlayerEntity) {
						PlayerEntity player = (PlayerEntity) this.entity;
						player.sendMessage(new TranslationTextComponent("modification.damaged_duration.expire"), Util.DUMMY_UUID);
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_DAMAGE, this.entity);
					}
				}
			}
			
			return true;
		}
	}
	
	private static final Map<UUID, OnDamageShapeInstance> ActiveMap = new HashMap<>();
	
	private static final boolean SetTrigger(LivingEntity entity, @Nullable OnDamageShapeInstance trigger) {
		OnDamageShapeInstance existing = ActiveMap.put(entity.getUniqueID(), trigger);
		if (existing != null && existing != trigger) {
			existing.expired = true;
		}
		return existing == null || existing != trigger;
	}
	
	private static final String ID = "hit";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.SPIDER_SILK, 1),
			ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1)));
	
	public static final SpellShapeProperty<Integer> DURATION = new IntSpellShapeProperty("delay", 20, 30, 40, 60, 300);
	public static final SpellShapeProperty<Boolean> AFFECT_ME = new BooleanSpellShapeProperty("affect_me");
	
	protected OnDamageShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(DURATION).addProperty(AFFECT_ME);
	}
	
	public OnDamageShape() {
		this(ID);
	}
	
	protected int getDurationSecs(SpellShapeProperties properties) {
		return properties.getValue(DURATION);
	}
	
	protected boolean getAffectCaster(SpellShapeProperties properties) {
		return properties.getValue(AFFECT_ME);
	}
	
	@Override
	public SpellShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new OnDamageShapeInstance(state, state.getSelf(), getDurationSecs(params), getAffectCaster(params), characteristics);
	}
	
	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.CACTUS);
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.REDSTONE),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.DIAMOND)
			);
		}
		return property == DURATION ? costs : super.getPropertyItemRequirements(property);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30;
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		builder.add(new SpellShapePreviewComponent.Ent(state.getSelf()));
		return true;
	}
	
	public SpellShapeProperties makeProps(int duration, boolean affectMe) {
		return this.getDefaultProperties().setValue(DURATION, duration).setValue(AFFECT_ME, affectMe);
	}
	
}
