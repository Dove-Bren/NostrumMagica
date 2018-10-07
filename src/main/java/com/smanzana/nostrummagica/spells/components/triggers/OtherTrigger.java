package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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
		
		NostrumMagica.proxy.spawnEffect(state.getOther().world,
				new SpellComponentWrapper(instance()),
				state.getCaster(), null, state.getOther(), null, null);
		
		return new TriggerData(Lists.newArrayList(state.getOther()), Lists.newArrayList(state.getSelf()), world, null);
	}
	
	@Override
	public int getManaCost() {
		return 15;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(1);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Other";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Item.getItemFromBlock(Blocks.GLASS_PANE));
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
	public ItemStack[] supportedFloatCosts() {
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
