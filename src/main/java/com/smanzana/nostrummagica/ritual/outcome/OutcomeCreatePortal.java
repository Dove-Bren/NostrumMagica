package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.PedestalBlockEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class OutcomeCreatePortal implements IRitualOutcome {

	public OutcomeCreatePortal() {
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		
		world.setBlockAndUpdate(center.below(), NostrumBlocks.sorceryPortalSpawner.defaultBlockState());
		BlockEntity te = world.getBlockEntity(center.offset(0, 0, 0));
		if (te == null || !(te instanceof PedestalBlockEntity))
			return;
		((PedestalBlockEntity) te).setItem(ItemStack.EMPTY);
		world.destroyBlock(center, true);
	}
	
	@Override
	public String getName() {
		return "create_portal";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.create_portal.desc");
	}
	
}
