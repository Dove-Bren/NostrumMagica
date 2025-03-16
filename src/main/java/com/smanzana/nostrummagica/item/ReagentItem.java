package com.smanzana.nostrummagica.item;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.integration.aetheria.AetheriaProxy;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class ReagentItem extends Item implements ILoreTagged, ICapabilityProvider {

	public static enum ReagentType implements StringRepresentable {
		// Do not rearrange.
		MANDRAKE_ROOT(ID_SUFFIX_MANDRAKE_ROOT),
		SPIDER_SILK(ID_SUFFIX_SPIDER_SILK),
		BLACK_PEARL(ID_SUFFIX_BLACK_PEARL),
		SKY_ASH(ID_SUFFIX_SKY_ASH),
		GINSENG(ID_SUFFIX_GINSENG),
		GRAVE_DUST(ID_SUFFIX_GRAVE_DUST),
		CRYSTABLOOM(ID_SUFFIX_CRYSTABLOOM),
		MANI_DUST(ID_SUFFIX_MANI_DUST);
		
		private String tag;
		
		private ReagentType(String tag) {
			this.tag = tag;
		}
		
		public String getTag() {
			return tag;
		}
		
		public String prettyName() {
			String name = this.name();
			String out = "";
			int pos = name.indexOf('_');
			while (pos != -1) {
				out += name.substring(0, 1)
						+ name.substring(1, pos).toLowerCase()
						+ " ";
				name = name.substring(pos + 1);
				pos = name.indexOf('_');
			}
			
			out += name.substring(0, 1)
					+ name.substring(1).toLowerCase();
			
			return out;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return getSerializedName();
		}
	}
	
	public static final String ID_PREFIX = "reagent_";
	public static final String ID_SUFFIX_MANDRAKE_ROOT = "mandrake_root";
	public static final String ID_SUFFIX_SPIDER_SILK = "spider_silk";
	public static final String ID_SUFFIX_BLACK_PEARL = "black_pearl";
	public static final String ID_SUFFIX_SKY_ASH = "sky_ash";
	public static final String ID_SUFFIX_GINSENG = "ginseng";
	public static final String ID_SUFFIX_GRAVE_DUST = "grave_dust";
	public static final String ID_SUFFIX_CRYSTABLOOM = "crystabloom";
	public static final String ID_SUFFIX_MANI_DUST = "mani_dust";
	public static final String ID_MANDRAKE_ROOT = ID_PREFIX + ID_SUFFIX_MANDRAKE_ROOT;
	public static final String ID_SPIDER_SILK = ID_PREFIX + ID_SUFFIX_SPIDER_SILK;
	public static final String ID_BLACK_PEARL = ID_PREFIX + ID_SUFFIX_BLACK_PEARL;
	public static final String ID_SKY_ASH = ID_PREFIX + ID_SUFFIX_SKY_ASH;
	public static final String ID_GINSENG = ID_PREFIX + ID_SUFFIX_GINSENG;
	public static final String ID_GRAVE_DUST = ID_PREFIX + ID_SUFFIX_GRAVE_DUST;
	public static final String ID_CRYSTABLOOM = ID_PREFIX + ID_SUFFIX_CRYSTABLOOM;
	public static final String ID_MANI_DUST = ID_PREFIX + ID_SUFFIX_MANI_DUST;
	
	
	private final ReagentType type;
	
	public ReagentItem(ReagentType type) {
		super(NostrumItems.PropBase());
		this.type = type;
	}
	
	public ReagentType getType() {
		return type;
	}
	
    public static ReagentType FindType(ItemStack reagent) {
    	if (reagent.isEmpty() || !(reagent.getItem() instanceof ReagentItem))
    		return null;
    	
    	return ((ReagentItem) reagent.getItem()).getType();
    }
    
    public static ReagentItem GetItem(ReagentType type) {
    	ReagentItem item = null;
    	switch (type) {
		case BLACK_PEARL:
			item = NostrumItems.reagentBlackPearl;
			break;
		case CRYSTABLOOM:
			item = NostrumItems.reagentCrystabloom;
			break;
		case GINSENG:
			item = NostrumItems.reagentGinseng;
			break;
		case GRAVE_DUST:
			item = NostrumItems.reagentGraveDust;
			break;
		case MANDRAKE_ROOT:
			item = NostrumItems.reagentMandrakeRoot;
			break;
		case MANI_DUST:
			item = NostrumItems.reagentManiDust;
			break;
		case SKY_ASH:
			item = NostrumItems.reagentSkyAsh;
			break;
		case SPIDER_SILK:
			item = NostrumItems.reagentSpiderSilk;
			break;
    	}
    	
    	return item;
    }
    
    public static ItemStack CreateStack(ReagentType type, int count) {
    	return new ItemStack(GetItem(type), count);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
    	final @Nonnull ItemStack stack = context.getItemInHand();
    	ReagentType type = FindType(stack);
    	
    	if (type == ReagentType.MANDRAKE_ROOT) {
    		// Try to plant as seed. Convenient!
    		return NostrumItems.reagentSeedMandrake.useOn(context);
    	}
    	
    	if (type == ReagentType.GINSENG) {
	    	return NostrumItems.reagentSeedGinseng.useOn(context);
    	}
    	
    	if (type == ReagentType.CRYSTABLOOM) {
    		final Level worldIn = context.getLevel();
    		final BlockPos pos = context.getClickedPos();
    		final Player playerIn = context.getPlayer();
    		final Direction facing = context.getClickedFace();
    		BlockState state = worldIn.getBlockState(pos);
	        if (facing == Direction.UP && playerIn.mayUseItemAt(pos.relative(facing), facing, stack) && state.getBlock().canSustainPlant(state, worldIn, pos, Direction.UP, NostrumBlocks.crystabloom) && worldIn.isEmptyBlock(pos.above())) {
	        	worldIn.setBlockAndUpdate(pos.above(), NostrumBlocks.crystabloom.defaultBlockState());
	            stack.shrink(1);
	            return InteractionResult.SUCCESS;
	        } else {
	        	return InteractionResult.FAIL;
	        }
    	}
    	
    	return InteractionResult.PASS;
	}

	@Override
	public String getLoreKey() {
		return "nostrum_reagent_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magical Reagents";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Reagents are used to create and cast spells, as well as in other magical recipes.", "Reagents can be found all over the world, such as in trees, undead enemies, or hidden in the grass.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Reagents are used to create and cast spells, as well as in other magical recipes.", "Reagents can be found all over the world, such as in trees, undead enemies, or hidden in the grass.", "Reagents can be stored in Reagent Bags or in the Spell Table.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_REAGENTS;
	}
	
	private LazyOptional<?> AetherBurnableLazy = LazyOptional.of(() -> NostrumMagica.instance.aetheria.makeBurnable(100, 150f));

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		
		if (NostrumMagica.instance.aetheria.isEnabled() && cap != null && cap == AetheriaProxy.AetherBurnableCapability) {
			return AetherBurnableLazy.cast();
		}
		
		return LazyOptional.empty();
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return this::getCapability;
	}
}
