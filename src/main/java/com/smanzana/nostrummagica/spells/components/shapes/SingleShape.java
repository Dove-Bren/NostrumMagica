package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SingleShape extends SpellShape {

	private static final String SHAPE_KEY = "shape_single";
	private static SingleShape instance = null;
	
	public static SingleShape instance() {
		if (instance == null)
			instance = new SingleShape();
		
		return instance;
	}
	
	private SingleShape() {
		super(SHAPE_KEY);
	}

	@Override
	protected List<EntityLivingBase> getTargets(SpellPartParam param, EntityLivingBase target, World world, BlockPos pos) {
		if (target != null)
			return Lists.newArrayList(target);
		
		return null;
	}

	@Override
	protected List<BlockPos> getTargetLocations(SpellPartParam param, EntityLivingBase target, World world,
			BlockPos pos) {
		if (target != null)
			return null;
		
		return Lists.newArrayList(pos);
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.GINSENG, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
		
		return list;
	}

}
