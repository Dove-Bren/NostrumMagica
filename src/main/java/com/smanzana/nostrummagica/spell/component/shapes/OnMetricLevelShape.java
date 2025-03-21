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

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

/**
 * Shape that waits until a specific metric on an entity to raise/lower beyond a limit
 * @author Skyler
 *
 */
public abstract class OnMetricLevelShape extends SpellShape {

	private static final TextComponent LABEL_BELOW = new StringTextComponent("Below");
	private static final TextComponent LABEL_ABOVE = new StringTextComponent("Above");
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
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.TRIPWIRE_HOOK),
				new ItemStack(Items.REPEATER),
				new ItemStack(Items.ENDER_PEARL)
			);
		}
		return property == LEVEL ? costs : super.getPropertyItemRequirements(property);
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
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		builder.add(new SpellShapePreviewComponent.Ent(state.getSelf()));
		return true;
	}
	
}
