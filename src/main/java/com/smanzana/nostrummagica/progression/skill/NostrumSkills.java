package com.smanzana.nostrummagica.progression.skill;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.progression.requirement.ElementMasteryRequirement;
import com.smanzana.nostrummagica.progression.requirement.ResearchRequirement;
import com.smanzana.nostrummagica.progression.requirement.SpellKnowledgeRequirement;
import com.smanzana.nostrummagica.progression.requirement.TierRequirement;
import com.smanzana.nostrummagica.progression.research.NostrumResearches;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

public class NostrumSkills {

	public static final SkillCategory Category_Magica = new SkillCategory(NostrumMagica.Loc("magica"), 0xFF2080A0, () -> new ItemStack(NostrumItems.spellScroll), null);
	public static final SkillCategory Category_Element_Neutral = new SkillCategory(NostrumMagica.Loc(EMagicElement.NEUTRAL.name().toLowerCase()), EMagicElement.NEUTRAL.getColor(), () -> SpellRune.getRune(EMagicElement.NEUTRAL), EMagicElement.NEUTRAL);
	public static final SkillCategory Category_Element_Fire = new SkillCategory(NostrumMagica.Loc(EMagicElement.FIRE.name().toLowerCase()), EMagicElement.FIRE.getColor(), () -> SpellRune.getRune(EMagicElement.FIRE), EMagicElement.FIRE);
	public static final SkillCategory Category_Element_Ice = new SkillCategory(NostrumMagica.Loc(EMagicElement.ICE.name().toLowerCase()), EMagicElement.ICE.getColor(), () -> SpellRune.getRune(EMagicElement.ICE), EMagicElement.ICE);
	public static final SkillCategory Category_Element_Earth = new SkillCategory(NostrumMagica.Loc(EMagicElement.EARTH.name().toLowerCase()), EMagicElement.EARTH.getColor(), () -> SpellRune.getRune(EMagicElement.EARTH), EMagicElement.EARTH);
	public static final SkillCategory Category_Element_Wind = new SkillCategory(NostrumMagica.Loc(EMagicElement.WIND.name().toLowerCase()), EMagicElement.WIND.getColor(), () -> SpellRune.getRune(EMagicElement.WIND), EMagicElement.WIND);
	public static final SkillCategory Category_Element_Lightning = new SkillCategory(NostrumMagica.Loc(EMagicElement.LIGHTNING.name().toLowerCase()), EMagicElement.LIGHTNING.getColor(), () -> SpellRune.getRune(EMagicElement.LIGHTNING), EMagicElement.LIGHTNING);
	public static final SkillCategory Category_Element_Ender = new SkillCategory(NostrumMagica.Loc(EMagicElement.ENDER.name().toLowerCase()), EMagicElement.ENDER.getColor(), () -> SpellRune.getRune(EMagicElement.ENDER), EMagicElement.ENDER);

	private static final ResourceLocation ID_SPELLCRAFT_TWOSHAPES = NostrumMagica.Loc("spellcraft.twoshapes");
	private static final ResourceLocation ID_SPELLCRAFT_ELEMBUILDING = NostrumMagica.Loc("spellcraft.element_building");
	private static final ResourceLocation ID_SPELLCRAFT_WEIGHT1 = NostrumMagica.Loc("spellcraft.weight1");
	private static final ResourceLocation ID_SPELLCRAFT_ELEMWEIGHT = NostrumMagica.Loc("spellcraft.elemweight");
	private static final ResourceLocation ID_SPELLCRAFT_MANADISCOUNT1 = NostrumMagica.Loc("spellcraft.manadiscount1");
	private static final ResourceLocation ID_SPELLCRAFT_MANADISCOUNT2 = NostrumMagica.Loc("spellcraft.manadiscount2");
	private static final ResourceLocation ID_SPELLCRAFT_INFOPANEL = NostrumMagica.Loc("spellcraft.infopanel");
	private static final ResourceLocation ID_SPELLCRAFT_ALTERATIONS = NostrumMagica.Loc("spellcraft.alterations");
	private static final ResourceLocation ID_SPELLCRAFT_PROXY = NostrumMagica.Loc("spellcraft.proxy");

	private static final ResourceLocation ID_SPELLCAST_POTENCY1 = NostrumMagica.Loc("spellcast.potency1");
	private static final ResourceLocation ID_SPELLCAST_ELEMLINGER = NostrumMagica.Loc("spellcast.elemlinger");
	private static final ResourceLocation ID_SPELLCAST_ELEMLINGEREATER = NostrumMagica.Loc("spellcast.elemlingereater");
	private static final ResourceLocation ID_SPELLCAST_TOOLCOOLDOWN = NostrumMagica.Loc("spellcast.toolcooldown");
	private static final ResourceLocation ID_SPELLCAST_COOLDOWNREDUC = NostrumMagica.Loc("spellcast.cooldownreduc");
	private static final ResourceLocation ID_SPELLCAST_OVERCHARGE = NostrumMagica.Loc("spellcast.overcharge");
	private static final ResourceLocation ID_SPELLCAST_FAST_OVERCHARGE = NostrumMagica.Loc("spellcast.fastovercharge");
	private static final ResourceLocation ID_SPELLCAST_STRONG_OVERCHARGE = NostrumMagica.Loc("spellcast.strongovercharge");
	// Also mid-charging actions to speed up?

