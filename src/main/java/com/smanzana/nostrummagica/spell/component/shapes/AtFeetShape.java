package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.Spell.SpellState;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Targets the block below the active spell target's feet
 * @author Skyler
 *
 */
public class AtFeetShape extends InstantShape {

	public static final String ID = "feet";
	public static final float TOUCH_RANGE = 3.0f;
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
	public AtFeetShape() {
		this(ID);
	}
	
	protected AtFeetShape(String key) {
		super(key);
	}

	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new TriggerData(null, world, Lists.newArrayList(state.getSelf().getPosition().down()));
	}

	@Override
	public String getDisplayName() {
		return "At Feet";
	}
	
	@Override
	public int getManaCost() {
		return 5;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return null;
	}

	@Override
	public int getWeight() {
		return 0;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return false;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DIRT);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(false, false, true);
	}
}
