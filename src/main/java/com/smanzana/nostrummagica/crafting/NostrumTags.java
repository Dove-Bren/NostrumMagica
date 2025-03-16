package com.smanzana.nostrummagica.crafting;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

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
		
		public static final Tag.Named<Item> RuneAny = tag(TAG_RUNE);
		
		public static final Tag.Named<Item> Reagent = tag(TAG_REAGENT);
		public static final Tag.Named<Item> ReagentMandrakeRoot = tag(TAG_REAGENT_MANDRAKEROOT);
		public static final Tag.Named<Item> ReagentGinseng = tag(TAG_REAGENT_GINSENG);
		public static final Tag.Named<Item> ReagentSpiderSilk = tag(TAG_REAGENT_SPIDERSILK);
		public static final Tag.Named<Item> ReagentBlackPearl = tag(TAG_REAGENT_BLACKPEARL);
		public static final Tag.Named<Item> ReagentSkyAsh = tag(TAG_REAGENT_SKYASH);
		public static final Tag.Named<Item> ReagentGraveDust = tag(TAG_REAGENT_GRAVEDUST);
		public static final Tag.Named<Item> ReagentManiDust = tag(TAG_REAGENT_MANIDUST);
		public static final Tag.Named<Item> ReagentCrystabloom = tag(TAG_REAGENT_CRYSTABLOOM);
		
		public static final Tag.Named<Item> InfusedGem = tag(TAG_INFGEM);
		public static final Tag.Named<Item> InfusedGemVoid = tag(TAG_INFGEM_VOID);
		public static final Tag.Named<Item> InfusedGemFire = tag(TAG_INFGEM_FIRE);
		public static final Tag.Named<Item> InfusedGemIce = tag(TAG_INFGEM_ICE);
		public static final Tag.Named<Item> InfusedGemWind = tag(TAG_INFGEM_WIND);
		public static final Tag.Named<Item> InfusedGemEarth = tag(TAG_INFGEM_EARTH);
		public static final Tag.Named<Item> InfusedGemLightning = tag(TAG_INFGEM_LIGHTNING);
		public static final Tag.Named<Item> InfusedGemEnder = tag(TAG_INFGEM_ENDER);

		public static final Tag.Named<Item> CrystalSmall = tag(TAG_CRYSTALSMALL);
		public static final Tag.Named<Item> CrystalMedium = tag(TAG_CRYSTALMEDIUM);
		public static final Tag.Named<Item> CrystalLarge = tag(TAG_CRYSTALLARGE);
		
		public static final Tag.Named<Item> MagicToken = tag(TAG_TOKEN);
		public static final Tag.Named<Item> PendantLeft = tag(TAG_PENDANTLEFT);
		public static final Tag.Named<Item> PendantRight = tag(TAG_PENDANTRIGHT);
		public static final Tag.Named<Item> SlabFierce = tag(TAG_SLABFIERCE);
		public static final Tag.Named<Item> SlabKind = tag(TAG_SLABKIND);
		public static final Tag.Named<Item> SlabBalanced = tag(TAG_SLABBALANCED);
		public static final Tag.Named<Item> SpriteCore = tag(TAG_SPRITECORE);
		public static final Tag.Named<Item> EnderBristle = tag(TAG_ENDERBRISTLE);
		public static final Tag.Named<Item> WispPebble = tag(TAG_WISPPEBBLE);
		public static final Tag.Named<Item> DragonWing = tag(TAG_DRAGON_WING);
		
		public static final Tag.Named<Item> Spellpage = tag(TAG_SPELLPAGE);
		
		public static final Tag.Named<Item> Essence = tag(TAG_ESSENCE);
		public static final Tag.Named<Item> EssenceVoid = tag(TAG_ESSENCE_VOID);
		public static final Tag.Named<Item> EssenceFire = tag(TAG_ESSENCE_FIRE);
		public static final Tag.Named<Item> EssenceIce = tag(TAG_ESSENCE_ICE);
		public static final Tag.Named<Item> EssenceWind = tag(TAG_ESSENCE_WIND);
		public static final Tag.Named<Item> EssenceEarth = tag(TAG_ESSENCE_EARTH);
		public static final Tag.Named<Item> EssenceLightning = tag(TAG_ESSENCE_LIGHTNING);
		public static final Tag.Named<Item> EssenceEnder = tag(TAG_ESSENCE_ENDER);
		
		public static final Tag.Named<Item> SkillItem = tag(TAG_SKILLITEM);
		public static final Tag.Named<Item> SkillItemMirror = tag(TAG_SKILLITEM_MIRROR);
		public static final Tag.Named<Item> SkillItemOoze = tag(TAG_SKILLITEM_OOZE);
		public static final Tag.Named<Item> SkillItemPendant = tag(TAG_SKILLITEM_PENDANT);
		public static final Tag.Named<Item> SkillItemFlute = tag(TAG_SKILLITEM_FLUTE);
		public static final Tag.Named<Item> SkillItemEnderPin = tag(TAG_SKILLITEM_ENDERPIN);
		public static final Tag.Named<Item> SkillItemSmallResearch = tag(TAG_SKILLITEM_RESEARCHSMALL);
		public static final Tag.Named<Item> SkillItemLargeResearch = tag(TAG_SKILLITEM_RESEARCHLARGE);
		
		public static final Tag.Named<Item> Rose = tag(TAG_ROSE);
		public static final Tag.Named<Item> RoseBlood = tag(TAG_ROSE_BLOOD);
		public static final Tag.Named<Item> RoseEldrich = tag(TAG_ROSE_ELDRICH);
		public static final Tag.Named<Item> RosePale = tag(TAG_ROSE_PALE);
		
		public static final Tag.Named<Item> Hookshot = tag(TAG_HOOKSHOT);
		public static final Tag.Named<Item> HookshotWeak = tag(TAG_HOOKSHOT_WEAK);
		public static final Tag.Named<Item> HookshotMedium = tag(TAG_HOOKSHOT_MEDIUM);
		public static final Tag.Named<Item> HookshotStrong = tag(TAG_HOOKSHOT_STRONG);
		public static final Tag.Named<Item> HookshotClaw = tag(TAG_HOOKSHOT_CLAW);
		
		public static final Tag.Named<Item> Seed = tag(TAG_REAGENTSEED);
		public static final Tag.Named<Item> SeedMandrake = tag(TAG_REAGENTSEED_MANDRAKE);
		public static final Tag.Named<Item> SeedGinseng = tag(TAG_REAGENTSEED_GINSENG);
		public static final Tag.Named<Item> SeedEssence = tag(TAG_REAGENTSEED_ESSENCE);
		
		public static final Tag.Named<Item> TomePlate = tag(TAG_TOMEPLATE);
		public static final Tag.Named<Item> TomePlateNovice = tag(TAG_TOMEPLATE_NOVICE);
		public static final Tag.Named<Item> TomePlateAdvanced = tag(TAG_TOMEPLATE_ADVANCED);
		public static final Tag.Named<Item> TomePlateCombat = tag(TAG_TOMEPLATE_COMBAT);
		public static final Tag.Named<Item> TomePlateDeath = tag(TAG_TOMEPLATE_DEATH);
		public static final Tag.Named<Item> TomePlateSpooky = tag(TAG_TOMEPLATE_SPOOKY);
		public static final Tag.Named<Item> TomePlateMuted = tag(TAG_TOMEPLATE_MUTED);
		public static final Tag.Named<Item> TomePlateLiving = tag(TAG_TOMEPLATE_LIVING);
		
		public static final Tag.Named<Item> SpellTome = tag(TAG_SPELLTOME);
		public static final Tag.Named<Item> SpellTomeNovice = tag(TAG_SPELLTOME_NOVICE);
		public static final Tag.Named<Item> SpellTomeAdvanced = tag(TAG_SPELLTOME_ADVANCED);
		public static final Tag.Named<Item> SpellTomeCombat = tag(TAG_SPELLTOME_COMBAT);
		public static final Tag.Named<Item> SpellTomeDeath = tag(TAG_SPELLTOME_DEATH);
		public static final Tag.Named<Item> SpellTomeSpooky = tag(TAG_SPELLTOME_SPOOKY);
		public static final Tag.Named<Item> SpellTomeMuted = tag(TAG_SPELLTOME_MUTED);
		public static final Tag.Named<Item> SpellTomeLiving = tag(TAG_SPELLTOME_LIVING);
		
		public static final Tag.Named<Item> SilverIngot = forgeTag(TAG_SILVER_INGOT);
		
		public static final Tag.Named<Item> TransmutableItem = tagOptional(TAG_TRANSMUTABLE_ITEM);
		
		private static Tag.Named<Item> tag(String path) {
			return ItemTags.bind(new ResourceLocation(NostrumMagica.MODID, path).toString());
		}
		
		private static Tag.Named<Item> tagOptional(String path) {
			return ItemTags.createOptional(new ResourceLocation(NostrumMagica.MODID, path));
		}
		
		private static Tag.Named<Item> forgeTag(String name) {
			final ResourceLocation loc = new ResourceLocation("forge", name);
			Tag<Item> found = ItemTags.getAllTags().getTag(loc);
			if (found != null && found instanceof Tag.Named) {
				return (Tag.Named<Item>) found;
			}
			
			return ItemTags.createOptional(loc);
		}
	}
	
	public static final class Blocks {
		
		private static final String TAG_TRANSMUTABLE_BLOCK = "transmutable/block";
		
		public static final Tag.Named<Block> TransmutableBlock = tag(TAG_TRANSMUTABLE_BLOCK);
		
		private static Tag.Named<Block> tag(String path) {
			return BlockTags.bind(new ResourceLocation(NostrumMagica.MODID, path).toString());
		}
	}
}