	private static final ResourceLocation ID_CRAFTEDCAST_FASTBASECAST = NostrumMagica.Loc("craftcast.fastbasecast");
	private static final ResourceLocation ID_CRAFTEDCAST_TOMEHANDS = NostrumMagica.Loc("craftcast.tomehands");
	private static final ResourceLocation ID_CRAFTEDCAST_ELEMFREE = NostrumMagica.Loc("craftcast.elemfree");
	private static final ResourceLocation ID_CRAFTEDCAST_ELEMWEIGHT = NostrumMagica.Loc("craftcast.elemweight");
	private static final ResourceLocation ID_CRAFTEDCAST_ELEMMANA = NostrumMagica.Loc("craftcast.elemmana");
	private static final ResourceLocation ID_CRAFTEDCAST_WEIGHT1 = NostrumMagica.Loc("craftcast.weight1");
	private static final ResourceLocation ID_CRAFTEDCAST_POTENCY1 = NostrumMagica.Loc("craftcast.potency1");
	private static final ResourceLocation ID_CRAFTEDCAST_SCROLLEFFICIENCY = NostrumMagica.Loc("craftcast.scrollefficiency");

	private static final ResourceLocation ID_INCANTING_TWOSHAPES = NostrumMagica.Loc("incanting.twoshapes");
	private static final ResourceLocation ID_INCANTING_ALLSHAPES = NostrumMagica.Loc("incanting.allshapes");
	private static final ResourceLocation ID_INCANTING_SELECT_INFO = NostrumMagica.Loc("incanting.select_info");
	private static final ResourceLocation ID_INCANTING_TOMEUSE = NostrumMagica.Loc("incanting.tomeuse");
	private static final ResourceLocation ID_INCANTING_MANADISCOUNT1 = NostrumMagica.Loc("incanting.manadiscount1");
	private static final ResourceLocation ID_INCANTING_POTENCY1 = NostrumMagica.Loc("incanting.potency1");
	private static final ResourceLocation ID_INCANTING_POTENCY2 = NostrumMagica.Loc("incanting.potency2");
	
	private static final ResourceLocation ID_NEUTRAL_NOVICE = NostrumMagica.Loc("neutral.novice");
	private static final ResourceLocation ID_NEUTRAL_ADEPT = NostrumMagica.Loc("neutral.adept");
	private static final ResourceLocation ID_NEUTRAL_MASTER = NostrumMagica.Loc("neutral.master");
	private static final ResourceLocation ID_NEUTRAL_CORRUPT = NostrumMagica.Loc("neutral.corrupt");
	private static final ResourceLocation ID_NEUTRAL_WEAPON = NostrumMagica.Loc("neutral.weapon");
	private static final ResourceLocation ID_NEUTRAL_INFLICT = NostrumMagica.Loc("neutral.inflict");
	private static final ResourceLocation ID_NEUTRAL_GROWTH = NostrumMagica.Loc("neutral.growth");

	private static final ResourceLocation ID_FIRE_NOVICE = NostrumMagica.Loc("fire.novice");
	private static final ResourceLocation ID_FIRE_ADEPT = NostrumMagica.Loc("fire.adept");
	private static final ResourceLocation ID_FIRE_MASTER = NostrumMagica.Loc("fire.master");
	private static final ResourceLocation ID_FIRE_CORRUPT = NostrumMagica.Loc("fire.corrupt");
	private static final ResourceLocation ID_FIRE_WEAPON = NostrumMagica.Loc("fire.weapon");
	private static final ResourceLocation ID_FIRE_INFLICT = NostrumMagica.Loc("fire.inflict");
	private static final ResourceLocation ID_FIRE_GROWTH = NostrumMagica.Loc("fire.growth");
	
	private static final ResourceLocation ID_ICE_NOVICE = NostrumMagica.Loc("ice.novice");
	private static final ResourceLocation ID_ICE_ADEPT = NostrumMagica.Loc("ice.adept");
	private static final ResourceLocation ID_ICE_MASTER = NostrumMagica.Loc("ice.master");
	private static final ResourceLocation ID_ICE_CORRUPT = NostrumMagica.Loc("ice.corrupt");
	private static final ResourceLocation ID_ICE_WEAPON = NostrumMagica.Loc("ice.weapon");
	private static final ResourceLocation ID_ICE_INFLICT = NostrumMagica.Loc("ice.inflict");
	private static final ResourceLocation ID_ICE_SUPPORT = NostrumMagica.Loc("ice.support");
	
