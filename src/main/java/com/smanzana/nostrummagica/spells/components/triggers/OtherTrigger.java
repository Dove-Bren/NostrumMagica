package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class OtherTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "other";
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
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw) {
		
		NostrumMagica.instance.proxy.spawnEffect(state.getOther().world,
				new SpellComponentWrapper(instance()),
				state.getCaster(), null, state.getOther(), null, null, false, 0);
		
		return new TriggerData(Lists.newArrayList(state.getOther()), Lists.newArrayList(state.getSelf()), world, null);
	}
	
	@Override
	public int getManaCost() {
		return 15;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.SPIDER_SILK, 1));
	}

	@Override
	public String getDisplayName() {
		return "Other";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.GLASS_PANE);
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
	
}
