package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.HSplitPage;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.ImagePage;
import com.smanzana.nostrummagica.client.gui.book.TitlePage;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Guide book
 * @author Skyler
 *
 */
public class SpellcraftGuide extends Item implements GuiBook {

	public static final String ID = "spellcraft_book";
	
	public SpellcraftGuide() {
		super(NostrumItems.PropUnstackable());
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (worldIn.isRemote) {
			NostrumMagica.instance.proxy.openBook(playerIn, this, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
    }
	
	@OnlyIn(Dist.CLIENT)
	public String T(String key) {
		return I18n.format("spellcraft." + key, new Object[0]);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BookScreen getScreen(Object userdata) {
		
		List<IBookPage> pages = new LinkedList<>();
		
		/*
		 * Spell Table (spell table is where runes and reagents go. Has interface. it's cool)
		 * Spell Runes
		 * Reagents
		 * Problems
		 */
		
		pages.add(new TitlePage("", false));
		pages.add(new TitlePage(T("title"), false));
		BookScreen.makePagesFrom(pages, T("intro"));
		pages.add(new TitlePage(T("spelltable"), true));
		BookScreen.makePagesFrom(pages, T("spelltable_intro1"));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table1.png"), 128, 128, 0, 0, 128, 128, null));
		BookScreen.makePagesFrom(pages, T("spelltable_intro2"));
		
		pages.add(new TitlePage(T("runes"), true));
		BookScreen.makePagesFrom(pages, T("runes_intro"));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table2.png"), 128, 73, 0, 0, 128, 73, null));
		BookScreen.makePagesFrom(pages, T("runes_shapes1"));
		pages.add(new HSplitPage(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table3.png"), 128, 52, 0, 0, 128, 52, null), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table4.png"), 128, 52, 0, 0, 128, 52, null)));
		BookScreen.makePagesFrom(pages, T("runes_shapes2"));
		BookScreen.makePagesFrom(pages, T("runes_shapes3"));
		
		pages.add(new TitlePage(T("reagents"), true));
		BookScreen.makePagesFrom(pages, T("reagents_info"));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table5.png"), 128, 83, 0, 0, 128, 83, null));
		
		pages.add(new TitlePage(T("finishing"), true));
		BookScreen.makePagesFrom(pages, T("finishing_info"));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table6.png"), 128, 99, 0, 0, 128, 99, null));
		
		pages.add(new TitlePage(T("problems"), true));
		BookScreen.makePagesFrom(pages, T("problems_info"));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/screenshot_table7.png"), 128, 31, 0, 0, 128, 31, null));
		
		pages.add(new TitlePage("", false));
		return new BookScreen("spellcraft_guide", pages);
	}
}
