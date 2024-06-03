package com.smanzana.nostrummagica.progression.skill;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class NostrumSkills {

	public static final SkillCategory Category_SpellCrafting = new SkillCategory(NostrumMagica.Loc("spellcrafting"), 0xFF2080A0);
	public static final SkillCategory Category_SpellCasting = new SkillCategory(NostrumMagica.Loc("spellcasting"), 0xFF804020);
	
	private static final ResourceLocation ID_TEST = NostrumMagica.Loc("test");
	private static final ResourceLocation ID_TEST2 = NostrumMagica.Loc("test2");
	private static final ResourceLocation ID_TEST3 = NostrumMagica.Loc("test3");
	private static final ResourceLocation ID_TEST4 = NostrumMagica.Loc("test4");
	
	public static final Skill Test = new Skill(ID_TEST, Category_SpellCrafting, null, () -> new ItemStack(NostrumItems.spellTomeAdvanced), -1, 0);
	public static final Skill Test2 = new Skill(ID_TEST2, Category_SpellCrafting, ID_TEST, () -> new ItemStack(NostrumItems.reagentManiDust), -2, 0);
	public static final Skill Test3 = new Skill(ID_TEST3, Category_SpellCrafting, ID_TEST2, () -> new ItemStack(NostrumItems.magicSwordBase), -2, 1);
	public static final Skill Test4 = new Skill(ID_TEST4, Category_SpellCasting, null, () -> new ItemStack(NostrumItems.enderRod), 1, 0);
	
	public static void init() {
		// Nothing to do but make sure this file is loaded
	}
	
}
