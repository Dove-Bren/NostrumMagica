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
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that waits until an entities mana reaches a certain level.
 * @author Skyler
 *
 */
public class OnManaShape extends SpellShape {

	public static class ManaShapeInstance extends SpellShapeInstance implements IGenericListener {

		private float amount;
		private boolean onHigh;
		private LivingEntity entity;
		private int duration;
		private boolean expired;
		
		public ManaShapeInstance(ISpellState state, LivingEntity entity, float amount, boolean higher, int duration, SpellCharacteristics characteristics) {
			super(state);
			this.amount = amount;
			this.onHigh = higher;
			this.entity = entity;
			this.duration = duration;
			
			if (this.amount <= 0f)
				this.amount = .5f;
			if (this.duration <= 0)
				this.duration = 20;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerMana(this, entity, amount, onHigh);
			NostrumMagica.playerListener.registerTimer(this, 0, 20 * duration);
			
			if (SetTrigger(entity, this)) {
				NostrumMagica.magicEffectProxy.applyOnManaEffect(entity, entity.ticksExisted, 20 * duration);
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
					
					this.trigger(data);
					NostrumMagica.instance.proxy.spawnEffect(this.getState().getSelf().world,
							new SpellComponentWrapper(NostrumSpellShapes.OnMana),
							this.getState().getSelf(), null, this.getState().getSelf(), null, null, false, 0);
					NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_MANA, this.entity);
					expired = true;
				}
			} else if (type == Event.TIME) {
				if (!expired) {
					expired = true;
					if (this.entity instanceof PlayerEntity) {
						PlayerEntity player = (PlayerEntity) this.entity;
						player.sendMessage(new TranslationTextComponent("modification.damaged_duration.mana"), Util.DUMMY_UUID);
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_MANA, this.entity);
					}
				}
			}
			
			return true;
		}
	}
	
	private static final Map<UUID, ManaShapeInstance> ActiveMap = new HashMap<>();
	
	private static final boolean SetTrigger(LivingEntity entity, @Nullable ManaShapeInstance trigger) {
		ManaShapeInstance existing = ActiveMap.put(entity.getUniqueID(), trigger);
		if (existing != null && existing != trigger) {
			existing.expired = true;
		}
		return existing == null || existing != trigger;
	}
	
	private static final String ID = "mana";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1),
			ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	protected OnManaShape(String key) {
		super(key);
	}
	
	public OnManaShape() {
		this(ID);
	}
	
	protected int getLevel(SpellShapePartProperties properties) {
		return Math.max((int) supportedFloats()[0], (int) properties.level);
	}
	
	protected boolean getOnAbove(SpellShapePartProperties properties) {
		return properties.flip;
	}
	
	@Override
	public ManaShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new ManaShapeInstance(state, state.getCaster(),
				getLevel(params), getOnAbove(params), 300, characteristics);
	}
	
	@Override
	public String getDisplayName() {
		return "Mana Level";
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
		return I18n.format("modification.mana.name", (Object[]) null);
	}

	@Override
	public int getManaCost(SpellShapePartProperties properties) {
		return 20;
	}

	@Override
	public int getWeight(SpellShapePartProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		builder.add(new SpellShapePreviewComponent.Ent(state.getSelf()));
		return true;
	}
	
}
