package com.smanzana.nostrummagica.items.equipment;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.ISpellActionListener;
import com.smanzana.nostrummagica.listeners.PlayerListener.SpellActionListenerData;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MirrorShield extends ShieldItem implements ISpellActionListener, ILoreTagged {

	public static final String ID = "mirror_shield";
	public static final UUID MOD_ATTACK_UUID = UUID.fromString("522BB274-43321-56AA-20AE-254BBB743ABB");
	public static final UUID MOD_RESIST_UUID = UUID.fromString("433CC363-43321-56AA-20AE-254BBB743ABB");
	
	public MirrorShield() {
		this(NostrumItems.PropEquipment().rarity(Rarity.UNCOMMON).maxDamage(750));
	}
	
	protected MirrorShield(Item.Properties properties) {
		super(properties);
		NostrumMagica.playerListener.registerMagicEffect(this, null);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlotType.OFFHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(Attributes.ARMOR, new AttributeModifier(MOD_ATTACK_UUID, "Offhand Modifier", 1, AttributeModifier.Operation.ADDITION));
			builder.put(NostrumAttributes.magicResist, new AttributeModifier(MOD_RESIST_UUID, "Magic Shield Resist", 10, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.CrystalMedium.contains(repair.getItem());
		}
    }
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000; // Maybe make longer/shorter?
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		playerIn.setActiveHand(hand);
		
		NostrumMagica.playerListener.registerMagicEffect(this, null);
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public boolean onEvent(Event type, LivingEntity entity, SpellActionListenerData data) {
		
		if (type == Event.MAGIC_EFFECT) {
			if (entity.isActiveItemStackBlocking() && entity.getActiveItemStack().getItem() instanceof MirrorShield) {
				ItemStacks.damageItem(entity.getActiveItemStack(), entity, entity.getActiveHand(), NostrumMagica.rand.nextInt(2) + 1);
				
				// If there was a caster, reflect part of the spell back
				if (data.caster != null && data.caster != entity) {
					data.summary.getAction().apply(entity, data.caster, 0.2f);
				}
				
				// Regardless, reduce efficiency
				data.summary.addEfficiency(-0.2f);
			}
		}
		
		return false;
	}

	@Override
	public String getLoreKey() {
		return "mirror_shield";
	}

	@Override
	public String getLoreDisplayName() {
		return I18n.format("lore." + getLoreKey() + ".name");
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A sturdy shield with a mirror magically created from glass and Vani Crystal dust affixed to the front.", "Passively reduces magic damage when in your offhand.", "Reflects some portion of a spell's effects back to the caster when blocking!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A sturdy shield with a mirror magically created from glass and Vani Crystal dust affixed to the front.", "Passively reduces magic damage by 10% when in your offhand.", "Reflects 20% of a spell's effects back to the caster when blocking!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		return;
	}
	
	@Override
	public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
		return true;
	}
	
//	@Override
//	public ITextComponent getDisplayName(ItemStack stack) {
//		// ItemShield hardcodes item.shield.name lol
//		return NostrumMagica.instance.proxy.getTranslation(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
//	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelBlocking(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		// Copied from vanilla
		return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
	}

}
