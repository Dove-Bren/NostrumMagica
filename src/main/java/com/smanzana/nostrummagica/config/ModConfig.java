package com.smanzana.nostrummagica.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.network.ServerConfigMessage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModConfig {

	public static enum Key {
		SPELL_DEBUG(Category.SPELL, "spell_debug", false, false, "Print targetting debug information for spells? (Requires config on server set to true as well)"),

		MP_DISPLAY_SPHERES(Category.DISPLAY, "display_spheres", true, false, "Display available mana as mana orbs on the HUD"),
		MP_DISPLAY_BAR(Category.DISPLAY, "display_bar", false, false, "Display mana as a mana bar on the HUD"),
		MP_DISPLAY_TEXT(Category.DISPLAY, "display_mp_text", false, false, "Display available mana as text over existing mana displays"),
		XP_DISPLAY_TEXT(Category.DISPLAY, "display_xp_text", false, false, "Display current and max XP"),
		XP_DISPLAY_BAR(Category.DISPLAY, "display_xp_bar", true, false, "Display current progress towards next level"),
		OBELISK_LIST(Category.DISPLAY, "obelisk_list", false, false, "Display known teleportation points in the obelisk as a list"),
		EFFECT_DISPLAY(Category.DISPLAY, "display_effects", true, false, "Allow cool client effects to be rendered. Turn off to enhance performance"),
		MIRROR_NODE_SPOILERS(Category.DISPLAY, "display_mirror_spoilers", false, false, "If true, show ALL magic mirror upgrades regardless of proximity"),
		ARMOR_DISPLAY(Category.DISPLAY, "overarmor_display", true, false, "Enable displaying armor overlay when armor is > 20 units"),
		DISPLAY_SHIELDS(Category.DISPLAY, "shield_display", true, false, "Show magical shield overlay on hearts"),
		
		LOGIN_TEXT(Category.DISPLAY, "display_login_text", true, false, "On login, show Nostrum Magica welcome text"),
		
		OBELISK_REQ_MAGIC(Category.SERVER, "obelisk_req_magic", true, true, "Magic must be unlocked before obelisks can be used or teleported to."),
		NOSTRUM_WORLDS(Category.SERVER, "nostrum_worlds", new int[]{0}, true, "Which worlds to generate Nostrum dungeons in"),
		NOSTRUM_DIMENSION_ID(Category.SERVER, "nostrum_dimension_id", 244, true, "Dimension ID for Nostrum's Sorcery dimension. Change if other mods want to use the same id."),
		;
		
		
		public static enum Category {
			SERVER("server", "Core properties that MUST be syncronized bytween the server and client. Client values ignored"),
			DISPLAY("display", "Item tag information and gui display options"),
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
			
			protected static void deployCategories(Configuration config) {
				for (Category cat : values()) {
					config.setCategoryComment(cat.categoryName, cat.comment);
					config.setCategoryRequiresWorldRestart(cat.categoryName, cat == SERVER);
					config.setCategoryLanguageKey(cat.categoryName,
							"config.nostrummagica." + cat.categoryName);
				}
			}
			
		}
		
		private Category category;
		
		private String key;
		
		private String desc;
		
		private Object def;
		
		private boolean serverBound;
		
		private Key(Category category, String key, Object def, String desc) {
			this(category, key, def, false, desc);
		}
		
		private Key(Category category, String key, Object def, boolean serverBound, String desc) {
			this.category = category;
			this.key = key;
			this.desc = desc;
			this.def = def;
			this.serverBound = serverBound;
			
			if (!(def instanceof Float || def instanceof Integer || def instanceof Boolean
					|| def instanceof String)) {
				NostrumMagica.logger.warn("Config property defaults to a value type that's not supported: " + def.getClass());
			}
		}
		
		protected String getKey() {
			return key;
		}
		
		protected String getDescription() {
			return desc;
		}
		
		protected String getCategory() {
			return category.getName();
		}
		
		protected Object getDefault() {
			return def;
		}
		
		/**
		 * Returns whether this config value should be replaced by
		 * the server's values instead of the clients
		 * @return
		 */
		public boolean isServerBound() {
			return serverBound;
		}
		
		/**
		 * Returns whether this config option can be changed at runtime
		 * @return
		 */
		public boolean isRuntime() {
			if (category == Category.SERVER)
				return false;
			
			//add other cases as they come
			
			return true;
		}
		
		public void saveToNBT(ModConfig config, NBTTagCompound tag) {
			if (tag == null)
				tag = new NBTTagCompound();
			
			if (def instanceof Float)
				tag.setFloat(key, config.getFloatValue(this, false)); 
			else if (def instanceof Boolean)
				tag.setBoolean(key, config.getBooleanValue(this, false));
			else if (def instanceof Integer)
				tag.setInteger(key, config.getIntValue(this, false));
			else if (def.getClass().isArray())
				tag.setIntArray(key,  config.getIntArrayValue(this, false));
			else
				tag.setString(key, config.getStringValue(this, false));
		}

		public Object valueFromNBT(NBTTagCompound tag) {
			if (tag == null)
				return null;
			
			if (def instanceof Float)
				return tag.getFloat(key); 
			else if (def instanceof Boolean)
				return tag.getBoolean(key);
			else if (def instanceof Integer)
				return tag.getInteger(key);
			else
				return tag.getString(key);
		}
		
		public Object getFromString(String val) {
			if (val == null)
				return null;
			
			Object out = null;
			if (def instanceof Float) {
				try {
					out = Float.parseFloat(val);
				} catch (NumberFormatException e) {
					return null;
				}
			} else if (def instanceof Boolean) {
				out = Boolean.parseBoolean(val);
			}
			else if (def instanceof Integer) {
				try {
					out = Integer.parseInt(val);
				} catch (NumberFormatException e) {
					return null;
				}
			}
			else {
				out = val;
			}
			
			return out;
		}
		
		public static Collection<Key> getCategoryKeys(Category category) {
			Set<Key> set = new HashSet<Key>();
			
			for (Key key : values()) {
				if (key.category == category)
					set.add(key);
			}
			
			return set;
		}
	}
	
	public static ModConfig config;
	
	public static SimpleNetworkWrapper channel;
	
	private static int discriminator = 0;
	
	private static final String CHANNEL_NAME = "armconfig_channel";
	
	public Configuration base;
	
	private Map<Key, Object> localValues;
	
	private Set<IConfigWatcher> watchers;
	
	public ModConfig(Configuration config) {
		this.base = config;
		this.watchers = new HashSet<IConfigWatcher>();
		localValues = new HashMap<Key, Object>();
		ModConfig.config = this;
		
		channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
		
		//channel.registerMessage(RequestServerConfigMessage.Handler.class, RequestServerConfigMessage.class, discriminator++, Side.SERVER);
		//channel.registerMessage(ResponseServerConfigMessage.Handler.class, ResponseServerConfigMessage.class, discriminator++, Side.CLIENT);
		channel.registerMessage(ServerConfigMessage.Handler.class, ServerConfigMessage.class, discriminator++, Side.CLIENT);
		
		Key.Category.deployCategories(base);
		initConfig();
		loadLocals();
		
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	private void initConfig() {
		for (Key key : Key.values())
		if (!base.hasKey(key.getCategory(), key.getKey())) {
			if (key.getDefault() instanceof Float) {
				base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
						Float.MIN_VALUE, Float.MAX_VALUE, "This if float",//key.getDescription(),
						"config.nostrummagica." + key.getCategory() + "." + key.getKey());
			}
			else if (key.getDefault() instanceof Boolean)
				base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
						key.getDescription(), "config.nostrummagica." + key.getCategory() + "." + key.getKey());
			else if (key.getDefault() instanceof Integer)
				base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
						Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription(),
						"config.nostrummagica." + key.getCategory() + "." + key.getKey());
			else if (key.getDefault().getClass().isArray())
				base.get(key.getCategory(), key.getKey(), (int[]) key.getDefault(),
						key.getDescription());
			else
				base.getString(key.getKey(), key.getCategory(), key.getDefault().toString(),
						key.getDescription(), "config.nostrummagica." + key.getCategory() + "." + key.getKey());
		}
		
		if (base.hasChanged())
			base.save();
	}
	
	/**
	 * Take a 'working copy' of values that will not be saved back to the config
	 * and instead will reside in a temporary local cache
	 */
	private void loadLocals() {
		for (Key key : Key.values())
		if (key.isServerBound() || !key.isRuntime()) {
			localValues.put(key, getRawObject(key, true));
		}
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
		
		base.save();
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP) {
			NostrumMagica.logger.info("sending config overrides to client...");
			NostrumMagica.proxy.sendServerConfig((EntityPlayerMP) event.player);
		} else {
			NostrumMagica.logger.info("Ignoring player join event, as no MP =========================================");
		}
	}
	
	@SubscribeEvent
	public void onPlayerDisconnect(WorldEvent.Unload event) {
		//reset config
		NostrumMagica.logger.info("Resetting config local values");
		loadLocals();
	}
	
	public boolean updateLocal(Key key, Object newValue) {
		if (localValues.containsKey(key)) {
			if (key.getDefault().getClass().isAssignableFrom(newValue.getClass())) {
				localValues.put(key, newValue);
				return true;
			} else {
				NostrumMagica.logger.warn("Bad attempted config assignment: "
						+ newValue + "[" + newValue.getClass() + "] -> ["
						+ key.getDefault().getClass() + "]");
				return false;
			}
		}
		
		return false;
	}
	
	public void registerWatcher(IConfigWatcher watcher) {
		this.watchers.add(watcher);
	}
	
	///////////////////////////////////////ENUM FILLING/////////
	// I wanted to make this dynamic, but there's no
	// configuration.get() that returns a blank object
	////////////////////////////////////////////////////////////
	protected boolean getBooleanValue(Key key, boolean ignoreLocal) {
		//DOESN'T cast check. Know what you're doing before you do it
		if (!ignoreLocal && localValues.containsKey(key))
			return (Boolean) localValues.get(key);
		
		return base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
				key.getDescription());
	}

	protected float getFloatValue(Key key, boolean ignoreLocal) {
		//DOESN'T cast check. Know what you're doing before you do it
		if (!ignoreLocal && localValues.containsKey(key))
			return (Float) localValues.get(key);
		
		return base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
				Float.MIN_VALUE, Float.MAX_VALUE, key.getDescription());
	}

	protected int getIntValue(Key key, boolean ignoreLocal) {
		//DOESN'T cast check. Know what you're doing before you do it
		if (!ignoreLocal && localValues.containsKey(key))
			return (Integer) localValues.get(key);
		
		return base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
				Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription());
	}
	
	protected int[] getIntArrayValue(Key key, boolean ignoreLocal) {
		if (!ignoreLocal && localValues.containsKey(key))
			return (int[]) localValues.get(key);
		
		int[] def = (int[]) key.getDefault();
		return base.get(key.getCategory(), key.getKey(), def).getIntList();
	}

	protected String getStringValue(Key key, boolean ignoreLocal) {
		//DOESN'T cast check. Know what you're doing before you do it
		if (!ignoreLocal && localValues.containsKey(key))
			return (String) localValues.get(key);
		
		return base.getString(key.getKey(), key.getCategory(), (String) key.getDefault(),
				key.getDescription());
	}
	
	private Object getRawObject(Key key, boolean ignoreLocal) {
		if (key.getDefault() instanceof Float)
			return getFloatValue(key, ignoreLocal); 
		else if (key.getDefault() instanceof Boolean)
			return getBooleanValue(key, ignoreLocal);
		else if (key.getDefault() instanceof Integer)
			return getIntValue(key, ignoreLocal);
		else if (key.getDefault().getClass().isArray())
			return getIntArrayValue(key, ignoreLocal);
		else
			return getStringValue(key, ignoreLocal);
	}	
	
	public boolean spellDebug() {
		return getBooleanValue(Key.SPELL_DEBUG, false);
	}
	
	public boolean getObeliskList() {
		return getBooleanValue(Key.OBELISK_LIST, false);
	}
	
	public boolean obeliskReqMagic() {
		return getBooleanValue(Key.OBELISK_REQ_MAGIC, true);
	}
	
	public boolean displayManaOrbs() {
		return getBooleanValue(Key.MP_DISPLAY_SPHERES, false);
	}
	
	public boolean displayManaBar() {
		return getBooleanValue(Key.MP_DISPLAY_BAR, false);
	}
	
	public boolean displayManaText() {
		return getBooleanValue(Key.MP_DISPLAY_TEXT, false);
	}
	
	public boolean displayXPText() {
		return getBooleanValue(Key.XP_DISPLAY_TEXT, false);
	}
	
	public boolean displayXPBar() {
		return getBooleanValue(Key.XP_DISPLAY_BAR, false);
	}

	public boolean displayEffects() {
		return getBooleanValue(Key.EFFECT_DISPLAY, false);
	}
	
	public boolean displayLoginText() {
		return getBooleanValue(Key.LOGIN_TEXT, false);
	}
	
	public int[] getDimensionList() {
		return getIntArrayValue(Key.NOSTRUM_WORLDS, true);
	}
	
	public boolean displayAllMirrorNodes() {
		return getBooleanValue(Key.MIRROR_NODE_SPOILERS, false);
	}
	
	public boolean displayArmorOverlay() {
		return getBooleanValue(Key.ARMOR_DISPLAY, false);
	}
	
	public int sorceryDimensionIndex() {
		return getIntValue(Key.NOSTRUM_DIMENSION_ID, true);
	}

	public boolean displayShieldHearts() {
		return getBooleanValue(Key.DISPLAY_SHIELDS, false);
	}
}
