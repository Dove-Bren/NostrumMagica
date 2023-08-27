package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.ObeliskPortal;
import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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

	private static PositionToken instance = null;

	public static PositionToken instance() {
		if (instance == null)
			instance = new PositionToken();
	
		return instance;

	}
	
	public PositionToken() {
		super(ID);
		this.setUnlocalizedName(ID);
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
		int dim = getDimension(token);
		return pos != null
				&& hasRecallUnlocked(playerIn, worldIn, token)
				&& canAffordRecall(playerIn, worldIn, token)
				&& dim == playerIn.dimension;
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
	public EnumActionResult onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return EnumActionResult.SUCCESS;
		
		if (pos == null || playerIn.isSneaking() || !playerIn.isCreative())
			return EnumActionResult.PASS;
		
		IBlockState state = worldIn.getBlockState(pos);
		while (state.getBlock() instanceof ObeliskPortal) {
			pos = pos.down();
			state = worldIn.getBlockState(pos);
		}
		setPosition(playerIn.getHeldItem(hand), playerIn.dimension, pos);
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, EnumHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (PositionToken.hasRecallUnlocked(playerIn, worldIn, itemStackIn)) {
			if (!worldIn.isRemote) {
				doRecall(playerIn, worldIn, itemStackIn);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		} else if (playerIn.isCreative()) {
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
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
		int dim = getDimension(centerItem);
		
		if (pos == null)
			return null;
		
		ItemStack ret = new ItemStack(instance(), tokenCount);
		
		setPosition(ret, dim, pos);
		
		return ret;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (entityItem.world.isRemote)
			return false;
			
		// check if item is above an obelisk. If so, add this target (if we have one) to it
		BlockPos storedPos = getBlockPosition(entityItem.getItem());
		if (storedPos != null) {
			BlockPos pos = entityItem.getPosition().add(0, -1, 0);
			if (pos.equals(storedPos))
				return false;
			
			IBlockState state = entityItem.world.getBlockState(pos);
			if (state != null && state.getBlock() instanceof NostrumObelisk && NostrumObelisk.blockIsMaster(state)) {
				TileEntity ent = entityItem.world.getTileEntity(pos);
				if (ent != null && ent instanceof NostrumObeliskEntity) {
					NostrumObeliskEntity obelisk = ((NostrumObeliskEntity) ent);
					if (obelisk.canAcceptTarget(storedPos)) {
						if (entityItem.getItem().hasDisplayName()) {
							obelisk.addTarget(storedPos, entityItem.getItem().getDisplayName());
						} else {
							obelisk.addTarget(storedPos);
						}
						NostrumMagicaSounds.SUCCESS_QUEST.play(
								entityItem.world,
								pos.getX(),
								pos.getY(),
								pos.getZ()
								);
						entityItem.setDead();
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		if (hasRecallUnlocked(NostrumMagica.proxy.getPlayer(), worldIn, stack)) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.proxy.getPlayer());
			if (attr != null && attr.hasEnhancedTeleport()) {
				tooltip.add(I18n.format("info.geotoken.recall_enhanced", new Object[0]));
			} else {
				tooltip.add(I18n.format("info.geotoken.recall", new Object[0]));
			}
			
			
		}
	}
}
