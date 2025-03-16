package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RedwoodSpellCraftGui;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tile.AdvancedSpellTableTileEntity;
import com.smanzana.nostrummagica.tile.BasicSpellTableTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;

public class AdvancedSpellTableBlock extends BasicSpellTableBlock {
	
	public static final String ID = "spelltable_advanced";
	
	public AdvancedSpellTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.noOcclusion()
				);
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new AdvancedSpellTableTileEntity();
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand handIn, BlockHitResult hit) {
		BasicSpellTableTileEntity te = (BasicSpellTableTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, RedwoodSpellCraftGui.RedwoodContainer.Make(te));
		
		return InteractionResult.SUCCESS;
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
