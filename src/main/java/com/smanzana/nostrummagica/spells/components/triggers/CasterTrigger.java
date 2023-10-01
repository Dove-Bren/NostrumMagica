package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CasterTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "trigger_caster";
	private static CasterTrigger instance = null;
	
	public static CasterTrigger instance() {
		if (instance == null)
			instance = new CasterTrigger();
		
		return instance;
	}
	
	private CasterTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vec3d pos, float pitch, float yaw) {
		return new TriggerData(Lists.newArrayList(state.getCaster()), null, world, null);
	}
	
	@Override
	public int getManaCost() {
		return 5;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.CRYSTABLOOM, 1));
	}

	@Override
	public String getDisplayName() {
		return "Caster";
	}

	@Override
	public ItemStack getCraftItem() {
		return SpellRune.getRune(SelfTrigger.instance());
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
	
}
