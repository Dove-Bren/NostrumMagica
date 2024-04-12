package com.smanzana.nostrummagica.crafting;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class NostrumTags {

	public static final class Items {
		private static final String TAG_RUNE = "rune";
//		private static final String TAG_RUNE_SHAPE_SINGLE = "runeShapeSingle";
//		private static final String TAG_RUNE_SHAPE_CHAIN = "runeShapeChain";
//		private static final String TAG_RUNE_SHAPE_AOE = "runeShapeAOE";
		
		private static final String TAG_REAGENT = "reagent";
		private static final String TAG_REAGENT_MANDRAKEROOT = "reagent/mandrake_root";
		private static final String TAG_REAGENT_GINSENG = "reagent/ginseng";
		private static final String TAG_REAGENT_SPIDERSILK = "reagent/spider_silk";
		private static final String TAG_REAGENT_BLACKPEARL = "reagent/black_pearl";
		private static final String TAG_REAGENT_SKYASH = "reagent/sky_ash";
		private static final String TAG_REAGENT_GRAVEDUST = "reagent/grave_dust";
		private static final String TAG_REAGENT_MANIDUST = "reagent/mani_dust";
		private static final String TAG_REAGENT_CRYSTABLOOM = "reagent/crystabloom";
		
		private static final String TAG_INFGEM = "infusedgem";
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
		
		private static final String TAG_SPELLPAGE = "spellpage";
		
		private static final String TAG_ESSENCE = "essence";
		private static final String TAG_ESSENCE_VOID = "essence/void";
		private static final String TAG_ESSENCE_FIRE = "essence/fire";
		private static final String TAG_ESSENCE_ICE = "essence/ice";
		private static final String TAG_ESSENCE_WIND = "essence/wind";
		private static final String TAG_ESSENCE_EARTH = "essence/earth";
		private static final String TAG_ESSENCE_LIGHTNING = "essence/lightning";
		private static final String TAG_ESSENCE_ENDER = "essence/ender";
		
		private static final String TAG_SKILLITEM = "skill_item";
		private static final String TAG_SKILLITEM_MIRROR = "skill_item/mirror";
		private static final String TAG_SKILLITEM_OOZE = "skill_item/ooze";
		private static final String TAG_SKILLITEM_PENDANT = "skill_item/pendant";
		private static final String TAG_SKILLITEM_FLUTE = "skill_item/flute";
		private static final String TAG_SKILLITEM_ENDERPIN = "skill_item/ender_pin";
		private static final String TAG_SKILLITEM_RESEARCHSMALL = "skill_item/small_research";
		private static final String TAG_SKILLITEM_RESEARCHLARGE = "skill_item/large_research";
		
		private static final String TAG_ROSE = "rose";
		private static final String TAG_ROSE_BLOOD = "rose/blood";
		private static final String TAG_ROSE_ELDRICH = "rose/eldrich";
		private static final String TAG_ROSE_PALE = "rose/pale";
		
		private static final String TAG_HOOKSHOT = "hookshot";
		private static final String TAG_HOOKSHOT_WEAK = "hookshot/weak";
		private static final String TAG_HOOKSHOT_MEDIUM = "hookshot/medium";
		private static final String TAG_HOOKSHOT_STRONG = "hookshot/strong";
		private static final String TAG_HOOKSHOT_CLAW = "hookshot/claw";
		
		private static final String TAG_REAGENTSEED = "seed";
		private static final String TAG_REAGENTSEED_MANDRAKE = "seed/mandrake";
		private static final String TAG_REAGENTSEED_GINSENG = "seed/ginseng";
		private static final String TAG_REAGENTSEED_ESSENCE = "seed/essence";
		
		private static final String TAG_TOMEPLATE = "tome_plate";
		private static final String TAG_TOMEPLATE_NOVICE = "tome_plate/novice";
		private static final String TAG_TOMEPLATE_ADVANCED = "tome_plate/advanced";
		private static final String TAG_TOMEPLATE_COMBAT = "tome_plate/combat";
		private static final String TAG_TOMEPLATE_DEATH = "tome_plate/death";
		private static final String TAG_TOMEPLATE_SPOOKY = "tome_plate/spooky";
		private static final String TAG_TOMEPLATE_MUTED = "tome_plate/muted";
		private static final String TAG_TOMEPLATE_LIVING = "tome_plate/living";
		
		private static final String TAG_SPELLTOME = "spell_tome";
		private static final String TAG_SPELLTOME_NOVICE = "spell_tome/novice";
		private static final String TAG_SPELLTOME_ADVANCED = "spell_tome/advanced";
		private static final String TAG_SPELLTOME_COMBAT = "spell_tome/combat";
		private static final String TAG_SPELLTOME_DEATH = "spell_tome/death";
		private static final String TAG_SPELLTOME_SPOOKY = "spell_tome/spooky";
		private static final String TAG_SPELLTOME_MUTED = "spell_tome/muted";
		private static final String TAG_SPELLTOME_LIVING = "spell_tome/living";
		
		private static final String TAG_SILVER_INGOT = "ingots/silver";
		
		private static final String TAG_TRANSMUTABLE_ITEM = "transmutable/item";
		
		public static final ITag.INamedTag<Item> RuneAny = tag(TAG_RUNE);
		
		public static final ITag.INamedTag<Item> Reagent = tag(TAG_REAGENT);
		public static final ITag.INamedTag<Item> ReagentMandrakeRoot = tag(TAG_REAGENT_MANDRAKEROOT);
		public static final ITag.INamedTag<Item> ReagentGinseng = tag(TAG_REAGENT_GINSENG);
		public static final ITag.INamedTag<Item> ReagentSpiderSilk = tag(TAG_REAGENT_SPIDERSILK);
		public static final ITag.INamedTag<Item> ReagentBlackPearl = tag(TAG_REAGENT_BLACKPEARL);
		public static final ITag.INamedTag<Item> ReagentSkyAsh = tag(TAG_REAGENT_SKYASH);
		public static final ITag.INamedTag<Item> ReagentGraveDust = tag(TAG_REAGENT_GRAVEDUST);
		public static final ITag.INamedTag<Item> ReagentManiDust = tag(TAG_REAGENT_MANIDUST);
		public static final ITag.INamedTag<Item> ReagentCrystabloom = tag(TAG_REAGENT_CRYSTABLOOM);
		
		public static final ITag.INamedTag<Item> InfusedGem = tag(TAG_INFGEM);
		public static final ITag.INamedTag<Item> InfusedGemVoid = tag(TAG_INFGEM_VOID);
		public static final ITag.INamedTag<Item> InfusedGemFire = tag(TAG_INFGEM_FIRE);
		public static final ITag.INamedTag<Item> InfusedGemIce = tag(TAG_INFGEM_ICE);
		public static final ITag.INamedTag<Item> InfusedGemWind = tag(TAG_INFGEM_WIND);
		public static final ITag.INamedTag<Item> InfusedGemEarth = tag(TAG_INFGEM_EARTH);
		public static final ITag.INamedTag<Item> InfusedGemLightning = tag(TAG_INFGEM_LIGHTNING);
		public static final ITag.INamedTag<Item> InfusedGemEnder = tag(TAG_INFGEM_ENDER);

		public static final ITag.INamedTag<Item> CrystalSmall = tag(TAG_CRYSTALSMALL);
		public static final ITag.INamedTag<Item> CrystalMedium = tag(TAG_CRYSTALMEDIUM);
		public static final ITag.INamedTag<Item> CrystalLarge = tag(TAG_CRYSTALLARGE);
		
		public static final ITag.INamedTag<Item> MagicToken = tag(TAG_TOKEN);
		public static final ITag.INamedTag<Item> PendantLeft = tag(TAG_PENDANTLEFT);
		public static final ITag.INamedTag<Item> PendantRight = tag(TAG_PENDANTRIGHT);
		public static final ITag.INamedTag<Item> SlabFierce = tag(TAG_SLABFIERCE);
		public static final ITag.INamedTag<Item> SlabKind = tag(TAG_SLABKIND);
		public static final ITag.INamedTag<Item> SlabBalanced = tag(TAG_SLABBALANCED);
		public static final ITag.INamedTag<Item> SpriteCore = tag(TAG_SPRITECORE);
		public static final ITag.INamedTag<Item> EnderBristle = tag(TAG_ENDERBRISTLE);
		public static final ITag.INamedTag<Item> WispPebble = tag(TAG_WISPPEBBLE);
		public static final ITag.INamedTag<Item> DragonWing = tag(TAG_DRAGON_WING);
		
		public static final ITag.INamedTag<Item> Spellpage = tag(TAG_SPELLPAGE);
		
		public static final ITag.INamedTag<Item> Essence = tag(TAG_ESSENCE);
		public static final ITag.INamedTag<Item> EssenceVoid = tag(TAG_ESSENCE_VOID);
		public static final ITag.INamedTag<Item> EssenceFire = tag(TAG_ESSENCE_FIRE);
		public static final ITag.INamedTag<Item> EssenceIce = tag(TAG_ESSENCE_ICE);
		public static final ITag.INamedTag<Item> EssenceWind = tag(TAG_ESSENCE_WIND);
		public static final ITag.INamedTag<Item> EssenceEarth = tag(TAG_ESSENCE_EARTH);
		public static final ITag.INamedTag<Item> EssenceLightning = tag(TAG_ESSENCE_LIGHTNING);
		public static final ITag.INamedTag<Item> EssenceEnder = tag(TAG_ESSENCE_ENDER);
		
		public static final ITag.INamedTag<Item> SkillItem = tag(TAG_SKILLITEM);
		public static final ITag.INamedTag<Item> SkillItemMirror = tag(TAG_SKILLITEM_MIRROR);
		public static final ITag.INamedTag<Item> SkillItemOoze = tag(TAG_SKILLITEM_OOZE);
		public static final ITag.INamedTag<Item> SkillItemPendant = tag(TAG_SKILLITEM_PENDANT);
		public static final ITag.INamedTag<Item> SkillItemFlute = tag(TAG_SKILLITEM_FLUTE);
		public static final ITag.INamedTag<Item> SkillItemEnderPin = tag(TAG_SKILLITEM_ENDERPIN);
		public static final ITag.INamedTag<Item> SkillItemSmallResearch = tag(TAG_SKILLITEM_RESEARCHSMALL);
		public static final ITag.INamedTag<Item> SkillItemLargeResearch = tag(TAG_SKILLITEM_RESEARCHLARGE);
		
		public static final ITag.INamedTag<Item> Rose = tag(TAG_ROSE);
		public static final ITag.INamedTag<Item> RoseBlood = tag(TAG_ROSE_BLOOD);
		public static final ITag.INamedTag<Item> RoseEldrich = tag(TAG_ROSE_ELDRICH);
		public static final ITag.INamedTag<Item> RosePale = tag(TAG_ROSE_PALE);
		
		public static final ITag.INamedTag<Item> Hookshot = tag(TAG_HOOKSHOT);
		public static final ITag.INamedTag<Item> HookshotWeak = tag(TAG_HOOKSHOT_WEAK);
		public static final ITag.INamedTag<Item> HookshotMedium = tag(TAG_HOOKSHOT_MEDIUM);
		public static final ITag.INamedTag<Item> HookshotStrong = tag(TAG_HOOKSHOT_STRONG);
		public static final ITag.INamedTag<Item> HookshotClaw = tag(TAG_HOOKSHOT_CLAW);
		
		public static final ITag.INamedTag<Item> Seed = tag(TAG_REAGENTSEED);
		public static final ITag.INamedTag<Item> SeedMandrake = tag(TAG_REAGENTSEED_MANDRAKE);
		public static final ITag.INamedTag<Item> SeedGinseng = tag(TAG_REAGENTSEED_GINSENG);
		public static final ITag.INamedTag<Item> SeedEssence = tag(TAG_REAGENTSEED_ESSENCE);
		
		public static final ITag.INamedTag<Item> TomePlate = tag(TAG_TOMEPLATE);
		public static final ITag.INamedTag<Item> TomePlateNovice = tag(TAG_TOMEPLATE_NOVICE);
		public static final ITag.INamedTag<Item> TomePlateAdvanced = tag(TAG_TOMEPLATE_ADVANCED);
		public static final ITag.INamedTag<Item> TomePlateCombat = tag(TAG_TOMEPLATE_COMBAT);
		public static final ITag.INamedTag<Item> TomePlateDeath = tag(TAG_TOMEPLATE_DEATH);
		public static final ITag.INamedTag<Item> TomePlateSpooky = tag(TAG_TOMEPLATE_SPOOKY);
		public static final ITag.INamedTag<Item> TomePlateMuted = tag(TAG_TOMEPLATE_MUTED);
		public static final ITag.INamedTag<Item> TomePlateLiving = tag(TAG_TOMEPLATE_LIVING);
		
		public static final ITag.INamedTag<Item> SpellTome = tag(TAG_SPELLTOME);
		public static final ITag.INamedTag<Item> SpellTomeNovice = tag(TAG_SPELLTOME_NOVICE);
		public static final ITag.INamedTag<Item> SpellTomeAdvanced = tag(TAG_SPELLTOME_ADVANCED);
		public static final ITag.INamedTag<Item> SpellTomeCombat = tag(TAG_SPELLTOME_COMBAT);
		public static final ITag.INamedTag<Item> SpellTomeDeath = tag(TAG_SPELLTOME_DEATH);
		public static final ITag.INamedTag<Item> SpellTomeSpooky = tag(TAG_SPELLTOME_SPOOKY);
		public static final ITag.INamedTag<Item> SpellTomeMuted = tag(TAG_SPELLTOME_MUTED);
		public static final ITag.INamedTag<Item> SpellTomeLiving = tag(TAG_SPELLTOME_LIVING);
		
		public static final ITag.INamedTag<Item> SilverIngot = forgeTag(TAG_SILVER_INGOT);
		
		public static final ITag.INamedTag<Item> TransmutableItem = tag(TAG_TRANSMUTABLE_ITEM);
		
		private static ITag.INamedTag<Item> tag(String path) {
			return ItemTags.makeWrapperTag(new ResourceLocation(NostrumMagica.MODID, path).toString());
		}
		
		private static ITag.INamedTag<Item> forgeTag(String name) {
			final ResourceLocation loc = new ResourceLocation("forge", name);
			ITag<Item> found = ItemTags.getCollection().get(loc);
			if (found != null && found instanceof ITag.INamedTag) {
				return (ITag.INamedTag<Item>) found;
			}
			
			return ItemTags.makeWrapperTag(loc.toString());
		}
	}
	
	public static final class Blocks {
		
		private static final String TAG_TRANSMUTABLE_BLOCK = "transmutable/block";
		
		public static final ITag.INamedTag<Block> TransmutableBlock = tag(TAG_TRANSMUTABLE_BLOCK);
		
		private static ITag.INamedTag<Block> tag(String path) {
			return BlockTags.makeWrapperTag(new ResourceLocation(NostrumMagica.MODID, path).toString());
		}
	}
}
