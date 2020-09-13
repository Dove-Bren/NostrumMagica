package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumObeliskEntity;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Solidified position crystal for obelisk linking
 * @author Skyler
 *
 */
public class PositionToken extends PositionCrystal {

	public static void init() {
	}
	
	public static final String ID = "nostrum_pos_token";

	private static PositionToken instance = null;

	public static PositionToken instance() {
		if (instance == null)
			instance = new PositionToken();
	
		return instance;

	}

	public PositionToken() {
		super();
		this.setUnlocalizedName(ID);
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return EnumActionResult.SUCCESS;
		
		if (pos == null || !playerIn.isCreative())
			return EnumActionResult.PASS;
		
		setPosition(stack, playerIn.dimension, pos);
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (playerIn.isCreative()) {
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

	public static ItemStack constructFrom(ItemStack centerItem) {
		if (centerItem == null || !(centerItem.getItem() instanceof PositionCrystal))
			return null;
		
		ItemStack ret = new ItemStack(instance());
		
		setPosition(ret,
				getDimension(centerItem),
				getBlockPosition(centerItem));
		
		return ret;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (entityItem.worldObj.isRemote)
			return false;
			
		// check if item is above an obelisk. If so, add this target (if we have one) to it
		BlockPos storedPos = getBlockPosition(entityItem.getEntityItem());
		if (storedPos != null) {
			BlockPos pos = entityItem.getPosition().add(0, -1, 0);
			if (pos.equals(storedPos))
				return false;
			
			IBlockState state = entityItem.worldObj.getBlockState(pos);
			if (state != null && state.getBlock() instanceof NostrumObelisk && NostrumObelisk.blockIsMaster(state)) {
				TileEntity ent = entityItem.worldObj.getTileEntity(pos);
				if (ent != null && ent instanceof NostrumObeliskEntity) {
					NostrumObeliskEntity obelisk = ((NostrumObeliskEntity) ent);
					if (obelisk.canAcceptTarget(storedPos)) {
						if (entityItem.getEntityItem().hasDisplayName()) {
							obelisk.addTarget(storedPos, entityItem.getEntityItem().getDisplayName());
						} else {
							obelisk.addTarget(storedPos);
						}
						NostrumMagicaSounds.SUCCESS_QUEST.play(
								entityItem.worldObj,
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
}
