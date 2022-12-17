package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRecipe.RitualMatchInfo;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeConstructGeotoken extends OutcomeSpawnItem {

	final int tokenCount;
	
	public OutcomeConstructGeotoken(int tokenCount) {
		super(ItemStack.EMPTY);
		this.tokenCount = tokenCount;
	}
	
	@Override
	public boolean canPerform(World world, EntityPlayer player, BlockPos center, RitualMatchInfo ingredients) {
		// Requires either a geogem or geotoken. Regardless of which, must contain a location!
		if (PositionCrystal.getBlockPosition(ingredients.center) == null
				&& PositionToken.getBlockPosition(ingredients.center) == null) {
			if (!world.isRemote) {
				player.sendMessage(new TextComponentTranslation("info.create_geotoken.nopos", new Object[0]));
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// set up stack and then call super to spawn it
		this.stack = PositionToken.constructFrom(centerItem, tokenCount);
		
		super.perform(world, player, centerItem, otherItems, center, recipe);
	}

	private static ItemStack RES = ItemStack.EMPTY;
	@Override
	public ItemStack getResult() {
		if (RES.isEmpty())
			RES = new ItemStack(PositionToken.instance());
		
		return RES;
	}
	
	@Override
	public String getName() {
		return "create_geotoken";
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.construct_geotoken.desc",
				new Object[0])
				.split("\\|"));
	}
	
}
