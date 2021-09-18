package com.smanzana.nostrummagica.items;

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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public class NostrumItems {
	
	private static final String OREDICT_RUNE_ANY = "runeAny";
//	private static final String OREDICT_RUNE_SHAPE_SINGLE = "runeShapeSingle";
//	private static final String OREDICT_RUNE_SHAPE_CHAIN = "runeShapeChain";
//	private static final String OREDICT_RUNE_SHAPE_AOE = "runeShapeAOE";
	
	private static final String OREDICT_REAGENT_ANY = "reagentAny";
	private static final String OREDICT_REAGENT_MANDRAKEROOT = "reagentMandrakeRoot";
	private static final String OREDICT_REAGENT_GINSENG = "reagentGinseng";
	private static final String OREDICT_REAGENT_SPIDERSILK = "reagentSpiderSilk";
	private static final String OREDICT_REAGENT_BLACKPEARL = "reagentBlackPearl";
	private static final String OREDICT_REAGENT_SKYASH = "reagentSkyAsh";
	private static final String OREDICT_REAGENT_GRAVEDUST = "reagentGraveDust";
	private static final String OREDICT_REAGENT_MANIDUST = "reagentManiDust";
	private static final String OREDICT_REAGENT_CRYSTABLOOM = "reagentCrystabloom";
	
	private static final String OREDICT_INFGEM_ANY = "infgemAny";
	private static final String OREDICT_INFGEM_VOID = "infgemVoid";
	private static final String OREDICT_INFGEM_FIRE = "infgemFire";
	private static final String OREDICT_INFGEM_ICE = "infgemIce";
	private static final String OREDICT_INFGEM_WIND = "infgemWind";
	private static final String OREDICT_INFGEM_EARTH = "infgemEarth";
	private static final String OREDICT_INFGEM_LIGHTNING = "infgemLightning";
	private static final String OREDICT_INFGEM_ENDER = "infgemEnder";
	
	private static final String OREDICT_TOKEN = "nrToken";
	private static final String OREDICT_CRYSTALSMALL = "nrCrystalSmall";
	private static final String OREDICT_CRYSTALMEDIUM = "nrCrystalMedium";
	private static final String OREDICT_CRYSTALLARGE = "nrCrystalLarge";
	private static final String OREDICT_PENDANTLEFT = "nrPendantLeft";
	private static final String OREDICT_PENDANTRIGHT = "nrPendantRight";
	private static final String OREDICT_SLABFIERCE = "nrSlabFierce";
	private static final String OREDICT_SLABKIND = "nrSlabKind";
	private static final String OREDICT_SLABBALANCED = "nrSlabBalanced";
	private static final String OREDICT_SPRITECORE = "nrSpriteCore";
	private static final String OREDICT_ENDERBRISTLE = "nrEnderBristle";
	private static final String OREDICT_WISPPEBBLE = "nrWispPebble";
	
	private static final String OREDICT_SPELLPAGE_ANY = "spellpageAny";
	
	private static final String OREDICT_ESSENCE_ANY = "nessenceAny";
	private static final String OREDICT_ESSENCE_VOID = "nessenceVoid";
	private static final String OREDICT_ESSENCE_FIRE = "nessenceFire";
	private static final String OREDICT_ESSENCE_ICE = "nessenceIce";
	private static final String OREDICT_ESSENCE_WIND = "nessenceWind";
	private static final String OREDICT_ESSENCE_EARTH = "nessenceEarth";
	private static final String OREDICT_ESSENCE_LIGHTNING = "nessenceLightning";
	private static final String OREDICT_ESSENCE_ENDER = "nessenceEnder";
	
	private static final String OREDICT_SKILLITEM_ANY = "nsiAny";
	private static final String OREDICT_SKILLITEM_MIRROR = "nsiMirror";
	private static final String OREDICT_SKILLITEM_OOZE = "nsiOoze";
	private static final String OREDICT_SKILLITEM_PENDANT = "nsiPendant";
	private static final String OREDICT_SKILLITEM_FLUTE = "nsiFlute";
	private static final String OREDICT_SKILLITEM_WING = "nsiWing";
	private static final String OREDICT_SKILLITEM_ENDERPIN = "nsiEnderPin";
	private static final String OREDICT_SKILLITEM_RESEARCHSMALL = "nsiResearchSmall";
	private static final String OREDICT_SKILLITEM_RESEARCHLARGE = "nsiResearchLarge";
	
	private static final String OREDICT_ROSE_ANY = "roseAny";
	private static final String OREDICT_ROSE_BLOOD = "roseBlood";
	private static final String OREDICT_ROSE_ELDRICH = "roseEldrich";
	private static final String OREDICT_ROSE_PALE = "rosePale";
	
	private static final String OREDICT_HOOKSHOT_ANY = "nhookshotAny";
	private static final String OREDICT_HOOKSHOT_WEAK = "nhookshotWeak";
	private static final String OREDICT_HOOKSHOT_MEDIUM = "nhookshotMedium";
	private static final String OREDICT_HOOKSHOT_STRONG = "nhookshotStrong";
	private static final String OREDICT_HOOKSHOT_CLAW = "nhookshotClaw";
	
	private static final String OREDICT_REAGENTSEED_MANDRAKE = "nrseedMandrake";
	private static final String OREDICT_REAGENTSEED_GINSENG = "nrseedGinseng";
	private static final String OREDICT_REAGENTSEED_ESSENCE = "nrseedEssence";
	
	public NostrumItems() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
    private void registerItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	registry.register(NostrumGuide.instance());
//    	GameRegistry.addShapelessRecipe(new ItemStack(NostrumGuide.instance()), Items.LEATHER,
//				Items.LEATHER, Items.LEATHER, BlankScroll.instance());
    	
    	registry.register(SpellcraftGuide.instance());
//    	GameRegistry.addShapelessRecipe(new ItemStack(SpellcraftGuide.instance()), Items.LEATHER,
//				Items.LEATHER, Items.LEATHER, new ItemStack(SpellRune.instance(), 1, OreDictionary.WILDCARD_VALUE));
    	
    	registry.register(SpellTome.instance());
    	registry.register(SpellPlate.instance());
    	registry.register(BlankScroll.instance());
//		GameRegistry.addShapelessRecipe(new ItemStack(BlankScroll.instance()),
//				ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1),
//				Items.PAPER,
//				Items.PAPER,
//				ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1));
    	
    	registry.register(SpellScroll.instance());
    	registry.register(SpellTableItem.instance());
//    	GameRegistry.addRecipe(new ItemStack(SpellTableItem.instance()), "CBD", "PPP", "L L",
//				'P', Blocks.PLANKS,
//				'L', Blocks.LOG,
//				'C', ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1),
//				'B', ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
//				'D', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
    	
    	registry.register(MirrorItem.instance());
//		GameRegistry.addRecipe(new ItemStack(MirrorItem.instance()), "RQR", "QGQ", "SSS",
//				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE),
//				'Q', Items.QUARTZ,
//				'G', Blocks.GLASS,
//				'S', Blocks.STONE);
    	
    	registry.register(MagicSwordBase.instance());
//		GameRegistry.addRecipe(new ItemStack(MagicSwordBase.instance()), " C ", " C ", " S ",
//				'S', Items.IRON_SWORD, 
//				'C', InfusedGemItem.instance().getGem(null, 1));
		
    	registry.register(MagicArmorBase.helm());
    	registry.register(MagicArmorBase.chest());
    	registry.register(MagicArmorBase.legs());
		registry.register(MagicArmorBase.feet());
		
//		GameRegistry.addShapedRecipe(new ItemStack(MagicArmorBase.helm()), "CCC", "C C", " D ",
//				'C', InfusedGemItem.instance().getGem(null, 1),
//				'D', Items.IRON_HELMET);
//		GameRegistry.addShapedRecipe(new ItemStack(MagicArmorBase.chest()), "CDC", "CCC", "CCC",
//				'C', InfusedGemItem.instance().getGem(null, 1),
//				'D', Items.IRON_CHESTPLATE);
//		GameRegistry.addShapedRecipe(new ItemStack(MagicArmorBase.legs()), "CCC", "CDC", "C C",
//				'C', InfusedGemItem.instance().getGem(null, 1),
//				'D', Items.IRON_LEGGINGS);
//		GameRegistry.addShapedRecipe(new ItemStack(MagicArmorBase.feet()), " D ", "C C", "C C",
//				'C', InfusedGemItem.instance().getGem(null, 1),
//				'D', Items.IRON_BOOTS);
		
    	EnchantedWeapon.registerWeapons(registry);
    	EnchantedArmor.registerArmors(registry);
    	DragonArmor.registerArmors(registry);
    	
    	registry.register(MirrorShield.instance());
    	registry.register(MirrorShieldImproved.instance());
    	
    	registry.register(ReagentItem.instance());
//    	GameRegistry.addShapelessRecipe(ReagentItem.getReagent(ReagentType.SPIDER_SILK, 1),
//				Items.STRING, Items.STRING, Items.SUGAR);
		
		MinecraftForge.addGrassSeed(ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1), 6);
		MinecraftForge.addGrassSeed(ReagentItem.instance().getReagent(ReagentType.GINSENG, 1), 5);
    	
    	registry.register(InfusedGemItem.instance());
