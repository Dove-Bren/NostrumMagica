package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.item.equipment.WarlockSword;
import com.smanzana.nostrummagica.tile.ModificationTableTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class ModificationTableBlock extends Block {
	
	public static final String ID = "modification_table";
	
	public ModificationTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide()) {
			ModificationTableTileEntity te = (ModificationTableTileEntity) worldIn.getBlockEntity(pos);
			NostrumMagica.instance.proxy.openContainer(player, ModificationTableGui.ModificationTableContainer.Make(te));
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ModificationTableTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getBlockEntity(pos);
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
