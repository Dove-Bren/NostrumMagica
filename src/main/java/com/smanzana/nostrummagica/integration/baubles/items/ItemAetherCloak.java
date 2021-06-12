package com.smanzana.nostrummagica.integration.baubles.items;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumaetheria.api.component.IAetherHandlerComponent;
import com.smanzana.nostrumaetheria.api.event.LivingAetherDrawEvent;
import com.smanzana.nostrumaetheria.api.event.LivingAetherDrawEvent.Phase;
import com.smanzana.nostrumaetheria.api.item.AetherItem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ICapeProvider;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ColorUtil;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Optional.Interface(iface="baubles.api.IBauble", modid="Baubles")
public class ItemAetherCloak extends AetherItem implements ILoreTagged, ISpellArmor, IBauble, ICapeProvider {

	public static String ID = "aether_cloak";
	private static final String NBT_AETHER_PROGRESS = "aether_progress";
	private static final String NBT_AETHER_SPENDER = "aether_spender";
	private static final String NBT_DISPLAY_WINGS = "display_wings";
	private static final String NBT_DISPLAY_TRIMMED = "display_trimmed";
	private static final String NBT_DISPLAY_COLOR_OUTSIDE = "color_outside";
	private static final String NBT_DISPLAY_COLOR_INSIDE = "color_inside";
	private static final String NBT_DISPLAY_RUNES = "display_runes";
	private static final String NBT_DISPLAY_COLOR_RUNES = "color_runes";
	
	private static final int MAX_AETHER_MIN = 5000;
	private static final int MAX_AETHER_MAX = 100000;
	
	public static final EnumDyeColor COLOR_DEFAULT_OUTSIDE = EnumDyeColor.BLUE;
	public static final EnumDyeColor COLOR_DEFAULT_INSIDE = EnumDyeColor.GRAY;
	public static final EnumDyeColor COLOR_DEFAULT_RUNES = EnumDyeColor.WHITE;

	private static final ResourceLocation CapeModelTrimmedOutside = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_trimmed_outside.obj");
	private static final ResourceLocation CapeModelTrimmedInside = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_trimmed_inside.obj");
	private static final ResourceLocation CapeModelTrimmedDecor = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_trimmed_decor.obj");
	private static final ResourceLocation CapeModelFullOutside = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_medium_outside.obj");
	private static final ResourceLocation CapeModelFullInside = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_medium_inside.obj");
	private static final ResourceLocation CapeModelFullDecor = new ResourceLocation(NostrumMagica.MODID, "entity/cloak_medium_decor.obj");
	
	private static final ResourceLocation[] CapeModelsTrimmed = new ResourceLocation[] {
		CapeModelTrimmedInside,
		CapeModelTrimmedOutside,
	};

	private static final ResourceLocation[] CapeModelsTrimmedDecor = new ResourceLocation[] {
		CapeModelTrimmedInside,
		CapeModelTrimmedOutside,
		CapeModelTrimmedDecor,
	};
	
	private static final ResourceLocation[] CapeModelsFull = new ResourceLocation[] {
		CapeModelFullInside,
		CapeModelFullOutside,
	};
	
	private static final ResourceLocation[] CapeModelsFullDecor = new ResourceLocation[] {
		CapeModelFullInside,
		CapeModelFullOutside,
		CapeModelFullDecor,
	};
	
	public static void init() {
		instance().setUnlocalizedName(ID);
		
		// Inside color (1 dye)
		ItemStack insideStack = new ItemStack(instance());
		instance().setInsideColor(insideStack, EnumDyeColor.BLACK);
		GameRegistry.addRecipe(new ShapelessRecipes(insideStack, Lists.newArrayList(
				new ItemStack(instance()),
				new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE)
				)) {

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack cloak = null;
				ItemStack dye = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						cloak = stack;
						if (dye != null) {
							break;
						}
					} else if (stack != null && stack.getItem() instanceof ItemDye) {
						dye = stack;
						if (cloak != null) {
							break;
						}
					}
				}
				
				if (cloak != null) {
					cloak = cloak.copy();
					EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(dye.getMetadata());
					instance().setInsideColor(cloak, dyeColor);
				}
				
