package com.smanzana.nostrummagica.integration.baubles.items;

import java.util.List;

import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface="baubles.api.IBauble", modid="Baubles")
public class ItemMagicBauble extends Item implements ILoreTagged, ISpellArmor, IBauble {

	public static enum ItemType {
		RIBBON_SMALL("ribbon_small"),
		RIBBON_MEDIUM("ribbon_medium"),
		RIBBON_LARGE("ribbon_large"),
		RIBBON_FIERCE("ribbon_fierce"),
		RIBBON_KIND("ribbon_kind"),
		BELT_LIGHTNING("belt_lightning"),
		BELT_ENDER("belt_ender"),
		RING_GOLD("ring_gold"),
		RING_GOLD_TRUE("ring_gold_true"),
		RING_GOLD_CORRUPTED("ring_gold_corrupted"),
		RING_SILVER("ring_silver"),
		RING_SILVER_TRUE("ring_silver_true"),
		RING_SILVER_CORRUPTED("ring_silver_corrupted"),
		TRINKET_FLOAT_GUARD("float_guard"),
		SHIELD_RING_SMALL("shield_ring_small"),
		SHIELD_RING_LARGE("shield_ring_large"),
		ELUDE_CAPE_SMALL("elude_cape_small");
		
		private String key;
		
		private ItemType(String key) {
			this.key = key;
		}
		
		public String getUnlocalizedKey() {
			return key;
		}
		
		private String getDescKey() {
			return "item." + key + ".desc";
		}
	}
	
	public static int getMetaFromType(ItemType type) {
    	return type.ordinal();
    }
    
