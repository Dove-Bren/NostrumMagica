package com.smanzana.nostrummagica.spell.component.shapes;

import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.LabeledBooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.PercentSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;

/**
 * Shape that waits until a specific metric on an entity to raise/lower beyond a limit
 * @author Skyler
 *
 */
public abstract class OnMetricLevelShape extends SpellShape {

	private static final BaseComponent LABEL_BELOW = new TextComponent("Below");
	private static final BaseComponent LABEL_ABOVE = new TextComponent("Above");
	public static final SpellShapeProperty<Boolean> WHEN_ABOVE = new LabeledBooleanSpellShapeProperty("when_above", LABEL_BELOW, LABEL_ABOVE);
	public static final SpellShapeProperty<Float> LEVEL = new PercentSpellShapeProperty("level", .5f, .2f, .8f, 1f);
	
	protected OnMetricLevelShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(WHEN_ABOVE).addProperty(LEVEL);
	}
	
	protected float getLevel(SpellShapeProperties properties) {
		return properties.getValue(LEVEL);
	}
	
	protected boolean getOnAbove(SpellShapeProperties properties) {
		return properties.getValue(WHEN_ABOVE);
	}
	
	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.TRIPWIRE_HOOK),
				new ItemStack(Items.REPEATER),
				new ItemStack(Items.ENDER_PEARL)
			);
		}
		return property == LEVEL ? costs : super.getPropertyItemRequirements(property);
	}
	
	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, false);
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
	
}
