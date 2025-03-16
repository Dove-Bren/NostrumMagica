package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class OutcomeRecall implements IRitualOutcome {

	public OutcomeRecall() {
		;
	}
	
	@Override
	public boolean canPerform(Level world, Player player, BlockPos center, IRitualLayout layout) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		BlockPos pos = attr.getMarkLocation();
		if (pos == null) {
			if (!world.isClientSide)
				player.sendMessage(new TranslatableComponent("info.recall.fail", new Object[0]), Util.NIL_UUID);
			return false;
		}
		
		if (!DimensionUtils.InDimension(player, attr.getMarkDimension())) {
			if (!world.isClientSide)
				player.sendMessage(new TranslatableComponent("info.recall.baddimension", new Object[0]), Util.NIL_UUID);
			return false;
		}
		
		return true;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Return the player to their marked location, if they have one
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		BlockPos pos = attr.getMarkLocation();
		if (pos == null) {
			if (world.isClientSide)
				player.sendMessage(new TranslatableComponent("info.recall.fail", new Object[0]), Util.NIL_UUID);
			return;
		}
		
		if (DimensionUtils.InDimension(player, attr.getMarkDimension())) {
			if (!world.isClientSide) {
				
				NostrumMagica.attemptTeleport(new Location(world, pos), player, true, NostrumMagica.rand.nextInt(4) == 0, player);
				
//				if (attr.hasEnhancedTeleport()) {
//					BlockPos portal = TemporaryTeleportationPortal.spawnNearby(world, center.up(), 4, true, pos, 20 * 30);
//					if (portal != null) {
//						TemporaryTeleportationPortal.spawnNearby(world, pos, 4, true, portal, 20 * 30);
//					}
//				} else {
//					player.setPositionAndUpdate(pos.getX() + .5, pos.getY() + .1, pos.getZ() + .5);
//				}
//				
//				if (NostrumMagica.rand.nextInt(10) == 0) {
//					float dist = 2 + NostrumMagica.rand.nextFloat() * 2;
//					float dir = NostrumMagica.rand.nextFloat();
//					double dirD = dir * 2 * Math.PI;
//					double dx = Math.cos(dirD) * dist;
//					double dz = Math.sin(dirD) * dist;
//					ItemEntity drop = new ItemEntity(world, pos.getX() + .5 + dx, pos.getY() + 2, pos.getZ() + .5 + dz,
//							NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1));
//					world.addEntity(drop);
//					NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
//				}
			}
		} else {
			player.sendMessage(new TranslatableComponent("info.recall.baddimension", new Object[0]), Util.NIL_UUID);
		}
	}
	
	@Override
	public String getName() {
		return "recall";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.recall.desc");
	}
}
