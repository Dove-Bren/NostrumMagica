package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.SorceryPortalSpawner;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeCreatePortal implements IRitualOutcome {

	public OutcomeCreatePortal() {
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		
		world.setBlockState(center.down(), SorceryPortalSpawner.instance().getDefaultState());
		TileEntity te = world.getTileEntity(center.add(0, 0, 0));
		if (te == null || !(te instanceof AltarTileEntity))
			return;
		((AltarTileEntity) te).setItem(null);
		world.destroyBlock(center, true);
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.create_portal.desc",
				(Object[]) null)
				.split("\\|"));
	}
	
}
