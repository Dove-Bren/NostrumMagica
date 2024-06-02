package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.Spell.SpellState;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Targets the caster themselves
 * @author Skyler
 *
 */
public class SelfShape extends InstantShape {

	public static final String ID = "self";
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.MANI_DUST, 1)));
	
	public SelfShape() {
		this(ID);
	}
	
	protected SelfShape(String key) {
		super(key);
	}

	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new TriggerData(Lists.newArrayList(state.getSelf()), world, null);
	}

	@Override
	public String getDisplayName() {
		return "Self";
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
		return new ItemStack(Items.GOLD_INGOT);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}
}
