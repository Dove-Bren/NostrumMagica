package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.BasicSpellCraftGui;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tile.BasicSpellTableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BasicSpellTableBlock extends BaseEntityBlock implements ILoreTagged {
	
	public static final String ID = "spelltable_basic";
	protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
	
	public BasicSpellTableBlock() {
		this(Block.Properties.of(Material.WOOD)
				.strength(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.noOcclusion()
				);
	}
	
	protected BasicSpellTableBlock(Block.Properties props) {
		super(props);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		BlockEntity ent = world.getBlockEntity(pos);
		if (!world.isClientSide && ent != null) {
			BasicSpellTableTileEntity table = (BasicSpellTableTileEntity) ent;
			for (int i = 0; i < table.getContainerSize(); i++) {
				if (table.getItem(i) != null) {
					ItemEntity item = new ItemEntity(
							world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
							table.removeItemNoUpdate(i));
					world.addFreshEntity(item);
				}
			}
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BasicSpellTableTileEntity(pos, state);
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand handIn, BlockHitResult hit) {
		BasicSpellTableTileEntity te = (BasicSpellTableTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.Proxy.openContainer(playerIn, BasicSpellCraftGui.BasicSpellCraftContainer.Make(te));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public String getLoreKey() {
		return ID;
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Table";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("The spell table is the place to sequence runes to create a spell!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("The spell table is the place to sequence runes to create a spell!", "This basic spell table can only hold a few runes, and isn't strong enough to make powerful spells.");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.BLOCK;
	}
	
	@Override
	public RenderShape getRenderShape(BlockState p_49232_) {
		return RenderShape.MODEL;
	}
}
