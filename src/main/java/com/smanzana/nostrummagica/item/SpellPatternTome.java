package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.render.item.SpellPatternTomeRenderer;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellPatternTome extends Item implements ILoreTagged {

	public static final String ID = "pattern_tome";
	private static final String NBT_PATTERN = "pattern";
	
	public SpellPatternTome(Item.Properties properties) {
		super(properties.setISTER(() -> SpellPatternTomeRenderer::new));
	}
	
	protected void setPatternID(ItemStack stack, ResourceLocation ID) {
		stack.getOrCreateTag().putString(NBT_PATTERN, ID.toString());
	}
	
	protected ResourceLocation getPatternID(ItemStack stack) {
		return new ResourceLocation(stack.getOrCreateTag().getString(NBT_PATTERN));
	}
	
	public @Nullable SpellCraftPattern getPattern(ItemStack stack) {
		ResourceLocation ID = getPatternID(stack);
		return SpellCraftPattern.Get(ID);
	}
	
	public void setPattern(ItemStack stack, SpellCraftPattern pattern) {
		setPatternID(stack, pattern.getRegistryName());
	}
	
	@Override
	public String getLoreKey() {
		return "pattern_tome";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Pattern Tomes";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("These ancient texts possess incredible power and can teach a spellcrafter a new spell craft pattern!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("These ancient texts possess incredible power and can teach a spellcrafter a new spell craft pattern!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		if (playerIn.isShiftKeyDown()) {
			return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
		}
		
		SpellCraftPattern pattern = this.getPattern(stack);
		if (pattern == null) {
			playerIn.sendMessage(new StringTextComponent("This pattern tome doesn't appear to have a pattern in it."), Util.NIL_UUID);
			return new ActionResult<ItemStack>(ActionResultType.FAIL, stack);
		}
		
		ISpellCrafting attr = NostrumMagica.getSpellCrafting(playerIn);
		if (!worldIn.isClientSide && attr != null) {
			if (attr.getKnownPatterns().contains(pattern)) {
				playerIn.sendMessage(new TranslationTextComponent("info.pattern.already_know", pattern.getName()), Util.NIL_UUID);
			} else {
				attr.addPattern(pattern);
				NostrumMagicaSounds.LORE.play(null, playerIn.level, playerIn.getX(), playerIn.getY(), playerIn.getZ());
				stack.shrink(1);
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) playerIn);
				playerIn.sendMessage(new TranslationTextComponent("info.pattern.learn", pattern.getName()), Util.NIL_UUID);
				
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		SpellCraftPattern pattern = this.getPattern(stack);
		if (pattern != null) {
			tooltip.add(new TranslationTextComponent("info.pattern.usage", pattern.getName()));
		}
	}
	
	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			
			for (SpellCraftPattern pattern : SpellCraftPattern.GetAll()) {
				ItemStack patternItem = new ItemStack(this);
				setPattern(patternItem, pattern);
				items.add(patternItem);
			}
		}
	}
}
