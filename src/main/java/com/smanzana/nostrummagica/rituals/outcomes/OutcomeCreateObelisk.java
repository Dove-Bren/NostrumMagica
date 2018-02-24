package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeCreateObelisk implements IRitualOutcome {

	public OutcomeCreateObelisk() {
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// All logic contained in obelisk class
		if (!NostrumObelisk.spawnObelisk(world, center.add(0, -1, 0))) {
			if (world.isRemote)
				player.addChatMessage(new TextComponentTranslation("info.create_obelisk.fail", new Object[0]));
		} else if (!world.isRemote) {
			// clear altar on server
			TileEntity te = world.getTileEntity(center.add(0, 0, 0));
			if (te == null || !(te instanceof AltarTileEntity))
				return;
			((AltarTileEntity) te).setItem(null);
			
			// Break all altars, chalk, candles
			int radius = 4;
			for (int i = -radius; i <= radius; i++)
			for (int j = -radius; j <= radius; j++) {
				BlockPos pos = center.add(i, 0, j);
				IBlockState state = world.getBlockState(pos);
				if (state != null &&
						(state.getBlock() instanceof Candle || state.getBlock() instanceof AltarBlock || state.getBlock() instanceof ChalkBlock)) {
					world.destroyBlock(pos, false);
				}
			}
		}
	}
	
}
