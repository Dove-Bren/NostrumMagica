package com.smanzana.nostrummagica.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.ISpellActionListener;
import com.smanzana.nostrummagica.listeners.PlayerListener.SpellActionListenerData;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class MirrorShield extends ItemShield implements ISpellActionListener, ILoreTagged {

	private static MirrorShield instance = null;
	
	public static MirrorShield instance() {
		if (instance == null)
			instance = new MirrorShield(ID);
		
		return instance;
	}
	
	public static final String ID = "mirror_shield";
	public static final UUID MOD_ATTACK_UUID = UUID.fromString("522BB274-43321-56AA-20AE-254BBB743ABB");
	public static final UUID MOD_RESIST_UUID = UUID.fromString("433CC363-43321-56AA-20AE-254BBB743ABB");
	
	protected MirrorShield(final String id) {
		super();
		this.setUnlocalizedName(id);
		this.setRegistryName(NostrumMagica.MODID, id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setMaxDamage(750);
		
//		this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
//			@OnlyIn(Dist.CLIENT)
//			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
//				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
//			}
//		});
		
		NostrumMagica.playerListener.registerMagicEffect(this, null);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (equipmentSlot == EntityEquipmentSlot.OFFHAND) {
			multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(MOD_ATTACK_UUID, "Offhand Modifier", 1, 0));
			multimap.put(AttributeMagicResist.instance().getName(), new AttributeModifier(MOD_RESIST_UUID, "Magic Shield Resist", 10, 0));
		}

		return multimap;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000; // Maybe make longer/shorter?
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		
		NostrumMagica.playerListener.registerMagicEffect(this, null);
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public boolean onEvent(Event type, LivingEntity entity, SpellActionListenerData data) {
		
		if (type == Event.MAGIC_EFFECT) {
			if (entity.isActiveItemStackBlocking() && entity.getActiveItemStack().getItem() instanceof MirrorShield) {
				entity.getActiveItemStack().damageItem(NostrumMagica.rand.nextInt(2) + 1, entity);
				
				// If there was a caster, reflect part of the spell back
				if (data.caster != null && data.caster != entity) {
					data.summary.getAction().apply(data.caster, 0.2f);
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
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		return;
	}
	
	@Override
	public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
		return true;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		// ItemShield hardcodes item.shield.name lol
		return NostrumMagica.proxy.getTranslation(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
	}

}
