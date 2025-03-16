package com.smanzana.nostrummagica.item.equipment;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.ISpellActionListener;
import com.smanzana.nostrummagica.listener.PlayerListener.SpellActionListenerData;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MirrorShield extends ShieldItem implements ISpellActionListener, ILoreTagged {

	public static final String ID = "mirror_shield";
	public static final UUID MOD_ATTACK_UUID = UUID.fromString("cc76c300-4ed7-4428-9c0f-596dd02a9469");
	public static final UUID MOD_RESIST_UUID = UUID.fromString("433CC363-4321-56AA-20AE-254BBB743ABB");
	
	public MirrorShield() {
		this(NostrumItems.PropEquipment().rarity(Rarity.UNCOMMON).durability(750));
	}
	
	protected MirrorShield(Item.Properties properties) {
		super(properties);
		NostrumMagica.playerListener.registerMagicEffect(this, null);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlot.OFFHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(Attributes.ARMOR, new AttributeModifier(MOD_ATTACK_UUID, "Offhand Modifier", 1, AttributeModifier.Operation.ADDITION));
			builder.put(NostrumAttributes.magicResist, new AttributeModifier(MOD_RESIST_UUID, "Magic Shield Resist", 10, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.CrystalMedium.contains(repair.getItem());
		}
    }
	
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000; // Maybe make longer/shorter?
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		playerIn.startUsingItem(hand);
		
		NostrumMagica.playerListener.registerMagicEffect(this, null);
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, playerIn.getItemInHand(hand));
	}

	@Override
	public boolean onEvent(Event type, LivingEntity entity, SpellActionListenerData data) {
		
		if (type == Event.MAGIC_EFFECT) {
			if (entity.isBlocking() && entity.getUseItem().getItem() instanceof MirrorShield) {
				ItemStacks.damageItem(entity.getUseItem(), entity, entity.getUsedItemHand(), NostrumMagica.rand.nextInt(2) + 1);
				
				// If there was a caster, reflect part of the spell back
				if (data.caster != null && data.caster != entity) {
					data.summary.getAction().apply(entity, data.caster, 0.2f, ISpellLogBuilder.Dummy);
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
		return I18n.get("lore." + getLoreKey() + ".name");
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
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
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
	public static final float ModelBlocking(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn) {
		// Copied from vanilla
		return entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack ? 1.0F : 0.0F;
	}

}
