package com.smanzana.nostrummagica.progression.research;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfTameLore;
import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.integration.curios.CuriosProxy;
import com.smanzana.nostrummagica.item.MagicCharm;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonArmorMaterial;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.item.equipment.AspectedWeapon;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NostrumResearches {

	public static NostrumResearchTab Tab_Magica = new NostrumResearchTab("magica", () -> new ItemStack(NostrumItems.spellTomeCombat));
	public static NostrumResearchTab Tab_Advanced_Magica = new NostrumResearchTab("mysticism", () -> new ItemStack(NostrumItems.crystalSmall));
	public static NostrumResearchTab Tab_Mysticism = new NostrumResearchTab("advanced_magica", () -> new ItemStack(NostrumItems.thanoPendant));
	public static NostrumResearchTab Tab_Tinkering = new NostrumResearchTab("tinkering", () -> new ItemStack(NostrumBlocks.putterBlock));
	public static NostrumResearchTab Tab_Outfitting = new NostrumResearchTab("outfitting", () -> new ItemStack(NostrumItems.mageStaff));

	public static final ResourceLocation ID_Origin = NostrumMagica.Loc("origin");
	public static final ResourceLocation ID_Spellcraft = NostrumMagica.Loc("spellcraft");
	public static final ResourceLocation ID_Spelltomes = NostrumMagica.Loc("spelltomes");
	public static final ResourceLocation ID_Spelltomes_Advanced = NostrumMagica.Loc("spelltomes_advanced");
	public static final ResourceLocation ID_Spellbinding = NostrumMagica.Loc("spellbinding");
	public static final ResourceLocation ID_Tome_Workshop = NostrumMagica.Loc("tome_workshop");
	public static final ResourceLocation ID_Advanced_Spelltable = NostrumMagica.Loc("advanced_spelltable");
	public static final ResourceLocation ID_Mystic_Spelltable = NostrumMagica.Loc("mystic_spelltable");
	public static final ResourceLocation ID_Elemental_Trials = NostrumMagica.Loc("elemental_trials");
	public static final ResourceLocation ID_Rituals = NostrumMagica.Loc("rituals");
	public static final ResourceLocation ID_Candles = NostrumMagica.Loc("candles");
	public static final ResourceLocation ID_Markrecall = NostrumMagica.Loc("markrecall");
	public static final ResourceLocation ID_Adv_Markrecall = NostrumMagica.Loc("adv_markrecall");
	public static final ResourceLocation ID_Boon = NostrumMagica.Loc("boon");
	public static final ResourceLocation ID_Summonkoids = NostrumMagica.Loc("summonkoids");
	public static final ResourceLocation ID_Loretable = NostrumMagica.Loc("loretable");
	public static final ResourceLocation ID_Magic_Token = NostrumMagica.Loc("magic_token");
	public static final ResourceLocation ID_Magic_Token_3 = NostrumMagica.Loc("magic_token_3");
	public static final ResourceLocation ID_Essence_Seeds = NostrumMagica.Loc("essence_seeds");
	public static final ResourceLocation ID_Kani = NostrumMagica.Loc("kani");
	public static final ResourceLocation ID_Vani = NostrumMagica.Loc("vani");
	public static final ResourceLocation ID_Seeking_Gems = NostrumMagica.Loc("seeking_gems");
	public static final ResourceLocation ID_Spellrunes = NostrumMagica.Loc("spellrunes");
	public static final ResourceLocation ID_Rune_Shaper = NostrumMagica.Loc("rune_shaper");
	public static final ResourceLocation ID_Fierce_Infusion = NostrumMagica.Loc("fierce_infusion");
	public static final ResourceLocation ID_Kind_Infusion = NostrumMagica.Loc("kind_infusion");
	public static final ResourceLocation ID_Balanced_Infusion = NostrumMagica.Loc("balanced_infusion");
	public static final ResourceLocation ID_Mage_Staff = NostrumMagica.Loc("mage_staff");
	public static final ResourceLocation ID_Thanos_Staff = NostrumMagica.Loc("thanos_staff");
	public static final ResourceLocation ID_Caster_Wand = NostrumMagica.Loc("caster_wand");
	public static final ResourceLocation ID_Warlock_Sword = NostrumMagica.Loc("warlock_sword");
	public static final ResourceLocation ID_Soul_Daggers = NostrumMagica.Loc("soul_daggers");
	public static final ResourceLocation ID_Mage_Blade = NostrumMagica.Loc("mage_blade");
	public static final ResourceLocation ID_Sword_Fire = NostrumMagica.Loc("sword_fire");
	public static final ResourceLocation ID_Sword_Earth = NostrumMagica.Loc("sword_earth");
	public static final ResourceLocation ID_Sword_Ender = NostrumMagica.Loc("sword_ender");
	public static final ResourceLocation ID_Sword_Physical = NostrumMagica.Loc("sword_physical");
	public static final ResourceLocation ID_Enchanted_Weapons = NostrumMagica.Loc("enchanted_weapons");
	public static final ResourceLocation ID_Enchanted_Armor = NostrumMagica.Loc("enchanted_armor");
	public static final ResourceLocation ID_Enchanted_Armor_Adv = NostrumMagica.Loc("enchanted_armor_adv");
	public static final ResourceLocation ID_Dragon_Armor = NostrumMagica.Loc("dragon_armor");
	public static final ResourceLocation ID_Mirror_Shield = NostrumMagica.Loc("mirror_shield");
	public static final ResourceLocation ID_True_Mirror_Shield = NostrumMagica.Loc("true_mirror_shield");
	public static final ResourceLocation ID_Reagent_Bag = NostrumMagica.Loc("reagent_bag");
	public static final ResourceLocation ID_Rune_Bag = NostrumMagica.Loc("rune_bag");
	public static final ResourceLocation ID_Hookshot_Medium = NostrumMagica.Loc("hookshot_medium");
	public static final ResourceLocation ID_Hookshot_Strong = NostrumMagica.Loc("hookshot_strong");
	public static final ResourceLocation ID_Hookshot_Claw = NostrumMagica.Loc("hookshot_claw");
	public static final ResourceLocation ID_Charms = NostrumMagica.Loc("charms");
	public static final ResourceLocation ID_Geogems = NostrumMagica.Loc("geogems");
	public static final ResourceLocation ID_Geotokens = NostrumMagica.Loc("geotokens");
	public static final ResourceLocation ID_Obelisks = NostrumMagica.Loc("obelisks");
	public static final ResourceLocation ID_Sorceryportal = NostrumMagica.Loc("sorceryportal");
	public static final ResourceLocation ID_Teleportrune = NostrumMagica.Loc("teleportrune");
	public static final ResourceLocation ID_Paradox_Mirrors = NostrumMagica.Loc("paradox_mirrors");
	public static final ResourceLocation ID_Silver_Mirror = NostrumMagica.Loc("silver_mirror");
	public static final ResourceLocation ID_Gold_Mirror = NostrumMagica.Loc("gold_mirror");
	public static final ResourceLocation ID_Mystic_Anchor = NostrumMagica.Loc("mystic_anchor");
	public static final ResourceLocation ID_Putter = NostrumMagica.Loc("putter");
	public static final ResourceLocation ID_Active_Hopper = NostrumMagica.Loc("active_hopper");
	public static final ResourceLocation ID_Item_Duct = NostrumMagica.Loc("item_duct");
	public static final ResourceLocation ID_Magicfacade = NostrumMagica.Loc("magicfacade");
	public static final ResourceLocation ID_Rune_Library = NostrumMagica.Loc("rune_library");
	public static final ResourceLocation ID_Thano_Pendant = NostrumMagica.Loc("thano_pendant");
	public static final ResourceLocation ID_Modification_Table = NostrumMagica.Loc("modification_table");
	public static final ResourceLocation ID_Stat_Items = NostrumMagica.Loc("stat_items");
	public static final ResourceLocation ID_Ender_Pin = NostrumMagica.Loc("ender_pin");
	public static final ResourceLocation ID_Soulbound_Pets = NostrumMagica.Loc("soulbound_pets");
	public static final ResourceLocation ID_Wolf_Transformation = NostrumMagica.Loc("wolf_transformation");
	public static final ResourceLocation ID_Mana_Armor = NostrumMagica.Loc("mana_armor");

	
	public static /*final*/ NostrumResearch Origin;
	public static /*final*/ NostrumResearch Spellcraft;
	public static /*final*/ NostrumResearch Spelltomes;
	public static /*final*/ NostrumResearch Spelltomes_Advanced;
	public static /*final*/ NostrumResearch Spellbinding;
	public static /*final*/ NostrumResearch Tome_Workshop;
	public static /*final*/ NostrumResearch Advanced_Spelltable;
	public static /*final*/ NostrumResearch Mystic_Spelltable;
	public static /*final*/ NostrumResearch Elemental_Trials;
	public static /*final*/ NostrumResearch Rituals;
	public static /*final*/ NostrumResearch Candles;
	public static /*final*/ NostrumResearch Markrecall;
	public static /*final*/ NostrumResearch Adv_Markrecall;
	public static /*final*/ NostrumResearch Boon;
	public static /*final*/ NostrumResearch Summonkoids;
	public static /*final*/ NostrumResearch Loretable;
	public static /*final*/ NostrumResearch Magic_Token;
	public static /*final*/ NostrumResearch Magic_Token_3;
	public static /*final*/ NostrumResearch Essence_Seeds;
	public static /*final*/ NostrumResearch Kani;
	public static /*final*/ NostrumResearch Vani;
	public static /*final*/ NostrumResearch Seeking_Gems;
	public static /*final*/ NostrumResearch Spellrunes;
	public static /*final*/ NostrumResearch Rune_Shaper;
	public static /*final*/ NostrumResearch Fierce_Infusion;
	public static /*final*/ NostrumResearch Kind_Infusion;
	public static /*final*/ NostrumResearch Balanced_Infusion;
	public static /*final*/ NostrumResearch Mage_Staff;
	public static /*final*/ NostrumResearch Thanos_Staff;
	public static /*final*/ NostrumResearch Caster_Wand;
	public static /*final*/ NostrumResearch Warlock_Sword;
	public static /*final*/ NostrumResearch Soul_Daggers;
	public static /*final*/ NostrumResearch Mage_Blade;
	public static /*final*/ NostrumResearch Sword_Fire;
	public static /*final*/ NostrumResearch Sword_Earth;
	public static /*final*/ NostrumResearch Sword_Ender;
	public static /*final*/ NostrumResearch Sword_Physical;
	public static /*final*/ NostrumResearch Enchanted_Weapons;
	public static /*final*/ NostrumResearch Enchanted_Armor;
	public static /*final*/ NostrumResearch Enchanted_Armor_Adv;
	public static /*final*/ NostrumResearch Dragon_Armor;
	public static /*final*/ NostrumResearch Mirror_Shield;
	public static /*final*/ NostrumResearch True_Mirror_Shield;
	public static /*final*/ NostrumResearch Reagent_Bag;
	public static /*final*/ NostrumResearch Rune_Bag;
	public static /*final*/ NostrumResearch Hookshot_Medium;
	public static /*final*/ NostrumResearch Hookshot_Strong;
	public static /*final*/ NostrumResearch Hookshot_Claw;
	public static /*final*/ NostrumResearch Charms;
	public static /*final*/ NostrumResearch Geogems;
	public static /*final*/ NostrumResearch Geotokens;
	public static /*final*/ NostrumResearch Obelisks;
	public static /*final*/ NostrumResearch Sorceryportal;
	public static /*final*/ NostrumResearch Teleportrune;
	public static /*final*/ NostrumResearch Paradox_Mirrors;
	public static /*final*/ NostrumResearch Silver_Mirror;
	public static /*final*/ NostrumResearch Gold_Mirror;
	public static /*final*/ NostrumResearch Mystic_Anchor;
	public static /*final*/ NostrumResearch Putter;
	public static /*final*/ NostrumResearch Active_Hopper;
	public static /*final*/ NostrumResearch Item_Duct;
	public static /*final*/ NostrumResearch Magicfacade;
	public static /*final*/ NostrumResearch Rune_Library;
	public static /*final*/ NostrumResearch Thano_Pendant;
	public static /*final*/ NostrumResearch Modification_Table;
	public static /*final*/ NostrumResearch Stat_Items;
	public static /*final*/ NostrumResearch Ender_Pin;
	public static /*final*/ NostrumResearch Soulbound_Pets;
	public static /*final*/ NostrumResearch Wolf_Transformation;
	public static /*final*/ NostrumResearch Mana_Armor;

	
	public static void init() {
		// Magica Tab
		Origin = NostrumResearch.startBuilding().build(ID_Origin, Tab_Magica, NostrumResearch.Size.LARGE, 0, 0, false,
				new ItemStack(NostrumItems.spellTomeNovice));

		Spellcraft = NostrumResearch.startBuilding().parent(ID_Origin)
				.reference("builtin::guides::spellmaking", "info.spellmaking.name").reference(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE)))
				.reference(NostrumItems.blankScroll).reference(NostrumItems.spellScroll).reference(NostrumItems.reagentMandrakeRoot)
				.build(ID_Spellcraft, Tab_Magica, NostrumResearch.Size.GIANT, -1, 1, false,
						new ItemStack(NostrumItems.spellScroll));

		Spelltomes = NostrumResearch.startBuilding().parent(ID_Spellcraft).hiddenParent(ID_Rituals).lore(NostrumItems.spellPlateNovice)
				.reference("builtin::guides::tomes", "info.tomes.name").reference("ritual::tome", "ritual.tome.name")
				.reference(NostrumItems.spellTomeNovice).reference(NostrumItems.spellPlateNovice).reference(NostrumItems.spellScroll)
				.build(ID_Spelltomes, Tab_Magica, NostrumResearch.Size.NORMAL, -2, 2, false,
						new ItemStack(NostrumItems.spellPlateNovice));

		Spelltomes_Advanced = NostrumResearch.startBuilding().parent(ID_Spelltomes).lore(NostrumItems.spellTomePage)
				.reference("builtin::guides::tomes", "info.tomes.name").reference("ritual::tome", "ritual.tome.name")
				.reference(NostrumItems.spellTomePage).build(ID_Spelltomes_Advanced, Tab_Magica,
						NostrumResearch.Size.NORMAL, -1, 3, true, new ItemStack(NostrumItems.spellTomePage));

		Spellbinding = NostrumResearch.startBuilding().parent(ID_Spelltomes)
				.reference("builtin::guides::spellmaking", "info.spellbinding.name")
				.reference("ritual::spell_binding", "ritual.spell_binding.name").build(ID_Spellbinding,
						Tab_Magica, NostrumResearch.Size.NORMAL, -2, 3, false, new ItemStack(NostrumItems.spellTomeNovice));

		Tome_Workshop = NostrumResearch.startBuilding().parent(ID_Spelltomes)
				.reference("ritual::tome_workshop", "ritual.tome_workshop.name").build(ID_Tome_Workshop,
						Tab_Magica, NostrumResearch.Size.NORMAL, -3, 3, false, new ItemStack(NostrumBlocks.tomeWorkshop));

		Advanced_Spelltable = NostrumResearch.startBuilding().parent(ID_Spellcraft)
				.reference("ritual::advanced_spelltable", "ritual.advanced_spelltable.name")
				.build(ID_Advanced_Spelltable,
						Tab_Magica, NostrumResearch.Size.NORMAL, -2, 0, true, true, new ItemStack(NostrumBlocks.advancedSpellTable));

		Mystic_Spelltable = NostrumResearch.startBuilding().parent(ID_Advanced_Spelltable)
				.reference("ritual::mystic_spelltable", "ritual.mystic_spelltable.name")
 				.build(ID_Mystic_Spelltable,
						Tab_Magica, NostrumResearch.Size.NORMAL, -2, -1, true, true, new ItemStack(NostrumBlocks.mysticSpellTable));

		Elemental_Trials = NostrumResearch.startBuilding().parent(ID_Origin).tier(EMagicTier.KANI).reference(NostrumItems.masteryOrb)
				.reference("builtin::trials::fire", "info.trial.fire.name")
				.reference("builtin::trials::ice", "info.trial.ice.name")
				.reference("builtin::trials::earth", "info.trial.earth.name")
				.reference("builtin::trials::wind", "info.trial.wind.name")
				.reference("builtin::trials::ender", "info.trial.ender.name")
				.reference("builtin::trials::lightning", "info.trial.lightning.name")
				.reference("builtin::trials::physical", "info.trial.physical.name").build(ID_Elemental_Trials,
						Tab_Magica, NostrumResearch.Size.NORMAL, 0, 1, true, new ItemStack(NostrumItems.masteryOrb));

		Rituals = NostrumResearch.startBuilding().parent(ID_Origin).reference("builtin::guides::rituals", "info.rituals.name")
				.reference(NostrumItems.altarItem).reference(NostrumItems.chalkItem).reference(NostrumItems.reagentMandrakeRoot)
				.reference(NostrumItems.infusedGemUnattuned).build(ID_Rituals, Tab_Magica, NostrumResearch.Size.GIANT, 1, 1,
						false, new ItemStack(NostrumItems.infusedGemUnattuned));

		Candles = NostrumResearch.startBuilding().parent(ID_Rituals).build(ID_Candles, Tab_Magica, NostrumResearch.Size.NORMAL, 2, 2,
				false, new ItemStack(NostrumBlocks.candle));

		Markrecall = NostrumResearch.startBuilding().hiddenParent(ID_Geotokens).parent(ID_Rituals).lore(NostrumItems.positionToken)
				.spellComponent(EMagicElement.ENDER, EAlteration.GROWTH)
				.spellComponent(EMagicElement.ENDER, EAlteration.SUPPORT).reference("ritual::mark", "ritual.mark.name")
				.reference("ritual::recall", "ritual.recall.name")
				.build(ID_Markrecall, Tab_Magica, NostrumResearch.Size.LARGE, 4, 2, true, new ItemStack(Items.COMPASS));

		Adv_Markrecall = NostrumResearch.startBuilding().parent(ID_Markrecall).lore(NostrumItems.seekingGem).build(ID_Adv_Markrecall,
				Tab_Magica, NostrumResearch.Size.LARGE, 4, 3, false, new ItemStack(NostrumItems.positionToken));

		Boon = NostrumResearch.startBuilding().parent(ID_Rituals).reference("ritual::buff.luck", "ritual.buff.luck.name")
				.reference("ritual::buff.speed", "ritual.buff.speed.name")
				.reference("ritual::buff.strength", "ritual.buff.strength.name")
				.reference("ritual::buff.leaping", "ritual.buff.leaping.name")
				.reference("ritual::buff.regen", "ritual.buff.regen.name")
				.reference("ritual::buff.fireresist", "ritual.buff.fireresist.name")
				.reference("ritual::buff.invisibility", "ritual.buff.invisibility.name")
				.reference("ritual::buff.nightvision", "ritual.buff.nightvision.name")
				.reference("ritual::buff.waterbreathing", "ritual.buff.waterbreathing.name")
				.build(ID_Boon, Tab_Magica, NostrumResearch.Size.LARGE, 3, 0, true, new ItemStack(Items.SPLASH_POTION));

		Summonkoids = NostrumResearch.startBuilding().parent(ID_Rituals).hiddenParent(ID_Magic_Token).lore(KoidEntity.KoidLore.instance())
				.reference("ritual::koid", "ritual.koid.name").build(ID_Summonkoids, Tab_Magica,
						NostrumResearch.Size.NORMAL, 3, 2, true, new ItemStack(NostrumItems.essencePhysical));

		Loretable = NostrumResearch.startBuilding().hiddenParent(ID_Spellcraft).parent(ID_Rituals)
				.reference("ritual::lore_table", "ritual.lore_table.name").build(ID_Loretable, Tab_Magica,
						NostrumResearch.Size.NORMAL, 2, 0, true, new ItemStack(NostrumBlocks.loreTable));

		// Mysticism Tab (Resources)
		Magic_Token = NostrumResearch.startBuilding().hiddenParent(ID_Rituals).lore(NostrumItems.reagentMandrakeRoot)
				.reference("ritual::magic_token", "ritual.magic_token.name").build(ID_Magic_Token,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, -1, 0, true,
						new ItemStack(NostrumItems.resourceToken));

		Magic_Token_3 = NostrumResearch.startBuilding().parent(ID_Magic_Token).tier(EMagicTier.KANI)
				.reference("ritual::magic_token_3", "ritual.magic_token_3.name").build(ID_Magic_Token_3,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, -2, 0, true,
						new ItemStack(NostrumItems.resourceToken));

		Essence_Seeds = NostrumResearch.startBuilding().hiddenParent(ID_Magic_Token).lore(NostrumItems.essencePhysical)
				.lore(KoidEntity.KoidLore.instance()).lore(WispEntity.WispLoreTag.instance())
				.reference("ritual::essence_seed", "ritual.essence_seed.name").build(ID_Essence_Seeds,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, -3, 0, false, new ItemStack(NostrumItems.reagentSeedEssence));

		Kani = NostrumResearch.startBuilding().parent(ID_Magic_Token).lore(NostrumItems.resourceToken).tier(EMagicTier.KANI)
				.reference("ritual::kani", "ritual.kani.name").build(ID_Kani, Tab_Mysticism, NostrumResearch.Size.NORMAL,
						-1, 1, true, new ItemStack(NostrumItems.crystalMedium));

		Vani = NostrumResearch.startBuilding().parent(ID_Kani).tier(EMagicTier.VANI).lore(NostrumItems.resourceToken)
				.reference("ritual::vani", "ritual.vani.name").build(ID_Vani, Tab_Mysticism, NostrumResearch.Size.LARGE,
						-1, 2, true, new ItemStack(NostrumItems.crystalLarge));

		Seeking_Gems = NostrumResearch.startBuilding().parent(ID_Kani)
				.reference("ritual::create_seeking_gem", "ritual.create_seeking_gem.name").build(ID_Seeking_Gems,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, -2, 1, true,
						new ItemStack(NostrumItems.seekingGem));

		Spellrunes = NostrumResearch.startBuilding().hiddenParent(ID_Magic_Token).hiddenParent(ID_Spellcraft)
				.lore(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE))).reference("ritual::rune.physical", "ritual.rune.physical.name")
				.reference("ritual::rune.single", "ritual.rune.single.name")
				.reference("ritual::rune.inflict", "ritual.rune.inflict.name")
				.reference("ritual::rune.touch", "ritual.rune.touch.name")
				.reference("ritual::rune.self", "ritual.rune.self.name").build(ID_Spellrunes,
						Tab_Mysticism, NostrumResearch.Size.GIANT, 0, 0, true,
						SpellRune.getRune(EMagicElement.FIRE));

		Rune_Shaper = NostrumResearch.startBuilding().parent(ID_Kani).parent(ID_Spellrunes)
				.reference("ritual::create_rune_shaper", "ritual.create_rune_shaper.name").build(ID_Rune_Shaper,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, 0, 1, true,
						new ItemStack(NostrumBlocks.runeShaper));

		Fierce_Infusion = NostrumResearch.startBuilding().hiddenParent(ID_Kani)
				.reference("ritual::fierce_infusion", "ritual.fierce_infusion.name").build(ID_Fierce_Infusion,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, 1, 0, true,
						new ItemStack(NostrumItems.resourceSlabFierce));

		Kind_Infusion = NostrumResearch.startBuilding().hiddenParent(ID_Kani)
				.reference("ritual::kind_infusion", "ritual.kind_infusion.name").build(ID_Kind_Infusion,
						Tab_Mysticism, NostrumResearch.Size.NORMAL, 3, 0, true,
						new ItemStack(NostrumItems.resourceSlabKind));

		Balanced_Infusion = NostrumResearch.startBuilding().hiddenParent(ID_Vani).parent(ID_Fierce_Infusion).parent(ID_Kind_Infusion)
				.reference("ritual::balanced_infusion", "ritual.balanced_infusion.name").build(ID_Balanced_Infusion,
						Tab_Mysticism, NostrumResearch.Size.LARGE, 2, 1, true,
						new ItemStack(NostrumItems.resourceSlabBalanced));

		// Outfitting (weapon/armor)
		Mage_Staff = NostrumResearch.startBuilding().hiddenParent(ID_Rituals)
				.reference("ritual::mage_staff", "ritual.mage_staff.name").lore(NostrumItems.resourceToken)
				.build(ID_Mage_Staff, Tab_Outfitting, NostrumResearch.Size.NORMAL, 1, 0, true,
						new ItemStack(NostrumItems.mageStaff));

		Thanos_Staff = NostrumResearch.startBuilding().parent(ID_Mage_Staff).hiddenParent(ID_Thano_Pendant)
				.reference("ritual::thanos_staff", "ritual.thanos_staff.name").lore(NostrumItems.thanoPendant)
				.build(ID_Thanos_Staff, Tab_Outfitting, NostrumResearch.Size.LARGE, 2, 0, true,
						new ItemStack(NostrumItems.thanosStaff));

		Caster_Wand = NostrumResearch.startBuilding().parent(ID_Mage_Staff).hiddenParent(ID_Modification_Table)
				.reference("ritual::caster_wand", "ritual.caster_wand.name")
				.build(ID_Caster_Wand, Tab_Outfitting, NostrumResearch.Size.NORMAL, 0, 0, true,
						new ItemStack(NostrumItems.casterWand));

		Warlock_Sword = NostrumResearch.startBuilding().parent(ID_Mage_Staff).parent(ID_Mage_Blade).hiddenParent(ID_Vani)
				.reference("ritual::spawn_warlock_sword", "ritual.spawn_warlock_sword.name").build(ID_Warlock_Sword,
						Tab_Outfitting, NostrumResearch.Size.LARGE, 1, 1, true, new ItemStack(NostrumItems.warlockSword));

		Soul_Daggers = NostrumResearch.startBuilding().parent(ID_Thanos_Staff).parent(ID_Warlock_Sword).hiddenParent(ID_Vani)
				.reference("ritual::spawn_soul_dagger", "ritual.spawn_soul_dagger.name").build(ID_Soul_Daggers,
						Tab_Outfitting, NostrumResearch.Size.LARGE, 2, 1, true, new ItemStack(NostrumItems.soulDagger));

		Mage_Blade = NostrumResearch.startBuilding().parent(ID_Enchanted_Weapons).hiddenParent(ID_Mage_Staff)
				.spellComponent(null, EAlteration.ENCHANT)
				.reference("ritual::spawn_mage_blade", "ritual.spawn_mage_blade.name").build(ID_Mage_Blade,
				Tab_Outfitting, NostrumResearch.Size.LARGE, 0, 1, true, new ItemStack(NostrumItems.mageBlade));

		Sword_Fire = NostrumResearch.startBuilding().hiddenParent(ID_Mage_Blade).hiddenParent(ID_Enchanted_Armor_Adv)
				.reference("ritual::spawn_sword_fire", "ritual.spawn_sword_fire.name").build(ID_Sword_Fire,
				Tab_Outfitting, NostrumResearch.Size.NORMAL, 1, 3, true, new ItemStack(NostrumItems.flameRod));
		
		Sword_Earth = NostrumResearch.startBuilding().hiddenParent(ID_Mage_Blade).hiddenParent(ID_Enchanted_Armor_Adv)
			.reference("ritual::spawn_sword_earth", "ritual.spawn_sword_earth.name").build(ID_Sword_Earth,
			Tab_Outfitting, NostrumResearch.Size.NORMAL, 2, 3, true, new ItemStack(NostrumItems.earthPike));
		
		Sword_Ender = NostrumResearch.startBuilding().hiddenParent(ID_Mage_Blade).hiddenParent(ID_Enchanted_Armor_Adv)
			.reference("ritual::spawn_sword_ender", "ritual.spawn_sword_ender.name").build(ID_Sword_Ender,
			Tab_Outfitting, NostrumResearch.Size.NORMAL, 1, 4, true, new ItemStack(NostrumItems.enderRod));
		
		Sword_Physical = NostrumResearch.startBuilding().hiddenParent(ID_Mage_Blade).hiddenParent(ID_Enchanted_Armor_Adv)
			.reference("ritual::spawn_sword_physical", "ritual.spawn_sword_physical.name").build(ID_Sword_Physical,
			Tab_Outfitting, NostrumResearch.Size.NORMAL, 2, 4, true, new ItemStack(NostrumItems.deepMetalAxe));

		Enchanted_Weapons = NostrumResearch.startBuilding().parent(ID_Enchanted_Armor)
				.reference("ritual::spawn_enchanted_weapon", "ritual.spawn_enchanted_weapon.name")
				.build(ID_Enchanted_Weapons, Tab_Outfitting, NostrumResearch.Size.LARGE, -1, 1, true,
						new ItemStack(AspectedWeapon.get(EMagicElement.WIND, AspectedWeapon.Type.MASTER)));

		Enchanted_Armor = NostrumResearch.startBuilding().hiddenParent(ID_Rituals).tier(EMagicTier.KANI)
				.reference("ritual::mage_armor", "ritual.mage_armor.name")
				.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
				.build(ID_Enchanted_Armor, Tab_Outfitting, NostrumResearch.Size.GIANT, -2, 0, true,
						new ItemStack(ElementalArmor.get(EMagicElement.FIRE, EquipmentSlot.CHEST, ElementalArmor.Type.MASTER)));

		Enchanted_Armor_Adv = NostrumResearch.startBuilding().parent(ID_Enchanted_Armor).hiddenParent(ID_Kind_Infusion)
				.hiddenParent(ID_Fierce_Infusion)
				.hiddenParent(ID_Vani)
				.reference("ritual::spawn_enchanted_armor", "ritual.spawn_enchanted_armor.name")
				.build(ID_Enchanted_Armor_Adv, Tab_Outfitting, NostrumResearch.Size.LARGE, -1, 3, true,
						new ItemStack(ElementalArmor.get(EMagicElement.ENDER, EquipmentSlot.CHEST, ElementalArmor.Type.MASTER)));

		Dragon_Armor = NostrumResearch.startBuilding().parent(ID_Enchanted_Armor).lore(TameRedDragonEntity.TameRedDragonLore.instance())
				.reference("ritual::craft_dragonarmor_body_iron", "ritual.craft_dragonarmor_body_iron.name")
				.build(ID_Dragon_Armor, Tab_Outfitting, NostrumResearch.Size.LARGE, -2, 4, true,
						new ItemStack(DragonArmor.GetArmor(DragonEquipmentSlot.HELM, DragonArmorMaterial.IRON)));

		Mirror_Shield = NostrumResearch.startBuilding().parent(ID_Enchanted_Armor).parent(NostrumMagica.CuriosProxy.isEnabled() ? CuriosProxy.ID_Belts : ID_Origin)
				.reference("ritual::mirror_shield", "ritual.mirror_shield.name").build(ID_Mirror_Shield,
						Tab_Outfitting, NostrumResearch.Size.LARGE, -3, 3, true, new ItemStack(NostrumItems.mirrorShield));

		True_Mirror_Shield = NostrumResearch.startBuilding().parent(ID_Mirror_Shield).lore(NostrumItems.mirrorShield)
				.reference("ritual::true_mirror_shield", "ritual.true_mirror_shield.name").build(ID_True_Mirror_Shield,
						Tab_Outfitting, NostrumResearch.Size.NORMAL, -3, 4, false,
						new ItemStack(NostrumItems.mirrorShieldImproved));

		Reagent_Bag = NostrumResearch.startBuilding().hiddenParent(ID_Rituals).lore(NostrumItems.reagentMandrakeRoot)
				.reference("ritual::reagent_bag", "ritual.reagent_bag.name").build(ID_Reagent_Bag,
						Tab_Outfitting, NostrumResearch.Size.NORMAL, -2, -1, true, new ItemStack(NostrumItems.reagentBag));

		Rune_Bag = NostrumResearch.startBuilding().parent(ID_Reagent_Bag).lore(NostrumItems.GetRune(new SpellComponentWrapper(EMagicElement.FIRE)))
				.reference("ritual::rune_bag", "ritual.rune_bag.name").build(ID_Rune_Bag, Tab_Outfitting,
						NostrumResearch.Size.NORMAL, -3, -1, true, new ItemStack(NostrumItems.runeBag));

		Hookshot_Medium = NostrumResearch.startBuilding().hiddenParent(ID_Rituals).hiddenParent(ID_Kani).lore(NostrumItems.hookshotWeak)
				.reference("ritual::improve_hookshot_medium", "ritual.improve_hookshot_medium.name")
				.build(ID_Hookshot_Medium, Tab_Outfitting, NostrumResearch.Size.NORMAL, 1, -1, true,
						new ItemStack(NostrumItems.hookshotMedium));

		Hookshot_Strong = NostrumResearch.startBuilding().parent(ID_Hookshot_Medium).hiddenParent(ID_Vani)
				.reference("ritual::improve_hookshot_strong", "ritual.improve_hookshot_strong.name")
				.build(ID_Hookshot_Strong, Tab_Outfitting, NostrumResearch.Size.NORMAL, 2, -1, false,
						new ItemStack(NostrumItems.hookshotStrong));

		Hookshot_Claw = NostrumResearch.startBuilding().parent(ID_Hookshot_Strong)
				.reference("ritual::improve_hookshot_claw", "ritual.improve_hookshot_claw.name").build(ID_Hookshot_Claw,
						Tab_Outfitting, NostrumResearch.Size.NORMAL, 3, -1, false,
						new ItemStack(NostrumItems.hookshotClaw));

		Charms = NostrumResearch.startBuilding().hiddenParent(ID_Magic_Token).lore(NostrumItems.essencePhysical)
				.reference("ritual::charm.physical", "ritual.charm.physical.name")
				.reference("ritual::charm.fire", "ritual.charm.fire.name")
				.reference("ritual::charm.ice", "ritual.charm.ice.name")
				.reference("ritual::charm.earth", "ritual.charm.earth.name")
				.reference("ritual::charm.wind", "ritual.charm.wind.name")
				.reference("ritual::charm.lightning", "ritual.charm.lightning.name")
				.reference("ritual::charm.ender", "ritual.charm.ender.name").build(ID_Charms,
						Tab_Outfitting, NostrumResearch.Size.NORMAL, 0, -1, true,
						MagicCharm.getCharm(EMagicElement.ENDER, 1));

		// Tinkering
		Geogems = NostrumResearch.startBuilding().parent(ID_Rituals).tier(EMagicTier.KANI).reference(NostrumItems.positionCrystal)
				.build(ID_Geogems, Tab_Tinkering, NostrumResearch.Size.LARGE, 1, -1, false,
				new ItemStack(NostrumItems.positionCrystal));

		Geotokens = NostrumResearch.startBuilding().parent(ID_Geogems).lore(NostrumItems.positionCrystal)
				.reference(NostrumItems.positionToken).build(ID_Geotokens, Tab_Tinkering, NostrumResearch.Size.LARGE, 1, 1,
						true, new ItemStack(NostrumItems.positionToken));

		Obelisks = NostrumResearch.startBuilding().parent(ID_Geotokens).hiddenParent(ID_Markrecall).hiddenParent(ID_Balanced_Infusion)
				// .lore(NostrumItems.positionToken)
				.tier(EMagicTier.VANI).reference("builtin::guides::obelisks", "info.obelisks.name")
				.reference("ritual::create_obelisk", "ritual.create_obelisk.name").build(ID_Obelisks,
						Tab_Tinkering, NostrumResearch.Size.GIANT, 2, 2, true,
						new ItemStack(NostrumBlocks.dungeonBlock));

		Sorceryportal = NostrumResearch.startBuilding().hiddenParent(ID_Markrecall).parent(ID_Obelisks)
				.reference("ritual::spawn_sorcery_portal", "ritual.spawn_sorcery_portal.name").build(ID_Sorceryportal,
						Tab_Tinkering, NostrumResearch.Size.NORMAL, 2, 3, true, new ItemStack(NostrumBlocks.sorceryPortal));

		Teleportrune = NostrumResearch.startBuilding().parent(ID_Geogems).hiddenParent(ID_Markrecall).lore(NostrumItems.positionCrystal)
				.reference("ritual::teleportrune", "ritual.teleportrune.name").build(ID_Teleportrune,
						Tab_Tinkering, NostrumResearch.Size.NORMAL, 2, 0, true, new ItemStack(NostrumBlocks.teleportRune));

		Paradox_Mirrors = NostrumResearch.startBuilding().parent(ID_Geogems).hiddenParent(ID_Item_Duct).lore(NostrumItems.positionCrystal)
				.tier(EMagicTier.KANI).reference("ritual::paradox_mirror", "ritual.paradox_mirror.name")
				.build(ID_Paradox_Mirrors, Tab_Tinkering, NostrumResearch.Size.NORMAL, 3, 0, true,
						new ItemStack(NostrumBlocks.paradoxMirror));

		Silver_Mirror = NostrumResearch.startBuilding().parent(ID_Paradox_Mirrors)
				.tier(EMagicTier.VANI).reference("ritual::silver_mirror", "ritual.silver_mirror.name")
				.build(ID_Silver_Mirror, Tab_Tinkering, NostrumResearch.Size.NORMAL, 3, 1, true,
						new ItemStack(NostrumItems.silverMirror));

		Gold_Mirror = NostrumResearch.startBuilding().parent(ID_Silver_Mirror)
				.tier(EMagicTier.VANI).reference("ritual::gold_mirror", "ritual.gold_mirror.name")
				.build(ID_Gold_Mirror, Tab_Tinkering, NostrumResearch.Size.NORMAL, 3, 2, true,
						new ItemStack(NostrumItems.goldMirror));

		Mystic_Anchor = NostrumResearch.startBuilding().parent(ID_Geogems).hiddenParent(ID_Teleportrune).lore(NostrumItems.positionCrystal)
				.reference("ritual::mystic_anchor", "ritual.mystic_anchor.name")
				.build(ID_Mystic_Anchor, Tab_Tinkering, NostrumResearch.Size.NORMAL, 4, 0, true,
						new ItemStack(NostrumBlocks.mysticAnchor));

		Putter = NostrumResearch.startBuilding().hiddenParent(ID_Rituals).hiddenParent(ID_Magic_Token)
				.reference("ritual::putter", "ritual.putter.name").build(ID_Putter, Tab_Tinkering,
						NostrumResearch.Size.NORMAL, -1, -1, true, new ItemStack(NostrumBlocks.putterBlock));

		Active_Hopper = NostrumResearch.startBuilding().parent(ID_Putter).reference("ritual::active_hopper", "ritual.active_hopper.name")
				.build(ID_Active_Hopper, Tab_Tinkering, NostrumResearch.Size.NORMAL, -1, 0, true,
						new ItemStack(NostrumBlocks.activeHopper));

		Item_Duct = NostrumResearch.startBuilding().parent(ID_Active_Hopper).reference("ritual::item_duct", "ritual.item_duct.name")
				.build(ID_Item_Duct, Tab_Tinkering, NostrumResearch.Size.LARGE, -1, 1, true,
						new ItemStack(NostrumBlocks.itemDuct));

		Magicfacade = NostrumResearch.startBuilding().hiddenParent(ID_Rituals)
				.reference("ritual::mimic_facade", "ritual.mimic_facade.name")
				.reference("ritual::mimic_door", "ritual.mimic_door.name").build(ID_Magicfacade,
						Tab_Tinkering, NostrumResearch.Size.NORMAL, -2, -1, true, new ItemStack(NostrumBlocks.mimicFacade));

		Rune_Library = NostrumResearch.startBuilding().hiddenParent(ID_Rituals).tier(EMagicTier.MANI)
				.reference("ritual::rune_library", "ritual.rune_library.name")
				.build(ID_Rune_Library,
						Tab_Tinkering, NostrumResearch.Size.NORMAL, -2, 0, true, new ItemStack(NostrumBlocks.runeLibrary));

		// Advanced Magica
		{
			NostrumResearch.Builder builder = NostrumResearch.startBuilding();
			builder.reference("ritual::thano_infusion", "ritual.thano_infusion.name");
			if (!ModConfig.config.usingEasierThano()) {
				builder.hiddenParent(ID_Vani).hiddenParent(ID_Reagent_Bag).hiddenParent(ID_Mage_Staff)
						.lore(NostrumItems.resourceToken).lore(NostrumItems.masteryOrb).lore(NostrumItems.essencePhysical);

			} else {
				builder.hiddenParent(ID_Kani).hiddenParent(ID_Reagent_Bag).lore(NostrumItems.resourceToken)
						.lore(NostrumItems.essencePhysical);
			}

			Thano_Pendant = builder.build(ID_Thano_Pendant, Tab_Advanced_Magica, NostrumResearch.Size.GIANT, 2, -1, true,
					new ItemStack(NostrumItems.thanoPendant));
		}

		Modification_Table = NostrumResearch.startBuilding().hiddenParent(ID_Vani).hiddenParent(ID_Loretable)
				.hiddenParent(ID_Spelltomes_Advanced)
				.reference("ritual::modification_table", "ritual.modification_table.name").build(ID_Modification_Table,
						Tab_Advanced_Magica, NostrumResearch.Size.GIANT, 0, -1, true,
						new ItemStack(NostrumBlocks.modificationTable));

		Stat_Items = NostrumResearch.startBuilding().hiddenParent(ID_Vani).lore(NostrumItems.roseBlood)
				.reference("ritual::form_essential_ooze", "ritual.form_essential_ooze.name")
				.reference("ritual::form_living_flute", "ritual.form_living_flute.name")
				.reference("ritual::form_eldrich_pendant", "ritual.form_eldrich_pendant.name")
				.reference("ritual::form_primordial_mirror", "ritual.form_primordial_mirror.name")
				.build(ID_Stat_Items,
						Tab_Advanced_Magica, NostrumResearch.Size.GIANT, -2, -1, true,
						new ItemStack(NostrumItems.resourceSkillPendant));

		Ender_Pin = NostrumResearch.startBuilding().parent(ID_Stat_Items).hiddenParent(ID_Sorceryportal)
				.reference("ritual::ender_pin", "ritual.ender_pin.name").build(ID_Ender_Pin,
						Tab_Advanced_Magica, NostrumResearch.Size.LARGE, -3, 0, true,
						new ItemStack(NostrumItems.skillEnderPin));

		Soulbound_Pets = NostrumResearch.startBuilding().hiddenParent(ID_Kani).lore(IPetWithSoul.SoulBoundLore.instance())
				.reference("ritual::revive_soulbound_pet_dragon", "ritual.revive_soulbound_pet_dragon.name")
				.reference("ritual::revive_soulbound_pet_wolf", "ritual.revive_soulbound_pet_wolf.name")
				.build(ID_Soulbound_Pets, Tab_Advanced_Magica, NostrumResearch.Size.GIANT, 0, 1, true,
						new ItemStack(NostrumItems.dragonSoulItem));

		Wolf_Transformation = NostrumResearch.startBuilding().parent(ID_Rituals).hiddenParent(ID_Soul_Daggers).hiddenParent(ID_Kani)
				.lore(WolfTameLore.instance()).reference("ritual::transform_wolf", "ritual.transform_wolf.name")
				.build(ID_Wolf_Transformation, Tab_Magica, NostrumResearch.Size.GIANT, 4, 0, true,
						new ItemStack(Items.BONE));

		Mana_Armor = NostrumResearch.startBuilding().hiddenParent(ID_Enchanted_Armor_Adv).hiddenParent(ID_Soul_Daggers).tier(EMagicTier.VANI)
				.reference("ritual::mana_armorer", "ritual.mana_armorer.name").build(ID_Mana_Armor,
						Tab_Advanced_Magica, NostrumResearch.Size.GIANT, 1, 0, true,
						new ItemStack(NostrumBlocks.manaArmorerBlock));

		// NostrumResearchTab tab, Size size, int x, int y, boolean hidden, ItemStack
		// icon
	}
	
}
