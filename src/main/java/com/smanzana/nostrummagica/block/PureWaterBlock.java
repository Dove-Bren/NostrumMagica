package com.smanzana.nostrummagica.block;

import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;

public class PureWaterBlock extends LiquidBlock {

	public static final String ID = "pure_water_block";
	
	public PureWaterBlock(Supplier<? extends FlowingFluid> supplier) {
		super(supplier, Block.Properties.of(Material.WATER)
				.noCollission().strength(100.0F).noDrops()
				);
	}
}
