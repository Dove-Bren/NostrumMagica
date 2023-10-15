package com.smanzana.nostrummagica.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;

public class ModConfig {
	
	public static enum Category {
		SERVER("server", "Core properties that MUST be syncronized bytween the server and client. Client values ignored"),
		DISPLAY("display", "Item tag information and gui display options"),
		CONTROL("controls", "Options to change various controls"),
		SPELL("spell", "Options that change client-side handling of spells");
		
		private String categoryName;
		
		private String comment;
		
		private Category(String name, String tooltip) {
			categoryName = name;
			comment = tooltip;
		}
		
		public String getName() {
			return categoryName;
		}
		
		@Override
		public String toString() {
			return getName(); 
		}
		
//		protected static void deployCategories(ForgeConfigSpec.Builder builder) {
//			for (Category cat : values()) {
//				builder.comment(cat.comment)
//					//.worldRestart()
//					.push(cat.categoryName);
//				
//				config.setCategoryComment(cat.categoryName, cat.comment);
//				config.setCategoryRequiresWorldRestart(cat.categoryName, cat == SERVER);
//				config.setCategoryLanguageKey(cat.categoryName,
//						"config.nostrummagica." + cat.categoryName);
//			}
//		}
		
		public ForgeConfigSpec.Builder start(ForgeConfigSpec.Builder builder) {
			builder.comment(this.comment)
				.push(this.categoryName);
			return builder;
		}
		
	}

//	public static enum Key {
//		SPELL_DEBUG(Category.SPELL, "spell_debug", false, false, "Print targetting debug information for spells? (Requires config on server set to true as well)"),
//
//		MP_DISPLAY_SPHERES(Category.DISPLAY, "display_spheres", true, false, "Display available mana as mana orbs on the HUD"),
//		MP_DISPLAY_BAR(Category.DISPLAY, "display_bar", false, false, "Display mana as a mana bar on the HUD"),
//		MP_DISPLAY_TEXT(Category.DISPLAY, "display_mp_text", false, false, "Display available mana as text over existing mana displays"),
//		XP_DISPLAY_TEXT(Category.DISPLAY, "display_xp_text", false, false, "Display current and max XP"),
//		XP_DISPLAY_BAR(Category.DISPLAY, "display_xp_bar", true, false, "Display current progress towards next level"),
//		OBELISK_LIST(Category.DISPLAY, "obelisk_list", false, false, "Display known teleportation points in the obelisk as a list"),
//		EFFECT_DISPLAY(Category.DISPLAY, "display_effects", true, false, "Allow cool client effects to be rendered. Turn off to enhance performance"),
//		MIRROR_QUEST_NODE_SPOILERS(Category.DISPLAY, "display_mirror_quest_spoilers", false, false, "If true, show ALL magic mirror character upgrades regardless of proximity"),
//		ARMOR_DISPLAY(Category.DISPLAY, "overarmor_display", true, false, "Enable displaying armor overlay when armor is > 20 units"),
//		DISPLAY_SHIELDS(Category.DISPLAY, "shield_display", true, false, "Show magical shield overlay on hearts"),
//		DISPLAY_HOOKSHOT_CROSSHAIR(Category.DISPLAY, "hookshot_crosshair", true, false, "Show special crosshair with the hookshot"),
//		DISPLAY_DRAGON_HEALTHBARS(Category.DISPLAY, "dragon_healthbar", true, false, "Display special healthbars for dragons"),
//		DISPLAY_PET_HEALTHBARS(Category.DISPLAY, "pet_healthbar", true, false, "Display healthbars for tamed pets"),
//		DISPLAY_MANA_HEIGHT(Category.DISPLAY, "mana_spheres_height", 0, false, "Extra space (in full rows) to move mana display up. Useful when other mods add info above the armor bar."),
//		
//		LOGIN_TEXT(Category.DISPLAY, "display_login_text", true, false, "On login, show Nostrum Magica welcome text"),
//		
//		CONTROL_DASH_DOUBLEPRESS(Category.CONTROL, "ender_dash_double_press", true, false, "Activate Ender Dash on movement key double-press"),
//		
//		OBELISK_REQ_MAGIC(Category.SERVER, "obelisk_req_magic", true, true, "Magic must be unlocked before obelisks can be used or teleported to."),
//		NOSTRUM_WORLDS(Category.SERVER, "nostrum_worlds", new int[]{0}, true, "Which worlds to generate Nostrum dungeons in"),
//		NOSTRUM_DIMENSION_ID(Category.SERVER, "nostrum_dimension_id", 244, true, "Dimension ID for Nostrum's Sorcery dimension. Change if other mods want to use the same id."),
//		NOSTRUM_OVERRIDE_ELYTRA(Category.SERVER, "nostrum_elytra_override", true, true, "If true, Nostrum will override (via ASM transformation) elytra flying."),
//		
//		HIGHER_BALANCED(Category.SERVER, "balance_armor_higher", false, true, "If true, set armor values to higher. Useful when other armor mods auto balance down armor values. I'm writing this while looking at First Aid and RLCraft."),
//		EASIER_THANO(Category.SERVER, "balance_easier_thano", false, true, "If true, make Thano pendants/staves/etc. easier to get. Good when using Aetheria and automation doesn't need to be late game. Changing requires a restart."),
//		BAG_VACUUM_ON_SNEAK(Category.SERVER, "bag_sneak_vacuum", false, true, "If true, sneaking does NOT bypass the vacuum feature of rune and reagent bags. Useful when used with mods that change what sneaking means for EntityItems.")
//		;
//		
//		
//		
//		
//		private Category category;
//		
//		private String key;
//		
//		private String desc;
//		
//		private Object def;
//		
//		private boolean serverBound;
//		
//		private Key(Category category, String key, Object def, String desc) {
//			this(category, key, def, false, desc);
//		}
//		
//		private Key(Category category, String key, Object def, boolean serverBound, String desc) {
//			this.category = category;
//			this.key = key;
//			this.desc = desc;
//			this.def = def;
//			this.serverBound = serverBound;
//			
//			if (!(def instanceof Float || def instanceof Integer || def instanceof Boolean
//					|| def instanceof String)) {
//				NostrumMagica.logger.warn("Config property defaults to a value type that's not supported: " + def.getClass());
//			}
//		}
//		
//		protected String getKey() {
//			return key;
//		}
//		
//		protected String getDescription() {
//			return desc;
//		}
//		
//		protected String getCategory() {
//			return category.getName();
//		}
//		
//		protected Object getDefault() {
//			return def;
//		}
//		
//		/**
//		 * Returns whether this config value should be replaced by
//		 * the server's values instead of the clients
//		 * @return
//		 */
//		public boolean isServerBound() {
//			return serverBound;
//		}
//		
//		/**
//		 * Returns whether this config option can be changed at runtime
//		 * @return
//		 */
//		public boolean isRuntime() {
//			if (category == Category.SERVER)
//				return false;
//			
//			//add other cases as they come
//			
//			return true;
//		}
//		
//		public void saveToNBT(ModConfig config, CompoundNBT tag) {
//			if (tag == null)
//				tag = new CompoundNBT();
//			
//			if (def instanceof Float)
//				tag.putFloat(key, config.getFloatValue(this, false)); 
//			else if (def instanceof Boolean)
//				tag.putBoolean(key, config.getBooleanValue(this, false));
//			else if (def instanceof Integer)
//				tag.putInt(key, config.getIntValue(this, false));
//			else if (def.getClass().isArray())
//				tag.putIntArray(key,  config.getIntArrayValue(this, false));
//			else
//				tag.putString(key, config.getStringValue(this, false));
//		}
//
//		public Object valueFromNBT(CompoundNBT tag) {
//			if (tag == null)
//				return null;
//			
//			if (def instanceof Float)
//				return tag.getFloat(key); 
//			else if (def instanceof Boolean)
//				return tag.getBoolean(key);
//			else if (def instanceof Integer)
//				return tag.getInt(key);
//			else
//				return tag.getString(key);
//		}
//		
//		public Object getFromString(String val) {
//			if (val == null)
//				return null;
//			
//			Object out = null;
//			if (def instanceof Float) {
//				try {
//					out = Float.parseFloat(val);
//				} catch (NumberFormatException e) {
//					return null;
//				}
//			} else if (def instanceof Boolean) {
//				out = Boolean.parseBoolean(val);
//			}
//			else if (def instanceof Integer) {
//				try {
//					out = Integer.parseInt(val);
//				} catch (NumberFormatException e) {
//					return null;
//				}
//			}
//			else {
//				out = val;
//			}
//			
//			return out;
//		}
//		
//		public static Collection<Key> getCategoryKeys(Category category) {
//			Set<Key> set = new HashSet<Key>();
//			
//			for (Key key : values()) {
//				if (key.category == category)
//					set.add(key);
//			}
//			
//			return set;
//		}
//	}
	
