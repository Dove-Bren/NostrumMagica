package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RedwoodSpellCraftGui;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tile.AdvancedSpellTableTileEntity;
import com.smanzana.nostrummagica.tile.BasicSpellTableTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class AdvancedSpellTableBlock extends BasicSpellTableBlock {
	
	public static final String ID = "spelltable_advanced";
	
	public AdvancedSpellTableBlock() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.notSolid()
				);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AdvancedSpellTableTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit) {
		BasicSpellTableTileEntity te = (BasicSpellTableTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, RedwoodSpellCraftGui.RedwoodContainer.Make(te));
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public String getLoreKey() {
		return ID;
	}

	@Override
	public String getLoreDisplayName() {
		return "Blackstone Spell Table";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A reinforced spell table with room or many runes.", "An experienced spell crafter can use this table to make powerful spells!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A reinforced spell table with room or many runes.", "An experienced spell crafter can use this table to make powerful spells!");
	}
}
