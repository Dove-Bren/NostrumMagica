package com.smanzana.nostrummagica.integration.curios.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class NostrumCurio extends Item implements INostrumCurio, ILoreTagged, ISpellEquipment {
	
	public static final String ID_RIBBON_SMALL = "ribbon_small";
	public static final String ID_RIBBON_MEDIUM = "ribbon_medium";
	public static final String ID_RIBBON_LARGE = "ribbon_large";
	public static final String ID_RIBBON_FIERCE = "ribbon_fierce";
	public static final String ID_RIBBON_KIND = "ribbon_kind";
	public static final String ID_BELT_LIGHTNING = "belt_lightning";
	public static final String ID_BELT_ENDER = "belt_ender";
	public static final String ID_RING_GOLD = "ring_gold";
	public static final String ID_RING_GOLD_TRUE = "ring_gold_true";
	public static final String ID_RING_GOLD_CORRUPTED = "ring_gold_corrupted";
	public static final String ID_RING_SILVER = "ring_silver";
	public static final String ID_RING_SILVER_TRUE = "ring_silver_true";
	public static final String ID_RING_SILVER_CORRUPTED = "ring_silver_corrupted";

	private boolean requiresMagic; // To equip
	
	private int manaBonus;
	private float manaRegenModifier;
	private float manaCostModifier;
	private float castEfficiency;
	private UUID attribID;
	
	private final String desckey;
	
	public NostrumCurio(Item.Properties builder, String descKey) {
		super(builder);
		this.desckey = descKey;
	}
	
	public NostrumCurio requiresMagic() {
		this.requiresMagic = true;
		return this;
	}
	
	public NostrumCurio attrID(UUID id) {
		this.attribID = id;
		return this;
	}

	public NostrumCurio manaBonus(int manaBonus) {
		Validate.notNull(this.attribID);
		this.manaBonus = manaBonus;
		return this;
	}

	public NostrumCurio manaRegenModifier(float manaRegenModifier) {
		Validate.notNull(this.attribID);
		this.manaRegenModifier = manaRegenModifier;
		return this;
	}

	public NostrumCurio manaCostModifier(float manaCostModifier) {
		Validate.notNull(this.attribID);
		this.manaCostModifier = manaCostModifier;
		return this;
	}

	public NostrumCurio castEfficiency(float castEfficiency) {
		this.castEfficiency = castEfficiency;
		return this;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		if (this.desckey == null) {
			return;
		}
		
		final String trans = this.getDefaultTranslationKey() + ".desc";
		
		if (!I18n.hasKey(trans)) {
			return;
		}
		
		// Format with placeholders for blue and red formatting
		String translation = I18n.format(trans, TextFormatting.GRAY, TextFormatting.BLUE, TextFormatting.DARK_RED);
		if (translation.trim().isEmpty())
			return;
		String lines[] = translation.split("\\|");
		for (String line : lines) {
			tooltip.add(new StringTextComponent(line));
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return INostrumCurio.initCapabilities(stack, nbt);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_baubles";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magic Baubles";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By imbuing raw materials with magical reagents, you've discovered a way to created small baubles that enhance your magical powers!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By imbuing raw materials with magical reagents, you've discovered a way to created small baubles that enhance your magical powers!", "Cloth and precious metals seem to be especially willing to be imbued. While leather hasn't proven the same, you're sure you can affix a crystal or two to make it work!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		;
	}

	@Override
	public void onEquipped(ItemStack stack, LivingEntity entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null) {
			return;
		}
		
		if (this.manaBonus != 0) attr.addManaBonus(this.attribID, this.manaBonus);
		if (this.manaRegenModifier != 0) attr.addManaRegenModifier(this.attribID, this.manaRegenModifier);
		if (this.manaCostModifier != 0) attr.addManaCostModifier(this.attribID, this.manaCostModifier);
		
		if (entity instanceof ServerPlayerEntity) {
			NetworkHandler.sendTo(
					new StatSyncMessage(attr), (ServerPlayerEntity) entity);
		}
		
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity entity) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
		if (attr == null) {
			return;
		}
		
		attr.removeManaBonus(this.attribID);
		attr.removeManaRegenModifier(this.attribID);
		attr.removeManaCostModifier(this.attribID);
		
		if (entity instanceof ServerPlayerEntity) {
			NetworkHandler.sendTo(
					new StatSyncMessage(attr), (ServerPlayerEntity) entity);
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		if (entity.world.isRemote && entity != NostrumMagica.instance.proxy.getPlayer()) {
			return true; // Auto allow for other entities on client side when the server says they have them
		}
		
		if (this.requiresMagic) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(entity);
			if (attr == null || !attr.isUnlocked()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		summary.addEfficiency(this.castEfficiency);
	}

	@Override
	public Multimap<Attribute,AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		return HashMultimap.create();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return false;
//		return !(stack.getItem() instanceof ICosmeticAttachable && !((ICosmeticAttachable) stack.getItem()).getCosmeticItem(stack).isEmpty())
//				&& !(stack.getItem() instanceof IPhantomInkable && ((IPhantomInkable) stack.getItem()).hasPhantomInk(stack))
//				&& ConfigHandler.CLIENT.renderAccessories.get()
//				&& living.getActivePotionEffect(Effects.INVISIBILITY) == null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void doRender(ItemStack stack, MatrixStack matrixStackIn, int index, IRenderTypeBuffer bufferIn, int packedLightIn, LivingEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {}
	
}
