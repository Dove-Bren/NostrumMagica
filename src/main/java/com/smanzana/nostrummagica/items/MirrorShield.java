package com.smanzana.nostrummagica.items;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.ISpellActionListener;
import com.smanzana.nostrummagica.listeners.PlayerListener.SpellActionListenerData;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MirrorShield extends Item implements ISpellActionListener {

	private static MirrorShield instance = null;
	
	public static MirrorShield instance() {
		if (instance == null)
			instance = new MirrorShield();
		
		return instance;
	}
	
	public static final String id = "mirror_shield";
	public static final UUID MOD_ATTACK_UUID = UUID.fromString("522BB274-43321-56AA-20AE-254BBB743ABB");
	public static final UUID MOD_RESIST_UUID = UUID.fromString("433CC363-43321-56AA-20AE-254BBB743ABB");
	
	private MirrorShield() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setMaxDamage(750);
		
		this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
			}
		});
		
		NostrumMagica.playerListener.registerMagicEffect(this, null);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (equipmentSlot == EntityEquipmentSlot.OFFHAND) {
			multimap.put(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName(), new AttributeModifier(MOD_ATTACK_UUID, "Offhand Modifier", 1, 0));
			multimap.put(AttributeMagicResist.instance().getAttributeUnlocalizedName(), new AttributeModifier(MOD_RESIST_UUID, "Magic Shield Resist", 10, 0));
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
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		playerIn.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
	}

	@Override
	public boolean onEvent(Event type, EntityLivingBase entity, SpellActionListenerData data) {
		
		if (type == Event.MAGIC_EFFECT) {
			if (entity.isActiveItemStackBlocking() && entity.getActiveItemStack().getItem() instanceof MirrorShield) {
				entity.getActiveItemStack().damageItem(NostrumMagica.rand.nextInt(3) + 1, entity);
				
				// If there was a caster, reflect part of the spell back
				if (data.caster != null) {
					data.summary.getAction().apply(data.caster, 0.2f);
				}
				
				// Regardless, reduce efficiency
				data.summary.addEfficiency(-0.2f);
			}
		}
		
		return false;
	}

}
