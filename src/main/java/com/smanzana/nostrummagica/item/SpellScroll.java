package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellRequestMessage;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastEvent;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellScroll extends Item implements ILoreTagged, IRaytraceOverlay, ISpellContainerItem {

	private static final String NBT_SPELL = "nostrum_spell";
	private static final String NBT_DURABILITY = "max_uses";
	public static final String ID = "spell_scroll";
	
	public SpellScroll() {
		super(NostrumItems.PropUnstackable().rarity(Rarity.UNCOMMON).maxDamage(100));
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (playerIn.isSneaking()) {
			// Open scroll screen
			final Spell spell = GetSpell(itemStackIn);
			if (spell != null && worldIn.isRemote()) {
				NostrumMagica.instance.proxy.openSpellScreen(spell);
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		
		if (worldIn.isRemote()) {
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		
		if (itemStackIn.isEmpty())
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		if (!itemStackIn.hasTag())
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		CompoundNBT nbt = itemStackIn.getTag();
		
		if (!nbt.contains(NBT_SPELL, NBT.TAG_INT))
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		Spell spell = GetSpell(itemStackIn);
		if (spell == null)
			return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
		
		SpellCastResult result = SpellCasting.AttemptScrollCast(spell, playerIn);
		if (result.succeeded) {
			if (!playerIn.isCreative()) {
				ItemStacks.damageItem(itemStackIn, playerIn, hand, getCastDurabilityCost(playerIn, GetSpell(itemStackIn)));
			}
			
			// Set cooldown directly even though event handler will have already set it.
			// Using a scroll has more cooldown than noticing other spells being cast.
			playerIn.getCooldownTracker().setCooldown(this.getItem(), SpellCasting.CalculateSpellCooldown(spell, playerIn, result.summary) * 2);
		}

		if (itemStackIn.getDamage() > itemStackIn.getMaxDamage() // Old way, I think never happens?
				|| itemStackIn.isEmpty()) {
			// Going to break
			NostrumMagica.instance.getSpellRegistry().evict(spell);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		
    }
	
	public static void setSpell(ItemStack itemStack, Spell spell) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return;
		
		CompoundNBT nbt = itemStack.getTag();
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.putInt(NBT_SPELL, spell.getRegistryID());
		nbt.putInt(NBT_DURABILITY, GetMaxUses(spell));
		
		itemStack.setTag(nbt);
		itemStack.setDisplayName(new StringTextComponent(spell.getName()));
		itemStack.addEnchantment(Enchantments.POWER, 1);
	}
	
	public static Spell GetSpell(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellScroll))
			return null;
		
		CompoundNBT nbt = itemStack.getTag();		
		if (nbt == null)
			return null;
		
		int id = nbt.getInt(NBT_SPELL);
		Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
		
		if (spell == null) {
			if (NostrumMagica.instance.proxy.isServer()) {
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
		
		CompoundNBT nbt = itemStack.getTag();		
		if (nbt == null || !nbt.contains(NBT_DURABILITY, NBT.TAG_INT))
			return 15; // old default
		
		return Math.max(1, nbt.getInt(NBT_DURABILITY));
	}
	
	public static ItemStack create(Spell spell) {
		ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
		setSpell(scroll, spell);
		return scroll;
	}
	
	/**
	 * Figure out how many uses to give the provided spell.
	 * More complex spells have less uses per scroll.
	 */
	protected static final int GetMaxUses(Spell spell) {
		final int weight = (spell == null ? 0 : spell.getWeight());
		if (weight == 0) {
			return 100;
		} else if (weight == 1) {
			return 50;
		} else {
			return 25;
		}
	}
	
	protected int getCastDurabilityCost(PlayerEntity caster, Spell spell) {
		// By default, cost durability-1 of the scroll so that it has exactly 2 casts.
		// With skill, take a constant base here (5).
		// With another skill, take less constant.
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr != null) {
			if (attr.hasSkill(NostrumSkills.Spellcasting_ScrollSanity)) {
				int base = 5;
				if (attr.hasSkill(NostrumSkills.Spellcasting_ScrollEfficiency)) {
					base = 1;
				}
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
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		;
	}
	
	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
		Spell spell = GetSpell(stack);
		return spell == null ? false : spell.shouldTrace();
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		return getMaxDurability(stack);
	}

	@Override
	public double getTraceRange(World world, PlayerEntity player, ItemStack stack) {
		Spell spell = GetSpell(stack);
		return spell == null ? 0 : spell.getTraceRange();
	}

	@Override
	public Spell getSpell(ItemStack stack) {
		return GetSpell(stack);
	}
	
	@SubscribeEvent
	public void onSpellCast(SpellCastEvent.Post event) {
		// Notice and respond any time any spell is cast to it's cooldown.
		// Note that our r-click handler will actually replace this result with a larger one if it's a scroll that
		// cast the spell.
		final SpellCastResult result = event.getCastResult();
		if (result.succeeded && result.caster != null && result.caster instanceof PlayerEntity && !result.caster.getShouldBeDead()) {
			// Vulnerability: vanilla's tracker only returns us progress which means we can't REALLY check if our new
			// cooldown time is going to be less than what's already there.
			// If we blindly PUT, player's can get around long cooldowns by casting a short-cooldown spell after
			// a long one.
			// If we only put when there is no cooldown, player's can cast a short cooldown spell and then use it as immunity
			// if they cast a long-cooldown spell before then.
			// That seems like the harder thing to do, so opt to let that happen. Try to guess though at 25% of remaining as a
			// good time to let it be overriden.
			
			final int cooldownTicks = SpellCasting.CalculateSpellCooldown(result);
			if (((PlayerEntity) result.caster).getCooldownTracker().getCooldown(this.getItem(), 0f) <= .25f) {
				((PlayerEntity) result.caster).getCooldownTracker().setCooldown(this.getItem(), cooldownTicks);
			}
		}
	}
}
