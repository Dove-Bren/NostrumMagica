package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		final BlockPos pos = context.getPos();
		final PlayerEntity playerIn = context.getPlayer();
		
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;
		
		if (pos == null)
			return ActionResultType.PASS;
		
		BlockPos.Mutable checkPos = new BlockPos.Mutable().setPos(pos);
		checkPos.setY(checkPos.getY() + 1);
		if (!worldIn.isAirBlock(checkPos)) {
			return ActionResultType.PASS;
		}
		
		checkPos.setY(checkPos.getY() + 1);
		if (!worldIn.isAirBlock(checkPos)) {
			return ActionResultType.PASS;
		}
		
		// Spawn
//		EntityTameDragonRed dragon = new EntityTameDragonRed(worldIn);
//		dragon.rollRandomStats();
//		dragon.setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
//		worldIn.spawnEntityInWorld(dragon);
		
		EntityDragonEgg egg = new EntityDragonEgg(NostrumEntityTypes.dragonEgg, worldIn, playerIn, EntityTameDragonRed.rollRandomStats());
		egg.setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
		egg.onInitialSpawn((ServerWorld) worldIn, worldIn.getDifficultyForLocation(pos), SpawnReason.EVENT, null, null);
		worldIn.addEntity(egg);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null) {
			attr.giveFullLore(egg.getLoreTag());
		}
		
		playerIn.sendMessage(new TranslationTextComponent("info.egg.place"), Util.DUMMY_UUID);
		
		if (!playerIn.isCreative()) {
			context.getItem().shrink(1);
		}
		
		return ActionResultType.SUCCESS;
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
