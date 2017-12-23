package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
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
	protected List<EntityLiving> getTargets(SpellPartParam param, EntityLiving target, World world, BlockPos pos) {
		return Lists.newArrayList(target);
	}

}
