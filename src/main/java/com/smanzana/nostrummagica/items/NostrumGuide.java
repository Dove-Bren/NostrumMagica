package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.HSplitPage;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.ImagePage;
import com.smanzana.nostrummagica.client.gui.book.PlainTextPage;
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
public class NostrumGuide extends Item implements GuiBook {

	public static final String ID = "nostrum_guide";
	
	public NostrumGuide() {
		super(NostrumItems.PropUnstackable());
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (worldIn.isRemote) {
			NostrumMagica.instance.proxy.openBook(playerIn, this, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
    }
	
	@OnlyIn(Dist.CLIENT)
	public String T(String key) {
		return I18n.format("guide." + key, new Object[0]);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BookScreen getScreen(Object userdata) {
		
		List<IBookPage> pages = new LinkedList<>();
		
		pages.add(new TitlePage("Title", new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/title.png"), 97, 66, 0, 0, 97, 66, null), false));
		pages.add(new TitlePage(T("title"), false));
		BookScreen.makePagesFrom(pages, T("page1"));
		pages.add(new TitlePage(T("getting_started"), true));
		BookScreen.makePagesFrom(pages, T("page2"));
		pages.add(new TitlePage(T("reagents"), true));
		BookScreen.makePagesFrom(pages, T("page3"));
		pages.add(new HSplitPage(new PlainTextPage(T("mandrake_root")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/mandrake_root.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage(T("black_pearl")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/black_pearl.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage(T("mani_dust")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/mani_dust.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage(T("grave_dust")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/grave_dust.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage(T("ginseng")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/ginseng.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage(T("sky_ash")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/sky_ash.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new PlainTextPage(T("spider_silk")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/spider_silk.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new TitlePage(T("discovery"), true));
		BookScreen.makePagesFrom(pages, T("page4"));
		pages.add(new TitlePage(T("first_steps"), true));
		BookScreen.makePagesFrom(pages, T("page5"));
		pages.add(new TitlePage(T("spell_tomes"), true));
		BookScreen.makePagesFrom(pages, T("page6"));
		pages.add(new TitlePage(T("rituals"), true));
		BookScreen.makePagesFrom(pages, T("page7"));
		pages.add(new HSplitPage(new PlainTextPage(T("candle")), new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/block/candle.png"), 32, 32, 0, 0, 32, 32, null)));
		pages.add(new HSplitPage(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/item/altar_item.png"), 32, 32, 0, 0, 32, 32, null), new PlainTextPage(T("pedestal"))));
		BookScreen.makePagesFrom(pages, T("page8"));
		pages.add(new HSplitPage(new TitlePage(T("tier1"), false), new PlainTextPage(T("tier1_desc"))));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/ritual_1.png"), 96, 96, 0, 0, 96, 96, null));
		pages.add(new HSplitPage(new TitlePage(T("tier2"), false), new PlainTextPage(T("tier2_desc"))));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/ritual_2.png"), 96, 76, 0, 0, 96, 76, null));
		pages.add(new HSplitPage(new TitlePage(T("tier3"), false), new PlainTextPage(T("tier3_desc"))));
		pages.add(new ImagePage(new ResourceLocation(NostrumMagica.MODID, "textures/gui/ritual_3.png"), 96, 76, 0, 0, 96, 76, null));
		BookScreen.makePagesFrom(pages, T("page9"));
		pages.add(new TitlePage(T("ritual_bonding"), true));
		BookScreen.makePagesFrom(pages, T("page10"));
		pages.add(new TitlePage(T("ritual_binding"), true));
		BookScreen.makePagesFrom(pages, T("page11"));
		
		pages.add(new PlainTextPage(""));
		
//		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
//		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
//		
//		pages.add(new TitlePage(T("ritual_index"), true));
//		for (RitualRecipe ritual : RitualRegistry.instance().getRegisteredRituals()) {
//			if (ritual.getRequirement() == null || ritual.getRequirement().matches(player, attr))
//				pages.add(new RitualRecipePage(ritual));
//		}
		
		pages.add(new PlainTextPage(""));
		return new BookScreen("nostrum_guide", pages);
	}
}