				return cloak;
			}
		});
		
		// Outside color (2 dye)
		ItemStack outsideStack = new ItemStack(instance());
		instance().setOutsideColor(outsideStack, EnumDyeColor.BLACK);
		GameRegistry.addRecipe(new ShapelessRecipes(outsideStack, Lists.newArrayList(
				new ItemStack(instance()),
				new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE)
				)) {
			
			@Override
			public boolean matches(InventoryCrafting inv, World worldIn) {
				// Require both dyes to be the same
				ItemStack cloak = null;
				ItemStack dye1 = null;
				ItemStack dye2 = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						if (cloak != null) {
							return false;
						}
						cloak = stack;
					} else if (stack != null && stack.getItem() instanceof ItemDye) {
						if (dye1 == null) {
							dye1 = stack;
						} else if (dye2 == null) {
							dye2 = stack;
							EnumDyeColor dyeColor1 = EnumDyeColor.byDyeDamage(dye1.getMetadata());
							EnumDyeColor dyeColor2 = EnumDyeColor.byDyeDamage(dye2.getMetadata());
							if (dyeColor1 != dyeColor2) {
								return false; // different colors!
							}
						} else {
							return false; // too many dyes!
						}
					}
				}
				
				return cloak != null && dye1 != null && dye2 != null;
			}

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack cloak = null;
				ItemStack dye = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						cloak = stack;
						if (dye != null) {
							break;
						}
					} else if (stack != null && stack.getItem() instanceof ItemDye) {
						dye = stack;
						if (cloak != null) {
							break;
						}
					}
				}
				
				if (cloak != null) {
					cloak = cloak.copy();
					EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(dye.getMetadata());
					instance().setOutsideColor(cloak, dyeColor);
				}
				
				return cloak;
			}
		});
		
		// Rune color (1 dye + nether quartz)
		ItemStack runeColorStack = new ItemStack(instance());
		instance().setRuneColor(runeColorStack, EnumDyeColor.BLACK);
		GameRegistry.addRecipe(new ShapelessRecipes(runeColorStack, Lists.newArrayList(
				new ItemStack(instance()),
				new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(Items.QUARTZ)
				)) {

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack cloak = null;
				ItemStack dye = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						cloak = stack;
						if (dye != null) {
							break;
						}
					} else if (stack != null && stack.getItem() instanceof ItemDye) {
						dye = stack;
						if (cloak != null) {
							break;
						}
					}
				}
				
				if (cloak != null) {
					cloak = cloak.copy();
					EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(dye.getMetadata());
					instance().setRuneColor(cloak, dyeColor);
				}
				
				return cloak;
			}
		});
		
		// Wing holes (Shears)
		ItemStack wingStack = new ItemStack(instance());
		instance().setDisplayWings(wingStack, true);
		GameRegistry.addRecipe(new ShapelessRecipes(wingStack, Lists.newArrayList(
				new ItemStack(instance()),
				new ItemStack(Items.SHEARS, 1, OreDictionary.WILDCARD_VALUE)
				)) {

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack cloak = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						cloak = stack;
						break;
					}
				}
				
				if (cloak != null) {
					cloak = cloak.copy();
					instance().setDisplayWings(cloak, true);
				}
				
				return cloak;
			}
		});
		
		// Trimmed design (2xShears)
		ItemStack trimmedStack = new ItemStack(instance());
		instance().setDisplayTrimmed(trimmedStack, true);
		GameRegistry.addRecipe(new ShapelessRecipes(trimmedStack, Lists.newArrayList(
				new ItemStack(instance()),
				new ItemStack(Items.SHEARS, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(Items.SHEARS, 1, OreDictionary.WILDCARD_VALUE)
				)) {

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack cloak = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						cloak = stack;
						break;
					}
				}
				
				if (cloak != null) {
					cloak = cloak.copy();
					instance().setDisplayTrimmed(cloak, true);
				}
				
				return cloak;
			}
		});
		
		// Show runes (quartz + mani dust)
		ItemStack runedStack = new ItemStack(instance());
		instance().setDisplayRunes(runedStack, true);
		GameRegistry.addRecipe(new ShapelessRecipes(runedStack, Lists.newArrayList(
				new ItemStack(instance()),
				new ItemStack(Items.QUARTZ),
				ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1)
				)) {

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack cloak = null;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack != null && stack.getItem() instanceof ItemAetherCloak) {
						cloak = stack;
						break;
					}
				}
				
				if (cloak != null) {
					cloak = cloak.copy();
					instance().setDisplayRunes(cloak, true);
				}
				
				return cloak;
			}
		});
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
		//super.getSubItems(itemIn, tab, subItems);
		
		ItemStack basic = new ItemStack(instance());
		subItems.add(basic);
		
		ItemStack full = new ItemStack(instance());
		
		IAetherHandlerComponent comp = this.getAetherHandler(full);
		comp.setMaxAether(MAX_AETHER_MAX);
		comp.setAether(MAX_AETHER_MAX);
		AetherItem.SaveItem(full);
		
