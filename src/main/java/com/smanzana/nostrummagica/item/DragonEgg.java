package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResult;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * Dragon spawning egg
 * @author Skyler
 *
 */
public class DragonEgg extends Item implements ILoreTagged {

	public static final String ID = "dragon_egg";

	public DragonEgg() {
		super(NostrumItems.PropUnstackable().rarity(Rarity.EPIC));
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player playerIn = context.getPlayer();
		
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (pos == null)
			return InteractionResult.PASS;
		
		BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos().set(pos);
		checkPos.setY(checkPos.getY() + 1);
		if (!worldIn.isEmptyBlock(checkPos)) {
			return InteractionResult.PASS;
		}
		
		checkPos.setY(checkPos.getY() + 1);
		if (!worldIn.isEmptyBlock(checkPos)) {
			return InteractionResult.PASS;
		}
		
		// Spawn
//		EntityTameDragonRed dragon = new EntityTameDragonRed(worldIn);
//		dragon.rollRandomStats();
//		dragon.setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
//		worldIn.spawnEntityInWorld(dragon);
		
		DragonEggEntity egg = new DragonEggEntity(NostrumEntityTypes.dragonEgg, worldIn, playerIn, TameRedDragonEntity.rollRandomStats());
		egg.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
		egg.finalizeSpawn((ServerLevel) worldIn, worldIn.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
		worldIn.addFreshEntity(egg);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null) {
			attr.giveFullLore(egg.getLoreTag());
		}
		
		playerIn.sendMessage(new TranslatableComponent("info.egg.place"), Util.NIL_UUID);
		
		if (!playerIn.isCreative()) {
			context.getItemInHand().shrink(1);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_dragon_egg";
	}

	@Override
	public String getLoreDisplayName() {
		return "Dragon Eggs";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A curious egg created from the shells of a Red Dragon's egg.", "", "Caution: Upon use, the egg will be placed. The egg must be kept warm to have any chance of hatching!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A curious egg created from the shells of a Red Dragon's egg.", "", "Caution: Upon use, the egg will be placed. The egg must be kept warm to have any chance of hatching! Lots of light and a hay-bale are encouraged!");
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
