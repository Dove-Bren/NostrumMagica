package com.smanzana.nostrummagica.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.AltarBlock;
import com.smanzana.nostrummagica.block.Candle;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.tile.CandleTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * One for each element, except physical
 * @author Skyler
 *
 */
public class InfusedGemItem extends Item implements ILoreTagged, IEnchantableItem {

	public static final String ID_PREFIX = "nostrum_gem_";
	public static final String MakeID(EMagicElement element) {
		return ID_PREFIX + element.getName().toLowerCase();
	}
	
	protected final EMagicElement element;
	
	public InfusedGemItem(EMagicElement element) {
		super(NostrumItems.PropLowStack());
		this.element = element;
	}
	
	/**
	 * Returns an itemstack containing the gem type specefied.
	 * If element is null, returns a basic gem.
	 * @param element
	 * @param count
	 * @return
	 */
	public static Item getGemItem(EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		
		InfusedGemItem gem = null;
		switch (element) {
		case EARTH:
			gem = NostrumItems.infusedGemEarth;
			break;
		case ENDER:
			gem = NostrumItems.infusedGemEnder;
			break;
		case FIRE:
			gem = NostrumItems.infusedGemFire;
			break;
		case ICE:
			gem = NostrumItems.infusedGemIce;
			break;
		case LIGHTNING:
			gem = NostrumItems.infusedGemLightning;
			break;
		case PHYSICAL:
			gem = NostrumItems.infusedGemUnattuned;
			break;
		case WIND:
			gem = NostrumItems.infusedGemWind;
			break;
		}
		
		return gem;
	}
	
	public static ItemStack getGem(EMagicElement element, int count) {
		return new ItemStack(getGemItem(element), count);
	}
	
//    public String getNameFromMeta(int meta) {
//    	String suffix = "basic";
//		
//    	EMagicElement type = getTypeFromMeta(meta);
//    	if (type != null)
//    		suffix = type.name().toLowerCase();
//    	
//		return suffix;
//    }
    
    @Override
	public String getLoreKey() {
		return "nostrum_infused_gem";
	}

	@Override
	public String getLoreDisplayName() {
		return "Infused Gems";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By combining magical reagents with an enderpearl, you can create a Void Gem.", "The gem lacks any elemental affinity and otherwise seems useless.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By combining magical reagents with an enderpearl, you can create a Void Gem.", "Void gems alone are not very useful.", "In order to use them, they must be inbued with the power of an element.", "Perhaps an Alter rune would do the trick...");
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		//if (worldIn.isRemote)
			//return ActionResultType.SUCCESS;
		
		final PlayerEntity playerIn = context.getPlayer();
		final BlockPos pos = context.getPos();
		final @Nonnull ItemStack stack = context.getItem();
		final World worldIn = context.getWorld();
		BlockState state = worldIn.getBlockState(pos);
		if (state.getBlock() == null)
			return ActionResultType.PASS;
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (state.getBlock() instanceof Candle) {
			if (!(te instanceof CandleTileEntity))
				return ActionResultType.PASS;
			
 			if (RitualRegistry.attemptRitual(worldIn, pos, playerIn, element)) {
 				stack.shrink(1);
 			}
 			
			return ActionResultType.SUCCESS;
		} else if (state.getBlock() instanceof AltarBlock) {
			if (!(te instanceof AltarTileEntity))
				return ActionResultType.PASS;
			
			if (RitualRegistry.attemptRitual(worldIn, pos, playerIn, element)) {
				stack.shrink(1);
			}
			
			return ActionResultType.SUCCESS;
		}
		
        return ActionResultType.PASS;
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public EMagicElement getElement() {
		return this.element;
	}
	
	public static final @Nullable EMagicElement GetElement(ItemStack stack) {
		EMagicElement elem = null;
		if (!stack.isEmpty() && stack.getItem() instanceof InfusedGemItem) {
			elem = ((InfusedGemItem) stack.getItem()).getElement();
		}
		return elem;
	}
	
	@Override
	public boolean canEnchant(ItemStack stack) {
		// Only void gems are enchantable
		return this.element == null || this.element == EMagicElement.PHYSICAL;
	}

	@Override
	public Result attemptEnchant(ItemStack stack, LivingEntity entity, EMagicElement element, int power) {
		if (this.element == EMagicElement.PHYSICAL || this.element == null) {
			int count = (int) Math.pow(2, power - 1);
			return new Result(true, InfusedGemItem.getGem(element, count));
		} else {
			return new Result(false);
		}
	}
}
