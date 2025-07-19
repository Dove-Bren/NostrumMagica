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
import com.smanzana.nostrummagica.progression.research.NostrumResearches;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ResearchTranscriptItem extends Item implements ILoreTagged {

	public static final String ID = "research_scroll_unlock";
	private static final String NBT_RESEARCH = "research";
	
	public ResearchTranscriptItem(Item.Properties properties) {
		super(properties);
	}
	
	protected void setResearchKey(ItemStack stack, ResourceLocation researchName) {
		stack.getOrCreateTag().putString(NBT_RESEARCH, researchName.toString());
	}
	
	protected void setResearch(ItemStack stack, NostrumResearch research) {
		setResearchKey(stack, research.getID());
	}
	
	protected ResourceLocation getResearchKey(ItemStack stack) {
		return ResourceLocation.parse(stack.getOrCreateTag().getString(NBT_RESEARCH));
	}
	
	public @Nullable NostrumResearch getResearch(ItemStack stack) {
		ResourceLocation key = getResearchKey(stack);
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		if (playerIn.isShiftKeyDown()) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
		}
		
		NostrumResearch research = getResearch(stack);
		if (research == null) {
			playerIn.sendMessage(new TextComponent("This transcript doesn't appear to have a valid research in it."), Util.NIL_UUID);
			return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (!worldIn.isClientSide && attr != null) {
			if (attr.getCompletedResearches().contains(research.getID())) {
				playerIn.sendMessage(new TranslatableComponent("info.research.already_know", new TranslatableComponent(research.getNameKey())), Util.NIL_UUID);
			} else {
				attr.completeResearch(research.getID());
				NostrumMagicaSounds.LORE.play(null, playerIn.level, playerIn.getX(), playerIn.getY(), playerIn.getZ());
				stack.shrink(1);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) playerIn);
				playerIn.sendMessage(new TranslatableComponent("info.research.learn", new TranslatableComponent(research.getNameKey())), Util.NIL_UUID);
				
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		NostrumResearch research = this.getResearch(stack);
		if (research != null) {
			tooltip.add(new TranslatableComponent("item.nostrummagica." + ID + ".desc", new TranslatableComponent(research.getNameKey()).withStyle(ChatFormatting.BLUE)));
			
			final Player player = NostrumMagica.Proxy.getPlayer();
			INostrumMagic attr = player == null ? null : NostrumMagica.getMagicWrapper(player);
			if (attr != null && attr.getCompletedResearches().contains(research.getID())) {
				tooltip.add(new TextComponent(" "));
				tooltip.add(new TextComponent("Already Researched").withStyle(ChatFormatting.RED));
			}
		}
	}
	
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			
			for (ResourceLocation key: CREATIVE_RESEARCHES) {
				NostrumResearch research = NostrumResearch.lookup(key);
				if (research != null) {
					ItemStack researchItem = new ItemStack(this);
					setResearch(researchItem, research);
					items.add(researchItem);
				}
			}
		}
	}
	
	public static final ResourceLocation[] CREATIVE_RESEARCHES = {
			NostrumResearches.ID_Advanced_Spelltable,
			NostrumResearches.ID_Mystic_Spelltable,
	};
}
