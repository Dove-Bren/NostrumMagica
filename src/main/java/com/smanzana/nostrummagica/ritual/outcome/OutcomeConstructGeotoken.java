package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.PositionToken;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRecipe.RitualMatchInfo;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OutcomeConstructGeotoken extends OutcomeSpawnItem {

	final int tokenCount;
	
	public OutcomeConstructGeotoken(int tokenCount) {
		super(ItemStack.EMPTY);
		this.tokenCount = tokenCount;
	}
	
	@Override
	public boolean canPerform(World world, PlayerEntity player, BlockPos center, RitualMatchInfo ingredients) {
		// Requires either a geogem or geotoken. Regardless of which, must contain a location!
		if (PositionCrystal.getBlockPosition(ingredients.center) == null
				&& PositionToken.getBlockPosition(ingredients.center) == null) {
			if (!world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.create_geotoken.nopos", new Object[0]), Util.DUMMY_UUID);
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// set up stack and then call super to spawn it
		this.stack = PositionToken.constructFrom(centerItem, tokenCount);
		
		super.perform(world, player, centerItem, otherItems, center, recipe);
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
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.construct_geotoken.desc");
	}
	
}
