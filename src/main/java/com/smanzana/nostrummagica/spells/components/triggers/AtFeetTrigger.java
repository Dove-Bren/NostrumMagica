package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.SpellPartProperties;
import com.smanzana.nostrummagica.spells.LegacySpell.SpellState;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class AtFeetTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "feet";
	private static AtFeetTrigger instance = null;
	
	public static AtFeetTrigger instance() {
		if (instance == null)
			instance = new AtFeetTrigger();
		
		return instance;
	}
	
	private AtFeetTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw) {
		return new TriggerData(null, null, world, Lists.newArrayList(state.getSelf().getPosition().down()));
	}
	
	@Override
	public int getManaCost() {
		return 5;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.SKY_ASH, 1));
	}

	@Override
	public String getDisplayName() {
		return "At Feet";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DIRT);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
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
	public String supportedBooleanName() {
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
	public boolean shouldTrace(SpellPartProperties params) {
		return false;
	}
}
