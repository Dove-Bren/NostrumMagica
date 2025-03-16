package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.block.AltarBlock;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.block.ChalkBlock;
import com.smanzana.nostrummagica.block.ObeliskBlock;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class OutcomeCreateObelisk implements IRitualOutcome {

	public OutcomeCreateObelisk() {
	}
	
	@Override
	public boolean canPerform(Level world, Player player, BlockPos center, IRitualLayout layout) {
		if (!ObeliskBlock.canSpawnObelisk(world, center.offset(0, -1, 0))) {
			if (!world.isClientSide) {
				player.sendMessage(new TranslatableComponent("info.create_obelisk.fail", new Object[0]), Util.NIL_UUID);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// All logic contained in obelisk class
		if (!ObeliskBlock.spawnObelisk(world, center.offset(0, -1, 0))) {
			player.sendMessage(new TranslatableComponent("info.create_obelisk.fail", new Object[0]), Util.NIL_UUID);
		} else if (!world.isClientSide) {
			// clear altar on server
			BlockEntity te = world.getBlockEntity(center.offset(0, 0, 0));
			if (te == null || !(te instanceof AltarTileEntity))
				return;
			((AltarTileEntity) te).setItem(ItemStack.EMPTY);
			
			// Break all altars, chalk, candles
			int radius = 4;
			for (int i = -radius; i <= radius; i++)
			for (int j = -radius; j <= radius; j++) {
				BlockPos pos = center.offset(i, 0, j);
				BlockState state = world.getBlockState(pos);
				if (state != null &&
						(state.getBlock() instanceof CandleBlock || state.getBlock() instanceof AltarBlock || state.getBlock() instanceof ChalkBlock)) {
					world.destroyBlock(pos, true);
				}
			}
		}
	}
	
	@Override
	public String getName() {
		return "create_obelisk";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.create_obelisk.desc");
	}
	
}