//    	GameRegistry.addRecipe(new ItemStack(InfusedGemItem.instance()), " G ", "DED", "BGB",
//				'D', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
//				'G', ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1),
//				'E', Items.ENDER_PEARL,
//				'B', ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1));
    	
    	
    	registry.register(SpellRune.instance());
//		GameRegistry.addRecipe(new SpellRune.RuneRecipe());
		
    	
    	registry.register(NostrumResourceItem.instance());
//		GameRegistry.addRecipe(NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), " MR", "MDM", "RM ",
//				'D', Items.DIAMOND,
//				'M', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
//				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
    	
    	registry.register(ReagentBag.instance());
    	
    	registry.register(SeekerIdol.instance());
    	
    	registry.register(ShrineSeekingGem.instance());
    	
    	registry.register(ChalkItem.instance());
//		GameRegistry.addShapelessRecipe(new ItemStack(ChalkItem.instance()),
//				new ItemStack(Items.DYE, 1, 15),
//				new ItemStack(Items.DYE, 1, 15),
//				new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
    	
    	registry.register(AltarItem.instance());
//		GameRegistry.addShapedRecipe(new ItemStack(AltarItem.instance()),
//				"SSS", " T ", "TRT",
//				'S', Blocks.STONE_SLAB,
//				'T', Blocks.STONE,
//				'R', NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)
//				);
    	
    	registry.register(PositionCrystal.instance());
    	
    	registry.register(PositionToken.instance());
    	
    	registry.register(SpellTomePage.instance());

