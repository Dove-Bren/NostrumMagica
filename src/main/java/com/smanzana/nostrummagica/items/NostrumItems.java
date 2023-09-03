package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumItems {
	
	// Not sure where to put these...
	public static final String ID_EARTH = "earth";
	public static final String ID_ENDER = "ender";
	public static final String ID_FIRE = "fire";
	public static final String ID_ICE = "ice";
	public static final String ID_LIGHTNING = "lightning";
	public static final String ID_PHYSICAL = "physical";
	public static final String ID_WIND = "wind";
	
	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem; // TODO could clean up; is just a BlockItem
	@ObjectHolder(ArcaneWolfSoulItem.ID) public static ArcaneWolfSoulItem arcaneWolfSoulItem;
	@ObjectHolder(BlankScroll.ID) public static BlankScroll blankScroll;
	@ObjectHolder(ChalkItem.ID) public static ChalkItem chalkItem;
	//@ObjectHolder(DragonArmor.ID_) public static DragonArmor dragonArmor_Helm; // TODO decide? Enumerate?
	@ObjectHolder(DragonEgg.ID) public static DragonEgg dragonEgg;
	@ObjectHolder(DragonEggFragment.ID) public static DragonEggFragment dragonEggFragment;
	@ObjectHolder(DragonSoulItem.ID) public static DragonSoulItem dragonSoulItem;
	//@ObjectHolder(EnchantedArmor.ID_ ) public static AltarItem altarItem;  // TODO decide? Enumerate?
	//@ObjectHolder(EnchantedWeapon.ID) public static AltarItem altarItem;  // TODO decide? Enumerate?
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_EARTH) public static EssenceItem essenceEarth;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_FIRE) public static EssenceItem essenceFire;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_ICE) public static EssenceItem essenceIce;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_ENDER) public static EssenceItem essenceEnder;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_LIGHTNING) public static EssenceItem essenceLightning;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_PHYSICAL) public static EssenceItem essencePhysical;
	@ObjectHolder(EssenceItem.ID_PREFIX + ID_WIND) public static EssenceItem essenceWind;
	@ObjectHolder(HookshotItem.ID_PREFIX + "weak") public static AltarItem hookshotWeak;
	@ObjectHolder(HookshotItem.ID_PREFIX + "medium") public static HookshotItem hookshotMedium;
	@ObjectHolder(HookshotItem.ID_PREFIX + "strong") public static HookshotItem hookshotStrong;
	@ObjectHolder(HookshotItem.ID_PREFIX + "claw") public static HookshotItem hookshotClaw;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + "unattuned") public static InfusedGemItem infusedGemUnattuned;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_EARTH) public static InfusedGemItem infusedGemEarth;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_ENDER) public static InfusedGemItem infusedGemEnder;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_FIRE) public static InfusedGemItem infusedGemFire;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_ICE) public static InfusedGemItem infusedGemIce;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_LIGHTNING) public static InfusedGemItem infusedGemLightning;
	@ObjectHolder(InfusedGemItem.ID_PREFIX + ID_WIND) public static InfusedGemItem infusedGemWind;
	@ObjectHolder(MageStaff.ID) public static MageStaff mageStaff;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_EARTH) public static MagicCharm magicCharmEarth;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_ENDER) public static MagicCharm magicCharmEnder;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_FIRE) public static MagicCharm magicCharmFire;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_ICE) public static MagicCharm magicCharmIce;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_LIGHTNING) public static MagicCharm magicCharmLightning;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_PHYSICAL) public static MagicCharm magicCharmPhysical;
	@ObjectHolder(MagicCharm.ID_PREFIX + ID_WIND) public static MagicCharm magicCharmWind;
	@ObjectHolder(MagicSwordBase.ID) public static MagicSwordBase magicSwordBase;
	@ObjectHolder(MasteryOrb.ID) public static MasteryOrb masteryOrb;
	@ObjectHolder(MirrorItem.ID) public static MirrorItem mirrorItem; // TODO could clean up; is just a BlockItem
	@ObjectHolder(MirrorShield.ID) public static MirrorShield mirrorShield;
	@ObjectHolder(MirrorShieldImproved.ID) public static MirrorShieldImproved mirrorShieldImproved;
	@ObjectHolder(NostrumGuide.ID) public static NostrumGuide nostrumGuide;
	@ObjectHolder(NostrumResourceItem.ID_TOKEN) public static NostrumResourceItem resourceToken;
	@ObjectHolder(NostrumResourceItem.ID_PENDANT_LEFT) public static NostrumResourceItem resourcePendantLeft;
	@ObjectHolder(NostrumResourceItem.ID_PENDANT_RIGHT) public static NostrumResourceItem resourcePendantRight;
	@ObjectHolder(NostrumResourceItem.ID_SLAB_FIERCE) public static NostrumResourceItem resourceSlabFierce;
	@ObjectHolder(NostrumResourceItem.ID_SLAB_KIND) public static NostrumResourceItem resourceSlabKind;
	@ObjectHolder(NostrumResourceItem.ID_SLAB_BALANCED) public static NostrumResourceItem resourceSlabBalanced;
	@ObjectHolder(NostrumResourceItem.ID_SPRITE_CORE) public static NostrumResourceItem resourceSpriteCore;
	@ObjectHolder(NostrumResourceItem.ID_ENDER_BRISTLE) public static NostrumResourceItem resourceEnderBristle;
	@ObjectHolder(NostrumResourceItem.ID_WISP_PEBBLE) public static NostrumResourceItem resourceWispPebble;
	@ObjectHolder(NostrumResourceItem.ID_MANA_LEAF) public static NostrumResourceItem resourceManaLeaf;
	@ObjectHolder(NostrumResourceItem.ID_EVIL_THISTLE) public static NostrumResourceItem resourceEvilThistle;
	@ObjectHolder(NostrumResourceItem.ID_DRAGON_WING) public static NostrumResourceItem resourceDragonWing;
	@ObjectHolder(NostrumResourceItem.ID_SEEKING_GEM) public static NostrumResourceItem resourceSeekingGem;
	@ObjectHolder(NostrumResourceCrystal.ID_CRYSTAL_SMALL) public static NostrumResourceCrystal crystalSmall;
	@ObjectHolder(NostrumResourceCrystal.ID_CRYSTAL_MEDIUM) public static NostrumResourceCrystal crystalMedium;
	@ObjectHolder(NostrumResourceCrystal.ID_CRYSTAL_LARGE) public static NostrumResourceCrystal crystalLarge;
	@ObjectHolder(NostrumRoseItem.ID_BLOOD_ROSE) public static NostrumRoseItem roseBlood;
	@ObjectHolder(NostrumRoseItem.ID_ELDRICH_ROSE) public static NostrumRoseItem roseEldrich;
	@ObjectHolder(NostrumRoseItem.ID_PALE_ROSE) public static NostrumRoseItem rosePale;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_MIRROR) public static NostrumSkillItem skillMirror;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_OOZE) public static NostrumSkillItem skillOoze;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_PENDANT) public static NostrumSkillItem skillPendant;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_FLUTE) public static NostrumSkillItem skillFlute;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_ENDER_PIN) public static NostrumSkillItem skillEnderPin;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_SCROLL_SMALL) public static NostrumSkillItem skillScrollSmall;
	@ObjectHolder(NostrumSkillItem.ID_SKILL_SCROLL_LARGE) public static NostrumSkillItem skillScrollLarge;
	@ObjectHolder(PositionCrystal.ID) public static PositionCrystal positionCrystal;
	@ObjectHolder(PositionToken.ID) public static PositionToken positionToken;
	@ObjectHolder(ReagentItem.ID_MANDRAKE_ROOT) public static ReagentItem reagentMandrakeRoot;
	@ObjectHolder(ReagentItem.ID_SPIDER_SILK) public static ReagentItem reagentSpiderSilk;
	@ObjectHolder(ReagentItem.ID_BLACK_PEARL) public static ReagentItem reagentBlackPearl;
	@ObjectHolder(ReagentItem.ID_SKY_ASH) public static ReagentItem reagentSkyAsh;
	@ObjectHolder(ReagentItem.ID_GINSENG) public static ReagentItem reagentGinseng;
	@ObjectHolder(ReagentItem.ID_GRAVE_DUST) public static ReagentItem reagentGraveDust;
	@ObjectHolder(ReagentItem.ID_CRYSTABLOOM) public static ReagentItem reagentCrystabloom;
	@ObjectHolder(ReagentItem.ID_MANI_DUST) public static ReagentItem reagentManiDust;
	@ObjectHolder(ReagentBag.ID) public static ReagentBag reagentBag;
	@ObjectHolder(ReagentSeed.ID_MANDRAKE_SEED) public static ReagentSeed reagentSeedMandrake;
	@ObjectHolder(ReagentSeed.ID_GINSENG_SEED) public static ReagentSeed reagentSeedGinseng;
	@ObjectHolder(ReagentSeed.ID_ESSENCE_SEED) public static ReagentSeed reagentSeedEssence;
	@ObjectHolder(RuneBag.ID) public static RuneBag runeBag;
	@ObjectHolder(SoulDagger.ID) public static SoulDagger soulDagger;
	@ObjectHolder(SpellcraftGuide.ID) public static SpellcraftGuide spellcraftGuide;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_NOVICE) public static SpellPlate spellPlateNovice;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_ADVANCED) public static SpellPlate spellPlateAdvanced;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_COMBAT) public static SpellPlate spellPlateCombat;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_DEATH) public static SpellPlate spellPlateDeath;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_SPOOKY) public static SpellPlate spellPlateSpooky;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_MUTED) public static SpellPlate spellPlateMuted;
	@ObjectHolder(SpellPlate.ID_PREFIX + SpellTome.TomeStyle.ID_SUFFIX_LIVING) public static SpellPlate spellPlateLiving;
	@ObjectHolder(SpellScroll.ID) public static SpellScroll spellScroll;
	@ObjectHolder(SpellTableItem.ID) public static SpellTableItem spellTableItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
