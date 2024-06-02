package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class OutcomeCreatePortal implements IRitualOutcome {

	public OutcomeCreatePortal() {
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		
		world.setBlockState(center.down(), NostrumBlocks.sorceryPortalSpawner.getDefaultState());
		TileEntity te = world.getTileEntity(center.add(0, 0, 0));
		if (te == null || !(te instanceof AltarTileEntity))
			return;
		((AltarTileEntity) te).setItem(ItemStack.EMPTY);
		world.destroyBlock(center, true);
	}
	
	@Override
	public String getName() {
		return "create_portal";
	}

	@Override
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.create_portal.desc");
	}
	
}
