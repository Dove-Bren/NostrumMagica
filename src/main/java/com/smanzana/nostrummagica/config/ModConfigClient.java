package com.smanzana.nostrummagica.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.overlay.ReagentHUDMode;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigClient {

	public static enum Key {
		MP_DISPLAY_SPHERES(ModConfig.Category.DISPLAY, "display_spheres", true, "Display available mana as mana orbs on the HUD"),
		MP_DISPLAY_BAR(ModConfig.Category.DISPLAY, "display_bar", false, "Display mana as a mana bar on the HUD"),
		MP_DISPLAY_TEXT(ModConfig.Category.DISPLAY, "display_mp_text", false, "Display available mana as text over existing mana displays"),
		XP_DISPLAY_TEXT(ModConfig.Category.DISPLAY, "display_xp_text", false, "Display current and max XP"),
		XP_DISPLAY_BAR(ModConfig.Category.DISPLAY, "display_xp_bar", true, "Display current progress towards next level"),
		OBELISK_LIST(ModConfig.Category.DISPLAY, "obelisk_list", false, "Display known teleportation points in the obelisk as a list"),
		EFFECT_DISPLAY(ModConfig.Category.DISPLAY, "display_effects", true, "Allow cool client effects to be rendered. Turn off to enhance performance"),
		MIRROR_QUEST_NODE_SPOILERS(ModConfig.Category.DISPLAY, "display_mirror_quest_spoilers", false, "If true, show ALL magic mirror character upgrades regardless of proximity"),
		ARMOR_DISPLAY(ModConfig.Category.DISPLAY, "overarmor_display", true, "Enable displaying armor overlay when armor is > 20 units"),
		DISPLAY_SHIELDS(ModConfig.Category.DISPLAY, "shield_display", true, "Show magical shield overlay on hearts"),
		DISPLAY_HOOKSHOT_CROSSHAIR(ModConfig.Category.DISPLAY, "hookshot_crosshair", true, "Show special crosshair with the hookshot"),
		DISPLAY_DRAGON_HEALTHBARS(ModConfig.Category.DISPLAY, "dragon_healthbar", true, "Display special healthbars for dragons"),
		DISPLAY_PET_HEALTHBARS(ModConfig.Category.DISPLAY, "pet_healthbar", true, "Display healthbars for tamed pets"),
		DISPLAY_MANA_HEIGHT(ModConfig.Category.DISPLAY, "mana_spheres_height", 0, "Extra space (in full rows) to move mana display up. Useful when other mods add info above the armor bar."),
		DISPLAY_REAGENTS_MODE(ModConfig.Category.DISPLAY, "reagenthud_mode", ReagentHUDMode.ONCHANGE, "When to display the reagent count overlay. ALWAYS displays it all the time, TOGGLE turns it on and off when you press the Nostrum HUD button, and ONCHANGE shows it automatically when reagent counts change"),
		DISPLAY_REAGENTS_XPOS(ModConfig.Category.DISPLAY, "reagenthud_xpos", 0, "X position to dipslay the reagent HUD element"),
		DISPLAY_REAGENTS_YPOS(ModConfig.Category.DISPLAY, "reagenthud_ypos", 0, "Y position to dipslay the reagent HUD element"),
		DISPLAY_REAGENTS_TALL(ModConfig.Category.DISPLAY, "reagenthud_tallmode", false, "Whether to show the reagent HUD element in tall mode"),
		
		LOGIN_TEXT(ModConfig.Category.DISPLAY, "display_login_text", true, "On login, show Nostrum Magica welcome text"),
		
		CONTROL_DASH_DOUBLEPRESS(ModConfig.Category.CONTROL, "ender_dash_double_press", true, "Activate Ender Dash on movement key double-press"),
		;
		
		//private ModConfig.Category Category;
		
		private String key;
		
		private String desc;
		
		//private Object def;
		
		private ForgeConfigSpec.ConfigValue<?> configField;
		
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
	
	ForgeConfigSpec.BooleanValue configMPDisplaySpheres;
	ForgeConfigSpec.BooleanValue configMPDisplayBar;
	ForgeConfigSpec.BooleanValue configMPDisplayText;
	ForgeConfigSpec.BooleanValue configXPDisplayText;
	ForgeConfigSpec.BooleanValue configXPDisplayBar;	
	ForgeConfigSpec.BooleanValue configObeliskList;
	ForgeConfigSpec.BooleanValue configEffectDisplay;
	ForgeConfigSpec.BooleanValue configMirrorNodeSpoilers;
	ForgeConfigSpec.BooleanValue configArmorDisplay;
	ForgeConfigSpec.BooleanValue configDisplayShields;
	ForgeConfigSpec.BooleanValue configDisplayHookshotCrosshair;
	ForgeConfigSpec.EnumValue<ReagentHUDMode> configDisplayReagentMode;
	ForgeConfigSpec.IntValue configDisplayReagentX;
	ForgeConfigSpec.IntValue configDisplayReagentY;
	ForgeConfigSpec.BooleanValue configDisplayReagentTall;
	ForgeConfigSpec.IntValue configManaHeight;
	ForgeConfigSpec.BooleanValue configLoginText;
	ForgeConfigSpec.BooleanValue configDashDoublePress;
	
	public ModConfigClient(ForgeConfigSpec.Builder builder) {
		/*
		 * optionField = builder
		 * 				.comment("stuff and things")
		 * 				.define("path", default)
		 */
		
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
//							: key.def);
//			}
//			builder.pop();
//		}
		
		ModConfig.Category.DISPLAY.start(builder);
		{
			configMPDisplaySpheres = builder
					.comment(Key.MP_DISPLAY_SPHERES.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.MP_DISPLAY_SPHERES.key, true); // Default pulled out
			configMPDisplayBar = builder
					.comment(Key.MP_DISPLAY_BAR.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.MP_DISPLAY_BAR.key, false); // Default pulled out
			configMPDisplayText = builder
					.comment(Key.MP_DISPLAY_TEXT.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.MP_DISPLAY_TEXT.key, false); // Default pulled out
			configXPDisplayText = builder
					.comment(Key.XP_DISPLAY_TEXT.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.XP_DISPLAY_TEXT.key, false); // Default pulled out
			configXPDisplayBar = builder
					.comment(Key.XP_DISPLAY_BAR.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.XP_DISPLAY_BAR.key, true); // Default pulled out
			configObeliskList = builder
					.comment(Key.OBELISK_LIST.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.OBELISK_LIST.key, false); // Default pulled out
			configEffectDisplay = builder
					.comment(Key.EFFECT_DISPLAY.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.EFFECT_DISPLAY.key, true); // Default pulled out
			configMirrorNodeSpoilers = builder
					.comment(Key.MIRROR_QUEST_NODE_SPOILERS.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.MIRROR_QUEST_NODE_SPOILERS.key, false); // Default pulled out
			configArmorDisplay = builder
					.comment(Key.ARMOR_DISPLAY.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.ARMOR_DISPLAY.key, true); // Default pulled out
			configDisplayShields = builder
					.comment(Key.DISPLAY_SHIELDS.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.DISPLAY_SHIELDS.key, true); // Default pulled out
			configDisplayHookshotCrosshair = builder
					.comment(Key.DISPLAY_HOOKSHOT_CROSSHAIR.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.DISPLAY_HOOKSHOT_CROSSHAIR.key, true); // Default pulled out
			configManaHeight = builder
					.comment(Key.DISPLAY_MANA_HEIGHT.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.defineInRange(Key.DISPLAY_MANA_HEIGHT.key, 0, 0, Integer.MAX_VALUE); // Default pulled out
			

			configDisplayReagentMode = builder
					.comment(Key.DISPLAY_REAGENTS_MODE.desc)
					.defineEnum(Key.DISPLAY_REAGENTS_MODE.key, ReagentHUDMode.ONCHANGE, EnumGetMethod.NAME_IGNORECASE);
			configDisplayReagentX = builder
					.comment(Key.DISPLAY_REAGENTS_XPOS.desc)
					.defineInRange(Key.DISPLAY_REAGENTS_XPOS.key, -1, Integer.MIN_VALUE, Integer.MAX_VALUE); // Default pulled out
			configDisplayReagentY = builder
					.comment(Key.DISPLAY_REAGENTS_YPOS.desc)
					.defineInRange(Key.DISPLAY_REAGENTS_YPOS.key, -1, Integer.MIN_VALUE, Integer.MAX_VALUE); // Default pulled out
			configDisplayReagentTall = builder
					.comment(Key.DISPLAY_REAGENTS_TALL.desc)
					.define(Key.DISPLAY_REAGENTS_TALL.key, false);
			
			configLoginText = builder
					.comment(Key.LOGIN_TEXT.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.LOGIN_TEXT.key, true); // Default pulled out
		}
		builder.pop();
		
		ModConfig.Category.CONTROL.start(builder);
		{
			configDashDoublePress = builder
					.comment(Key.CONTROL_DASH_DOUBLEPRESS.desc)
					//.translation("") ? config.nostrummagica.[CATEGORY].[NAME] ?
					.define(Key.CONTROL_DASH_DOUBLEPRESS.key, true); // Default pulled out
		}
		builder.pop();
	}
}
