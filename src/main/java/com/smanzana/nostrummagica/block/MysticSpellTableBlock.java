package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.MysticSpellCraftGui;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tile.MysticSpellTableTileEntity;

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

public class MysticSpellTableBlock extends BasicSpellTableBlock {
	
	public static final String ID = "spelltable_mystic";
	
	public MysticSpellTableBlock() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.notSolid()
				);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MysticSpellTableTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockRayTraceResult hit) {
		
		MysticSpellTableTileEntity te = (MysticSpellTableTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, MysticSpellCraftGui.MysticContainer.Make(te));
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public String getLoreKey() {
		return ID;
	}

	@Override
	public String getLoreDisplayName() {
		return "Mystic Spell Table";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A magic spell table with room or many runes.", "An advanced spell crafter can use this table to make powerful spells!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A magic spell table with room or many runes.", "An advanced spell crafter can use this table to make powerful spells!");
	}
}
