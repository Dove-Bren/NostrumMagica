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

public class OtherTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "trigger_other";
	private static OtherTrigger instance = null;
	
	public static OtherTrigger instance() {
		if (instance == null)
			instance = new OtherTrigger();
		
		return instance;
	}
	
	private OtherTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vec3d pos, float pitch, float yaw) {
		return new TriggerData(Lists.newArrayList(state.getOther()), Lists.newArrayList(state.getSelf()), world, null);
	}
	
	@Override
	public int getManaCost() {
		return 15;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Other";
	}
	
}
