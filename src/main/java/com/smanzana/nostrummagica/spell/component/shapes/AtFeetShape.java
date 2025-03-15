package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Lazy;

/**
 * Targets the block below the active spell target's feet
 * @author Skyler
 *
 */
public class AtFeetShape extends InstantShape {

	public static final String ID = "feet";
	public static final float TOUCH_RANGE = 3.0f;
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
	public AtFeetShape() {
		this(ID);
	}
	
	protected AtFeetShape(String key) {
		super(key);
	}

	@Override
	protected TriggerData getTargetData(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new TriggerData(null, Lists.newArrayList(new SpellLocation(location.world, state.getSelf().blockPosition().below())));
	}

	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 5;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		return null;
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 0;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DIRT);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, false, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		builder.add(new SpellShapePreviewComponent.Position(new SpellLocation(location.world, state.getSelf().blockPosition().below())));
		return true;
	}
}
