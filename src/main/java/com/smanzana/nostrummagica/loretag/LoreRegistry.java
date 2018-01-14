package com.smanzana.nostrummagica.loretag;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.items.SpellTome;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;

/**
 * Provides lookup from key to ILoreTagged to support offlining and onlining
 * of earned lore
 * @author Skyler
 *
 */
public class LoreRegistry {

	private static LoreRegistry instance = null;
	public static LoreRegistry instance() {
		if (instance == null)
			instance = new LoreRegistry();
		
		return instance;
	}
	
	private Map<String, ILoreTagged> lore;
	
	private LoreRegistry() {
		lore = new HashMap<>();
		
		init();
	}
	
	public void register(ILoreTagged tagged) {
		lore.put(tagged.getLoreKey(), tagged);
	}
	
	public ILoreTagged lookup(String key) {
		return lore.get(key);
	}
	
	private void init() {
		// All of the compile-time known lore elements are here.
		register(ReagentItem.instance());
		register(SpellTome.instance());
		register(SpellScroll.instance());
		register(SpellRune.instance());
		register(ReagentBag.instance());
		register(MagicSwordBase.instance());
		register(MagicArmorBase.helm);
		register(BlankScroll.instance());
		register(SpellTableItem.instance());
		register(InfusedGemItem.instance());
		register(new EntityGolemPhysical(null));
		
		for (Preset preset : Preset.values()) {
			register(preset);
		}
	}
	
	public static ILoreTagged getPreset(EntityLivingBase entityLiving) {
		if (entityLiving == null)
			return null;
		
		for (Preset preset : Preset.values()) {
			if (preset.matches(entityLiving)) {
				return preset;
			}
		}
		
		return null;
	}
	
	public static ILoreTagged getPreset(Block block) {
		if (block == null)
			return null;
		
		for (Preset preset : Preset.values()) {
			if (preset.matches(block)) {
				return preset;
			}
		}
		
		return null;
	}
	
	public static enum Preset implements ILoreTagged {
		
		UNDEAD(new Filter() {public boolean matches(EntityLivingBase base) {
				return base.isEntityUndead();
			}
			
		}, "Undead", new String[] {"Undead mobs are those that have literally risen from the grave.", "While bad for business, you suspect the Grave Dust they occasionally carry around could be of some use..."}, "Undead monsters are those that have been waken from death.", "Because they are not living, they are resistant to ice magics.", "Their dry skin and bones, however, are weak to fire.", "Undead creatures have a chance of dropping Grave Dust, a spell reagent."),
		LEAVES(Blocks.LEAVES, "Leaves", new String[] {"What's up there in the leaves?", "It looks like some sort of dust..."}, "Leaves catch the small amount of Sky Ash that falls during the day.");
		
		protected static interface Filter {
			public boolean matches(EntityLivingBase base);
		}
		
		private Filter filter;
		private Class<? extends EntityLivingBase> clazz;
		private Block block;
		private Lore basic;
		private Lore deep;
		private String key;
		
		private Preset(Class<? extends EntityLivingBase> clazz, String key, String lore[], String ... deep) {
			this.clazz = clazz;
			this.key = key;
			this.basic = new Lore();
			this.basic.add(lore);
			this.deep = new Lore();
			this.deep.add(deep);
		}
		
		private Preset(Filter filter, String key, String lore[], String ... deep) {
			this.filter = filter;
			this.key = key;
			this.basic = new Lore();
			this.basic.add(lore);
			this.deep = new Lore();
			this.deep.add(deep);
		}
		
		private Preset(Block block, String key, String lore[], String ... deep) {
			this.block = block;
			this.key = key;
			this.basic = new Lore();
			this.basic.add(lore);
			this.deep = new Lore();
			this.deep.add(deep);
		}

		@Override
		public String getLoreKey() {
			return key;
		}

		@Override
		public String getLoreDisplayName() {
			return key;
		}

		@Override
		public Lore getBasicLore() {
			return basic;
		}

		@Override
		public Lore getDeepLore() {
			return deep;
		}
		
		public boolean matches(EntityLivingBase base) {
			if (clazz != null && clazz.isAssignableFrom(base.getClass()))
				return true;
			
			if (filter != null)
				return filter.matches(base);
			
			return false;
		}
		
		public boolean matches(Block block) {
			if (this.block != null)
				return this.block == block;
			
			return false;
		}
		
	}
}