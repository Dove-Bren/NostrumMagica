package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.ObeliskPortal;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.utils.DimensionUtils;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
	
	protected static boolean hasRecallUnlocked(PlayerEntity playerIn, World worldIn, ItemStack token) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null && attr.getCompletedResearches().contains("adv_markrecall")) {
			return true;
		}
		return false;
	}
	
	protected static boolean canAffordRecall(PlayerEntity playerIn, World worldIn, ItemStack token) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null && token.getItem() instanceof PositionToken) {
			return attr.getMana() >= ((PositionToken)token.getItem()).getManaCost(playerIn, worldIn, token);
		}
		return false;
	}
	
	protected int getManaCost(PlayerEntity playerIn, World worldIn, ItemStack token) {
		return 50;
	}
	
	protected static boolean canPerformRecall(PlayerEntity playerIn, World worldIn, ItemStack token) {
		BlockPos pos = getBlockPosition(token);
		RegistryKey<World> dim = getDimension(token);
		return pos != null
				&& hasRecallUnlocked(playerIn, worldIn, token)
				&& canAffordRecall(playerIn, worldIn, token)
				&& DimensionUtils.InDimension(playerIn, dim);
	}
	
	protected boolean doRecall(PlayerEntity playerIn, World worldIn, ItemStack token) {
		if (canPerformRecall(playerIn, worldIn, token)) {
			// Try to do actual recall
			BlockPos pos = getBlockPosition(token);
			if (NostrumMagica.attemptTeleport(worldIn, pos, playerIn, !playerIn.isSneaking(), NostrumMagica.rand.nextInt(32) == 0)) {
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
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		BlockPos pos = context.getPos();
		final PlayerEntity playerIn = context.getPlayer();
		
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;
		
		if (pos == null || playerIn.isSneaking() || !playerIn.isCreative())
			return ActionResultType.PASS;
		
		BlockState state = worldIn.getBlockState(pos);
		while (state.getBlock() instanceof ObeliskPortal) {
			pos = pos.down();
			state = worldIn.getBlockState(pos);
		}
		setPosition(context.getItem(), DimensionUtils.GetDimension(playerIn), pos);
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (PositionToken.hasRecallUnlocked(playerIn, worldIn, itemStackIn)) {
			if (!worldIn.isRemote) {
				doRecall(playerIn, worldIn, itemStackIn);
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		} else if (playerIn.isCreative()) {
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
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
		RegistryKey<World> dim = getDimension(centerItem);
		
		if (pos == null)
			return null;
		
		ItemStack ret = new ItemStack(NostrumItems.positionToken, tokenCount);
		
		setPosition(ret, dim, pos);
		
		return ret;
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if (entityItem.world.isRemote)
			return false;
			
		// check if item is above an obelisk. If so, add this target (if we have one) to it
		BlockPos storedPos = getBlockPosition(entityItem.getItem());
		if (storedPos != null) {
			BlockPos pos = entityItem.getPosition().add(0, -1, 0);
			if (pos.equals(storedPos))
				return false;
			
			BlockState state = entityItem.world.getBlockState(pos);
			if (state != null && state.getBlock() instanceof NostrumObelisk && NostrumObelisk.blockIsMaster(state)) {
				TileEntity ent = entityItem.world.getTileEntity(pos);
				if (ent != null && ent instanceof NostrumObeliskEntity) {
					NostrumObeliskEntity obelisk = ((NostrumObeliskEntity) ent);
					if (obelisk.canAcceptTarget(storedPos)) {
						if (entityItem.getItem().hasDisplayName()) {
							obelisk.addTarget(storedPos, entityItem.getItem().getDisplayName().getString());
						} else {
							obelisk.addTarget(storedPos);
						}
						NostrumMagicaSounds.SUCCESS_QUEST.play(
								entityItem.world,
								pos.getX(),
								pos.getY(),
								pos.getZ()
								);
						entityItem.remove();
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		if (hasRecallUnlocked(NostrumMagica.instance.proxy.getPlayer(), worldIn, stack)) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
			if (attr != null && attr.hasEnhancedTeleport()) {
				tooltip.add(new TranslationTextComponent("info.geotoken.recall_enhanced"));
			} else {
				tooltip.add(new TranslationTextComponent("info.geotoken.recall"));
			}
			
			
		}
	}
}
