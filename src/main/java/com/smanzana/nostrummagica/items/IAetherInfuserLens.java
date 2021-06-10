package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Item may provide functionallity when aether from an aether infuser is used on it
 * @author Skyler
 *
 */
public interface IAetherInfuserLens {

	/**
	 * Quick check whether this item can even attempt to accept any aether.
	 * No work should be done by this func besides checking if things look roughly right.
	 * @param source
	 * @param maxAether
	 * @return
	 */
	public boolean canAcceptAetherInfuse(ItemStack stack, BlockPos pos, AetherInfuserTileEntity source, int maxAether);
	
	/**
	 * Attempt to accept some aether from an Aether Infuser.
	 * Not all aether must be taken. Instead, return what couldn't be used.
	 * @param source
	 * @param maxAether Max aether the infuser can provide in this call
	 * @return
	 */
	public int acceptAetherInfuse(ItemStack stack, BlockPos pos, AetherInfuserTileEntity source, int maxAether);
	
}