//		tag.setInteger(NBT_AETHER_MAX, MAX_AETHER_MAX);
//		tag.setInteger(NBT_AETHER_STORED, MAX_AETHER_MAX);
		
		subItems.add(full);
		
		ItemStack complete = new ItemStack(instance());
		comp = this.getAetherHandler(complete);
		comp.setMaxAether(MAX_AETHER_MAX);
		comp.setAether(MAX_AETHER_MAX);
		AetherItem.SaveItem(complete);
		setRuneColor(complete, EnumDyeColor.RED);
		setOutsideColor(complete, EnumDyeColor.BLACK);
		setInsideColor(complete, EnumDyeColor.PINK);
		setDisplayWings(complete, true);
		setDisplayRunes(complete, true);
		setDisplayTrimmed(complete, true);
		setAetherCaster(complete, true);
		subItems.add(complete);
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
		
		final boolean displayRunes = getDisplayRunes(stack);
		final boolean displayWings = getDisplayWings(stack);
		final boolean displayTrimmed = getDisplayTrimmed(stack);
		EnumDyeColor colorRunes = getRuneColor(stack);
		EnumDyeColor colorOutside = getOutsideColor(stack);
		EnumDyeColor colorInside = getInsideColor(stack);
		
		if (displayTrimmed) {
			tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.aether_cloak.trimmed") + TextFormatting.RESET);
		}
		if (displayRunes) {
			tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.aether_cloak.runed") + TextFormatting.RESET);
		}
		if (displayWings) {
			tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.aether_cloak.wings") + TextFormatting.RESET);
		}
		if (colorOutside != COLOR_DEFAULT_OUTSIDE) {
			String name = I18n.format(colorOutside.getUnlocalizedName());
			name = name.toLowerCase();
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			tooltip.add(TextFormatting.DARK_BLUE + I18n.format("item.aether_cloak.color.outside", name) + TextFormatting.RESET);
		}
		if (colorInside != COLOR_DEFAULT_INSIDE) {
			String name = I18n.format(colorInside.getUnlocalizedName());
			name = name.toLowerCase();
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			tooltip.add(TextFormatting.DARK_BLUE + I18n.format("item.aether_cloak.color.inside", name) + TextFormatting.RESET);
		}
		if (colorRunes != COLOR_DEFAULT_RUNES) {
			String name = I18n.format(colorRunes.getUnlocalizedName());
			name = name.toLowerCase();
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			tooltip.add(TextFormatting.DARK_BLUE + I18n.format("item.aether_cloak.color.runes", name) + TextFormatting.RESET);
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
		if (player.worldObj.isRemote && player != NostrumMagica.proxy.getPlayer()) {
			return true;
		}
		
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
		
		if (isAetherCaster(stack) && summary.getReagentCost() > 0f) {
			// Attempt to spend aether to cover reagent cost
			// 100 aether for full cast
			final int cost = (int) Math.ceil(100 * summary.getReagentCost());
			if (this.getAether(stack) >= cost) {
				if ((!(caster instanceof EntityPlayer) || !((EntityPlayer) caster).isCreative()) && !caster.worldObj.isRemote) {
					this.deductAether(stack, cost);
				}
				summary.addReagentCost(-summary.getReagentCost());
			}
		}
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
	
	public EnumDyeColor getRuneColor(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_DISPLAY_COLOR_RUNES)) {
			return COLOR_DEFAULT_RUNES;
		}
		
		int colorIdx = nbt.getInteger(NBT_DISPLAY_COLOR_RUNES);
		return EnumDyeColor.values()[colorIdx % EnumDyeColor.values().length];
	}
	
	public void setRuneColor(ItemStack stack, EnumDyeColor color) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setInteger(NBT_DISPLAY_COLOR_RUNES, color.ordinal());
		stack.setTagCompound(nbt);
	}
	
	public EnumDyeColor getOutsideColor(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_DISPLAY_COLOR_OUTSIDE)) {
			return COLOR_DEFAULT_OUTSIDE;
		}
		
		int colorIdx = nbt.getInteger(NBT_DISPLAY_COLOR_OUTSIDE);
		return EnumDyeColor.values()[colorIdx % EnumDyeColor.values().length];
	}
	
	public void setOutsideColor(ItemStack stack, EnumDyeColor color) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setInteger(NBT_DISPLAY_COLOR_OUTSIDE, color.ordinal());
		stack.setTagCompound(nbt);
	}

	public EnumDyeColor getInsideColor(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_DISPLAY_COLOR_INSIDE)) {
			return COLOR_DEFAULT_INSIDE;
		}
		
		int colorIdx = nbt.getInteger(NBT_DISPLAY_COLOR_INSIDE);
		return EnumDyeColor.values()[colorIdx % EnumDyeColor.values().length];
	}
	
	public void setInsideColor(ItemStack stack, EnumDyeColor color) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setInteger(NBT_DISPLAY_COLOR_INSIDE, color.ordinal());
		stack.setTagCompound(nbt);
	}
	
	public boolean getDisplayWings(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_DISPLAY_WINGS)) {
			return false;
		}
		return nbt.getBoolean(NBT_DISPLAY_WINGS);
	}
	
	public void setDisplayWings(ItemStack stack, boolean display) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_DISPLAY_WINGS, display);
		stack.setTagCompound(nbt);
	}
	
	public boolean getDisplayTrimmed(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_DISPLAY_TRIMMED)) {
			return false;
		}
		return nbt.getBoolean(NBT_DISPLAY_TRIMMED);
	}
	
	public void setDisplayTrimmed(ItemStack stack, boolean trimmed) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_DISPLAY_TRIMMED, trimmed);
		stack.setTagCompound(nbt);
	}
	
	public boolean getDisplayRunes(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_DISPLAY_RUNES)) {
			return false;
		}
		return nbt.getBoolean(NBT_DISPLAY_RUNES);
	}
	
	public void setDisplayRunes(ItemStack stack, boolean display) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_DISPLAY_RUNES, display);
		stack.setTagCompound(nbt);
	}
	
	public boolean isAetherCaster(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey(NBT_AETHER_SPENDER)) {
			return false;
		}
		return nbt.getBoolean(NBT_AETHER_SPENDER);
	}
	
	public void setAetherCaster(ItemStack stack, boolean caster) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_AETHER_SPENDER, caster);
		stack.setTagCompound(nbt);
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
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderCape(EntityLivingBase entity, ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemAetherCloak;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation[] getCapeModels(EntityLivingBase entity, ItemStack stack) {
		final boolean trimmed = getDisplayTrimmed(stack);
		final boolean runes = getDisplayRunes(stack);
		if (trimmed) {
			if (runes) {
				return CapeModelsTrimmedDecor;
			} else {
				return CapeModelsTrimmed;
			}
		} else {
			if (runes) {
				return CapeModelsFullDecor;
			} else {
				return CapeModelsFull;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preRender(Entity entity, int model, ItemStack stack, float headYaw, float partialTicks) {
		if (model == 2) {
			// Decor needs to be scaled just a litle to not z fight
			GlStateManager.scale(1.001, 1.001, 1.001);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation[] getCapeTextures(EntityLivingBase entity, ItemStack stack) {
		return null; // obj uses material and has prebaked texture
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldPreventOtherRenders(EntityLivingBase entity, ItemStack stack) {
		return !getDisplayWings(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getColor(EntityLivingBase entity, ItemStack stack, int model) {
		if (model == 0) {
			return ColorUtil.dyeToARGB(getInsideColor(stack));
		} else if (model == 1) {
			return ColorUtil.dyeToARGB(getOutsideColor(stack));
		} else {
			final int glowPeriod = 20 * 8;
			final float brightnessMod = (float) Math.sin(((float) entity.ticksExisted % (float) glowPeriod) / (float) glowPeriod
					* Math.PI * 2) // -1 to 1
					* .25f // -.25 to .25
					+ .5f; // .25 to .75
			
			float[] colors = EntitySheep.getDyeRgb(getRuneColor(stack));
			return ColorUtil.colorToARGB(colors[0] * brightnessMod,
					colors[1] * brightnessMod,
					colors[2] * brightnessMod);
		}
	}

}
