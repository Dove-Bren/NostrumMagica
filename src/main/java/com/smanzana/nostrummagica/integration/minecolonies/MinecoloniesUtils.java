package com.smanzana.nostrummagica.integration.minecolonies;

import javax.annotation.Nullable;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MinecoloniesUtils {

	public static final boolean IsSameColony(LivingEntity ent1, LivingEntity ent2) {
		@Nullable IColony colony1 = GetColony(ent1);
		@Nullable IColony colony2 = GetColony(ent2);
		return colony1 != null && colony2 != null && colony1 == colony2;
	}
	
	protected static final @Nullable IColony GetColony(LivingEntity ent) {
		if (ent == null) {
			return null;
		}
		
		if (ent instanceof AbstractEntityCitizen) {
			AbstractEntityCitizen civilian = (AbstractEntityCitizen) ent;
			if (civilian.getCitizenData() != null) {
				return civilian.getCitizenData().getColony();
			}
		}
		
		if (ent instanceof Player) {
			return IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(ent.level, ent.getUUID());
		}
		
		return null;
	}
	
}
