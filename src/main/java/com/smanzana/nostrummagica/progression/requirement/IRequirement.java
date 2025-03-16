package com.smanzana.nostrummagica.progression.requirement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

public interface IRequirement {
	
	public abstract boolean matches(Player player);
	
	/**
	 * Check if this requirement can ever be satisfied. For example, quest requirements
	 * can check that their configured key is actually a quest.
	 * @return
	 */
	public abstract boolean isValid();
	
	public List<Component> getDescription(Player player);
	
	public static IRequirement AND(IRequirement ... requirements) {
		return new IRequirement() {

			@Override
			public boolean matches(Player player) {
				for (IRequirement req : requirements) {
					if (!req.matches(player)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean isValid() {
				for (IRequirement req : requirements) {
					if (!req.isValid()) {
						return false;
					}
				}
				return true;
			}

			@Override
			public List<Component> getDescription(Player player) {
				List<Component> list = new ArrayList<>();
				for (IRequirement req : requirements) {
					list.addAll(req.getDescription(player));
				}
				return list;
			}
			
		};
	}
	
}
