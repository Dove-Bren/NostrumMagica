package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.item.equipment.WarlockSword;
import com.smanzana.nostrummagica.tile.ModificationTableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class ModificationTableBlock extends BaseEntityBlock {
	
	public static final String ID = "modification_table";
	
	public ModificationTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!worldIn.isClientSide()) {
			ModificationTableTileEntity te = (ModificationTableTileEntity) worldIn.getBlockEntity(pos);
			NostrumMagica.Proxy.openContainer(player, ModificationTableGui.ModificationTableContainer.Make(te));
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ModificationTableTileEntity(pos, state);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof ModificationTableTileEntity))
			return;
		
		ModificationTableTileEntity table = (ModificationTableTileEntity) ent;
		for (int i = 0; i < table.getContainerSize(); i++) {
			if (!table.getItem(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeItemNoUpdate(i));
				world.addFreshEntity(item);
			}
		}
		
	}
	
	public static boolean IsModifiable(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		
		Item item = stack.getItem();
		
		return item instanceof SpellTome
				//|| item instanceof SpellRune
				|| (item instanceof SpellScroll && SpellScroll.GetSpell(stack) != null)
				|| item instanceof WarlockSword
				|| item instanceof CasterWandItem;
	}
}
