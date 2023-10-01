package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
	protected List<LivingEntity> getTargets(SpellPartParam param, LivingEntity target, World world, BlockPos pos) {
		if (target != null)
			return Lists.newArrayList(target);
		
		return null;
	}

	@Override
	protected List<BlockPos> getTargetLocations(SpellPartParam param, LivingEntity target, World world,
			BlockPos pos) {
		if (target != null)
			return null;
		
		return Lists.newArrayList(pos);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.CRYSTABLOOM, 1)
			);
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Single";
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
