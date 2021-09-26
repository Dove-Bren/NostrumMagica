package com.smanzana.nostrummagica.items;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.listeners.PlayerListener.SpellActionListenerData;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Dumb name to keep it sorted. Worth?
public class MirrorShieldImproved extends MirrorShield {

	private static MirrorShieldImproved instance = null;
	
	public static MirrorShieldImproved instance() {
		if (instance == null)
			instance = new MirrorShieldImproved();
		
		return instance;
	}
	
	public static final String id = "true_mirror_shield";
	private static final String NBT_CHARGED = "charged";
	
	public static final float CHARGE_CHANCE = 0.25f;
	
	private MirrorShieldImproved() {
		super(MirrorShieldImproved.id);
		this.setUnlocalizedName(id);
		this.setMaxDamage(1250);
		
		this.addPropertyOverride(new ResourceLocation("charged"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return getBlockCharged(stack) ? 1.0F : 0.0F;
			}
		});
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (equipmentSlot == EntityEquipmentSlot.OFFHAND) {
			multimap.put(AttributeMagicResist.instance().getName(), new AttributeModifier(MOD_RESIST_UUID, "Magic Shield Resist", 20, 0));
		}

		return multimap;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000; // Maybe make longer/shorter?
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		return super.onItemRightClick(worldIn, playerIn, hand);
	}

	@Override
	public boolean onEvent(Event type, EntityLivingBase entity, SpellActionListenerData data) {
		
		if (type == Event.MAGIC_EFFECT) {
			if (entity.isActiveItemStackBlocking() && entity.getActiveItemStack().getItem() instanceof MirrorShieldImproved) {
				entity.getActiveItemStack().damageItem(NostrumMagica.rand.nextInt(2) + 1, entity);
				
				float reduc = 0.3f;
				
				if (getBlockCharged(entity.getActiveItemStack())) {
					markBlockCharged(entity.getActiveItemStack(), false);
					reduc = 1f;
				}
				
				// If there was a caster, reflect part of the spell back
				if (data.caster != null && data.caster != entity) {
					data.summary.getAction().apply(data.caster, reduc);
				}
				
				// If fully blocked, cancel. Otherwise, reduce
				if (reduc >= 1f)
					data.summary.cancel();
				else
					data.summary.addEfficiency(-reduc);
			} else {
				if (!entity.getHeldItemOffhand().isEmpty() && entity.getHeldItemOffhand().getItem() instanceof MirrorShieldImproved) {
					// If holding mirror shield in offhand but not actively blocking, have chance of charging
					if (!getBlockCharged(entity.getHeldItemOffhand()) && itemRand.nextFloat() < CHARGE_CHANCE)
						markBlockCharged(entity.getHeldItemOffhand(), true);
				}
			}
		}
		
		return false;
	}

	@Override
	public String getLoreKey() {
		return "true_mirror_shield";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A Mirror Shield further refined to block and reflect magic damage better.", "This shield blocks and reflects more damage than the regular Mirror Shield!", "However, it appears to have lost its defensive bonus.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A Mirror Shield further refined to block and reflect magic damage better.", "This shield passively blocks 20% of magic damage, and reduces+reflects an additional 30% while blocking.", "Additionally, it charges off of the magic it absorbs when not blocking.", "Once charged, it can completely reflect spell effects when used to block a spell!");
	}
	
	// TODO another shield that absorbs mana instead of reflecting?

	public static boolean getBlockCharged(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MirrorShieldImproved)) {
			return false;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && nbt.hasKey(NBT_CHARGED)) {
			return nbt.getBoolean(NBT_CHARGED);
		}
		
		return false;
	}
	
	public static void markBlockCharged(ItemStack stack, boolean charged) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MirrorShieldImproved)) {
			return;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_CHARGED, charged);
		stack.setTagCompound(nbt);
	}

}
