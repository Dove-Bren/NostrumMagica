package com.smanzana.nostrummagica.item;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.render.item.NostrumItemSpecialRenderer;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.Util;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraftforge.client.IItemRenderProperties;

public class SpellPatternTome extends Item implements ILoreTagged {

	public static final String ID = "pattern_tome";
	private static final String NBT_PATTERN = "pattern";
	
	public SpellPatternTome(Item.Properties properties) {
		super(properties);
	}
	
	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		super.initializeClient(consumer);
		
		consumer.accept(new IItemRenderProperties() {
			@Override
			public BlockEntityWithoutLevelRenderer getItemStackRenderer()  {
				return NostrumItemSpecialRenderer.INSTANCE;
			}
		});
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
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		if (playerIn.isShiftKeyDown()) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
		}
		
		SpellCraftPattern pattern = this.getPattern(stack);
		if (pattern == null) {
			playerIn.sendMessage(new TextComponent("This pattern tome doesn't appear to have a pattern in it."), Util.NIL_UUID);
			return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
		}
		
		ISpellCrafting attr = NostrumMagica.getSpellCrafting(playerIn);
		if (!worldIn.isClientSide && attr != null) {
			if (attr.getKnownPatterns().contains(pattern)) {
				playerIn.sendMessage(new TranslatableComponent("info.pattern.already_know", pattern.getName()), Util.NIL_UUID);
			} else {
				attr.addPattern(pattern);
				NostrumMagicaSounds.LORE.play(null, playerIn.level, playerIn.getX(), playerIn.getY(), playerIn.getZ());
				stack.shrink(1);
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) playerIn);
				playerIn.sendMessage(new TranslatableComponent("info.pattern.learn", pattern.getName()), Util.NIL_UUID);
				
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		SpellCraftPattern pattern = this.getPattern(stack);
		if (pattern != null) {
			tooltip.add(new TranslatableComponent("info.pattern.usage", pattern.getName()));
		}
	}
	
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			
			for (SpellCraftPattern pattern : SpellCraftPattern.GetAll()) {
				ItemStack patternItem = new ItemStack(this);
				setPattern(patternItem, pattern);
				items.add(patternItem);
			}
		}
	}
}
