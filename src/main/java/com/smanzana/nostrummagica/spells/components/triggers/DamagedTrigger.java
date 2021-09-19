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
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class DamagedTrigger extends SpellTrigger {
	
	public class DamagedTriggerInstance extends SpellTrigger.SpellTriggerInstance implements IGenericListener {

		private EntityLivingBase entity;
		private int duration;
		private boolean expired;
		
		public DamagedTriggerInstance(SpellState state, EntityLivingBase entity, int duration) {
			super(state);
			this.entity = entity;
			this.duration = duration == 0 ? 20 : duration;
			this.expired = false;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// We are instant! Whoo!
			NostrumMagica.playerListener.registerHit(this, entity);
			NostrumMagica.playerListener.registerTimer(this, 0, 20 * duration);
			
			if (SetTrigger(entity, this)) {
				NostrumMagica.magicEffectProxy.applyOnHitEffect(entity, entity.ticksExisted, 20 * duration);
			}
		}

		@Override
		public boolean onEvent(Event type, EntityLivingBase entity, Object unused) {
			if (type == Event.DAMAGED) {
				if (!expired) {
					TriggerData data = new TriggerData(
							Lists.newArrayList(this.getState().getSelf()),
							Lists.newArrayList(this.entity),
							null,
							null
							);
					
					entity.world.getMinecraftServer().addScheduledTask(() -> {
						this.trigger(data);
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_DAMAGE, this.entity);
					});
					expired = true;
				}
			} else if (type == Event.TIME) {
				if (!expired) {
					expired = true;
					if (this.entity instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) this.entity;
						player.sendMessage(new TextComponentTranslation("modification.damaged_duration.expire"));
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.CONTINGENCY_DAMAGE, this.entity);
					}
				}
			}
			
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
	
	private static final Map<UUID, DamagedTriggerInstance> ActiveMap = new HashMap<>();
	
	private static final boolean SetTrigger(EntityLivingBase entity, @Nullable DamagedTriggerInstance trigger) {
		DamagedTriggerInstance existing = ActiveMap.put(entity.getUniqueID(), trigger);
		if (existing != null && existing != trigger) {
			existing.expired = true;
		}
		return existing == null || existing != trigger;
	}
	
	private DamagedTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1),
				ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1));
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw,
			SpellPartParam params) {
		return new DamagedTriggerInstance(state, state.getSelf(), (int) params.level);
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
		return new float[] {20f, 30f, 40f, 60f, 300f};
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
		return I18n.format("modification.damaged_duration.name", (Object[]) null);
	}
	
}
