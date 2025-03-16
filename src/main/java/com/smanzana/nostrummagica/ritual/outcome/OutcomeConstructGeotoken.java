package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.PositionToken;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class OutcomeConstructGeotoken extends OutcomeSpawnItem {

	final int tokenCount;
	
	public OutcomeConstructGeotoken(int tokenCount) {
		super(ItemStack.EMPTY);
		this.tokenCount = tokenCount;
	}
	
	@Override
	public boolean canPerform(Level world, Player player, BlockPos center, IRitualLayout layout) {
		// Requires either a geogem or geotoken. Regardless of which, must contain a location!
		if (PositionCrystal.getBlockPosition(layout.getCenterItem(world, center)) == null
				&& PositionToken.getBlockPosition(layout.getCenterItem(world, center)) == null) {
			if (!world.isClientSide) {
				player.sendMessage(new TranslatableComponent("info.create_geotoken.nopos", new Object[0]), Util.NIL_UUID);
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// set up stack and then call super to spawn it
		this.stack = PositionToken.constructFrom(layout.getCenterItem(world, center), tokenCount);
		
		super.perform(world, player, center, layout, recipe);
	}

	private static ItemStack RES = ItemStack.EMPTY;
	@Override
	public ItemStack getResult() {
		if (RES.isEmpty())
			RES = new ItemStack(NostrumItems.positionToken);
		
		return RES;
	}
	
	@Override
	public String getName() {
		return "create_geotoken";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.construct_geotoken.desc");
	}
	
}
