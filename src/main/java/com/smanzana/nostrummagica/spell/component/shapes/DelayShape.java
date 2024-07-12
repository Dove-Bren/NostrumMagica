package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Lazy;

/**
 * Shape that waits some time before proceeding
 * @author Skyler
 *
 */
public class DelayShape extends SpellShape {

	public static class DelayShapeInstance extends SpellShapeInstance implements IGenericListener {

		private final int delayTicks;
		
		public DelayShapeInstance(ISpellState state, int delayTicks, SpellCharacteristics characteristics) {
			super(state);
			this.delayTicks = delayTicks;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			NostrumMagica.playerListener.registerTimer(this, 0, delayTicks);
		}
		
		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object unused) {
			// We only registered for time, so don't bother checking
			
			TriggerData data = new TriggerData(
					Lists.newArrayList(this.getState().getSelf()),
					null
					);
			this.trigger(data);
			return true;
		}
	}
	
	private static final String ID = "delay";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
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
	public SpellShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new DelayShapeInstance(state, 20 * getDelaySecs(params), characteristics);
	}
	
	@Override
	public String getDisplayName() {
		return "Delay";
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
			costs = NonNullList.from(ItemStack.EMPTY,
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
	
}
