package com.smanzana.nostrummagica.loretag;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenIndexed;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;

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
	 * actually represents multiple, register an instance of each
	 * @param o
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
	
	/**
	 * Return which tab this piece of lore should be displayed under
	 * @return
	 */
	public InfoScreenTabs getTab();
	
	public static String GetInfoKey(ILoreTagged lore) {
		if (lore instanceof InfoScreenIndexed) {
			return ((InfoScreenIndexed) lore).getInfoScreenKey();
		} else {
			return "Lore::" + lore.getLoreKey();
		}
	}
}
