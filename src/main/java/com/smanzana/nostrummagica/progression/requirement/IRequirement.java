package com.smanzana.nostrummagica.progression.requirement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

public interface IRequirement {
	
	public abstract boolean matches(PlayerEntity player);
	
	/**
	 * Check if this requirement can ever be satisfied. For example, quest requirements
	 * can check that their configured key is actually a quest.
	 * @return
	 */
	public abstract boolean isValid();
	
	public List<ITextComponent> getDescription();
	
	public static IRequirement AND(IRequirement ... requirements) {
		return new IRequirement() {

			@Override
			public boolean matches(PlayerEntity player) {
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
			public List<ITextComponent> getDescription() {
				List<ITextComponent> list = new ArrayList<>();
				for (IRequirement req : requirements) {
					list.addAll(req.getDescription());
				}
				return list;
			}
			
		};
	}
	
}
