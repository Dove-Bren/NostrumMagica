package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.IntSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that waits some time before proceeding
 * @author Skyler
 *
 */
public class DelayShape extends SpellShape {

	public static class DelayShapeInstance extends SpellShapeInstance implements IGenericListener {

		private final int delayTicks;
		private LivingEntity entity;
		private SpellLocation location;
		
		public DelayShapeInstance(ISpellState state, LivingEntity entity, SpellLocation location, int delayTicks, SpellCharacteristics characteristics) {
			super(state);
			this.delayTicks = delayTicks;
			this.entity = entity;
			this.location = location;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerTimer(this, 0, delayTicks);
		}
		
		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object unused) {
			// We only registered for time, so don't bother checking
			
			List<LivingEntity> ents = null;
			if (this.entity != null) {
				ents = new ArrayList<>(1);
				ents.add(this.entity);
			}
			
			List<SpellLocation> locs = null;
			if (this.location != null) {
				locs = new ArrayList<>(1);
				locs.add(location);
			}
			
			TriggerData data = new TriggerData(
					ents, locs
					);
			this.trigger(data);
			return true;
		}
	}
	
	private static final String ID = "delay";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
	public static final SpellShapeProperty<Integer> DELAY = new IntSpellShapeProperty("delay", 10, 30, 60, 120, 300);
	
	protected DelayShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(DELAY);
	}
	
	public DelayShape() {
		this(ID);
	}
	
	protected int getDelaySecs(SpellShapeProperties properties) {
		return properties.getValue(DELAY);
	}
	
	@Override
	public SpellShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new DelayShapeInstance(state, entity, location, 20 * getDelaySecs(params), characteristics);
	}
	
	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.CLOCK);
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.REDSTONE),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Items.DIAMOND)
			);
		}
		return property == DELAY ? costs : super.getPropertyItemRequirements(property);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 10;
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		builder.add(new SpellShapePreviewComponent.Ent(state.getSelf()));
		return true;
	}
	
	public SpellShapeProperties makeProps(int delay) {
		return this.getDefaultProperties().setValue(DELAY, delay);
	}
	
}
