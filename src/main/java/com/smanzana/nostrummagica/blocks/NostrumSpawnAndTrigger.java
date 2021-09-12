package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SpawnerTriggerTileEntity;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Like the single spawner, but spawns and then (once the entity is dead) triggers a triggerable block
 * @author Skyler
 *
 */
public class NostrumSpawnAndTrigger extends NostrumSingleSpawner {
	
	public static final String ID = "nostrum_spawner_trigger";
	
	private static NostrumSpawnAndTrigger instance = null;
	public static NostrumSpawnAndTrigger instance() {
		if (instance == null)
			instance = new NostrumSpawnAndTrigger();
		
		return instance;
	}
	
	public NostrumSpawnAndTrigger() {
		super();
		this.setUnlocalizedName(ID);
		this.setBlockUnbreakable();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (NostrumMagica.proxy.getPlayer().isCreative()) {
			TileEntity te = blockAccess.getTileEntity(pos);
			if (te != null && te instanceof SpawnerTriggerTileEntity) {
				SpawnerTriggerTileEntity ent = ((SpawnerTriggerTileEntity) te);
				return (ent.getSpawnedEntity() == null && ent.getUnlinkedEntID() == null);
			}
		}
		return false;
	}
	
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		;//super.updateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SpawnerTriggerTileEntity();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SpawnerTriggerTileEntity)) {
			return true;
		}
		
		SpawnerTriggerTileEntity ent = (SpawnerTriggerTileEntity) te;
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new TextComponentString("Currently set to " + state.getValue(MOB).getName()));
			} else if (heldItem.getItem() instanceof EssenceItem) {
				Type type = null;
				switch (EssenceItem.findType(heldItem)) {
				case EARTH:
					type = Type.GOLEM_EARTH;
					break;
				case ENDER:
					type = Type.GOLEM_ENDER;
					break;
				case FIRE:
					type = Type.GOLEM_FIRE;
					break;
				case ICE:
					type = Type.GOLEM_ICE;
					break;
				case LIGHTNING:
					type = Type.GOLEM_LIGHTNING;
					break;
				case PHYSICAL:
					type = Type.GOLEM_PHYSICAL;
					break;
				case WIND:
					type = Type.GOLEM_WIND;
					break;
				}
				
				worldIn.setBlockState(pos, state.withProperty(MOB, type));
			} else if (heldItem.getItem() instanceof NostrumSkillItem) {
				if (NostrumSkillItem.getTypeFromMeta(heldItem.getMetadata()) == SkillItemType.WING) {
					worldIn.setBlockState(pos, state.withProperty(MOB, Type.DRAGON_RED));
				}
			} if (heldItem.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
				if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.provider.getDimension()) {
					ent.setTriggerPosition(heldPos.getX(), heldPos.getY(), heldPos.getZ());
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
				return true;
			} else if (heldItem.getItem() instanceof ItemEnderEye) {
				BlockPos loc = (ent.getTriggerOffset() == null ? null : ent.getTriggerOffset().toImmutable().add(pos));
				if (loc != null) {
					IBlockState atState = worldIn.getBlockState(loc);
					if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
						playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
					} else {
						playerIn.sendMessage(new TextComponentString("Not pointed at valid triggered block!"));
					}
				}
			}
			return true;
		}
		
		return false;
	}
}
