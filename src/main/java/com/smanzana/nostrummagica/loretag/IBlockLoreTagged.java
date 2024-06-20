package com.smanzana.nostrummagica.loretag;

import net.minecraft.block.Block;

/**
 * Subclass of ILoreTagged for blocks that can't be actually set up as ILoreTagged.
 * @author Skyler
 *
 */
public interface IBlockLoreTagged extends ILoreTagged {
	
	public Block getBlock();
}