	private static final ResourceLocation ID_EARTH_NOVICE = NostrumMagica.Loc("earth.novice");
	private static final ResourceLocation ID_EARTH_ADEPT = NostrumMagica.Loc("earth.adept");
	private static final ResourceLocation ID_EARTH_MASTER = NostrumMagica.Loc("earth.master");
	private static final ResourceLocation ID_EARTH_CORRUPT = NostrumMagica.Loc("earth.corrupt");
	private static final ResourceLocation ID_EARTH_WEAPON = NostrumMagica.Loc("earth.weapon");
	private static final ResourceLocation ID_EARTH_INFLICT = NostrumMagica.Loc("earth.inflict");
	private static final ResourceLocation ID_EARTH_SUPPORT = NostrumMagica.Loc("earth.support");
	
	private static final ResourceLocation ID_WIND_NOVICE = NostrumMagica.Loc("wind.novice");
	private static final ResourceLocation ID_WIND_ADEPT = NostrumMagica.Loc("wind.adept");
	private static final ResourceLocation ID_WIND_MASTER = NostrumMagica.Loc("wind.master");
	private static final ResourceLocation ID_WIND_CORRUPT = NostrumMagica.Loc("wind.corrupt");
	private static final ResourceLocation ID_WIND_WEAPON = NostrumMagica.Loc("wind.weapon");
	private static final ResourceLocation ID_WIND_INFLICT = NostrumMagica.Loc("wind.inflict");
	private static final ResourceLocation ID_WIND_SUPPORT = NostrumMagica.Loc("wind.support");
	
	private static final ResourceLocation ID_LIGHTNING_NOVICE = NostrumMagica.Loc("lightning.novice");
	private static final ResourceLocation ID_LIGHTNING_ADEPT = NostrumMagica.Loc("lightning.adept");
	private static final ResourceLocation ID_LIGHTNING_MASTER = NostrumMagica.Loc("lightning.master");
	private static final ResourceLocation ID_LIGHTNING_CORRUPT = NostrumMagica.Loc("lightning.corrupt");
	private static final ResourceLocation ID_LIGHTNING_WEAPON = NostrumMagica.Loc("lightning.weapon");
	private static final ResourceLocation ID_LIGHTNING_INFLICT = NostrumMagica.Loc("lightning.inflict");
	private static final ResourceLocation ID_LIGHTNING_GROWTH = NostrumMagica.Loc("lightning.growth");
	
	private static final ResourceLocation ID_ENDER_NOVICE = NostrumMagica.Loc("ender.novice");
	private static final ResourceLocation ID_ENDER_ADEPT = NostrumMagica.Loc("ender.adept");
	private static final ResourceLocation ID_ENDER_MASTER = NostrumMagica.Loc("ender.master");
	private static final ResourceLocation ID_ENDER_CORRUPT = NostrumMagica.Loc("ender.corrupt");
	private static final ResourceLocation ID_ENDER_WEAPON = NostrumMagica.Loc("ender.weapon");
	private static final ResourceLocation ID_ENDER_INFLICT = NostrumMagica.Loc("ender.inflict");
	private static final ResourceLocation ID_ENDER_GROWTH = NostrumMagica.Loc("ender.growth");
	
	public static /*final*/ Skill Spellcraft_TwoShapes;
	public static /*final*/ Skill Spellcraft_ElemBuilding;
	public static /*final*/ Skill Spellcraft_Weight1;
	public static /*final*/ Skill Spellcraft_ElemWeight;
	public static /*final*/ Skill Spellcraft_ManaDiscount1;
	public static /*final*/ Skill Spellcraft_ManaDiscount2;
	public static /*final*/ Skill Spellcraft_Infopanel;
	public static /*final*/ Skill Spellcraft_Alterations;
	public static /*final*/ Skill Spellcraft_Proxy;

	public static /*final*/ Skill Spellcasting_Potency1;
	public static /*final*/ Skill Spellcasting_ElemLinger;
	public static /*final*/ Skill Spellcasting_ElemLingerEater;
	public static /*final*/ Skill Spellcasting_ToolCooldown;
	public static /*final*/ Skill Spellcasting_CooldownReduc;
	public static /*final*/ Skill Spellcasting_Overcharge;
	public static /*final*/ Skill Spellcasting_FastOvercharge;
	public static /*final*/ Skill Spellcasting_StrongOvercharge;
	
	public static /*final*/ Skill Craftcast_FastBaseCast;
	public static /*final*/ Skill Craftcast_TomeHands;
	public static /*final*/ Skill Craftcast_ElemFree;
	public static /*final*/ Skill Craftcast_ElemWeight;
	public static /*final*/ Skill Craftcast_ElemMana;
	public static /*final*/ Skill Craftcast_Weight1;
	public static /*final*/ Skill Craftcast_Potency1;
	public static /*final*/ Skill Craftcast_ScrollEfficiency;
	
	public static /*final*/ Skill Incanting_TwoShapes;
	public static /*final*/ Skill Incanting_AllShapes;
	public static /*final*/ Skill Incanting_SelectInfo;
	public static /*final*/ Skill Incanting_TomeUse;
	public static /*final*/ Skill Incanting_ManaDiscount1;
	public static /*final*/ Skill Incanting_Potency1;
	public static /*final*/ Skill Incanting_Potency2;
	
