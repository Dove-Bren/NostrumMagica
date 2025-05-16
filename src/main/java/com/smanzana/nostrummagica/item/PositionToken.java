package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ObeliskPortal;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Location;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Solidified position crystal for obelisk linking
 * @author Skyler
 *
 */
public class PositionToken extends PositionCrystal {

	public static final String ID = "nostrum_pos_token";
	
	public PositionToken() {
		super();
	}
	
	protected static boolean hasRecallUnlocked(Player playerIn, Level worldIn, ItemStack token) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null && attr.getCompletedResearches().contains("adv_markrecall")) {
			return true;
		}
		return false;
	}
	
	protected static boolean canAffordRecall(Player playerIn, Level worldIn, ItemStack token) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null && token.getItem() instanceof PositionToken) {
			return attr.getMana() >= ((PositionToken)token.getItem()).getManaCost(playerIn, worldIn, token);
		}
		return false;
	}
	
	protected int getManaCost(Player playerIn, Level worldIn, ItemStack token) {
		return 50;
	}
	
	protected static boolean canPerformRecall(Player playerIn, Level worldIn, ItemStack token) {
		BlockPos pos = getBlockPosition(token);
		ResourceKey<Level> dim = getDimension(token);
		return pos != null
				&& hasRecallUnlocked(playerIn, worldIn, token)
				&& canAffordRecall(playerIn, worldIn, token)
				&& DimensionUtils.InDimension(playerIn, dim);
	}
	
	protected boolean doRecall(Player playerIn, Level worldIn, ItemStack token) {
		if (canPerformRecall(playerIn, worldIn, token)) {
			// Try to do actual recall
			BlockPos pos = getBlockPosition(token);
			// TODO: use stored dimension and support moving dimensions
			if (NostrumMagica.attemptTeleport(new Location(worldIn, pos), playerIn, !playerIn.isShiftKeyDown(), NostrumMagica.rand.nextInt(32) == 0, playerIn)) {
				// If success, take mana andreturn true
				INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn); // assumption: not null!
				attr.addMana(-getManaCost(playerIn, worldIn, token));
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		BlockPos pos = context.getClickedPos();
		final Player playerIn = context.getPlayer();
		
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (pos == null || playerIn.isShiftKeyDown() || !playerIn.isCreative())
			return InteractionResult.PASS;
		
		BlockState state = worldIn.getBlockState(pos);
		while (state.getBlock() instanceof ObeliskPortal) {
			pos = pos.below();
			state = worldIn.getBlockState(pos);
		}
		setPosition(context.getItemInHand(), DimensionUtils.GetDimension(playerIn), pos);
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getItemInHand(hand);
		if (PositionToken.hasRecallUnlocked(playerIn, worldIn, itemStackIn)) {
			if (!worldIn.isClientSide) {
				doRecall(playerIn, worldIn, itemStackIn);
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		} else if (playerIn.isCreative()) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		}
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
	}

	@Override
	public String getLoreKey() {
		return "nostrum_pos_token";
	}

	@Override
	public String getLoreDisplayName() {
		return "GeoToken";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Geogems that have been sealed inside magic tokens.", "They are more stable than Geogems, but cannot be reassigned.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("By exposing a Geogem to strong earthern energies and providing a tablet for it to be set in, you can create a stable version.", "These tokens are used in rituals or structures that take location inputs.");
	}

	public static ItemStack constructFrom(ItemStack centerItem, int tokenCount) {
		if (centerItem.isEmpty())
			return null;
		
		BlockPos pos = getBlockPosition(centerItem);
		ResourceKey<Level> dim = getDimension(centerItem);
		
		if (pos == null)
			return null;
		
		ItemStack ret = new ItemStack(NostrumItems.positionToken, tokenCount);
		
		setPosition(ret, dim, pos);
		
		return ret;
	}
	
//	@Override
//	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
//		if (entityItem.world.isRemote)
//			return false;
//			
//		// check if item is above an obelisk. If so, add this target (if we have one) to it
//		BlockPos storedPos = getBlockPosition(entityItem.getItem());
//		if (storedPos != null) {
//			BlockPos pos = entityItem.getPosition().add(0, -1, 0);
//			if (pos.equals(storedPos))
//				return false;
//			
//			BlockState state = entityItem.world.getBlockState(pos);
//			if (state != null && state.getBlock() instanceof ObeliskBlock && ObeliskBlock.blockIsMaster(state)) {
//				TileEntity ent = entityItem.world.getTileEntity(pos);
//				if (ent != null && ent instanceof ObeliskTileEntity) {
//					ObeliskTileEntity obelisk = ((ObeliskTileEntity) ent);
//					if (obelisk.canAcceptTarget(storedPos)) {
//						if (entityItem.getItem().hasDisplayName()) {
//							obelisk.addTarget(storedPos, entityItem.getItem().getDisplayName().getString());
//						} else {
//							obelisk.addTarget(storedPos);
//						}
//						NostrumMagicaSounds.SUCCESS_QUEST.play(
//								entityItem.world,
//								pos.getX(),
//								pos.getY(),
//								pos.getZ()
//								);
//						entityItem.discard();
//						return true;
//					}
//				}
//			}
//		}
//		
//		return false;
//	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		if (hasRecallUnlocked(NostrumMagica.instance.proxy.getPlayer(), worldIn, stack)) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
			if (attr != null && attr.hasEnhancedTeleport()) {
				tooltip.add(new TranslatableComponent("info.geotoken.recall_enhanced"));
			} else {
				tooltip.add(new TranslatableComponent("info.geotoken.recall"));
			}
			
			
		}
	}
}
