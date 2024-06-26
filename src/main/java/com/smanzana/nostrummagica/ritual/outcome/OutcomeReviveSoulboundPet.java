package com.smanzana.nostrummagica.ritual.outcome;

import com.smanzana.nostrummagica.item.PetSoulItem;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRecipe.RitualMatchInfo;
import com.smanzana.nostrummagica.tile.AltarTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OutcomeReviveSoulboundPet extends OutcomeSpawnEntity {

	public OutcomeReviveSoulboundPet() {
		super(new IEntityFactory() {
			@Override
			public void spawn(World world, Vector3d pos, PlayerEntity invoker, ItemStack centerItem) {
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
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		if (world.isRemote)
			return;
		
		super.perform(world, player, centerItem, otherItems, center, recipe);
		
		// Also return soul item to center pedestal
		TileEntity te;
		te = world.getTileEntity(center);
		if (te != null && te instanceof AltarTileEntity) {
			((AltarTileEntity) te).setItem(centerItem.copy());
		}
	}
	
	@Override
	public boolean canPerform(World world, PlayerEntity player, BlockPos center, RitualMatchInfo ingredients) {
		// Must have PetSoulItem in center, and must have valid soul.
		if (ingredients.center.isEmpty() || !(ingredients.center.getItem() instanceof PetSoulItem)) {
			player.sendMessage(new TranslationTextComponent("info.respawn_soulbound_pet.fail.baditem", new Object[0]), Util.DUMMY_UUID);
			return false;
		}
		
		PetSoulItem item = (PetSoulItem) ingredients.center.getItem();
		if (item.getPetSoulID(ingredients.center) == null) {
			player.sendMessage(new TranslationTextComponent("info.respawn_soulbound_pet.fail.baditem", new Object[0]), Util.DUMMY_UUID);
			return false;
		}
		
		if (!item.canSpawnEntity(world, player, Vector3d.copyCentered(center), ingredients.center)) {
			return false;
		}
		
		return true;
	}
}