    public static ItemType getTypeFromMeta(int meta) {
    	ItemType ret = null;
    	for (ItemType type : ItemType.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
	
	public static String ID = "ribbon";
	
	public static void init() {
		instance().setUnlocalizedName(ID);
		
//		GameRegistry.addRecipe(new ItemStack(instance), " W ", "WCW", " W ",
//				'W', Blocks.WOOL, 
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
	}
	
	private static ItemMagicBauble instance = null;

	public static ItemMagicBauble instance() {
		if (instance == null)
			instance = new ItemMagicBauble();
	
		return instance;

	}
	
	public static ItemStack getItem(ItemType type, int count) {
		int meta = getMetaFromType(type);
		
		return new ItemStack(instance(), count, meta);
	}
	

	public ItemMagicBauble() {
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		ItemType type = getTypeFromMeta(i);
		return "item." + type.getUnlocalizedKey();
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (ItemType type : ItemType.values()) {
			if (!NostrumMagica.aetheria.isEnabled()) {
				if (type == ItemType.SHIELD_RING_LARGE
						|| type == ItemType.SHIELD_RING_SMALL
						|| type == ItemType.ELUDE_CAPE_SMALL) {
					continue;
				}
			}
			subItems.add(new ItemStack(this, 1, getMetaFromType(type)));
		}
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		ItemType type = getTypeFromMeta(stack.getMetadata());
		if (type == null)
			return;
		
		if (I18n.hasKey(type.getDescKey())) {
			// Format with placeholders for blue and red formatting
			String translation = I18n.format(type.getDescKey(), TextFormatting.GRAY, TextFormatting.BLUE, TextFormatting.DARK_RED);
			if (translation.trim().isEmpty())
				return;
			String lines[] = translation.split("\\|");
			for (String line : lines) {
				tooltip.add(line);
			}
		}
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public BaubleType getBaubleType(ItemStack itemstack) {
		BaubleType btype = BaubleType.RING;
		ItemType type = getTypeFromMeta(itemstack.getMetadata());
		switch (type) {
		case BELT_ENDER:
		case BELT_LIGHTNING:
			btype = BaubleType.BELT;
			break;
		case RIBBON_LARGE:
		case RIBBON_MEDIUM:
		case RIBBON_SMALL:
		case RIBBON_FIERCE:
		case RIBBON_KIND:
			btype = BaubleType.AMULET;
			break;
		case RING_GOLD:
		case RING_GOLD_CORRUPTED:
		case RING_GOLD_TRUE:
		case RING_SILVER:
		case RING_SILVER_CORRUPTED:
		case RING_SILVER_TRUE:
		case SHIELD_RING_LARGE:
		case SHIELD_RING_SMALL:
			btype = BaubleType.RING;
			break;
		case TRINKET_FLOAT_GUARD:
			btype = BaubleType.CHARM;
			break;
		case ELUDE_CAPE_SMALL:
			btype = BaubleType.BODY;
			break;
		}
		
		return btype;
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		ItemType type = getTypeFromMeta(itemstack.getMetadata());
		switch (type) {
		case RIBBON_LARGE:
			attr.addManaBonus(600);
			break;
		case RIBBON_MEDIUM:
			attr.addManaBonus(250);
			break;
		case RIBBON_SMALL:
			attr.addManaBonus(100);
			break;
		case RIBBON_FIERCE:
			attr.addManaBonus(1000);
			attr.addManaRegenModifier(-.75f);
			break;
		case RIBBON_KIND:
			attr.addManaRegenModifier(1.5f);
			break;
		case BELT_ENDER:
			attr.addManaCostModifer(-0.01f);
			break;
		case BELT_LIGHTNING:
			attr.addManaRegenModifier(0.10f);
			break;
		case RING_GOLD:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_GOLD_CORRUPTED:
			; // Potency Handled on-cast. Not an attribute thing. Skip.
			attr.addManaCostModifer(-0.02f);
			break;
		case RING_GOLD_TRUE:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_SILVER:
			attr.addManaCostModifer(-0.025f);
			break;
		case RING_SILVER_CORRUPTED:
			attr.addManaCostModifer(-0.04f);
			; // Potency handled on-cast
			break;
		case RING_SILVER_TRUE:
			attr.addManaCostModifer(-0.05f);
			break;
		case TRINKET_FLOAT_GUARD:
			; // Checked upon floating
			break;
		case ELUDE_CAPE_SMALL:
			; // Checked upon attack
			break;
		case SHIELD_RING_LARGE:
		case SHIELD_RING_SMALL:
			; // Checked on equipped tick
			break;
		}
		
	}
	
	/**
	 * This method is called when the bauble is unequipped by a player
	 */
	@Override
	@Optional.Method(modid="Baubles")
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {	
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		ItemType type = getTypeFromMeta(itemstack.getMetadata());
		switch (type) {
		case RIBBON_LARGE:
			attr.addManaBonus(-600);
			break;
		case RIBBON_MEDIUM:
			attr.addManaBonus(-250);
			break;
		case RIBBON_SMALL:
			attr.addManaBonus(-100);
			break;
		case RIBBON_FIERCE:
			attr.addManaBonus(-1000);
			attr.addManaRegenModifier(.75f);
			break;
		case RIBBON_KIND:
			attr.addManaRegenModifier(-1.5f);
			break;
		case BELT_ENDER:
			attr.addManaCostModifer(0.01f);
			break;
		case BELT_LIGHTNING:
			attr.addManaRegenModifier(-0.10f);
			break;
		case RING_GOLD:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_GOLD_CORRUPTED:
			; // Potency Handled on-cast. Not an attribute thing. Skip.
			attr.addManaCostModifer(0.02f);
			break;
		case RING_GOLD_TRUE:
			; // Handled on-cast. Not an attribute thing. Skip.
			break;
		case RING_SILVER:
			attr.addManaCostModifer(0.025f);
			break;
		case RING_SILVER_CORRUPTED:
			attr.addManaCostModifer(0.04f);
			; // Potency handled on-cast
			break;
		case RING_SILVER_TRUE:
			attr.addManaCostModifer(0.05f);
			break;
		case TRINKET_FLOAT_GUARD:
			; // Checked upon floating
			break;
		case ELUDE_CAPE_SMALL:
			; // Checked upon attack
			break;
		case SHIELD_RING_LARGE:
		case SHIELD_RING_SMALL:
			; // Checked on equipped tick
			break;
		}
	}

	/**
	 * can this bauble be placed in a bauble slot
	 */
	@Override
	@Optional.Method(modid="Baubles")
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr != null && attr.isUnlocked();
	}

	@Override
	public void apply(EntityLivingBase caster, SpellCastSummary summary, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		
		if (!(stack.getItem() instanceof ItemMagicBauble)) {
			return;
		}
		
		ItemType type = getTypeFromMeta(stack.getMetadata());
		switch (type) {
		case BELT_ENDER:
		case BELT_LIGHTNING:
		case RIBBON_FIERCE:
		case RIBBON_KIND:
		case RIBBON_LARGE:
		case RIBBON_MEDIUM:
		case RIBBON_SMALL:
		case RING_SILVER:
		case RING_SILVER_TRUE:
		case TRINKET_FLOAT_GUARD:
		case ELUDE_CAPE_SMALL:
		case SHIELD_RING_LARGE:
		case SHIELD_RING_SMALL:
			; // Nothing to do
			break;
		case RING_GOLD:
			// Increase potency by 12.5%
			summary.addEfficiency(.125f);
			break;
		case RING_GOLD_CORRUPTED:
			summary.addEfficiency(.20f);
			break;
		case RING_GOLD_TRUE:
			summary.addEfficiency(.25f);
			break;
		case RING_SILVER_CORRUPTED:
			summary.addEfficiency(.10f);
			break;
		}
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (stack.isEmpty()) {
			return;
		}
		
		if (!(stack.getItem() instanceof ItemMagicBauble)) {
			return;
		}
		
		ItemType type = getTypeFromMeta(stack.getMetadata());
		switch (type) {
		case BELT_ENDER:
		case BELT_LIGHTNING:
		case RIBBON_FIERCE:
		case RIBBON_KIND:
		case RIBBON_LARGE:
		case RIBBON_MEDIUM:
		case RIBBON_SMALL:
		case RING_SILVER:
		case RING_SILVER_TRUE:
		case RING_GOLD:
		case RING_GOLD_CORRUPTED:
		case RING_GOLD_TRUE:
		case RING_SILVER_CORRUPTED:
			break;
		case TRINKET_FLOAT_GUARD:
			player.removePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
			break;
		case ELUDE_CAPE_SMALL:
			break;
		case SHIELD_RING_LARGE:
		case SHIELD_RING_SMALL:
			
			if (!player.world.isRemote) {
				double shield = (type == ItemType.SHIELD_RING_LARGE ? 4 : 2);
				int cost = (int) (shield * 100);
				
				// Check if we have enough aether and if the player is missing a shield
				if (player.ticksExisted % 40 == 0 && NostrumMagica.magicEffectProxy.getData(player, SpecialEffect.SHIELD_PHYSICAL) == null) {
					IInventory inv = null;
					if (player instanceof EntityPlayer) {
						inv = ((EntityPlayer) player).inventory;
					}
					
					if (inv != null) {
						int taken = APIProxy.drawFromInventory(player.world, player, inv, cost, stack);
						if (taken > 0) {
							// Apply shields! Amount depends on how much aether was consumed
							shield *= ((float) taken / (float) cost);
							NostrumMagica.magicEffectProxy.applyPhysicalShield(player, shield);
						}
					}
				}
			}
			
			break;
		}
	}
	
	@SubscribeEvent
	public void onAttack(LivingAttackEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getAmount() > 0f && event.getEntityLiving() instanceof EntityPlayer && event.getSource() instanceof EntityDamageSource) {
			Entity source = ((EntityDamageSource) event.getSource()).getTrueSource();
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			IInventory inv = NostrumMagica.baubles.getBaubles(player);
			if (inv != null) {
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicBauble))
						continue;
					
					ItemType type = getTypeFromMeta(stack.getMetadata());
					switch (type) {
					case BELT_ENDER:
					case BELT_LIGHTNING:
					case RIBBON_FIERCE:
					case RIBBON_KIND:
					case RIBBON_LARGE:
					case RIBBON_MEDIUM:
					case RIBBON_SMALL:
					case RING_SILVER:
					case RING_SILVER_TRUE:
					case TRINKET_FLOAT_GUARD:
					case SHIELD_RING_LARGE:
					case SHIELD_RING_SMALL:
					case RING_GOLD:
					case RING_GOLD_CORRUPTED:
					case RING_GOLD_TRUE:
					case RING_SILVER_CORRUPTED:
						break;
					case ELUDE_CAPE_SMALL:
						
						float chance = .15f;
						int cost = 150;
						
						// Check to see if we're facing the enemy that attacked us
						Vec3d attackFrom = source.getPositionVector().subtract(player.getPositionVector());
						double attackFromYaw = -Math.atan2(attackFrom.x, attackFrom.z) * 180.0F / (float)Math.PI;
						
						if (Math.abs(((player.rotationYaw + 360f) % 360f) - ((attackFromYaw + 360f) % 360f)) < 30f) {
							if (NostrumMagica.rand.nextFloat() < chance) {
								// If there's aether, dodge!
								int taken = APIProxy.drawFromInventory(player.world, player, player.inventory, cost, stack);
								if (taken > 0) {
									// Dodge!
									event.setCanceled(true);
									NostrumMagicaSounds.DAMAGE_WIND.play(player.world, player.posX, player.posY, player.posZ);
									float dir = player.rotationYaw + (NostrumMagica.rand.nextBoolean() ? -1 : 1) * 90f;
									float velocity = .5f;
									player.motionX = velocity * MathHelper.cos(dir);
									player.motionZ = velocity * MathHelper.sin(dir);
									player.velocityChanged = true;
								}
							}
						}
						break;
					}
				}
			}
		}
	}
}
