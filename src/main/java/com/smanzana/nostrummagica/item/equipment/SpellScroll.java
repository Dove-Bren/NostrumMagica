package com.smanzana.nostrummagica.item.equipment;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.api.ISpellContainerItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellRequestMessage;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.MagicCapability;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.SpellCastEvent;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellScroll extends Item implements ILoreTagged, IRaytraceOverlay, ISpellContainerItem {

	private static final String NBT_SPELL = "nostrum_spell";
	private static final String NBT_DURABILITY = "max_uses";
	public static final String ID = "spell_scroll";
	
	public SpellScroll() {
		super(NostrumItems.PropUnstackable().rarity(Rarity.UNCOMMON).durability(100));
		MinecraftForge.EVENT_BUS.addListener(SpellScroll::onSpellCast);
	}
	
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getItemInHand(hand);
		
		if (playerIn.isShiftKeyDown()) {
			// Open scroll screen
			final RegisteredSpell spell = GetSpell(itemStackIn);
			if (spell != null && worldIn.isClientSide()) {
				NostrumMagica.Proxy.openSpellScreen(spell);
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		}
		
		if (!MagicCapability.SCROLLCAST_ENABLED.matches(playerIn)) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
		}
		
		if (itemStackIn.isEmpty())
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
		
		if (!itemStackIn.hasTag())
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
		
		CompoundTag nbt = itemStackIn.getTag();
		
		if (!nbt.contains(NBT_SPELL, Tag.TAG_INT))
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
		
		RegisteredSpell spell = GetSpell(itemStackIn);
		if (spell == null)
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
		
		if (worldIn.isClientSide()) {
			NostrumMagica.Proxy.castScroll(hand, itemStackIn, spell);
			//((ClientPlayerListener) NostrumMagica.playerListener).startScrollCast(hand, itemStackIn, spell);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		
    }
	
	public static void setSpell(ItemStack itemStack, RegisteredSpell spell) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return;
		
		CompoundTag nbt = itemStack.getTag();
		
		if (nbt == null)
			nbt = new CompoundTag();
		
		nbt.putInt(NBT_SPELL, spell.getRegistryID());
		nbt.putInt(NBT_DURABILITY, GetMaxUses(spell));
		
		itemStack.setTag(nbt);
		itemStack.setHoverName(new TextComponent(spell.getName()));
		itemStack.enchant(Enchantments.POWER_ARROWS, 1);
	}
	
	public static RegisteredSpell GetSpell(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return null;
		
		CompoundTag nbt = itemStack.getTag();		
		if (nbt == null)
			return null;
		
		int id = nbt.getInt(NBT_SPELL);
		RegisteredSpell spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
		
		if (spell == null) {
			if (NostrumMagica.Proxy.isServer()) {
				NostrumMagica.logger.error("Failed to lookup spell in scroll with id " + id);
			} else {
				NostrumMagica.logger.info("Requesting spell " + id
						 + " from the server...");
					NetworkHandler.sendToServer(
			    			new SpellRequestMessage(new int[] {id}));
			}
		}
			
		return spell;
	}
	
	public static int getMaxDurability(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return 1;
		
		CompoundTag nbt = itemStack.getTag();		
		if (nbt == null || !nbt.contains(NBT_DURABILITY, Tag.TAG_INT))
			return 15; // old default
		
		return Math.max(1, nbt.getInt(NBT_DURABILITY));
	}
	
	public static ItemStack create(RegisteredSpell spell) {
		ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
		setSpell(scroll, spell);
		return scroll;
	}
	
	/**
	 * Figure out how many uses to give the provided spell.
	 * More complex spells have less uses per scroll.
	 */
	protected static final int GetMaxUses(RegisteredSpell spell) {
		final int weight = (spell == null ? 0 : spell.getWeight());
		if (weight == 0) {
			return 100;
		} else if (weight == 1) {
			return 50;
		} else {
			return 25;
		}
	}
	
	public int getCastDurabilityCost(Player caster, RegisteredSpell spell) {
		// By default, cost durability-1 of the scroll so that it has exactly 2 casts.
		// With skill, take a constant base here (1)
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr != null) {
			if (attr.hasSkill(NostrumSkills.Craftcast_ScrollEfficiency)) {
				int base = 1;
				return base;
			}
		}
		
		// Base case: take all but one durability
		final int max = GetMaxUses(spell);
		return max-1;
	}

	@Override
	public String getLoreKey() {
		return "nostrum_spell_scroll";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Scrolls";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Spell scrolls are created from blank scrolls, spell runes, and reagents.", "Using a spell scroll will cast the spell on it.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Spell scrolls are created from blank scrolls, spell runes, and reagents.", "Using a spell scroll will cast the spell on it.", "Scrolls can be bound to Spell Tomes so that they can be cast over and over at the cost of reagents.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		;
	}
	
	@Override
	public boolean shouldTrace(Level world, Player player, ItemStack stack) {
		RegisteredSpell spell = GetSpell(stack);
		return spell == null ? false : spell.shouldTrace(player);
	}
	
	@Override
	public boolean shouldOutline(Level world, Player player, ItemStack stack) {
		return false;
		// Ideally this would be true, but scroll casting is done server side with no client hint, so don't show it
		// and confuse players into thinking it will help them
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		return getMaxDurability(stack);
	}

	@Override
	public double getTraceRange(Level world, Player player, ItemStack stack) {
		RegisteredSpell spell = GetSpell(stack);
		return spell == null ? 0 : spell.getTraceRange(player);
	}

	@Override
	public RegisteredSpell getSpell(ItemStack stack) {
		return GetSpell(stack);
	}
	
	@SubscribeEvent
	public static void onSpellCast(SpellCastEvent.Post event) {
		// Notice and respond any time any spell is cast to it's cooldown.
		// Note that our r-click handler will actually replace this result with a larger one if it's a scroll that
		// cast the spell.
		final SpellCastResult result = event.getCastResult();
		if (!event.isChecking && result.succeeded && result.caster != null && result.caster instanceof Player && !result.caster.isDeadOrDying()) {
			// Vulnerability: vanilla's tracker only returns us progress which means we can't REALLY check if our new
			// cooldown time is going to be less than what's already there.
			// If we blindly PUT, player's can get around long cooldowns by casting a short-cooldown spell after
			// a long one.
			// If we only put when there is no cooldown, player's can cast a short cooldown spell and then use it as immunity
			// if they cast a long-cooldown spell before then.
			// That seems like the harder thing to do, so opt to let that happen. Try to guess though at 25% of remaining as a
			// good time to let it be overriden.
			
			final int cooldownTicks = SpellCasting.CalculateSpellCooldown(result);
			if (((Player) result.caster).getCooldowns().getCooldownPercent(NostrumItems.spellScroll, 0f) <= .25f) {
				((Player) result.caster).getCooldowns().addCooldown(NostrumItems.spellScroll, cooldownTicks);
			}
		}
	}
}
