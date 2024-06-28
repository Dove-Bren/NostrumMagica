package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ResearchTranscriptItem extends Item implements ILoreTagged {

	public static final String ID = "research_scroll_unlock";
	private static final String NBT_RESEARCH = "research";
	
	public ResearchTranscriptItem(Item.Properties properties) {
		super(properties);
	}
	
	protected void setResearchKey(ItemStack stack, String researchName) {
		stack.getOrCreateTag().putString(NBT_RESEARCH, researchName);
	}
	
	protected void setResearch(ItemStack stack, NostrumResearch research) {
		setResearchKey(stack, research.getKey());
	}
	
	protected String getResearchKey(ItemStack stack) {
		return stack.getOrCreateTag().getString(NBT_RESEARCH);
	}
	
	public @Nullable NostrumResearch getResearch(ItemStack stack) {
		String key = getResearchKey(stack);
		return NostrumResearch.lookup(key);
	}
	
	@Override
	public String getLoreKey() {
		return "research_transcript";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Transcripts";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Reading this unlocks a hidden research item!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Reading this unlocks a hidden research item!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.isSneaking()) {
			return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
		}
		
		NostrumResearch research = getResearch(stack);
		if (research == null) {
			playerIn.sendMessage(new StringTextComponent("This transcript doesn't appear to have a valid research in it."), Util.DUMMY_UUID);
			return new ActionResult<ItemStack>(ActionResultType.FAIL, stack);
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (!worldIn.isRemote && attr != null) {
			if (attr.getCompletedResearches().contains(research.getKey())) {
				playerIn.sendMessage(new TranslationTextComponent("info.research.already_know", new TranslationTextComponent(research.getNameKey())), Util.DUMMY_UUID);
			} else {
				attr.completeResearch(research.getKey());
				NostrumMagicaSounds.LORE.play(null, playerIn.world, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ());
				stack.shrink(1);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) playerIn);
				playerIn.sendMessage(new TranslationTextComponent("info.research.learn", new TranslationTextComponent(research.getNameKey())), Util.DUMMY_UUID);
				
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		NostrumResearch research = this.getResearch(stack);
		if (research != null) {
			tooltip.add(new TranslationTextComponent("item.nostrummagica." + ID + ".desc", new TranslationTextComponent(research.getNameKey()).mergeStyle(TextFormatting.BLUE)));
			
			final PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			INostrumMagic attr = player == null ? null : NostrumMagica.getMagicWrapper(player);
			if (attr != null && attr.getCompletedResearches().contains(research.getKey())) {
				tooltip.add(new StringTextComponent(" "));
				tooltip.add(new StringTextComponent("Already Researched").mergeStyle(TextFormatting.RED));
			}
		}
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			
			for (String key: CREATIVE_RESEARCHES) {
				NostrumResearch research = NostrumResearch.lookup(key);
				if (research != null) {
					ItemStack researchItem = new ItemStack(this);
					setResearch(researchItem, research);
					items.add(researchItem);
				}
			}
		}
	}
	
	public static final String[] CREATIVE_RESEARCHES = {
			"advanced_spelltable",
			"mystic_spelltable",
	};
}
