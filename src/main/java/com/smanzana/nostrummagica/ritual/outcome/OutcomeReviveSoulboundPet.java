package com.smanzana.nostrummagica.ritual.outcome;

import com.smanzana.nostrummagica.item.PetSoulItem;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class OutcomeReviveSoulboundPet extends OutcomeSpawnEntity {

	public OutcomeReviveSoulboundPet() {
		super(new IEntityFactory() {
			@Override
			public void spawn(Level world, Vec3 pos, Player invoker, ItemStack centerItem) {
				PetSoulItem.SpawnPet(centerItem, world, pos.add(0, 1, 0));
//				EntityKoid koid = new EntityKoid(world);
//				koid.setPosition(pos.x, pos.y, pos.z);
//				world.addEntity(koid);
//				koid.setAttackTarget(invoker);
			}

			@Override
			public String getEntityName() {
				return "entity.nostrummagica.placeholder.soulbound";
			}
		}, 1);
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		if (world.isClientSide)
			return;
		
		super.perform(world, player, center, layout, recipe);
		
		// Also return soul item to center pedestal
		BlockEntity te;
		te = world.getBlockEntity(center);
		if (te != null && te instanceof AltarTileEntity) {
			((AltarTileEntity) te).setItem(layout.getCenterItem(world, center).copy());
		}
	}
	
	@Override
	public boolean canPerform(Level world, Player player, BlockPos center, IRitualLayout layout) {
		// Must have PetSoulItem in center, and must have valid soul.
		final ItemStack centerItem = layout.getCenterItem(world, center);
		if (centerItem.isEmpty() || !(centerItem.getItem() instanceof PetSoulItem)) {
			player.sendMessage(new TranslatableComponent("info.respawn_soulbound_pet.fail.baditem", new Object[0]), Util.NIL_UUID);
			return false;
		}
		
		PetSoulItem item = (PetSoulItem) centerItem.getItem();
		if (item.getPetSoulID(centerItem) == null) {
			player.sendMessage(new TranslatableComponent("info.respawn_soulbound_pet.fail.baditem", new Object[0]), Util.NIL_UUID);
			return false;
		}
		
		if (!item.canSpawnEntity(world, player, Vec3.atCenterOf(center), centerItem)) {
			return false;
		}
		
		return true;
	}
}
