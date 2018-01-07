package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.HSplitPage;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.ImagePage;
import com.smanzana.nostrummagica.client.gui.book.LinedTextPage;
import com.smanzana.nostrummagica.client.gui.book.PlainTextPage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Guide book
 * @author Skyler
 *
 */
public class NostrumGuide extends Item implements GuiBook {

	private static NostrumGuide instance = null;
	
	public static NostrumGuide instance() {
		if (instance == null)
			instance = new NostrumGuide();
		
		return instance;
	}
	
	public static final String id = "nostrum_guide";
	
	private NostrumGuide() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote) {
			NostrumMagica.proxy.openBook(playerIn, this, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public BookScreen getScreen(Object userdata) {
		
		List<IBookPage> pages = new LinkedList<>();
		
		pages.add(new HSplitPage(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/title.png"), 97, 66, 0, 0, 97, 66, null), null));
		pages.add(new LinedTextPage("", "Nostrum Magica Guide"));
		pages.add(new PlainTextPage("  Nostrum Magica is a magic mod that focuses on spell creation. The goal is to give you the ability to make the spells YOU want to have and use while providing a reasonable challenge and sense of progression."));
		pages.add(new PlainTextPage("To do this, Nostrum Magica defines a spell system that revolves around spell components -- Triggers, Shapes, Elements, and Alterations. Spells then cost mana and physical reagents and do everything from healing to reducing attack to creating powerful gear!"));
		pages.add(new LinedTextPage("", "Getting Started"));
		pages.add(new PlainTextPage("  Nostrum Magica is intended to be a mid-game mod. In other words, getting started in Nostrum Magica requires a small number of resources you won't have right away. For example, you'll need to find a small handful of runes and a decent amount of most reagent types in order to create your first spell."));
		pages.add(new PlainTextPage("Some of these materials are the same you use in Vanilla Minecraft: wood, iron, leather, paper, etc. Besides runes, the others are magic reagents, which come from a variety of sources. These are explained in the next section."));
		pages.add(new PlainTextPage("Before getting started, you must unlocked the magic within you. If you are lucky enough to apprentice to an existing mage, this is as easy as having them create a Writ of Learning. If you are not so lucky, you must perform the Ritual of Discovery which is described in the following sections."));
		pages.add(new PlainTextPage("Your first task should be to gather reagents. After you've built up a decent amount of each, hunt down runes. Runes can be found in dungeons as loot or in shrines. After you have a handful of triggers, shapes, and elements, craft a spell table and some blank scrolls and create your first spell."));
		pages.add(new LinedTextPage("", "Magical Reagents"));
		pages.add(new PlainTextPage("   Creating spells or casting spells from tomes requires reagents. The reagents required depend on the components of the spell. There are 7 different types of reagents. Each is a little bit different to obtain than the last."));
		pages.add(new HSplitPage(new PlainTextPage("Mandrake Root can be found in tall grass, just like vanilla seeds. It can be planted and grown like wheat. Mandrake Root is mainly used in spells with some form of materialization."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/mandrake_root.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage("Black Pearl is obtained by harvesting Midnight Irises, which can be found growing all over. Black Pearl is used in spells that require great amounts of energy."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/black_pearl.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage("Mani Dust is found deep in the earth in the form of ore deposits. The dust is an important one for many non-spell items as well as spells that perform over an area."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/mani_dust.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage("Grave Dust is soil that was used to bury someone and sanctified. Undead are sure to have a couple of pinches of the stuff."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/grave_dust.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage("Ginseng is found just like mandrake root; find it in grass and plant it to grow more. Ginseng is used in spells that are restorative."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/ginseng.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage("As magic is released into the sky, it mixes and eventually falls back down in the form of Sky Ash. Naturally, trees are a good source of the powder."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/sky_ash.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage("Spider silk is strong yet smooth. These properties lend themselves to spell components that are binding."), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/items/spider_silk.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new LinedTextPage("", "Ritual of Discovery"));
		pages.add(new PlainTextPage("  The Ritual of Discovery is performed to unlock the magic potential in the player. The ritual requires the player to travel far and wide in search of special altars. In order to unlock magic and really get started, you must locate a shrine to each element. Deep in the shrine are obelisks"));
		pages.add(new PlainTextPage("which can be interacted with to provide insight into the element. Once you've gained knowledge of all 7 elements, you must continue the ritual by locating a Shrine of Touch. Like the other shrines, the Shrine of Touch has an obelisk deep within it that will give you the knowledge you need"));
		pages.add(new PlainTextPage("in order to acquire your first trigger rune, which is required for a spell. To bring the ritual to a close, you must locate a Shrine of Self to unlock the shape Self. Upon aquiring knowledge of all components, magic will be unlocked and the ritual completed."));
		pages.add(new LinedTextPage("", "First Steps"));
		pages.add(new PlainTextPage("  Creating spells requires runes. Most runes can be found as loot in dungeons or shrines. However, experienced mages are able to craft the runes they need themselves instead of hunting them in the field. In order to craft runes like this, however, the mage must have mastery of the rune."));
		pages.add(new PlainTextPage("As part of the Ritual of Discovery, you unlocked knowledge of all the elements as well as the Touch and Self runes. However, you only start with mastery of the Touch, Self, and Physical runes. In order to create any other runes, you must perform whichever ritual is required to attain"));
		pages.add(new PlainTextPage(" mastery. The requirements are determined by the type of rune. Trigger runes require bringing a handful of the rune to the Shrine for that rune, and then beating some sort of challenge. Elemental Runes first require finding the propery codex of pages as loot, and then performing"));
		pages.add(new PlainTextPage("the specialized rituals or feats as described in the codex. Elemental mastery also has tiers of mastery (Tier I, II, and III) whereas other rune types do not. Both Shape and Alteration runes are mastered in ways unknown."));
		
		
		
		return new BookScreen(pages);
	}
}
