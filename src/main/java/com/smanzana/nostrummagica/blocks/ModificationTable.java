package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.equipment.CasterWandItem;
import com.smanzana.nostrummagica.items.equipment.WarlockSword;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.tiles.ModificationTableEntity;

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

public class ModificationTable extends Block {
	
	public static final String ID = "modification_table";
	
	public ModificationTable() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.0f, 10.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote()) {
			ModificationTableEntity te = (ModificationTableEntity) worldIn.getTileEntity(pos);
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
		return new ModificationTableEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeTileEntity(pos);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof ModificationTableEntity))
			return;
		
		ModificationTableEntity table = (ModificationTableEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (!table.getStackInSlot(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.addEntity(item);
			}
		}
		
	}
	
	public static boolean IsModifiable(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		
		Item item = stack.getItem();
		
		if (item instanceof SpellRune) {
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
			
			if (comp.isTrigger()) {
				return comp.getTrigger().supportedFloats() != null || comp.getTrigger().supportsBoolean();
			} else if (comp.isShape()) {
				return comp.getShape().supportedFloats() != null || comp.getShape().supportsBoolean();
			}
		}
		
		return item instanceof SpellTome
				//|| item instanceof SpellRune
				|| (item instanceof SpellScroll && SpellScroll.getSpell(stack) != null)
				|| item instanceof WarlockSword
				|| item instanceof CasterWandItem;
	}
}
