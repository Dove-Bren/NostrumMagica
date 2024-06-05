package com.smanzana.nostrummagica.progression.skill;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.progression.requirement.ElementMasteryRequirement;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public class NostrumSkills {

	public static final SkillCategory Category_SpellCrafting = new SkillCategory(NostrumMagica.Loc("spellcrafting"), 0xFF2080A0);
	public static final SkillCategory Category_SpellCasting = new SkillCategory(NostrumMagica.Loc("spellcasting"), 0xFF804020);
	public static final SkillCategory Category_Element_Physical = new SkillCategory(NostrumMagica.Loc(EMagicElement.PHYSICAL.name().toLowerCase()), EMagicElement.PHYSICAL.getColor());
	public static final SkillCategory Category_Element_Fire = new SkillCategory(NostrumMagica.Loc(EMagicElement.FIRE.name().toLowerCase()), EMagicElement.FIRE.getColor());
	public static final SkillCategory Category_Element_Ice = new SkillCategory(NostrumMagica.Loc(EMagicElement.ICE.name().toLowerCase()), EMagicElement.ICE.getColor());
	public static final SkillCategory Category_Element_Earth = new SkillCategory(NostrumMagica.Loc(EMagicElement.EARTH.name().toLowerCase()), EMagicElement.EARTH.getColor());
	public static final SkillCategory Category_Element_Wind = new SkillCategory(NostrumMagica.Loc(EMagicElement.WIND.name().toLowerCase()), EMagicElement.WIND.getColor());
	public static final SkillCategory Category_Element_Lightning = new SkillCategory(NostrumMagica.Loc(EMagicElement.LIGHTNING.name().toLowerCase()), EMagicElement.LIGHTNING.getColor());
	public static final SkillCategory Category_Element_Ender = new SkillCategory(NostrumMagica.Loc(EMagicElement.ENDER.name().toLowerCase()), EMagicElement.ENDER.getColor());

	private static final ResourceLocation ID_SPELLCRAFT_TWOSHAPES = NostrumMagica.Loc("spellcraft.twoshapes");
	private static final ResourceLocation ID_SPELLCRAFT_ELEMBUILDING = NostrumMagica.Loc("spellcraft.element_building");
	private static final ResourceLocation ID_SPELLCRAFT_WEIGHT1 = NostrumMagica.Loc("spellcraft.weight1");
	private static final ResourceLocation ID_SPELLCRAFT_ELEMWEIGHT = NostrumMagica.Loc("spellcraft.elemweight");
	private static final ResourceLocation ID_SPELLCRAFT_MANADISCOUNT1 = NostrumMagica.Loc("spellcraft.manadiscount1");
	private static final ResourceLocation ID_SPELLCRAFT_MANADISCOUNT2 = NostrumMagica.Loc("spellcraft.manadiscount2");

	private static final ResourceLocation ID_SPELLCAST_ELEMFREE = NostrumMagica.Loc("spellcast.elemfree");
	private static final ResourceLocation ID_SPELLCAST_ELEMWEIGHT = NostrumMagica.Loc("spellcast.elemweight");
	private static final ResourceLocation ID_SPELLCAST_ELEMMANA = NostrumMagica.Loc("spellcast.elemmana");
	private static final ResourceLocation ID_SPELLCAST_ELEMLINGER = NostrumMagica.Loc("spellcast.elemlinger");
	private static final ResourceLocation ID_SPELLCAST_WEIGHT1 = NostrumMagica.Loc("spellcast.weight1");
	private static final ResourceLocation ID_SPELLCAST_POTENCY1 = NostrumMagica.Loc("spellcast.potency1");
	
	private static final ResourceLocation ID_PHYSICAL_NOVICE = NostrumMagica.Loc("physical.novice");
	private static final ResourceLocation ID_PHYSICAL_ADEPT = NostrumMagica.Loc("physical.adept");
	private static final ResourceLocation ID_PHYSICAL_MASTER = NostrumMagica.Loc("physical.master");

	private static final ResourceLocation ID_FIRE_NOVICE = NostrumMagica.Loc("fire.novice");
	private static final ResourceLocation ID_FIRE_ADEPT = NostrumMagica.Loc("fire.adept");
	private static final ResourceLocation ID_FIRE_MASTER = NostrumMagica.Loc("fire.master");
	
	private static final ResourceLocation ID_ICE_NOVICE = NostrumMagica.Loc("ice.novice");
	private static final ResourceLocation ID_ICE_ADEPT = NostrumMagica.Loc("ice.adept");
	private static final ResourceLocation ID_ICE_MASTER = NostrumMagica.Loc("ice.master");
	
	private static final ResourceLocation ID_EARTH_NOVICE = NostrumMagica.Loc("earth.novice");
	private static final ResourceLocation ID_EARTH_ADEPT = NostrumMagica.Loc("earth.adept");
	private static final ResourceLocation ID_EARTH_MASTER = NostrumMagica.Loc("earth.master");
	
	private static final ResourceLocation ID_WIND_NOVICE = NostrumMagica.Loc("wind.novice");
	private static final ResourceLocation ID_WIND_ADEPT = NostrumMagica.Loc("wind.adept");
	private static final ResourceLocation ID_WIND_MASTER = NostrumMagica.Loc("wind.master");
	
	private static final ResourceLocation ID_LIGHTNING_NOVICE = NostrumMagica.Loc("lightning.novice");
	private static final ResourceLocation ID_LIGHTNING_ADEPT = NostrumMagica.Loc("lightning.adept");
	private static final ResourceLocation ID_LIGHTNING_MASTER = NostrumMagica.Loc("lightning.master");
	
	private static final ResourceLocation ID_ENDER_NOVICE = NostrumMagica.Loc("ender.novice");
	private static final ResourceLocation ID_ENDER_ADEPT = NostrumMagica.Loc("ender.adept");
	private static final ResourceLocation ID_ENDER_MASTER = NostrumMagica.Loc("ender.master");
	
	public static /*final*/ Skill Spellcraft_TwoShapes;
	public static /*final*/ Skill Spellcraft_ElemBuilding;
	public static /*final*/ Skill Spellcraft_Weight1;
	public static /*final*/ Skill Spellcraft_ElemWeight;
	public static /*final*/ Skill Spellcraft_ManaDiscount1;
	public static /*final*/ Skill Spellcraft_ManaDiscount2;

	public static /*final*/ Skill Spellcasting_ElemFree;
	public static /*final*/ Skill Spellcasting_ElemWeight;
	public static /*final*/ Skill Spellcasting_Weight1;
	public static /*final*/ Skill Spellcasting_Potency1;
	public static /*final*/ Skill Spellcasting_ElemMana;
	public static /*final*/ Skill Spellcasting_ElemLinger;
	
	public static /*final*/ Skill Physical_Novice;
	public static /*final*/ Skill Physical_Adept;
	public static /*final*/ Skill Physical_Master;

	public static /*final*/ Skill Fire_Novice;
	public static /*final*/ Skill Fire_Adept;
	public static /*final*/ Skill Fire_Master;

	public static /*final*/ Skill Ice_Novice;
	public static /*final*/ Skill Ice_Adept;
	public static /*final*/ Skill Ice_Master;

	public static /*final*/ Skill Earth_Novice;
	public static /*final*/ Skill Earth_Adept;
	public static /*final*/ Skill Earth_Master;

	public static /*final*/ Skill Wind_Novice;
	public static /*final*/ Skill Wind_Adept;
	public static /*final*/ Skill Wind_Master;

	public static /*final*/ Skill Lightning_Novice;
	public static /*final*/ Skill Lightning_Adept;
	public static /*final*/ Skill Lightning_Master;

	public static /*final*/ Skill Ender_Novice;
	public static /*final*/ Skill Ender_Adept;
	public static /*final*/ Skill Ender_Master;
	
	public static void init() {
		// Nothing to do but make sure this file is loaded
		
		int unused; // testing code
		{
			Skill.ClearSkills();
			int x = -1;
			int y = 1;
			Spellcraft_TwoShapes = new Skill(ID_SPELLCRAFT_TWOSHAPES, Category_SpellCrafting, null, () -> SpellRune.getRune(NostrumSpellShapes.Touch), x, y);
			Spellcraft_ElemBuilding = new Skill(ID_SPELLCRAFT_ELEMBUILDING, Category_SpellCrafting, ID_SPELLCRAFT_TWOSHAPES, () -> SpellRune.getRune(EMagicElement.ICE), x-1, y+1);
			Spellcraft_ElemWeight = new Skill(ID_SPELLCRAFT_ELEMWEIGHT, Category_SpellCrafting, ID_SPELLCRAFT_ELEMBUILDING, () -> new ItemStack(Items.FEATHER), x-2, y+1);
			Spellcraft_Weight1 = new Skill(ID_SPELLCRAFT_WEIGHT1, Category_SpellCrafting, ID_SPELLCRAFT_TWOSHAPES, () -> new ItemStack(NostrumItems.masteryOrb), x-1, y-1);
			Spellcraft_ManaDiscount1 = new Skill(ID_SPELLCRAFT_MANADISCOUNT1, Category_SpellCrafting, ID_SPELLCRAFT_WEIGHT1, () -> new ItemStack(Items.BOOK), x-2, y-1);
			Spellcraft_ManaDiscount2 = new Skill(ID_SPELLCRAFT_MANADISCOUNT2, Category_SpellCrafting, ID_SPELLCRAFT_MANADISCOUNT1, () -> new ItemStack(Items.WRITABLE_BOOK), x-3, y-1);
			
			x = 1;
			y = 1;
			Spellcasting_ElemFree = new Skill(ID_SPELLCAST_ELEMFREE, Category_SpellCasting, null, () -> new ItemStack(NostrumItems.mageStaff), x, y+0);
			Spellcasting_ElemWeight = new Skill(ID_SPELLCAST_ELEMWEIGHT, Category_SpellCasting, ID_SPELLCAST_ELEMFREE, () -> new ItemStack(NostrumItems.crystalSmall), x+1, y+1);
			Spellcasting_Weight1 = new Skill(ID_SPELLCAST_WEIGHT1, Category_SpellCasting, ID_SPELLCAST_ELEMWEIGHT, () -> new ItemStack(NostrumItems.reagentSpiderSilk), x+2, y+1);
			Spellcasting_Potency1 = new Skill(ID_SPELLCAST_POTENCY1, Category_SpellCasting, ID_SPELLCAST_ELEMWEIGHT, () -> new ItemStack(NostrumItems.infusedGemUnattuned), x+2, y);
			Spellcasting_ElemMana = new Skill(ID_SPELLCAST_ELEMMANA, Category_SpellCasting, ID_SPELLCAST_ELEMFREE, () -> new ItemStack(NostrumItems.spellScroll), x+1, y-1);
			Spellcasting_ElemLinger = new Skill(ID_SPELLCAST_ELEMLINGER, Category_SpellCasting, ID_SPELLCAST_ELEMMANA, () -> new ItemStack(NostrumItems.thanosStaff), x+2, y-1);
			
			x = 0;
			y = -1;
			Physical_Novice = new HiddenSkill(ID_PHYSICAL_NOVICE, Category_Element_Physical, null, () -> SpellRune.getRune(EMagicElement.PHYSICAL), x, y, new ElementMasteryRequirement(EMagicElement.PHYSICAL, EElementalMastery.NOVICE));
			Physical_Adept = new HiddenSkill(ID_PHYSICAL_ADEPT, Category_Element_Physical, ID_PHYSICAL_NOVICE, () -> new ItemStack(Items.IRON_CHESTPLATE), x, y-1, new ElementMasteryRequirement(EMagicElement.PHYSICAL, EElementalMastery.ADEPT));
			Physical_Master = new HiddenSkill(ID_PHYSICAL_MASTER, Category_Element_Physical, ID_PHYSICAL_ADEPT, () -> new ItemStack(Items.NETHERITE_SWORD), x, y-2, new ElementMasteryRequirement(EMagicElement.PHYSICAL, EElementalMastery.MASTER));
			
			x = -1;
			y = -1;
			Fire_Novice = new HiddenSkill(ID_FIRE_NOVICE, Category_Element_Fire, null, () -> SpellRune.getRune(EMagicElement.FIRE), x, y, new ElementMasteryRequirement(EMagicElement.FIRE, EElementalMastery.NOVICE));
			Fire_Adept = new HiddenSkill(ID_FIRE_ADEPT, Category_Element_Fire, ID_FIRE_NOVICE, () -> new ItemStack(NostrumItems.infusedGemFire), x, y-1, new ElementMasteryRequirement(EMagicElement.FIRE, EElementalMastery.ADEPT));
			Fire_Master = new HiddenSkill(ID_FIRE_MASTER, Category_Element_Fire, ID_FIRE_ADEPT, () -> new ItemStack(Items.SOUL_CAMPFIRE), x, y-2, new ElementMasteryRequirement(EMagicElement.FIRE, EElementalMastery.MASTER));
			
			x = 1;
			y = -1;
			Ice_Novice = new HiddenSkill(ID_ICE_NOVICE, Category_Element_Ice, null, () -> SpellRune.getRune(EMagicElement.ICE), x, y, new ElementMasteryRequirement(EMagicElement.ICE, EElementalMastery.NOVICE));
			Ice_Adept = new HiddenSkill(ID_ICE_ADEPT, Category_Element_Ice, ID_ICE_NOVICE, () -> new ItemStack(NostrumItems.infusedGemIce), x, y-1, new ElementMasteryRequirement(EMagicElement.ICE, EElementalMastery.ADEPT));
			Ice_Master = new HiddenSkill(ID_ICE_MASTER, Category_Element_Ice, ID_ICE_ADEPT, () -> SpellRune.getRune(NostrumSpellShapes.OnHealth), x, y-2, new ElementMasteryRequirement(EMagicElement.ICE, EElementalMastery.MASTER));
			
			x = 2;
			y = -1;
			Earth_Novice = new HiddenSkill(ID_EARTH_NOVICE, Category_Element_Earth, null, () -> SpellRune.getRune(EMagicElement.EARTH), x, y, new ElementMasteryRequirement(EMagicElement.EARTH, EElementalMastery.NOVICE));
			Earth_Adept = new HiddenSkill(ID_EARTH_ADEPT, Category_Element_Earth, ID_EARTH_NOVICE, () -> new ItemStack(Items.NETHERITE_AXE), x, y-1, new ElementMasteryRequirement(EMagicElement.EARTH, EElementalMastery.ADEPT));
			Earth_Master = new HiddenSkill(ID_EARTH_MASTER, Category_Element_Earth, ID_EARTH_ADEPT, () -> new ItemStack(Items.GLISTERING_MELON_SLICE), x, y-2, new ElementMasteryRequirement(EMagicElement.EARTH, EElementalMastery.MASTER));
			
			x = -2;
			y = -1;
			Wind_Novice = new HiddenSkill(ID_WIND_NOVICE, Category_Element_Wind, null, () -> SpellRune.getRune(EMagicElement.WIND), x, y, new ElementMasteryRequirement(EMagicElement.WIND, EElementalMastery.NOVICE));
			Wind_Adept = new HiddenSkill(ID_WIND_ADEPT, Category_Element_Wind, ID_WIND_NOVICE, () -> SpellRune.getRune(NostrumSpellShapes.Cutter), x, y-1, new ElementMasteryRequirement(EMagicElement.WIND, EElementalMastery.ADEPT));
			Wind_Master = new HiddenSkill(ID_WIND_MASTER, Category_Element_Wind, ID_WIND_ADEPT, () -> new ItemStack(NostrumItems.magicCharmWind), x, y-2, new ElementMasteryRequirement(EMagicElement.WIND, EElementalMastery.MASTER));
			
			x = -3;
			y = -1;
			Lightning_Novice = new HiddenSkill(ID_LIGHTNING_NOVICE, Category_Element_Lightning, null, () -> SpellRune.getRune(EMagicElement.LIGHTNING), x, y, new ElementMasteryRequirement(EMagicElement.LIGHTNING, EElementalMastery.NOVICE));
			Lightning_Adept = new HiddenSkill(ID_LIGHTNING_ADEPT, Category_Element_Lightning, ID_LIGHTNING_NOVICE, () -> new ItemStack(NostrumItems.enchantedWeaponLightningAdept), x, y-1, new ElementMasteryRequirement(EMagicElement.LIGHTNING, EElementalMastery.ADEPT));
			Lightning_Master = new HiddenSkill(ID_LIGHTNING_MASTER, Category_Element_Lightning, ID_LIGHTNING_ADEPT, () -> new ItemStack(NostrumItems.magicCharmLightning), x, y-2, new ElementMasteryRequirement(EMagicElement.LIGHTNING, EElementalMastery.MASTER));
			
			x = 3;
			y = -1;
			Ender_Novice = new HiddenSkill(ID_ENDER_NOVICE, Category_Element_Ender, null, () -> SpellRune.getRune(EMagicElement.ENDER), x, y, new ElementMasteryRequirement(EMagicElement.ENDER, EElementalMastery.NOVICE));
			Ender_Adept = new HiddenSkill(ID_ENDER_ADEPT, Category_Element_Ender, ID_ENDER_NOVICE, () -> new ItemStack(Items.ENDER_PEARL), x, y-1, new ElementMasteryRequirement(EMagicElement.ENDER, EElementalMastery.ADEPT));
			Ender_Master = new HiddenSkill(ID_ENDER_MASTER, Category_Element_Ender, ID_ENDER_ADEPT, () -> new ItemStack(Items.ENDER_EYE), x, y-2, new ElementMasteryRequirement(EMagicElement.ENDER, EElementalMastery.MASTER));
		}
	}
	
}
