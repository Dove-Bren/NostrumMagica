package com.smanzana.nostrummagica.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.TeleportRuneTileEntity;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TeleportRune extends BlockContainer  {
	
	public static final String ID = "teleport_rune";
	protected static final AxisAlignedBB RUNE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
	
	private static TeleportRune instance = null;
	public static TeleportRune instance() {
		if (instance == null) {
			instance = new TeleportRune();
		}
		
		return instance;
	}
	
	public TeleportRune() {
		super(Material.CLOTH, MapColor.OBSIDIAN);
		this.setUnlocalizedName(ID);
		this.setHardness(5.0f);
		this.setResistance(5.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setTickRandomly(true);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public int getLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
		//return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		return RUNE_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	protected void teleportEntity(World worldIn, BlockPos portalPos, Entity entityIn) {
		TileEntity te = worldIn.getTileEntity(portalPos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		BlockPos offset = ent.getOffset();
		if (offset == null) {
			return;
		}
		
		BlockPos target = portalPos.add(offset);
		entityIn.lastTickPosX = entityIn.prevPosX = target.getX() + .5;
		entityIn.lastTickPosY = entityIn.prevPosY = target.getY() + .005;
		entityIn.lastTickPosZ = entityIn.prevPosZ = target.getZ() + .5;
		
		if (!worldIn.isRemote) {
			NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				//Event type, LivingEntity entity, T data
				entityIn.setPositionAndUpdate(target.getX() + .5, target.getY() + .005, target.getZ() + .5);
	
				double dx = target.getX() + .5;
				double dy = target.getY() + 1;
				double dz = target.getZ() + .5;
				for (int i = 0; i < 10; i++) {
					
					((WorldServer) worldIn).spawnParticle(EnumParticleTypes.DRAGON_BREATH,
							dx,
							dy,
							dz,
							10,
							.25,
							.6,
							.25,
							.1,
							new int[0]);
				}
				NostrumMagicaSounds.DAMAGE_ENDER.play(worldIn, dx, dy, dz);
				return true;
			}, 1, 0);
		}
	}
	
	// Note: Just doing a dumb little static map cause we don't really care to persist portal cooldowns. Just
	// There to be nice to players.
	private static final Map<UUID, Integer> EntityTeleportCharge = new HashMap<>();
	
	// How long entities must wait in the teleporation block before they teleport
	public static final int TELEPORT_CHARGE_TIME = 2;
	public static final int TELEPORT_RANGE = 64;
	
	private static boolean DumbIntegratedGuard = false;
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, BlockState state, Entity entityIn) {
		Integer charge = EntityTeleportCharge.get(entityIn.getUniqueID());
		if (charge == null) {
			charge = 0;
		}
		
		if (worldIn.isRemote && entityIn == NostrumMagica.proxy.getPlayer() && ((!DumbIntegratedGuard && charge == 0) || (DumbIntegratedGuard && charge == 2))) {
			entityIn.playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, 1f, (4f / (float) TELEPORT_CHARGE_TIME));
		}
		
		if (!DumbIntegratedGuard) {
			charge += 2;
			DumbIntegratedGuard = true;
		}
		
		
		if (charge > TELEPORT_CHARGE_TIME * 20) {
			EntityTeleportCharge.put(entityIn.getUniqueID(), -(TELEPORT_CHARGE_TIME * 20));
			if (!worldIn.isRemote) {
				this.teleportEntity(worldIn, pos, entityIn);
			}
		} else {
			EntityTeleportCharge.put(entityIn.getUniqueID(), charge);
			if (worldIn.isRemote && charge >= 0) {
				int count = (charge / 20) / TELEPORT_CHARGE_TIME;
				for (int i = 0; i < count + 1; i++) {
					double dx = pos.getX() + .5;
					double dy = pos.getY() + .5;
					double dz = pos.getZ() + .5;
					
					double mx = .25 * (NostrumMagica.rand.nextFloat() - .5f);
					double my = .5 * (NostrumMagica.rand.nextFloat() - .5f);
					double mz = .25 * (NostrumMagica.rand.nextFloat() - .5f);
					worldIn.spawnParticle(EnumParticleTypes.DRAGON_BREATH, dx + mx, dy, dz + mz, mx / 3, my, mz / 3, new int[0]);
				}
			}
		}
	}
	
	public static void tick() {
		Iterator<UUID> it = EntityTeleportCharge.keySet().iterator();
		while (it.hasNext()) {
			UUID key = it.next();
			Integer charge = EntityTeleportCharge.get(key);
			if (charge != null && charge > 0) {
				charge--;
			}
			
			if (charge == null || charge == 0) {
				it.remove();
			} else {
				EntityTeleportCharge.put(key, charge);
			}
		}
		DumbIntegratedGuard = false;
	}
	
	public static void resetTimers() {
		EntityTeleportCharge.clear();
	}
	
	public static int getRemainingCharge(Entity ent) {
		Integer charge = EntityTeleportCharge.get(ent.getPersistentID());
		return TELEPORT_CHARGE_TIME - (charge == null ? 0 : charge) * 20; 
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof PositionCrystal)) {
			return false;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return true;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
		if (heldPos == null) {
			return true;
		}
		
		if (!playerIn.isCreative()) {
			// 1) has to be another teleport rune there, and 2) has to be within X blocks
			if (!NostrumMagica.isBlockLoaded(worldIn, heldPos)) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.unloaded"));
				return true;
			}
			
			BlockState targetState = worldIn.getBlockState(heldPos);
			if (targetState == null || !(targetState.getBlock() instanceof TeleportRune)) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.norune"));
				return true;
			}
			
			int dist = Math.abs(heldPos.getX() - pos.getX())
					+ Math.abs(heldPos.getY() - pos.getY())
					+ Math.abs(heldPos.getZ() - pos.getZ());
			
			boolean hasEnderBelt = false;
			// Look for lightning belt
			IInventory baubles = NostrumMagica.baubles.getBaubles(playerIn);
			if (baubles != null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					ItemStack stack = baubles.getStackInSlot(i);
					if (stack.isEmpty() || !(stack.getItem() instanceof ItemMagicBauble)) {
						continue;
					}
					
					ItemType type = ItemMagicBauble.getTypeFromMeta(stack.getMetadata());
					if (type == ItemType.BELT_ENDER) {
						hasEnderBelt = true;
						break;
					}
				}
			}
			final boolean hasEnderSet = EnchantedArmor.GetSetCount(playerIn, EMagicElement.ENDER, 3) == 4;
			final double range = TELEPORT_RANGE * (hasEnderBelt ? 2 : 1) * (hasEnderSet ? 2 : 1);
			
			if (dist > range) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.toofar"));
				return true;
			}
		}
		
		BlockState targetState = worldIn.getBlockState(heldPos);
		if (targetState != null && targetState.getBlock() instanceof TeleportRune) {
			;
		} else {
			heldPos = heldPos.up();
		}
		
		ent.setTargetPosition(heldPos);
		playerIn.sendMessage(new TranslationTextComponent("info.generic.block_linked"));
		
		// If creative, can target tele tiles that are pointing to other ones. But, if it's not pointing anywhere, we'll conveniently hook them up.
		// Non-creative placement forces them to be linked to eachother, though.
		boolean shouldLink = true;
		if (playerIn.isCreative()) {
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				shouldLink = (otherEnt.getOffset() == null);
			}
		}
		
		if (shouldLink) {
			BlockPos oldOffset = null;
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				oldOffset = otherEnt.getOffset();
				otherEnt.setTargetPosition(pos);
			}
			
			if (oldOffset != null && !playerIn.isCreative()) {
				// Unlink old one, too!
				otherTE = worldIn.getTileEntity(heldPos.add(oldOffset));
				if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
					TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
					otherEnt.setTargetPosition(null);
				}
			}
		}
		
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TeleportRuneTileEntity();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		if (ent.getOffset() != null) {
			double dx = pos.getX() + .5;
			double dy = pos.getY() + .1;
			double dz = pos.getZ() + .5;
			
			double mx = 1 * (rand.nextFloat() - .5f);
			double mz = 1 * (rand.nextFloat() - .5f);
			
			worldIn.spawnParticle(EnumParticleTypes.PORTAL, dx + mx, dy, dz + mz, mx / 3, 0.0D, mz / 3, new int[0]);
		}
	}
}