//		GameRegistry.addRecipe(new SpellTomePageCombineRecipe());
    	
    	registry.register(EssenceItem.instance());
//    	GameRegistry.addShapelessRecipe(InfusedGemItem.instance().getGem(null, 1),
//    			EssenceItem.getEssence(EMagicElement.EARTH, 1),
//    			EssenceItem.getEssence(EMagicElement.ENDER, 1),
//    			EssenceItem.getEssence(EMagicElement.FIRE, 1),
//    			EssenceItem.getEssence(EMagicElement.ICE, 1),
//    			EssenceItem.getEssence(EMagicElement.LIGHTNING, 1),
//    			EssenceItem.getEssence(EMagicElement.PHYSICAL, 1),
//    			EssenceItem.getEssence(EMagicElement.WIND, 1));
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
		
//		EMagicElement[] all = EMagicElement.values();
//		EMagicElement last = all[all.length - 1];
//		for (EMagicElement element : all) {
//			GameRegistry.addShapelessRecipe(EssenceItem.getEssence(element, 1),
//					EssenceItem.getEssence(last, 1),
//					new ItemStack(EssenceItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
//			last = element;
//		}
    	
    	registry.register(MageStaff.instance());
    	registry.register(ThanoPendant.instance());
    	registry.register(ThanosStaff.instance());
    	registry.register(MagicCharm.instance());
    	
    	registry.register(RuneBag.instance());
    	
    	registry.register(DragonEggFragment.instance());
    	
    	registry.register(DragonEgg.instance());
//    	{
//
//    		ItemStack gold = new ItemStack(Item.getItemFromBlock(Blocks.GOLD_BLOCK), 1, OreDictionary.WILDCARD_VALUE);
//    		ItemStack crystal = NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1);
//    		ItemStack star = new ItemStack(Items.NETHER_STAR, 1, OreDictionary.WILDCARD_VALUE);
//    		ItemStack shell = new ItemStack(DragonEggFragment.instance());
//    		ItemStack egg = new ItemStack(Item.getItemFromBlock(Blocks.DRAGON_EGG));
//    		GameRegistry.addRecipe(new ShapedRecipes(3, 3, new ItemStack[] {
//    				crystal, shell, star, shell, egg, shell, gold, gold, gold
//    		}, new ItemStack(DragonEgg.instance())) {
//    			@Override
//    			public ItemStack[] getRemainingItems(InventoryCrafting inv) {
//    				ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];
//    				
//    				int j = 4;
//    				int len = inv.getSizeInventory();
//    				if (len < 4) {
//    					len = 0;
//    				}
//    				
//    				for (int i = 0; i < len; i++) {
//    					if (i == j) {
//    						aitemstack[i] = egg;
//    					} else {
//    						aitemstack[i] = ItemStack.EMPTY;
//    					}
//    				}
//    				
//    				return aitemstack;
//    			}
//    		});
//    	}
    	
    	registry.register(NostrumSkillItem.instance());
    	
    	registry.register(NostrumRoseItem.instance());
    	
    	registry.register(WarlockSword.instance());
    	
    	registry.register(HookshotItem.instance());

//		GameRegistry.addShapedRecipe(new ItemStack(HookshotItem.instance(), 1, HookshotItem.MakeMeta(HookshotType.WEAK, false)),
//				" RF", "DIR", "WD ",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'R', new ItemStack(Items.REDSTONE),
//				'D', new ItemStack(Items.DIAMOND),
//				'I', new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE),
//				'F', new ItemStack(Items.FLINT));
    	
    	registry.register(ReagentSeed.mandrake);
    	registry.register(ReagentSeed.ginseng);
    	registry.register(ReagentSeed.essence);
    	
    	registry.register(MasteryOrb.instance());
    	
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