//	@ObjectHolder(AltarItem.ID) public static AltarItem altarItem;
	
	
	public NostrumItems() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static Item.Properties PropBase() {
		return new Item.Properties()
				.group(NostrumMagica.creativeTab)
				;
	}
	
	public static Item.Properties PropLowStack() {
		return PropBase()
				.maxStackSize(16);
	}
	
	public static Item.Properties PropUnstackable() {
		return PropBase()
				.maxStackSize(1);
	}
	
	public static Item.Properties PropEquipment() {
		return PropUnstackable()
				;
	}
	
	public static Item.Properties PropTomeBase() {
		return new Item.Properties()
				.group(NostrumMagica.enhancementTab);
	}
	
	public static Item.Properties PropTomeUnstackable() {
		return PropTomeBase()
				.maxStackSize(1);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		broke() {
			// I removed all these items doing a setRegistryName() but they need it?
		};
		
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	registry.register(NostrumGuide.instance());
    	registry.register(SpellcraftGuide.instance());
    	registry.register(SpellTome.instance());
    	registry.register(SpellPlate.instance());
    	registry.register(BlankScroll.instance());
    	registry.register(SpellScroll.instance());
    	registry.register(SpellTableItem.instance());
    	registry.register(MirrorItem.instance());
    	registry.register(MagicSwordBase.instance());
    	registry.register(MagicArmorBase.helm());
    	registry.register(MagicArmorBase.chest());
    	registry.register(MagicArmorBase.legs());
		registry.register(MagicArmorBase.feet());
		
    	EnchantedWeapon.registerWeapons(registry);
    	EnchantedArmor.registerArmors(registry);
    	DragonArmor.registerArmors(registry);
    	
    	registry.register(MirrorShield.instance());
    	registry.register(MirrorShieldImproved.instance());
    	
    	registry.register(ReagentItem.instance());
		
		MinecraftForge.addGrassSeed(ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1), 6);
		MinecraftForge.addGrassSeed(ReagentItem.instance().getReagent(ReagentType.GINSENG, 1), 5);
    	
    	registry.register(InfusedGemItem.instance());
    	
    	registry.register(SpellRune.instance());
    	
    	registry.register(NostrumResourceItem.instance());
    	
    	registry.register(ReagentBag.instance());
    	
    	registry.register(SeekerIdol.instance());
    	
    	registry.register(ShrineSeekingGem.instance());
    	
    	registry.register(ChalkItem.instance());
    	
    	registry.register(AltarItem.instance());
    	
    	registry.register(PositionCrystal.instance());
    	
    	registry.register(PositionToken.instance());
    	
    	registry.register(SpellTomePage.instance());

    	registry.register(EssenceItem.instance());
    	
    	registry.register(MageStaff.instance());
    	registry.register(ThanoPendant.instance());
    	registry.register(ThanosStaff.instance());
    	registry.register(MagicCharm.instance());
    	
    	registry.register(RuneBag.instance());
    	
    	registry.register(DragonEggFragment.instance());
    	
    	registry.register(DragonEgg.instance());
    	
    	registry.register(NostrumSkillItem.instance());
    	
    	registry.register(NostrumRoseItem.instance());
    	
    	registry.register(WarlockSword.instance());
    	
    	registry.register(HookshotItem.instance());

    	registry.register(ReagentSeed.mandrake);
    	registry.register(ReagentSeed.ginseng);
    	registry.register(ReagentSeed.essence);
    	
    	registry.register(MasteryOrb.instance());
    	
    	registry.register(DragonSoulItem.instance());
    	registry.register(SoulDagger.instance());
    	registry.register(ArcaneWolfSoulItem.instance());
    	
    	registerOreDict(); // Different func to make it easier to read
	}
	
	private void registerOreDict() {
		OreDictionary.registerOre(OREDICT_RUNE_ANY, new ItemStack(SpellRune.instance(), 1, OreDictionary.WILDCARD_VALUE));
		
		OreDictionary.registerOre(OREDICT_REAGENT_ANY, new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(OREDICT_REAGENT_MANDRAKEROOT, ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_GINSENG, ReagentItem.instance().getReagent(ReagentType.GINSENG, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_SPIDERSILK, ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_BLACKPEARL, ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_SKYASH, ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_GRAVEDUST, ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_MANIDUST, ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
		OreDictionary.registerOre(OREDICT_REAGENT_CRYSTABLOOM, ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1));
		
		OreDictionary.registerOre(OREDICT_INFGEM_ANY, new ItemStack(InfusedGemItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(OREDICT_INFGEM_VOID, InfusedGemItem.instance().getGem(null, 1));
		OreDictionary.registerOre(OREDICT_INFGEM_FIRE, InfusedGemItem.instance().getGem(EMagicElement.FIRE, 1));
		OreDictionary.registerOre(OREDICT_INFGEM_ICE, InfusedGemItem.instance().getGem(EMagicElement.ICE, 1));
		OreDictionary.registerOre(OREDICT_INFGEM_WIND, InfusedGemItem.instance().getGem(EMagicElement.WIND, 1));
		OreDictionary.registerOre(OREDICT_INFGEM_EARTH, InfusedGemItem.instance().getGem(EMagicElement.EARTH, 1));
		OreDictionary.registerOre(OREDICT_INFGEM_LIGHTNING, InfusedGemItem.instance().getGem(EMagicElement.LIGHTNING, 1));
		OreDictionary.registerOre(OREDICT_INFGEM_ENDER, InfusedGemItem.instance().getGem(EMagicElement.ENDER, 1));
		
		OreDictionary.registerOre(OREDICT_TOKEN, NostrumResourceItem.getItem(ResourceType.TOKEN, 1));
		OreDictionary.registerOre(OREDICT_CRYSTALSMALL, NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
		OreDictionary.registerOre(OREDICT_CRYSTALMEDIUM, NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1));
		OreDictionary.registerOre(OREDICT_CRYSTALLARGE, NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
		OreDictionary.registerOre(OREDICT_PENDANTLEFT, NostrumResourceItem.getItem(ResourceType.PENDANT_LEFT, 1));
		OreDictionary.registerOre(OREDICT_PENDANTRIGHT, NostrumResourceItem.getItem(ResourceType.PENDANT_RIGHT, 1));
		OreDictionary.registerOre(OREDICT_SLABFIERCE, NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1));
		OreDictionary.registerOre(OREDICT_SLABKIND, NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1));
		OreDictionary.registerOre(OREDICT_SLABBALANCED, NostrumResourceItem.getItem(ResourceType.SLAB_BALANCED, 1));
		OreDictionary.registerOre(OREDICT_SPRITECORE, NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1));
		OreDictionary.registerOre(OREDICT_ENDERBRISTLE, NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1));
		OreDictionary.registerOre(OREDICT_WISPPEBBLE, NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1));
		
		OreDictionary.registerOre(OREDICT_SPELLPAGE_ANY, new ItemStack(SpellTomePage.instance(), 1, OreDictionary.WILDCARD_VALUE));
		
		OreDictionary.registerOre(OREDICT_ESSENCE_ANY, new ItemStack(EssenceItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(OREDICT_ESSENCE_VOID, EssenceItem.getEssence(EMagicElement.PHYSICAL, 1));
		OreDictionary.registerOre(OREDICT_ESSENCE_FIRE, EssenceItem.getEssence(EMagicElement.FIRE, 1));
		OreDictionary.registerOre(OREDICT_ESSENCE_ICE, EssenceItem.getEssence(EMagicElement.ICE, 1));
		OreDictionary.registerOre(OREDICT_ESSENCE_WIND, EssenceItem.getEssence(EMagicElement.WIND, 1));
		OreDictionary.registerOre(OREDICT_ESSENCE_EARTH, EssenceItem.getEssence(EMagicElement.EARTH, 1));
		OreDictionary.registerOre(OREDICT_ESSENCE_LIGHTNING, EssenceItem.getEssence(EMagicElement.LIGHTNING, 1));
		OreDictionary.registerOre(OREDICT_ESSENCE_ENDER, EssenceItem.getEssence(EMagicElement.ENDER, 1));
		
		OreDictionary.registerOre(OREDICT_SKILLITEM_ANY, new ItemStack(NostrumSkillItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(OREDICT_SKILLITEM_MIRROR, NostrumSkillItem.getItem(SkillItemType.MIRROR, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_OOZE, NostrumSkillItem.getItem(SkillItemType.OOZE, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_PENDANT, NostrumSkillItem.getItem(SkillItemType.PENDANT, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_FLUTE, NostrumSkillItem.getItem(SkillItemType.FLUTE, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_WING, NostrumSkillItem.getItem(SkillItemType.WING, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_ENDERPIN, NostrumSkillItem.getItem(SkillItemType.ENDER_PIN, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_RESEARCHSMALL, NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1));
		OreDictionary.registerOre(OREDICT_SKILLITEM_RESEARCHLARGE, NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_LARGE, 1));
		
		OreDictionary.registerOre(OREDICT_ROSE_ANY, new ItemStack(NostrumRoseItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(OREDICT_ROSE_BLOOD, NostrumRoseItem.getItem(RoseType.BLOOD, 1));
		OreDictionary.registerOre(OREDICT_ROSE_ELDRICH, NostrumRoseItem.getItem(RoseType.ELDRICH, 1));
		OreDictionary.registerOre(OREDICT_ROSE_PALE, NostrumRoseItem.getItem(RoseType.PALE, 1));
		
		OreDictionary.registerOre(OREDICT_HOOKSHOT_ANY, new ItemStack(HookshotItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(OREDICT_HOOKSHOT_WEAK, new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.WEAK, false)));
		OreDictionary.registerOre(OREDICT_HOOKSHOT_MEDIUM, new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.MEDIUM, false)));
		OreDictionary.registerOre(OREDICT_HOOKSHOT_STRONG, new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.STRONG, false)));
		OreDictionary.registerOre(OREDICT_HOOKSHOT_CLAW, new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.CLAW, false)));
		
		OreDictionary.registerOre(OREDICT_REAGENTSEED_MANDRAKE, new ItemStack(ReagentSeed.mandrake, 1));
		OreDictionary.registerOre(OREDICT_REAGENTSEED_GINSENG, new ItemStack(ReagentSeed.ginseng, 1));
		OreDictionary.registerOre(OREDICT_REAGENTSEED_ESSENCE, new ItemStack(ReagentSeed.essence, 1));
	}
	
}