	public static ModConfig config;
	
	//public static SimpleChannel channel;
	
	//private static int discriminator = 0;
	
	//private static final String CHANNEL_NAME = "armconfig_channel";
	
	private ForgeConfigSpec clientSpec;
	private ModConfigClient client;
	
	private ForgeConfigSpec commonSpec;
	private ModConfigCommon common;
	
	//private Map<Key, Object> localValues;
	
	private Set<IConfigWatcher> watchers;
	
	public ModConfig() {
		this.watchers = new HashSet<IConfigWatcher>();
		//localValues = new HashMap<Key, Object>();
		
		final Pair<ModConfigCommon, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(ModConfigCommon::new);
		commonSpec = commonPair.getRight();
		common = commonPair.getLeft();
		
		final Pair<ModConfigClient, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ModConfigClient::new);
		clientSpec = clientPair.getRight();
		client = clientPair.getLeft();
		
		ModConfig.config = this;
		
		//channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
		
		//channel.registerMessage(RequestServerConfigMessage.Handler.class, RequestServerConfigMessage.class, discriminator++, Side.SERVER);
		//channel.registerMessage(ResponseServerConfigMessage.Handler.class, ResponseServerConfigMessage.class, discriminator++, Side.CLIENT);
		//channel.registerMessage(ServerConfigMessage.Handler.class, ServerConfigMessage.class, discriminator++, Side.CLIENT);
		
