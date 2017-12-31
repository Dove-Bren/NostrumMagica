package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SelfTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "trigger_self";
	private static SelfTrigger instance = null;
	
	public static SelfTrigger instance() {
		if (instance == null)
			instance = new SelfTrigger();
		
		return instance;
	}
	
	private SelfTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vec3d pos, float pitch, float yaw) {
		return new TriggerData(Lists.newArrayList(state.getSelf()), null, world, null);
	}
	
	@Override
	public int getManaCost() {
		return 10;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.GINSENG, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1));
		
		return list;
	}
	
}
