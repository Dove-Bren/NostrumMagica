package com.smanzana.nostrummagica.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.AltarBlock;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.api.ICrystalEnchantableItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.ritual.AltarRitualLayout;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.tile.CandleTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One for each element, except neutral
 * @author Skyler
 *
 */
public class InfusedGemItem extends Item implements ILoreTagged, ICrystalEnchantableItem {

	public static final String ID_PREFIX = "nostrum_gem_";
	public static final String MakeID(EMagicElement element) {
		return ID_PREFIX + element.name().toLowerCase();
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
			element = EMagicElement.NEUTRAL;
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
		case NEUTRAL:
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
	public InteractionResult useOn(UseOnContext context) {
		//if (worldIn.isRemote)
			//return ActionResultType.SUCCESS;
		
		final Player playerIn = context.getPlayer();
		final BlockPos pos = context.getClickedPos();
		final @Nonnull ItemStack stack = context.getItemInHand();
		final Level worldIn = context.getLevel();
		BlockState state = worldIn.getBlockState(pos);
		if (state.getBlock() == null)
			return InteractionResult.PASS;
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (state.getBlock() instanceof CandleBlock) {
			if (!(te instanceof CandleTileEntity))
				return InteractionResult.PASS;
			
 			if (AltarRitualLayout.AttemptRitual(worldIn, pos, playerIn, element)) {
 				stack.shrink(1);
 			}
 			
			return InteractionResult.SUCCESS;
		} else if (state.getBlock() instanceof AltarBlock) {
			if (!(te instanceof AltarTileEntity))
				return InteractionResult.PASS;
			
			if (AltarRitualLayout.AttemptRitual(worldIn, pos, playerIn, element)) {
				stack.shrink(1);
			}
			
			return InteractionResult.SUCCESS;
		}
		
        return InteractionResult.PASS;
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
	public boolean canEnchant(ItemStack stack, EMagicElement element) {
		// Only void gems are enchantable
		return element != EMagicElement.NEUTRAL
				&& (this.element == null || this.element == EMagicElement.NEUTRAL)
				;
	}

	@Override
	public Result attemptEnchant(ItemStack stack, EMagicElement element) {
		if (this.element == EMagicElement.NEUTRAL || this.element == null) {
			int count = 1;
			return new Result(true, InfusedGemItem.getGem(element, count));
		} else {
			return new Result(false);
		}
	}
}
