package com.smanzana.nostrummagica.lore;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;

/**
 * Something tagged with lore.
 * Lore has two stages: basic and deep. Basic knowledge is
 * obtained by collecting the item, killing the entity, or mining the block.
 * Deep knowledge is obtained doing the same thing except with a mage's journal.
 * Deep knowledge trumps basic. Someone can skip basic.
 * <p>
 * There are exactly three events that give lore:
 * <ol>
 * <li>Collecting an item</li>
 * <li>Breaking a block</li>
 * <li>Killing an entity</li>
 * </ol>
 * As such, only Items, Blocks, and LivingEntities marked with ILoreTagged matter.
 * </p>
 * To use, tag your LivingEntity, Item, or Block with ILoreTagged and implement it. The rest
 * is handled.
 * @author Skyler
 *
 */
public interface ILoreTagged {

	/**
	 * Return a unique string used to persist knowledge.
	 * Best prefix with your modid or something. This is not shown to
	 * the player.
	 * <br />
	 * This is used as a key when looking up Lore objects. If your entity/block/item
	 * actually represents multiple, have each type return a unique key (and
	 * unique lore and display names). Voila!
	 * @return
	 */
	public String getLoreKey();
	
	/**
	 * Return the name of this lore-tagged object. <b>This should be translated already.</b>
	 * @return
	 */
	public String getLoreDisplayName();
	
	/**
	 * Return the basic Lore associated with this object.
	 * @return
	 */
	public Lore getBasicLore();
	
	/**
	 * Return the deep lore associated with this object.
	 * Basic is not displayed after deep is obtained. Be sure to include
	 * relevant information from basic in deep if you want players to remember it!
	 * @return
	 */
	public Lore getDeepLore();

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