		//Key.Category.deployCategories(base);
		initConfig();
		loadLocals();
		
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	public void register() {
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, clientSpec);
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, commonSpec);
	}
	
	private void initConfig() {
//		for (Key key : Key.values())
//		//if (!base.contains(key.getCategory(), key.getKey())) {
//		{
//			if (key.getDefault() instanceof Float) {
//				base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
//						Float.MIN_VALUE, Float.MAX_VALUE, key.getDescription(),
//						"config.nostrummagica." + key.getCategory() + "." + key.getKey());
//			}
//			else if (key.getDefault() instanceof Boolean)
//				base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
//						key.getDescription(), "config.nostrummagica." + key.getCategory() + "." + key.getKey());
//			else if (key.getDefault() instanceof Integer)
//				base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
//						Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription(),
//						"config.nostrummagica." + key.getCategory() + "." + key.getKey());
//			else if (key.getDefault().getClass().isArray())
//				base.get(key.getCategory(), key.getKey(), (int[]) key.getDefault(),
//						key.getDescription());
//			else
//				base.getString(key.getKey(), key.getCategory(), key.getDefault().toString(),
//						key.getDescription(), "config.nostrummagica." + key.getCategory() + "." + key.getKey());
//		}
//		
//		if (base.hasChanged())
//			base.save();
	}
	
	/**
	 * Take a 'working copy' of values that will not be saved back to the config
	 * and instead will reside in a temporary local cache
	 */
	private void loadLocals() {
		// hopefully I don't have to do this anymore?
//		for (Key key : Key.values())
//		if (key.isServerBound() || !key.isRuntime()) {
//			localValues.put(key, getRawObject(key, true));
//		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(NostrumMagica.MODID)) {

			//tell each watcher the c onfig has been updated
			if (watchers != null)
			for (IConfigWatcher watcher : watchers) {
				watcher.onConfigUpdate(this);
			}
		}
		
		//base.save();
	}
	
//	@SubscribeEvent
//	public void onPlayerLogin(PlayerLoggedInEvent event) {
//		if (event.getPlayer() instanceof ServerPlayerEntity) {
//			NostrumMagica.logger.info("sending config overrides to client...");
//			NostrumMagica.instance.proxy.sendServerConfig((ServerPlayerEntity) event.getPlayer());
//		} else {
//			NostrumMagica.logger.info("Ignoring player join event, as no MP =========================================");
//		}
//	}
//	
//	@SubscribeEvent
//	public void onPlayerDisconnect(WorldEvent.Unload event) {
//		//reset config
//		NostrumMagica.logger.info("Resetting config local values");
//		loadLocals();
//	}
	
//	public boolean updateLocal(Key key, Object newValue) {
//		if (localValues.containsKey(key)) {
//			if (key.getDefault().getClass().isAssignableFrom(newValue.getClass())) {
//				localValues.put(key, newValue);
//				return true;
//			} else {
//				NostrumMagica.logger.warn("Bad attempted config assignment for " + key.key + ": "
//						+ newValue + "[" + newValue.getClass() + "] -> ["
//						+ key.getDefault().getClass() + "]");
//				return false;
//			}
//		}
//		
//		return false;
//	}
	
	public void registerWatcher(IConfigWatcher watcher) {
		this.watchers.add(watcher);
	}
	
