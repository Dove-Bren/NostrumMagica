package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SkillItem extends Item implements ILoreTagged {

	public static final String ID_SKILL_MIRROR = "primordial_mirror";
	public static final String ID_SKILL_ENDER_PIN = "ender_pin";
	public static final String ID_SKILL_SCROLL_SMALL = "research_scroll_small";
	public static final String ID_SKILL_SCROLL_LARGE = "research_scroll_large";
	
	private static interface SkillFunc {
		public boolean award(Player player, INostrumMagic attr, ItemStack stack);
	}
	
	private final SkillFunc func;
	
	public SkillItem(Item.Properties properties, SkillFunc func) {
		super(properties.stacksTo(1));
		this.func = func;
	}
	
//	@Override
//	public String getUnlocalizedName(ItemStack stack) {
//		int i = stack.getMetadata();
//		
//		SkillItemType type = getTypeFromMeta(i);
//		return "item." + type.getUnlocalizedKey();
//	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_skill_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Skill Items";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Some items possess an insane amount of magical energies. These items can be combined in certain ways that you might be able to utilize...");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Some items possess an insane amount of magical energies.", "The most useful is the Primordial Mirror, which you can use to gain an extra Skill Point!");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
	
	protected String getDescKey() {
		return "item.nostrummagica." + this.getRegistryName().getPath() + ".desc"; 
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		if (playerIn.isShiftKeyDown()) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (!worldIn.isClientSide && attr != null && attr.isUnlocked()) {
			if (this.func.award(playerIn, attr, stack)) {
				NostrumMagicaSounds.LORE.play(null, playerIn.level, playerIn.getX(), playerIn.getY(), playerIn.getZ());
				stack.shrink(1);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) playerIn);
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (I18n.exists(getDescKey())) {
			tooltip.add(new TranslatableComponent(getDescKey()).withStyle(ChatFormatting.BLUE));
		}
	}
	
	public static class Mirror extends SkillItem {
		public Mirror() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.EPIC), (player, attr, stack) -> {
				attr.addSkillPoint();
				player.sendMessage(new TranslatableComponent("info.skillitem." + ID_SKILL_MIRROR), Util.NIL_UUID);
				return true;
			});
		}
	}
	
	public static class EnderPin extends SkillItem {
		public EnderPin() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				if (attr.hasEnhancedTeleport()) {
					player.sendMessage(new TranslatableComponent("info.skillitem.advtele.unlocked", new Object[0]), Util.NIL_UUID);
					return false;
				} else {
					attr.unlockEnhancedTeleport();
					player.sendMessage(new TranslatableComponent("info.skillitem." + ID_SKILL_ENDER_PIN), Util.NIL_UUID);
					return true;
				}
			});
		}
	}
	
	public static class SmallScroll extends SkillItem {
		public SmallScroll() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addResearchPoint();
				player.sendMessage(new TranslatableComponent("info.skillitem." + ID_SKILL_SCROLL_SMALL), Util.NIL_UUID);
				return true;
			});
		}
	}
	
	public static class LargeScroll extends SkillItem {
		public LargeScroll() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addResearchPoint();
				attr.addResearchPoint();
				attr.addResearchPoint();
				player.sendMessage(new TranslatableComponent("info.skillitem." + ID_SKILL_SCROLL_LARGE), Util.NIL_UUID);
				return true;
			});
		}
	}
}
