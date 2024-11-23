package com.smanzana.nostrummagica.integration.minecolonies;

import javax.annotation.Nullable;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

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
		
		if (ent instanceof AbstractCivilianEntity) {
			AbstractCivilianEntity civilian = (AbstractCivilianEntity) ent;
			if (civilian.getCivilianData() != null) {
				return civilian.getCivilianData().getColony();
			}
		}
		
		if (ent instanceof PlayerEntity) {
			return IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(ent.world, ent.getUniqueID());
		}
		
		return null;
	}
	
}
