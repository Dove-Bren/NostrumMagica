package com.smanzana.nostrummagica.blocks;

import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;

/**
 * Like HorizontalBlock but an interface
 * @author Skyler
 *
 */
public interface IDirectionalBlock {
	
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

}
