package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.block.AltarBlock;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.block.ChalkBlock;
import com.smanzana.nostrummagica.block.ObeliskBlock;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRecipe.RitualMatchInfo;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OutcomeCreateObelisk implements IRitualOutcome {

	public OutcomeCreateObelisk() {
	}
	
	@Override
	public boolean canPerform(World world, PlayerEntity player, BlockPos center, RitualMatchInfo ingredients) {
		if (!ObeliskBlock.canSpawnObelisk(world, center.add(0, -1, 0))) {
			if (!world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.create_obelisk.fail", new Object[0]), Util.DUMMY_UUID);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// All logic contained in obelisk class
		if (!ObeliskBlock.spawnObelisk(world, center.add(0, -1, 0))) {
			player.sendMessage(new TranslationTextComponent("info.create_obelisk.fail", new Object[0]), Util.DUMMY_UUID);
		} else if (!world.isRemote) {
			// clear altar on server
			TileEntity te = world.getTileEntity(center.add(0, 0, 0));
			if (te == null || !(te instanceof AltarTileEntity))
				return;
			((AltarTileEntity) te).setItem(ItemStack.EMPTY);
			
			// Break all altars, chalk, candles
			int radius = 4;
			for (int i = -radius; i <= radius; i++)
			for (int j = -radius; j <= radius; j++) {
				BlockPos pos = center.add(i, 0, j);
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
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.create_obelisk.desc");
	}
	
}
