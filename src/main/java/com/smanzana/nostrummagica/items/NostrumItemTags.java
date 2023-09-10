package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class NostrumItemTags {

	public static final class Items {
		private static final String TAG_RUNE_ANY = "rune_any";
//		private static final String TAG_RUNE_SHAPE_SINGLE = "runeShapeSingle";
//		private static final String TAG_RUNE_SHAPE_CHAIN = "runeShapeChain";
//		private static final String TAG_RUNE_SHAPE_AOE = "runeShapeAOE";
		
		private static final String TAG_REAGENT_ANY = "reagent/any";
		private static final String TAG_REAGENT_MANDRAKEROOT = "reagent/mandrake_root";
		private static final String TAG_REAGENT_GINSENG = "reagent/ginseng";
		private static final String TAG_REAGENT_SPIDERSILK = "reagent/spider_silk";
		private static final String TAG_REAGENT_BLACKPEARL = "reagent/black_pearl";
		private static final String TAG_REAGENT_SKYASH = "reagent/sky_ash";
		private static final String TAG_REAGENT_GRAVEDUST = "reagent/grave_dust";
		private static final String TAG_REAGENT_MANIDUST = "reagent/mani_dust";
		private static final String TAG_REAGENT_CRYSTABLOOM = "reagent/crystabloom";
		
		private static final String TAG_INFGEM_ANY = "infusedgem/any";
		private static final String TAG_INFGEM_VOID = "infusedgem/void";
		private static final String TAG_INFGEM_FIRE = "infusedgem/fire";
		private static final String TAG_INFGEM_ICE = "infusedgem/ice";
		private static final String TAG_INFGEM_WIND = "infusedgem/wind";
		private static final String TAG_INFGEM_EARTH = "infusedgem/earth";
		private static final String TAG_INFGEM_LIGHTNING = "infusedgem/lightning";
		private static final String TAG_INFGEM_ENDER = "infusedgem/ender";
		
		private static final String TAG_TOKEN = "magic_token";
		private static final String TAG_CRYSTALSMALL = "crystal_small";
		private static final String TAG_CRYSTALMEDIUM = "crystal_medium";
		private static final String TAG_CRYSTALLARGE = "crystal_large";
		private static final String TAG_PENDANTLEFT = "pendant_left";
		private static final String TAG_PENDANTRIGHT = "pendant_right";
		private static final String TAG_SLABFIERCE = "slab_fierce";
		private static final String TAG_SLABKIND = "slab_kind";
		private static final String TAG_SLABBALANCED = "slab_balanced";
		private static final String TAG_SPRITECORE = "sprite_core";
		private static final String TAG_ENDERBRISTLE = "ender_bristle";
		private static final String TAG_WISPPEBBLE = "wisp_pebble";
		private static final String TAG_DRAGON_WING = "dragon_wing";
		
		private static final String TAG_SPELLPAGE_ANY = "spellpage_any";
		
		private static final String TAG_ESSENCE_ANY = "essence/any";
		private static final String TAG_ESSENCE_VOID = "essence/void";
		private static final String TAG_ESSENCE_FIRE = "essence/fire";
		private static final String TAG_ESSENCE_ICE = "essence/ice";
		private static final String TAG_ESSENCE_WIND = "essence/wind";
		private static final String TAG_ESSENCE_EARTH = "essence/earth";
		private static final String TAG_ESSENCE_LIGHTNING = "essence/lightning";
		private static final String TAG_ESSENCE_ENDER = "essence/ender";
		
		private static final String TAG_SKILLITEM_ANY = "skill_item/any";
		private static final String TAG_SKILLITEM_MIRROR = "skill_item/mirror";
		private static final String TAG_SKILLITEM_OOZE = "skill_item/ooze";
		private static final String TAG_SKILLITEM_PENDANT = "skill_item/pendant";
		private static final String TAG_SKILLITEM_FLUTE = "skill_item/flute";
		private static final String TAG_SKILLITEM_ENDERPIN = "skill_item/ender_pin";
		private static final String TAG_SKILLITEM_RESEARCHSMALL = "skill_item/small_research";
		private static final String TAG_SKILLITEM_RESEARCHLARGE = "skill_item/large_research";
		
		private static final String TAG_ROSE_ANY = "rose/any";
		private static final String TAG_ROSE_BLOOD = "rose/blood";
		private static final String TAG_ROSE_ELDRICH = "rose/eldrich";
		private static final String TAG_ROSE_PALE = "rose/pale";
		
		private static final String TAG_HOOKSHOT_ANY = "hookshot/any";
		private static final String TAG_HOOKSHOT_WEAK = "hookshot/weak";
		private static final String TAG_HOOKSHOT_MEDIUM = "hookshot/medium";
		private static final String TAG_HOOKSHOT_STRONG = "hookshot/strong";
		private static final String TAG_HOOKSHOT_CLAW = "hookshot/claw";
		
		private static final String TAG_REAGENTSEED_MANDRAKE = "seed/mandrake";
		private static final String TAG_REAGENTSEED_GINSENG = "seed/ginseng";
		private static final String TAG_REAGENTSEED_ESSENCE = "seed/essence";
		
		public static final Tag<Item> RuneAny = tag(TAG_RUNE_ANY);
		
		public static final Tag<Item> ReagentAny = tag(TAG_REAGENT_ANY);
		public static final Tag<Item> ReagentMandrakeRoot = tag(TAG_REAGENT_MANDRAKEROOT);
		public static final Tag<Item> ReagentGinseng = tag(TAG_REAGENT_GINSENG);
		public static final Tag<Item> ReagentSpiderSilk = tag(TAG_REAGENT_SPIDERSILK);
		public static final Tag<Item> ReagentBlackPearl = tag(TAG_REAGENT_BLACKPEARL);
		public static final Tag<Item> ReagentSkyAsh = tag(TAG_REAGENT_SKYASH);
		public static final Tag<Item> ReagentGraveDust = tag(TAG_REAGENT_GRAVEDUST);
		public static final Tag<Item> ReagentManiDust = tag(TAG_REAGENT_MANIDUST);
		public static final Tag<Item> ReagentCrystabloom = tag(TAG_REAGENT_CRYSTABLOOM);
		
		public static final Tag<Item> InfusedGemAny = tag(TAG_INFGEM_ANY);
		public static final Tag<Item> InfusedGemVoid = tag(TAG_INFGEM_VOID);
		public static final Tag<Item> InfusedGemFire = tag(TAG_INFGEM_FIRE);
		public static final Tag<Item> InfusedGemIce = tag(TAG_INFGEM_ICE);
		public static final Tag<Item> InfusedGemWind = tag(TAG_INFGEM_WIND);
		public static final Tag<Item> InfusedGemEarth = tag(TAG_INFGEM_EARTH);
		public static final Tag<Item> InfusedGemLightning = tag(TAG_INFGEM_LIGHTNING);
		public static final Tag<Item> InfusedGemEnder = tag(TAG_INFGEM_ENDER);

		public static final Tag<Item> CrystalSmall = tag(TAG_CRYSTALSMALL);
		public static final Tag<Item> CrystalMedium = tag(TAG_CRYSTALMEDIUM);
		public static final Tag<Item> CrystalLarge = tag(TAG_CRYSTALLARGE);
		
		public static final Tag<Item> MagicToken = tag(TAG_TOKEN);
		public static final Tag<Item> PendantLeft = tag(TAG_PENDANTLEFT);
		public static final Tag<Item> PendantRight = tag(TAG_PENDANTRIGHT);
		public static final Tag<Item> SlabFierce = tag(TAG_SLABFIERCE);
		public static final Tag<Item> SlabKind = tag(TAG_SLABKIND);
		public static final Tag<Item> SlabBalanced = tag(TAG_SLABBALANCED);
		public static final Tag<Item> SpriteCore = tag(TAG_SPRITECORE);
		public static final Tag<Item> EnderBristle = tag(TAG_ENDERBRISTLE);
		public static final Tag<Item> WispPebble = tag(TAG_WISPPEBBLE);
		public static final Tag<Item> DragonWing = tag(TAG_DRAGON_WING);
		
		public static final Tag<Item> SpellpageAny = tag(TAG_SPELLPAGE_ANY);
		
		public static final Tag<Item> EssenceAny = tag(TAG_ESSENCE_ANY);
		public static final Tag<Item> EssenceVoid = tag(TAG_ESSENCE_VOID);
		public static final Tag<Item> EssenceFire = tag(TAG_ESSENCE_FIRE);
		public static final Tag<Item> EssenceIce = tag(TAG_ESSENCE_ICE);
		public static final Tag<Item> EssenceWind = tag(TAG_ESSENCE_WIND);
		public static final Tag<Item> EssenceEarth = tag(TAG_ESSENCE_EARTH);
		public static final Tag<Item> EssenceLightning = tag(TAG_ESSENCE_LIGHTNING);
		public static final Tag<Item> EssenceEnder = tag(TAG_ESSENCE_ENDER);
		
		public static final Tag<Item> SkillItemAny = tag(TAG_SKILLITEM_ANY);
		public static final Tag<Item> SkillItemMirror = tag(TAG_SKILLITEM_MIRROR);
		public static final Tag<Item> SkillItemOoze = tag(TAG_SKILLITEM_OOZE);
		public static final Tag<Item> SkillItemPendant = tag(TAG_SKILLITEM_PENDANT);
		public static final Tag<Item> SkillItemFlute = tag(TAG_SKILLITEM_FLUTE);
		public static final Tag<Item> SkillItemEnderPin = tag(TAG_SKILLITEM_ENDERPIN);
		public static final Tag<Item> SkillItemSmallResearch = tag(TAG_SKILLITEM_RESEARCHSMALL);
		public static final Tag<Item> SkillItemLargeResearch = tag(TAG_SKILLITEM_RESEARCHLARGE);
		
		public static final Tag<Item> RoseAny = tag(TAG_ROSE_ANY);
		public static final Tag<Item> RoseBlood = tag(TAG_ROSE_BLOOD);
		public static final Tag<Item> RoseEldrich = tag(TAG_ROSE_ELDRICH);
		public static final Tag<Item> RosePale = tag(TAG_ROSE_PALE);
		
		public static final Tag<Item> HookshotAny = tag(TAG_HOOKSHOT_ANY);
		public static final Tag<Item> HookshotWeak = tag(TAG_HOOKSHOT_WEAK);
		public static final Tag<Item> HookshotMedium = tag(TAG_HOOKSHOT_MEDIUM);
		public static final Tag<Item> HookshotStrong = tag(TAG_HOOKSHOT_STRONG);
		public static final Tag<Item> HookshotClaw = tag(TAG_HOOKSHOT_CLAW);
		
		public static final Tag<Item> SeedMandrake = tag(TAG_REAGENTSEED_MANDRAKE);
		public static final Tag<Item> SeedGinseng = tag(TAG_REAGENTSEED_GINSENG);
		public static final Tag<Item> SeedEssence = tag(TAG_REAGENTSEED_ESSENCE);
		
		private static Tag<Item> tag(String path) {
			return new ItemTags.Wrapper(new ResourceLocation(NostrumMagica.MODID, path));
		}
	}
}
