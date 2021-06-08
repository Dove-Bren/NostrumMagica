package com.smanzana.nostrummagica.integration.baubles.items;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumaetheria.api.component.IAetherHandlerComponent;
import com.smanzana.nostrumaetheria.api.event.LivingAetherDrawEvent;
import com.smanzana.nostrumaetheria.api.event.LivingAetherDrawEvent.Phase;
import com.smanzana.nostrumaetheria.api.item.AetherItem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ICapeProvider;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface="baubles.api.IBauble", modid="Baubles")
public class ItemAetherCloak extends AetherItem implements ILoreTagged, ISpellArmor, IBauble, ICapeProvider {

	public static String ID = "aether_cloak";
//	private static final String NBT_AETHER_STORED = "aether_stored";
//	private static final String NBT_AETHER_MAX = "aether_max";
	private static final String NBT_AETHER_PROGRESS = "aether_progress";
	
	private static final int MAX_AETHER_MIN = 5000;
	private static final int MAX_AETHER_MAX = 100000;

	private static final ResourceLocation CapeModelTrimmed = new ResourceLocation(NostrumMagica.MODID, "entity/cloak.obj");
	private static final ResourceLocation CapeModelFull = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_medium.obj");
	
	public static void init() {
		instance().setUnlocalizedName(ID);
	}
	
	private static ItemAetherCloak instance = null;

	public static ItemAetherCloak instance() {
		if (instance == null)
			instance = new ItemAetherCloak();
	
		return instance;

	}
	
	public ItemAetherCloak() {
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setHasSubtypes(false);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack);
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		super.getSubItems(itemIn, tab, subItems);
		
		ItemStack full = new ItemStack(instance());
		
		IAetherHandlerComponent comp = this.getAetherHandler(full);
		comp.setMaxAether(MAX_AETHER_MAX);
		comp.setAether(MAX_AETHER_MAX);
		AetherItem.SaveItem(full);
		
//		tag.setInteger(NBT_AETHER_MAX, MAX_AETHER_MAX);
//		tag.setInteger(NBT_AETHER_STORED, MAX_AETHER_MAX);
		
		subItems.add(full);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_aether_cloak";
	}

	@Override
	public String getLoreDisplayName() {
		return "Aether Cloak";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		
		if (stack == null)
			return;
		
		if (I18n.hasKey("item.aether_cloak.desc")) {
			// Format with placeholders for blue and red formatting
			String translation = I18n.format("item.aether_cloak.desc", TextFormatting.GRAY, TextFormatting.BLUE, TextFormatting.DARK_RED);
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
		return BaubleType.BODY;
	}
	
	public static float GetAetherProgress(ItemStack stack) {
		if (stack == null) {
			return 0;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			return 0;
		}
		
		return nbt.getFloat(NBT_AETHER_PROGRESS);
	}
	
	protected static void SetAetherProgress(ItemStack stack, float progress) {
		if (stack == null) {
			return;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setFloat(NBT_AETHER_PROGRESS, progress);
		stack.setTagCompound(nbt);
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			return;
		}
		
		attr.addManaRegenModifier(0.25f);
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
		
		attr.addManaRegenModifier(-.25f);
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
		if (stack == null) {
			return;
		}
		
		if (!(stack.getItem() instanceof ItemAetherCloak)) {
			return;
		}
		
		summary.addEfficiency(.25f);
	}
	
	@Override
	@Optional.Method(modid="Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (stack == null) {
			return;
		}
		
		if (!(stack.getItem() instanceof ItemAetherCloak)) {
			return;
		}
		
	}
	
	@SubscribeEvent
	public void onAetherDraw(LivingAetherDrawEvent event) {
		// Aether cloak contributes aether after inventory items if it has any
		if (event.phase == Phase.BEFORE_LATE) {
			if (event.getAmtRemaining() > 0 && event.getEntity() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) event.getEntity();
				
				IInventory inv = NostrumMagica.baubles.getBaubles(player);
				if (inv != null) {
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						ItemStack stack = inv.getStackInSlot(i);
						if (stack == null || stack.getItem() != this)
							continue;
						
						NBTTagCompound nbt = stack.getTagCompound();
						if (nbt != null) {
							final int taken = this.deductAether(stack, event.getAmtRemaining());
							
							event.contributeAmt(taken);
							
							if (taken > 0) {
								growFromAether(stack, -taken);
							}
							
							if (event.isFinished()) {
								break; // no more needed
							}
						}
					}
				}
			}
		}
	}
	
	protected void growFromAether(ItemStack stack, int diff) {
		// Award more capacity any time aether is drawn from the cape
		if (diff < 0) {
			// for every 100 aether, award 5?
			float adv = (float) -diff / 20f + GetAetherProgress(stack);
			final int whole = (int) adv;
			adv -= whole;
			
			IAetherHandlerComponent comp = this.getAetherHandler(stack);
			int currentMax = Math.min(comp.getMaxAether(null) + whole, MAX_AETHER_MAX);
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			nbt.setFloat(NBT_AETHER_PROGRESS, adv);
			stack.setTagCompound(nbt);
			comp.setMaxAether(currentMax);
			AetherItem.SaveItem(stack);
		}
	}

	@Override
	protected int getDefaultMaxAether(ItemStack stack) {
		return MAX_AETHER_MIN;
	}

	@Override
	protected boolean shouldShowAether(ItemStack stack, EntityPlayer playerIn, boolean advanced) {
		return true;
	}

	@Override
	protected boolean shouldAutoFill(ItemStack stack, World worldIn, Entity entityIn) {
		return false;
	}

	@Override
	public boolean canBeDrawnFrom(@Nullable ItemStack stack, @Nullable World worldIn, Entity entityIn) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderCape(EntityLivingBase entity, ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemAetherCloak && this.getAether(stack) > 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getCapeModel(EntityLivingBase entity, ItemStack stack) {
		return CapeModelFull;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preRender(Entity entity, int model, VertexBuffer buffer, double x, double y, double z, float entityYaw,
			float partialTicks) {
		;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getCapeTexture(EntityLivingBase entity, ItemStack stack) {
		return null; // obj uses material and has prebaked texture
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldPreventOtherRenders(EntityLivingBase entity, ItemStack stack) {
		return true;
	}

}