	public static /*final*/ Skill Neutral_Novice;
	public static /*final*/ Skill Neutral_Adept;
	public static /*final*/ Skill Neutral_Master;
	public static /*final*/ Skill Neutral_Corrupt;
	public static /*final*/ Skill Neutral_Weapon;
	public static /*final*/ Skill Neutral_Inflict;
	public static /*final*/ Skill Neutral_Growth;

	public static /*final*/ Skill Fire_Novice;
	public static /*final*/ Skill Fire_Adept;
	public static /*final*/ Skill Fire_Master;
	public static /*final*/ Skill Fire_Corrupt;
	public static /*final*/ Skill Fire_Weapon;
	public static /*final*/ Skill Fire_Inflict;
	public static /*final*/ Skill Fire_Growth;

	public static /*final*/ Skill Ice_Novice;
	public static /*final*/ Skill Ice_Adept;
	public static /*final*/ Skill Ice_Master;
	public static /*final*/ Skill Ice_Corrupt;
	public static /*final*/ Skill Ice_Weapon;
	public static /*final*/ Skill Ice_Inflict;
	public static /*final*/ Skill Ice_Resist;

	public static /*final*/ Skill Earth_Novice;
	public static /*final*/ Skill Earth_Adept;
	public static /*final*/ Skill Earth_Master;
	public static /*final*/ Skill Earth_Corrupt;
	public static /*final*/ Skill Earth_Weapon;
	public static /*final*/ Skill Earth_Inflict;
	public static /*final*/ Skill Earth_Resist;

	public static /*final*/ Skill Wind_Novice;
	public static /*final*/ Skill Wind_Adept;
	public static /*final*/ Skill Wind_Master;
	public static /*final*/ Skill Wind_Corrupt;
	public static /*final*/ Skill Wind_Weapon;
	public static /*final*/ Skill Wind_Inflict;
	public static /*final*/ Skill Wind_Support;

	public static /*final*/ Skill Lightning_Novice;
	public static /*final*/ Skill Lightning_Adept;
	public static /*final*/ Skill Lightning_Master;
	public static /*final*/ Skill Lightning_Corrupt;
	public static /*final*/ Skill Lightning_Weapon;
	public static /*final*/ Skill Lightning_Inflict;
	public static /*final*/ Skill Lightning_Growth;

	public static /*final*/ Skill Ender_Novice;
	public static /*final*/ Skill Ender_Adept;
	public static /*final*/ Skill Ender_Master;
	public static /*final*/ Skill Ender_Corrupt;
	public static /*final*/ Skill Ender_Weapon;
	public static /*final*/ Skill Ender_Inflict;
	public static /*final*/ Skill Ender_Growth;
	
