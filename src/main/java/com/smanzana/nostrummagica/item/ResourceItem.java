package com.smanzana.nostrummagica.item;

import java.util.List;

import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Misc. resource items for delayed progression
 * @author Skyler
 *
 */
public class ResourceItem extends Item implements ILoreTagged {

	public static final String ID_TOKEN = "token";
	public static final String ID_PENDANT_LEFT = "pendant_left";
	public static final String ID_PENDANT_RIGHT = "pendant_right";
	public static final String ID_SLAB_FIERCE = "slab_fierce";
	public static final String ID_SLAB_KIND = "slab_kind";
	public static final String ID_SLAB_BALANCED = "slab_balanced";
	public static final String ID_SPRITE_CORE = "sprite_core";
	public static final String ID_ENDER_BRISTLE = "ender_bristle";
	public static final String ID_WISP_PEBBLE = "wisp_pebble";
	public static final String ID_MANA_LEAF = "mana_leaf";
	public static final String ID_EVIL_THISTLE = "evil_thistle";
	public static final String ID_DRAGON_WING = "dragon_wing";
	public static final String ID_SKILL_OOZE = "essential_ooze";
	public static final String ID_SKILL_PENDANT = "eldrich_pendant";
	public static final String ID_SKILL_FLUTE = "living_flute";
	
	public ResourceItem() {
		this(NostrumItems.PropBase());
	}
	
	public ResourceItem(Item.Properties properties) {
		super(properties);
	}
	
    @Override
	public String getLoreKey() {
		return "nostrum_resource";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Resources";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("There are many crafted resources in Nostrum Magica.", "Each is a little different, but you can't help but feel as if they are all somehow connected...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Nostrum Magica adds a handful of unique crafted resources.", "These resources are used in rituals and special crafts.");
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		final String descKey = "item." + this.getRegistryName().getPath() + ".desc";
		
		if (I18n.exists(descKey)) {
			tooltip.add(new TranslatableComponent(descKey));
		}
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
}
