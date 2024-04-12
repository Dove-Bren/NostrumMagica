package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class NostrumSkillItem extends Item implements ILoreTagged {

	public static final String ID_SKILL_MIRROR = "primordial_mirror";
	public static final String ID_SKILL_OOZE = "essential_ooze";
	public static final String ID_SKILL_PENDANT = "eldrich_pendant";
	public static final String ID_SKILL_FLUTE = "living_flute";
	public static final String ID_SKILL_ENDER_PIN = "ender_pin";
	public static final String ID_SKILL_SCROLL_SMALL = "research_scroll_small";
	public static final String ID_SKILL_SCROLL_LARGE = "research_scroll_large";
	
	private static interface SkillFunc {
		public boolean award(PlayerEntity player, INostrumMagic attr, ItemStack stack);
	}
	
	private final SkillFunc func;
	
	public NostrumSkillItem(Item.Properties properties, SkillFunc func) {
		super(properties.maxStackSize(1));
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
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	protected String getDescKey() {
		return "item.nostrummagica." + this.getRegistryName().getPath() + ".desc"; 
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.isSneaking()) {
			return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (!worldIn.isRemote && attr != null && attr.isUnlocked()) {
			if (this.func.award(playerIn, attr, stack)) {
				NostrumMagicaSounds.LORE.play(null, playerIn.world, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ());
				stack.shrink(1);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) playerIn);
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (I18n.hasKey(getDescKey())) {
			tooltip.add(new TranslationTextComponent(getDescKey()).applyTextStyle(TextFormatting.BLUE));
		}
	}
	
	public static class Mirror extends NostrumSkillItem {
		public Mirror() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addSkillPoint();
				player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_MIRROR));
				return true;
			});
		}
	}
	
	public static class Ooze extends NostrumSkillItem {
		public Ooze() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addControl();
				player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_OOZE));
				return true;
			});
		}
	}
	
	public static class Pendant extends NostrumSkillItem {
		public Pendant() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addTech();
				player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_PENDANT));
				return true;
			});
		}
	}
	
	public static class Flute extends NostrumSkillItem {
		public Flute() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addFinesse();
				player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_FLUTE));
				return true;
			});
		}
	}
	
	public static class EnderPin extends NostrumSkillItem {
		public EnderPin() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				if (attr.hasEnhancedTeleport()) {
					player.sendMessage(new TranslationTextComponent("info.skillitem.advtele.unlocked", new Object[0]));
					return false;
				} else {
					attr.unlockEnhancedTeleport();
					player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_ENDER_PIN));
					return true;
				}
			});
		}
	}
	
	public static class SmallScroll extends NostrumSkillItem {
		public SmallScroll() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addResearchPoint();
				player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_SCROLL_SMALL));
				return true;
			});
		}
	}
	
	public static class LargeScroll extends NostrumSkillItem {
		public LargeScroll() {
			super(NostrumItems.PropUnstackable().rarity(Rarity.RARE), (player, attr, stack) -> {
				attr.addResearchPoint();
				attr.addResearchPoint();
				attr.addResearchPoint();
				player.sendMessage(new TranslationTextComponent("info.skillitem." + ID_SKILL_SCROLL_LARGE));
				return true;
			});
		}
	}
}
