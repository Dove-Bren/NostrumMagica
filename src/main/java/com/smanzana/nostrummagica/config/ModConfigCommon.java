package com.smanzana.nostrummagica.config;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigCommon {

	public static enum Key {
		SPELL_DEBUG(ModConfig.Category.SPELL, "spell_debug", false, "Print targetting debug information for spells? (Requires config on server set to true as well)"),
	
		OBELISK_REQ_MAGIC(ModConfig.Category.SERVER, "obelisk_req_magic", true, "Magic must be unlocked before obelisks can be used or teleported to."),
		//NOSTRUM_WORLDS(ModConfig.Category.SERVER, "nostrum_worlds", new int[]{0}, "Which worlds to generate Nostrum dungeons in"),
		NOSTRUM_SORCERY_DIM(ModConfig.Category.SERVER, "sorcery_dimension", "sorcery_dim", "Name of dimension to treat as Sorcery dimension, including changing how players enter and what's allowed there."),
		NOSTRUM_OVERRIDE_ELYTRA(ModConfig.Category.SERVER, "nostrum_elytra_override", true, "If true, Nostrum will override (via ASM transformation) elytra flying."),
		
		HIGHER_BALANCED(ModConfig.Category.SERVER, "balance_armor_higher", false, "If true, set armor values to higher. Useful when other armor mods auto balance down armor values. I'm writing this while looking at First Aid and RLCraft."),
		EASIER_THANO(ModConfig.Category.SERVER, "balance_easier_thano", false, "If true, make Thano pendants/staves/etc. easier to get. Good when using Aetheria and automation doesn't need to be late game. Changing requires a restart."),
		BAG_VACUUM_ON_SNEAK(ModConfig.Category.SERVER, "bag_sneak_vacuum", false, "If true, sneaking does NOT bypass the vacuum feature of rune and reagent bags. Useful when used with mods that change what sneaking means for EntityItems."),
		;
		
		//private ModConfig.Category Category;
		
		private String key;
		
		private String desc;
		
		//private Object def;
		
		
		private Key(ModConfig.Category Category, String key, Object def, String desc) {
			//this.Category = Category;
			this.key = key;
			this.desc = desc;
			
			if (!(def instanceof Float || def instanceof Integer || def instanceof Boolean
					|| def instanceof String)) {
				NostrumMagica.logger.warn("Config property defaults to a value type that's not supported: " + def.getClass());
			}
		}
	}
	
	ForgeConfigSpec.BooleanValue configSpellDebug;
	ForgeConfigSpec.BooleanValue configObeliskMagic;
//	ForgeConfigSpec.IntValue configNostrumDungeonDim;
//	ForgeConfigSpec.IntValue configNostrumSorceryDimID;
	ForgeConfigSpec.BooleanValue configOverrideElytra;
	ForgeConfigSpec.BooleanValue configHigherBalancedArmor;
	ForgeConfigSpec.BooleanValue configEasierThano;
	ForgeConfigSpec.BooleanValue configBagSneakVacuum;
	ForgeConfigSpec.ConfigValue<String> configSorceryDimensionKey;
	
	
	public ModConfigCommon(ForgeConfigSpec.Builder builder) {
		/*
		 * optionField = builder
		 * 				.comment("stuff and things")
		 * 				.define("path", default)
		 */
		
		// Not sure generics are strong enough for this...
//		for (ModConfig.Category category : ModConfig.Category.values()) {
//			category.start(builder);
//			for (Key key : Key.values()) {
//				if (key.Category != category) {
//					continue;
//				}
//				
//				key.configField = builder
//					.comment(key.desc)
//					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
//					.worldRestart()
//					.define(key.key, key.def instanceof String ? (String) key.def 
//							: key.def instanceof Float ? (Float) key.def
//							: key.def instanceof Boolean ? (Boolean) key.def
//							: key.def instanceof Integer ? (Integer) key.def
//							: key.def.getClass().isArray() ? (int[]) key.def
//							: key.def);
//			}
//			builder.pop();
//		}
		
		ModConfig.Category.SPELL.start(builder);
		{
			configSpellDebug = builder
					.comment(Key.SPELL_DEBUG.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.worldRestart()
					.define(Key.SPELL_DEBUG.key, false); // Default pulled out
		}
		builder.pop();
		
		ModConfig.Category.SERVER.start(builder);
		{
			configObeliskMagic = builder
					.comment(Key.OBELISK_REQ_MAGIC.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.worldRestart()
					.define(Key.OBELISK_REQ_MAGIC.key, true); // Default pulled out
//			configNostrumDungeonDim = builder
//					.comment(Key.NOSTRUM_WORLDS.desc)
//					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
//					.worldRestart()
//					.defineInRange(Key.NOSTRUM_WORLDS.key, 0, 0, Integer.MAX_VALUE); // Default pulled out
//			configNostrumSorceryDimID = builder
//					.comment(Key.NOSTRUM_DIMENSION_ID.desc)
//					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
//					.worldRestart()
//					.defineInRange(Key.NOSTRUM_DIMENSION_ID.key, 244, 0, Integer.MAX_VALUE); // Default pulled out
			configOverrideElytra = builder
					.comment(Key.NOSTRUM_OVERRIDE_ELYTRA.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.worldRestart()
					.define(Key.NOSTRUM_OVERRIDE_ELYTRA.key, true); // Default pulled out
			configHigherBalancedArmor = builder
					.comment(Key.HIGHER_BALANCED.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.worldRestart()
					.define(Key.HIGHER_BALANCED.key, false); // Default pulled out
			configEasierThano = builder
					.comment(Key.EASIER_THANO.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.worldRestart()
					.define(Key.EASIER_THANO.key, false); // Default pulled out
			configBagSneakVacuum = builder
					.comment(Key.BAG_VACUUM_ON_SNEAK.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.worldRestart()
					.define(Key.BAG_VACUUM_ON_SNEAK.key, false); // Default pulled out
			configSorceryDimensionKey = builder
					.comment(Key.NOSTRUM_SORCERY_DIM.desc)
					//.translation
					.worldRestart()
					.define(Key.NOSTRUM_SORCERY_DIM.desc, "sorcery_dim");
		}
		builder.pop();
	}
}