	public static void init() {
		Skill.ClearSkills();
		int x = 0;
		int y = -1;
		Spellcraft_Alterations = new HiddenSkill(ID_SPELLCRAFT_ALTERATIONS, Category_Magica, null, () -> SpellRune.getRune(EAlteration.INFLICT), x, y, new ResearchRequirement(NostrumResearches.ID_Spellcraft));
		Spellcraft_TwoShapes = new Skill(ID_SPELLCRAFT_TWOSHAPES, Category_Magica, ID_SPELLCRAFT_ALTERATIONS, () -> SpellRune.getRune(NostrumSpellShapes.Touch), x, y-1);
		Spellcraft_Proxy = new Skill(ID_SPELLCRAFT_PROXY, Category_Magica, ID_SPELLCRAFT_TWOSHAPES, () -> new ItemStack(NostrumBlocks.mysticSpellTable), x, y-2);
		//
		Spellcraft_Infopanel = new Skill(ID_SPELLCRAFT_INFOPANEL, Category_Magica, ID_SPELLCRAFT_ALTERATIONS, () -> new ItemStack(Items.PAPER), x-1, y-1);
		Spellcraft_ElemBuilding = new Skill(ID_SPELLCRAFT_ELEMBUILDING, Category_Magica, ID_SPELLCRAFT_INFOPANEL, () -> SpellRune.getRune(EMagicElement.ICE), x-1, y-2);
		//
		Spellcraft_ManaDiscount1 = new Skill(ID_SPELLCRAFT_MANADISCOUNT1, Category_Magica, ID_SPELLCRAFT_ALTERATIONS, () -> new ItemStack(Items.BOOK), x+1, y-1);
		Spellcraft_ElemWeight = new Skill(ID_SPELLCRAFT_ELEMWEIGHT, Category_Magica, ID_SPELLCRAFT_MANADISCOUNT1, () -> new ItemStack(NostrumItems.masteryOrb), x+1, y-2);
		Spellcraft_Weight1 = new Skill(ID_SPELLCRAFT_WEIGHT1, Category_Magica, ID_SPELLCRAFT_MANADISCOUNT1, () -> new ItemStack(Items.FEATHER), x+2, y-2);
		Spellcraft_ManaDiscount2 = new Skill(ID_SPELLCRAFT_MANADISCOUNT2, Category_Magica, ID_SPELLCRAFT_WEIGHT1, () -> new ItemStack(Items.WRITABLE_BOOK), x+2, y-3);
		
		x = 0;
		y = 1;
		Spellcasting_Potency1 = new Skill(ID_SPELLCAST_POTENCY1, Category_Magica, null, () -> new ItemStack(NostrumItems.crystalSmall), x, y+0);
		Spellcasting_ElemLinger = new Skill(ID_SPELLCAST_ELEMLINGER, Category_Magica, ID_SPELLCAST_POTENCY1, () -> new ItemStack(NostrumItems.mageStaff), x-1, y+1);
		Spellcasting_ElemLingerEater = new Skill(ID_SPELLCAST_ELEMLINGEREATER, Category_Magica, ID_SPELLCAST_ELEMLINGER, () -> new ItemStack(NostrumItems.thanosStaff), x-1, y+2);
		//
		Spellcasting_CooldownReduc = new Skill(ID_SPELLCAST_COOLDOWNREDUC, Category_Magica, ID_SPELLCAST_POTENCY1, () -> new ItemStack(Items.CLOCK), x+0, y+1);
		Spellcasting_ToolCooldown = new Skill(ID_SPELLCAST_TOOLCOOLDOWN, Category_Magica, ID_SPELLCAST_COOLDOWNREDUC, () -> new ItemStack(NostrumItems.casterWand), x+0, y+2);
		//
		Spellcasting_Overcharge = new HiddenSkill(ID_SPELLCAST_OVERCHARGE, Category_Magica, ID_SPELLCAST_POTENCY1, () -> new ItemStack(NostrumItems.crystalLarge), x+1, y+1, new TierRequirement(EMagicTier.VANI));
		Spellcasting_FastOvercharge = new Skill(ID_SPELLCAST_FAST_OVERCHARGE, Category_Magica, ID_SPELLCAST_OVERCHARGE, () -> new ItemStack(NostrumItems.resourceSlabKind), x+2, y+2);
		Spellcasting_StrongOvercharge = new Skill(ID_SPELLCAST_STRONG_OVERCHARGE, Category_Magica, ID_SPELLCAST_OVERCHARGE, () -> new ItemStack(NostrumItems.resourceSlabFierce), x+1, y+2);
		
		x = 1;
		y = 0;
		Craftcast_FastBaseCast = new Skill(ID_CRAFTEDCAST_FASTBASECAST, Category_Magica, null, () -> new ItemStack(NostrumItems.spellScroll), x, y);
		Craftcast_TomeHands = new Skill(ID_CRAFTEDCAST_TOMEHANDS, Category_Magica, ID_CRAFTEDCAST_FASTBASECAST, () -> new ItemStack(NostrumItems.spellTomeSpooky), x+1, y+1);
		Craftcast_ScrollEfficiency = new Skill(ID_CRAFTEDCAST_SCROLLEFFICIENCY, Category_Magica, ID_CRAFTEDCAST_TOMEHANDS, () -> new ItemStack(NostrumItems.blankScroll), x+2, y+1);
		//
		Craftcast_ElemFree = new Skill(ID_CRAFTEDCAST_ELEMFREE, Category_Magica, ID_CRAFTEDCAST_FASTBASECAST, () -> new ItemStack(NostrumItems.mageStaff), x+1, y+0);
		Craftcast_ElemWeight = new HiddenSkill(ID_CRAFTEDCAST_ELEMWEIGHT, Category_Magica, ID_CRAFTEDCAST_ELEMFREE, () -> new ItemStack(NostrumItems.crystalSmall), x+2, y-1, new TierRequirement(EMagicTier.KANI));
		Craftcast_ElemMana = new Skill(ID_CRAFTEDCAST_ELEMMANA, Category_Magica, ID_CRAFTEDCAST_ELEMFREE, () -> new ItemStack(NostrumItems.infusedGemFire), x+2, y+0);
		//
		Craftcast_Potency1 = new Skill(ID_CRAFTEDCAST_POTENCY1, Category_Magica, ID_CRAFTEDCAST_FASTBASECAST, () -> new ItemStack(NostrumItems.infusedGemUnattuned), x+1, y-1);
		Craftcast_Weight1 = new Skill(ID_CRAFTEDCAST_WEIGHT1, Category_Magica, ID_CRAFTEDCAST_POTENCY1, () -> new ItemStack(NostrumItems.reagentSpiderSilk), x+2, y-2);
		
		x = -1;
		y = 0;
		Incanting_SelectInfo = new Skill(ID_INCANTING_SELECT_INFO, Category_Magica, null, () -> new ItemStack(Items.PAPER), x, y);
		Incanting_AllShapes = new HiddenSkill(ID_INCANTING_ALLSHAPES, Category_Magica, ID_INCANTING_SELECT_INFO, () -> new ItemStack(NostrumItems.crystalMedium), x-1, y+1, new TierRequirement(EMagicTier.KANI));
		Incanting_TwoShapes = new HiddenSkill(ID_INCANTING_TWOSHAPES, Category_Magica, ID_INCANTING_ALLSHAPES, () -> SpellRune.getRune(NostrumSpellShapes.Touch), x-2, y+2, new TierRequirement(EMagicTier.KANI));
		
		//
		Incanting_Potency1 = new Skill(ID_INCANTING_POTENCY1, Category_Magica, ID_INCANTING_SELECT_INFO, () -> new ItemStack(Items.BELL), x-1, y-1);
		Incanting_Potency2 = new Skill(ID_INCANTING_POTENCY2, Category_Magica, ID_INCANTING_POTENCY1, () -> new ItemStack(Items.TURTLE_HELMET), x-2, y-1);
		Incanting_ManaDiscount1 = new Skill(ID_INCANTING_MANADISCOUNT1, Category_Magica, ID_INCANTING_POTENCY1, () -> new ItemStack(NostrumItems.resourceManaLeaf), x-2, y-2);
		Incanting_TomeUse = new Skill(ID_INCANTING_TOMEUSE, Category_Magica, ID_INCANTING_POTENCY1, () -> new ItemStack(NostrumItems.spellTomePage), x-2, y-0);
		
		
		x = 0;
		y = 1;
		Neutral_Novice = new HiddenSkill(ID_NEUTRAL_NOVICE, Category_Element_Neutral, null, () -> SpellRune.getRune(EMagicElement.NEUTRAL), x, y, new ElementMasteryRequirement(EMagicElement.NEUTRAL, EElementalMastery.NOVICE));
		Neutral_Adept = new HiddenSkill(ID_NEUTRAL_ADEPT, Category_Element_Neutral, ID_NEUTRAL_NOVICE, () -> new ItemStack(Items.IRON_CHESTPLATE), x, y-1, new ElementMasteryRequirement(EMagicElement.NEUTRAL, EElementalMastery.ADEPT));
		Neutral_Master = new HiddenSkill(ID_NEUTRAL_MASTER, Category_Element_Neutral, ID_NEUTRAL_ADEPT, () -> new ItemStack(Items.NETHERITE_SWORD), x, y-2, new ElementMasteryRequirement(EMagicElement.NEUTRAL, EElementalMastery.MASTER));
		Neutral_Inflict = new HiddenSkill(ID_NEUTRAL_INFLICT, Category_Element_Neutral, ID_NEUTRAL_NOVICE, () -> new ItemStack(Items.WOODEN_SWORD), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.NEUTRAL, EAlteration.INFLICT));
		Neutral_Growth = new HiddenSkill(ID_NEUTRAL_GROWTH, Category_Element_Neutral, ID_NEUTRAL_NOVICE, () -> new ItemStack(Items.GOLDEN_CARROT), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.NEUTRAL, EAlteration.GROWTH));
		Neutral_Corrupt = new HiddenSkill(ID_NEUTRAL_CORRUPT, Category_Element_Neutral, ID_NEUTRAL_ADEPT, () -> new ItemStack(Items.POPPED_CHORUS_FRUIT), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.NEUTRAL, EAlteration.CORRUPT));
		Neutral_Weapon = new HiddenSkill(ID_NEUTRAL_WEAPON, Category_Element_Neutral, ID_NEUTRAL_ADEPT, () -> new ItemStack(NostrumItems.deepMetalAxe), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Sword_Neutral));

		x = 0;
		y = 1;
		Fire_Novice = new HiddenSkill(ID_FIRE_NOVICE, Category_Element_Fire, null, () -> SpellRune.getRune(EMagicElement.FIRE), x, y, new ElementMasteryRequirement(EMagicElement.FIRE, EElementalMastery.NOVICE));
		Fire_Adept = new HiddenSkill(ID_FIRE_ADEPT, Category_Element_Fire, ID_FIRE_NOVICE, () -> new ItemStack(NostrumItems.infusedGemFire), x, y-1, new ElementMasteryRequirement(EMagicElement.FIRE, EElementalMastery.ADEPT));
		Fire_Master = new HiddenSkill(ID_FIRE_MASTER, Category_Element_Fire, ID_FIRE_ADEPT, () -> new ItemStack(Items.SOUL_CAMPFIRE), x, y-2, new ElementMasteryRequirement(EMagicElement.FIRE, EElementalMastery.MASTER));
		Fire_Inflict = new HiddenSkill(ID_FIRE_INFLICT, Category_Element_Fire, ID_FIRE_NOVICE, () -> new ItemStack(Items.BLAZE_POWDER), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.FIRE, EAlteration.INFLICT));
		Fire_Growth = new HiddenSkill(ID_FIRE_GROWTH, Category_Element_Fire, ID_FIRE_NOVICE, () -> new ItemStack(Items.CHAINMAIL_CHESTPLATE), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.FIRE, EAlteration.GROWTH));
		Fire_Corrupt = new HiddenSkill(ID_FIRE_CORRUPT, Category_Element_Fire, ID_FIRE_ADEPT, () -> new ItemStack(Items.LAVA_BUCKET), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.FIRE, EAlteration.CORRUPT));
		Fire_Weapon = new HiddenSkill(ID_FIRE_WEAPON, Category_Element_Fire, ID_FIRE_ADEPT, () -> new ItemStack(NostrumItems.flameRod), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Sword_Fire));

		x = 0;
		y = 1;
		Ice_Novice = new HiddenSkill(ID_ICE_NOVICE, Category_Element_Ice, null, () -> SpellRune.getRune(EMagicElement.ICE), x, y, new ElementMasteryRequirement(EMagicElement.ICE, EElementalMastery.NOVICE));
		Ice_Adept = new HiddenSkill(ID_ICE_ADEPT, Category_Element_Ice, ID_ICE_NOVICE, () -> new ItemStack(NostrumItems.infusedGemIce), x, y-1, new ElementMasteryRequirement(EMagicElement.ICE, EElementalMastery.ADEPT));
		Ice_Master = new HiddenSkill(ID_ICE_MASTER, Category_Element_Ice, ID_ICE_ADEPT, () -> SpellRune.getRune(NostrumSpellShapes.OnHealth), x, y-2, new ElementMasteryRequirement(EMagicElement.ICE, EElementalMastery.MASTER));
		Ice_Inflict = new HiddenSkill(ID_ICE_INFLICT, Category_Element_Ice, ID_ICE_NOVICE, () -> new ItemStack(Items.POTION), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.ICE, EAlteration.INFLICT));
		Ice_Resist = new HiddenSkill(ID_ICE_SUPPORT, Category_Element_Ice, ID_ICE_NOVICE, () -> SpellRune.getRune(NostrumSpellShapes.OnMana), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.ICE, EAlteration.RESIST));
		Ice_Corrupt = new HiddenSkill(ID_ICE_CORRUPT, Category_Element_Ice, ID_ICE_ADEPT, () -> new ItemStack(Items.SOUL_LANTERN), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.ICE, EAlteration.CORRUPT));
		Ice_Weapon = new HiddenSkill(ID_ICE_WEAPON, Category_Element_Ice, ID_ICE_ADEPT, () -> new ItemStack(NostrumItems.enchantedWeaponIceMaster), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Enchanted_Weapons));

		x = 0;
		y = 1;
		Earth_Novice = new HiddenSkill(ID_EARTH_NOVICE, Category_Element_Earth, null, () -> SpellRune.getRune(EMagicElement.EARTH), x, y, new ElementMasteryRequirement(EMagicElement.EARTH, EElementalMastery.NOVICE));
		Earth_Adept = new HiddenSkill(ID_EARTH_ADEPT, Category_Element_Earth, ID_EARTH_NOVICE, () -> new ItemStack(Items.NETHERITE_AXE), x, y-1, new ElementMasteryRequirement(EMagicElement.EARTH, EElementalMastery.ADEPT));
		Earth_Master = new HiddenSkill(ID_EARTH_MASTER, Category_Element_Earth, ID_EARTH_ADEPT, () -> new ItemStack(Items.GLISTERING_MELON_SLICE), x, y-2, new ElementMasteryRequirement(EMagicElement.EARTH, EElementalMastery.MASTER));
		Earth_Inflict = new HiddenSkill(ID_EARTH_INFLICT, Category_Element_Earth, ID_EARTH_NOVICE, () -> new ItemStack(Items.IRON_BLOCK), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.EARTH, EAlteration.INFLICT));
		Earth_Resist = new HiddenSkill(ID_EARTH_SUPPORT, Category_Element_Earth, ID_EARTH_NOVICE, () -> new ItemStack(NostrumItems.mirrorShield), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.EARTH, EAlteration.RESIST));
		Earth_Corrupt = new HiddenSkill(ID_EARTH_CORRUPT, Category_Element_Earth, ID_EARTH_ADEPT, () -> new ItemStack(Items.DIAMOND), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.EARTH, EAlteration.CORRUPT));
		Earth_Weapon = new HiddenSkill(ID_EARTH_WEAPON, Category_Element_Earth, ID_EARTH_ADEPT, () -> new ItemStack(NostrumItems.earthPike), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Sword_Earth));

		x = 0;
		y = 1;
		Wind_Novice = new HiddenSkill(ID_WIND_NOVICE, Category_Element_Wind, null, () -> SpellRune.getRune(EMagicElement.WIND), x, y, new ElementMasteryRequirement(EMagicElement.WIND, EElementalMastery.NOVICE));
		Wind_Adept = new HiddenSkill(ID_WIND_ADEPT, Category_Element_Wind, ID_WIND_NOVICE, () -> SpellRune.getRune(NostrumSpellShapes.Cutter), x, y-1, new ElementMasteryRequirement(EMagicElement.WIND, EElementalMastery.ADEPT));
		Wind_Master = new HiddenSkill(ID_WIND_MASTER, Category_Element_Wind, ID_WIND_ADEPT, () -> new ItemStack(NostrumItems.magicCharmWind), x, y-2, new ElementMasteryRequirement(EMagicElement.WIND, EElementalMastery.MASTER));
		Wind_Inflict = new HiddenSkill(ID_WIND_INFLICT, Category_Element_Wind, ID_WIND_NOVICE, () -> new ItemStack(Items.FEATHER), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.WIND, EAlteration.INFLICT));
		Wind_Support = new HiddenSkill(ID_WIND_SUPPORT, Category_Element_Wind, ID_WIND_NOVICE, () -> new ItemStack(Items.GOLDEN_PICKAXE), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.WIND, EAlteration.SUPPORT));
		Wind_Corrupt = new HiddenSkill(ID_WIND_CORRUPT, Category_Element_Wind, ID_WIND_ADEPT, () -> new ItemStack(Items.ELYTRA), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.WIND, EAlteration.CORRUPT));
		Wind_Weapon = new HiddenSkill(ID_WIND_WEAPON, Category_Element_Wind, ID_WIND_ADEPT, () -> new ItemStack(NostrumItems.enchantedWeaponWindMaster), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Enchanted_Weapons));

		x = 0;
		y = 1;
		Lightning_Novice = new HiddenSkill(ID_LIGHTNING_NOVICE, Category_Element_Lightning, null, () -> SpellRune.getRune(EMagicElement.LIGHTNING), x, y, new ElementMasteryRequirement(EMagicElement.LIGHTNING, EElementalMastery.NOVICE));
		Lightning_Adept = new HiddenSkill(ID_LIGHTNING_ADEPT, Category_Element_Lightning, ID_LIGHTNING_NOVICE, () -> new ItemStack(NostrumItems.enchantedWeaponLightningAdept), x, y-1, new ElementMasteryRequirement(EMagicElement.LIGHTNING, EElementalMastery.ADEPT));
		Lightning_Master = new HiddenSkill(ID_LIGHTNING_MASTER, Category_Element_Lightning, ID_LIGHTNING_ADEPT, () -> new ItemStack(NostrumItems.magicCharmLightning), x, y-2, new ElementMasteryRequirement(EMagicElement.LIGHTNING, EElementalMastery.MASTER));
		Lightning_Inflict = new HiddenSkill(ID_LIGHTNING_INFLICT, Category_Element_Lightning, ID_LIGHTNING_NOVICE, () -> new ItemStack(Blocks.ICE), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.LIGHTNING, EAlteration.INFLICT));
		Lightning_Growth = new HiddenSkill(ID_LIGHTNING_GROWTH, Category_Element_Lightning, ID_LIGHTNING_NOVICE, () -> new ItemStack(Items.GOLDEN_BOOTS), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.LIGHTNING, EAlteration.SUPPORT));
		Lightning_Corrupt = new HiddenSkill(ID_LIGHTNING_CORRUPT, Category_Element_Lightning, ID_LIGHTNING_ADEPT, () -> new ItemStack(Items.SPLASH_POTION), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.LIGHTNING, EAlteration.CORRUPT));
		Lightning_Weapon = new HiddenSkill(ID_LIGHTNING_WEAPON, Category_Element_Lightning, ID_LIGHTNING_ADEPT, () -> new ItemStack(NostrumItems.enchantedWeaponLightningMaster), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Enchanted_Weapons));

		x = 0;
		y = 1;
		Ender_Novice = new HiddenSkill(ID_ENDER_NOVICE, Category_Element_Ender, null, () -> SpellRune.getRune(EMagicElement.ENDER), x, y, new ElementMasteryRequirement(EMagicElement.ENDER, EElementalMastery.NOVICE));
		Ender_Adept = new HiddenSkill(ID_ENDER_ADEPT, Category_Element_Ender, ID_ENDER_NOVICE, () -> new ItemStack(Items.ENDER_PEARL), x, y-1, new ElementMasteryRequirement(EMagicElement.ENDER, EElementalMastery.ADEPT));
		Ender_Master = new HiddenSkill(ID_ENDER_MASTER, Category_Element_Ender, ID_ENDER_ADEPT, () -> new ItemStack(Items.ENDER_EYE), x, y-2, new ElementMasteryRequirement(EMagicElement.ENDER, EElementalMastery.MASTER));
		Ender_Inflict = new HiddenSkill(ID_ENDER_INFLICT, Category_Element_Ender, ID_ENDER_NOVICE, () -> new ItemStack(Items.SPIDER_EYE), x-1, y-1, new SpellKnowledgeRequirement(EMagicElement.ENDER, EAlteration.INFLICT));
		Ender_Growth = new HiddenSkill(ID_ENDER_GROWTH, Category_Element_Ender, ID_ENDER_NOVICE, () -> new ItemStack(NostrumBlocks.paradoxMirror), x+1, y-1, new SpellKnowledgeRequirement(EMagicElement.ENDER, EAlteration.GROWTH));
		Ender_Corrupt = new HiddenSkill(ID_ENDER_CORRUPT, Category_Element_Ender, ID_ENDER_ADEPT, () -> new ItemStack(Items.CARVED_PUMPKIN), x-1, y-2, new SpellKnowledgeRequirement(EMagicElement.ENDER, EAlteration.CORRUPT));
		Ender_Weapon = new HiddenSkill(ID_ENDER_WEAPON, Category_Element_Ender, ID_ENDER_ADEPT, () -> new ItemStack(NostrumItems.enderRod), x+1, y-2, new ResearchRequirement(NostrumResearches.ID_Sword_Ender));
	}
	
}