//	///////////////////////////////////////ENUM FILLING/////////
//	// I wanted to make this dynamic, but there's no
//	// configuration.get() that returns a blank object
//	////////////////////////////////////////////////////////////
//	protected boolean getBooleanValue(Key key, boolean ignoreLocal) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		if (!ignoreLocal && localValues.containsKey(key))
//			return (Boolean) localValues.get(key);
//		
//		return base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
//				key.getDescription());
//	}
//
//	protected float getFloatValue(Key key, boolean ignoreLocal) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		if (!ignoreLocal && localValues.containsKey(key))
//			return (Float) localValues.get(key);
//		
//		return base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
//				Float.MIN_VALUE, Float.MAX_VALUE, key.getDescription());
//	}
//
//	protected int getIntValue(Key key, boolean ignoreLocal) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		if (!ignoreLocal && localValues.containsKey(key))
//			return (Integer) localValues.get(key);
//		
//		return base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
//				Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription());
//	}
//	
//	protected int[] getIntArrayValue(Key key, boolean ignoreLocal) {
//		if (!ignoreLocal && localValues.containsKey(key))
//			return (int[]) localValues.get(key);
//		
//		int[] def = (int[]) key.getDefault();
//		return base.get(key.getCategory(), key.getKey(), def).getIntList();
//	}
//
//	protected String getStringValue(Key key, boolean ignoreLocal) {
//		//DOESN'T cast check. Know what you're doing before you do it
//		if (!ignoreLocal && localValues.containsKey(key))
//			return (String) localValues.get(key);
//		
//		return base.getString(key.getKey(), key.getCategory(), (String) key.getDefault(),
//				key.getDescription());
//	}
//	
//	private Object getRawObject(Key key, boolean ignoreLocal) {
//		if (key.getDefault() instanceof Float)
//			return getFloatValue(key, ignoreLocal); 
//		else if (key.getDefault() instanceof Boolean)
//			return getBooleanValue(key, ignoreLocal);
//		else if (key.getDefault() instanceof Integer)
//			return getIntValue(key, ignoreLocal);
//		else if (key.getDefault().getClass().isArray())
//			return getIntArrayValue(key, ignoreLocal);
//		else
//			return getStringValue(key, ignoreLocal);
//	}	
	
	public boolean spellDebug() {
		return common.configSpellDebug.get();
	}
	
	public boolean getObeliskList() {
		return client.configObeliskList.get();
	}
	
	public boolean obeliskReqMagic() {
		return common.configObeliskMagic.get();
	}
	
	public boolean displayManaOrbs() {
		return client.configMPDisplaySpheres.get();
	}
	
	public boolean displayManaBar() {
		return client.configMPDisplayBar.get();
	}
	
	public boolean displayManaText() {
		return client.configMPDisplayText.get();
	}
	
	public boolean displayXPText() {
		return client.configXPDisplayText.get();
	}
	
	public boolean displayXPBar() {
		return client.configXPDisplayBar.get();
	}

	public boolean displayEffects() {
		return client.configEffectDisplay.get();
	}
	
	public boolean displayLoginText() {
		return client.configLoginText.get();
	}
	
//	public int[] getDimensionList() {
//		return new int[] {(int)(common.get(ModConfigCommon.Key.NOSTRUM_WORLDS))};
//	}
	
	public boolean displayAllMirrorQuestNodes() {
		return client.configMirrorNodeSpoilers.get();
	}
	
	public boolean displayArmorOverlay() {
		return client.configArmorDisplay.get();
	}
	
//	public int sorceryDimensionIndex() {
//		return (Integer) common.get(ModConfigCommon.Key.NOSTRUM_DIMENSION_ID);
//	}

	public boolean displayShieldHearts() {
		return client.configDisplayShields.get();
	}
	
	public boolean displayHookshotCrosshair() {
		return client.configDisplayHookshotCrosshair.get();
	}
	
	public boolean displayDragonHealthbars() {
		return client.configDisplayDragonHealthbars.get();
	}
	
	public boolean displayPetHealthbars() {
		return client.configDisplayPetHealthbars.get();
	}
	
	public boolean overrideElytraCode() {
		return common.configOverrideElytra.get();
	}
	
	public boolean doubleEnderDash() {
		return client.configDashDoublePress.get();
	}
	
	public boolean usingAdvancedArmors() {
		return common.configHigherBalancedArmor.get();
	}
	
	public int getManaSphereOffset() {
		return client.configManaHeight.get();
	}
	
	public boolean vacuumWhileSneaking() {
		return common.configBagSneakVacuum.get();
	}
	
	public boolean usingEasierThano() {
		return common.configEasierThano.get();
	}
}
