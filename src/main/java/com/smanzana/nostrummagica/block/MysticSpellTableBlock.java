package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.MysticSpellCraftGui;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tile.MysticSpellTableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class MysticSpellTableBlock extends BasicSpellTableBlock {
	
	public static final String ID = "spelltable_mystic";
	
	public MysticSpellTableBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.noOcclusion()
				);
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new MysticSpellTableTileEntity();
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand handIn, BlockHitResult hit) {
		
		MysticSpellTableTileEntity te = (MysticSpellTableTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, MysticSpellCraftGui.MysticContainer.Make(te));
		
		return InteractionResult.SUCCESS;
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
