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
		//MANA_DISPLAY(),
		SPELL_DEBUG(Category.SPELL, "spell_debug", false, false, "Print targetting debug information for spells? (Requires config on server set to true as well)");
		
		
//		DEPTH_S(Category.TEST, "depth_s", new Float(0.1f), false, "south depth"),
//		DEPTH_N(Category.TEST, "depth_n", new Float(0.1f), false, "north depth"),
//		ROTATE_ANGLE(Category.TEST, "rotate_angle", new Float(45.0f), false, "angle"),
//		ROTATE_X(Category.TEST, "rotate_x", new Float(0.5f), false, "x"),
//		ROTATE_Y(Category.TEST, "rotate_y", new Float(1.0f), false, "y"),
//		ROTATE_Z(Category.TEST, "rotate_z", new Float(0.5f), false, "z");
		
		public static enum Category {
			SERVER("server", "Core properties that MUST be syncronized bytween the server and client. Client values ignored"),
			DISPLAY("display", "Item tag information and gui display options"),
			SPELL("spell", "Options that change client-side handling of spells"),
			TEST("test", "Options used just for debugging and development");
			
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
				System.out.println("it's a float");
				base.getFloat(key.getKey(), key.getCategory(), (Float) key.getDefault(),
						Float.MIN_VALUE, Float.MAX_VALUE, key.getDescription());
			}
			else if (key.getDefault() instanceof Boolean)
				base.getBoolean(key.getKey(), key.getCategory(), (Boolean) key.getDefault(),
						key.getDescription());
			else if (key.getDefault() instanceof Integer)
				base.getInt(key.getKey(), key.getCategory(), (Integer) key.getDefault(),
						Integer.MIN_VALUE, Integer.MAX_VALUE, key.getDescription());
			else
				base.getString(key.getKey(), key.getCategory(), key.getDefault().toString(),
						key.getDescription());
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
		else
			return getStringValue(key, ignoreLocal);
	}
	
	
	public boolean spellDebug() {
		return getBooleanValue(Key.SPELL_DEBUG, false);
	}
	
}
