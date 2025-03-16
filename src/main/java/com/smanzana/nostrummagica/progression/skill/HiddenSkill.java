package com.smanzana.nostrummagica.progression.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.progression.requirement.IRequirement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class HiddenSkill extends Skill {

	protected IRequirement[] requirements;
	
	public HiddenSkill(ResourceLocation key,
			SkillCategory category,
			@Nullable ResourceLocation parent,
			Supplier<ItemStack> icon,
			int x,
			int y,
			IRequirement ... requirements) {
		super(key, category, parent, icon, x, y);
		this.requirements = requirements;
	}
	
	public IRequirement[] getRequirements() {
		return this.requirements;
	}
	
	@Override
	public boolean isHidden(Player player) {
		return !meetsRequirements(player);
	}
	
	@Override
	public boolean meetsRequirements(Player player) {
		if (!super.meetsRequirements(player)) {
			return false;
		}
		
		if (requirements != null) {
			for (IRequirement req : requirements) {
				if (!req.matches(player)) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	protected void validate() {
		super.validate();
		
		// Check requirements
		if (this.requirements != null) {
			List<IRequirement> list = new ArrayList<>(this.requirements.length);
			for (IRequirement req : this.requirements) {
				if (req.isValid()) {
					list.add(req);
				}
			}
			if (list.isEmpty()) {
				this.requirements = null;
			} else {
				this.requirements = list.toArray(new IRequirement[list.size()]);
			}
		}
	}
	
}
