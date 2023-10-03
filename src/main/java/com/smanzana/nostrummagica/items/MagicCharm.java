package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.world.dimension.NostrumEmptyDimension;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicCharm extends Item implements ILoreTagged {

	public static final String ID_PREFIX = "charm_";
	public static final String MakeID(EMagicElement element) {
		return ID_PREFIX + element.name().toLowerCase();
	}
	
	protected final EMagicElement element;
	
	public MagicCharm(EMagicElement element) {
		super(NostrumItems.PropLowStack());
		this.element = element;
	}
	
	
	public static ItemStack getCharm(EMagicElement element, int count) {
		MagicCharm item = null;
		switch (element) {
		case EARTH:
			item = NostrumItems.magicCharmEarth;
			break;
		case ENDER:
			item = NostrumItems.magicCharmEnder;
			break;
		case FIRE:
			item = NostrumItems.magicCharmFire;
			break;
		case ICE:
			item = NostrumItems.magicCharmIce;
			break;
		case LIGHTNING:
			item = NostrumItems.magicCharmLightning;
			break;
		case PHYSICAL:
			item = NostrumItems.magicCharmPhysical;
			break;
		case WIND:
			item = NostrumItems.magicCharmWind;
			break;
		}
		
		return new ItemStack(item, count);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_charm_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Magical Charms";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Magical charms are constructed by mages. It's said each elemental charm has a different effect.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Magical charms are constructed by mages by stabilizing magic essences with reagents. Each element has a different effect.", "Fire charms cause a fiery eruption around the wielder.", "Ender charms teleport the user to their spawn point.", "Earth charms cause an explosion that breaks nearby blocks.", "Ice charms protect the wielder from physical and magical damages.", "Wind charms push back all nearby entities.", "Lightning charms bring lightning storms. If a storm is already happening, calls down lightning to attack all nearby entities.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		
		final ItemStack stack = playerIn.getHeldItem(hand);
		
		if (worldIn.isRemote)
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		
		boolean used = false;
		
		switch (element) {
		case EARTH:
			used = doEarth(playerIn, (ServerWorld) worldIn);
			break;
		case ENDER:
			used = doEnder(playerIn, (ServerWorld) worldIn);
			break;
		case FIRE:
			used = doFire(playerIn, (ServerWorld) worldIn);
			break;
		case ICE:
			used = doIce(playerIn, (ServerWorld) worldIn);
			break;
		case LIGHTNING:
			used = doLightning(playerIn, (ServerWorld) worldIn);
			break;
		case PHYSICAL:
			used = doPhysical(playerIn, (ServerWorld) worldIn);
			break;
		case WIND:
			used = doWind(playerIn, (ServerWorld) worldIn);
			break;
		}
		
		if (used) {
			stack.shrink(1);			
		}
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
	}
	
	private boolean doEarth(PlayerEntity player, ServerWorld world) {
		
		if (world.getDimension().getType().getId() == ModConfig.config.sorceryDimensionIndex()) {
			return false;
		}
		
		for (int x = -4; x <= 4; x++)
		for (int y = 0; y < 5; y++)
		for (int z = -4; z <= 4; z++) {
			BlockPos pos = new BlockPos(player.posX + x, player.posY + y, player.posZ + z);
			
			if (world.isAirBlock(pos))
				continue;

			BlockState state = world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				continue;
			
			float hardness = state.getBlockHardness(world, pos);
			int harvestLevel = state.getBlock().getHarvestLevel(state);
			
			if (hardness > 10 || harvestLevel > 1 || hardness < 0)
				continue;
			
			List<ItemStack> drops = Block.getDrops(state, world, pos, world.getTileEntity(pos));
			if (!drops.isEmpty()) {
				for (ItemStack drop : drops) {
					Block.spawnAsEntity(world, pos, drop);
				}
			}
			
			world.removeBlock(pos, false);
		}
		
		NostrumMagicaSounds.DAMAGE_EARTH.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doFire(PlayerEntity player, ServerWorld world) {
		
		if (world.getDimension().getType().getId() == ModConfig.config.sorceryDimensionIndex()) {
			return false;
		}
		
		for (int x = -5; x <= 5; x++)
		for (int z = -5; z <= 5; z++)
		for (int y = -3; y <= 2; y++) {
			// Y is last so we can break when we find a good spot but continue on xz plane
			
			// Leave small circle around player un-lit
			if (Math.abs(x) <= 1 && Math.abs(z) <= 1)
				break;
			
			BlockPos pos = new BlockPos(player.posX + x, player.posY + y, player.posZ + z);
			
			if (y == -3) {
				// Bottom block has to be non-air or we can't place fire
				if (world.isAirBlock(pos))
					break; // Skip whole column
				
				BlockState state = world.getBlockState(pos);
				if (!Block.hasSolidSide(state, world, pos, Direction.UP))
					break; // Same if it's not a solid block
				
				continue;
			}
			
			// Non-top block. Just need to find some air
			if (world.isAirBlock(pos)) {
				// Success!
				world.setBlockState(pos, Blocks.FIRE.getDefaultState());
				break;
			}
			
		}
		
		NostrumMagicaSounds.DAMAGE_FIRE.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doIce(PlayerEntity player, ServerWorld world) {
		player.addPotionEffect(new EffectInstance(NostrumEffects.magicShield, 20 * 60 * 2, 0));
		player.addPotionEffect(new EffectInstance(NostrumEffects.physicalShield, 20 * 60 * 2, 0));
		
		NostrumMagicaSounds.DAMAGE_ICE.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doWind(PlayerEntity player, ServerWorld world) {
		AxisAlignedBB bb = new AxisAlignedBB(
				player.posX - 3,
				player.posY - 1,
				player.posZ - 3,
				player.posX + 3,
				player.posY + 2,
				player.posZ + 3);
		List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(player, bb);
		if (entities != null && !entities.isEmpty())
			for (Entity e : entities) {
				Vec3d vec = e.getPositionVector().subtract(player.getPositionVector().add(0, -1, 0));
				vec = vec.normalize();
				vec = vec.scale(2);
				e.setVelocity(vec.x, vec.y, vec.z);
			}
		
		NostrumMagicaSounds.DAMAGE_WIND.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doEnder(PlayerEntity player, ServerWorld world) { 
		if (world.getDimension().getType().getId() == 0) {
			BlockPos pos = player.getBedLocation(world.getDimension().getType());
			if (pos == null) {
				pos = world.getSpawnPoint();
				while (!world.isAirBlock(pos)) {
					pos = pos.add(0, 2, 0);
				}
			}
			
			if (NostrumMagica.attemptTeleport(world, pos, player, !player.isSneaking(), false)) {
				NostrumMagicaSounds.DAMAGE_ENDER.play(world, player.posX, player.posY, player.posZ);
				return true;
			}
			
			return false;
			
//			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
//			if (attr != null && attr.hasEnhancedTeleport() && !player.isSneaking()) {
//				BlockPos portal = TemporaryTeleportationPortal.spawnNearby(world, player.getPosition(), 4, false, pos, 20 * 30);
//				if (portal != null) {
//					TemporaryTeleportationPortal.spawnNearby(world, pos, 4, true, portal, 20 * 30);
//				}
//			} else {
//				player.setPositionAndUpdate(pos.getX() + .5, pos.getY() + .1, pos.getZ() + .5);
//			}
//			
//			
//			return true;
		} else if (world.getDimension().getType().getId() == ModConfig.config.sorceryDimensionIndex()) {
			// In  sorcery dimension. Return to beginning
			BlockPos spawn = NostrumMagica.getDimensionMapper(player.world).register(player.getUniqueID()).getCenterPos(NostrumEmptyDimension.SPAWN_Y);
			player.setPositionAndUpdate(spawn.getX() + .5, spawn.getY() + 4, spawn.getZ() + .5);
			// Allow this type of teleportation by updating last coords...
			player.lastTickPosX = player.posX;
			player.lastTickPosY = player.posY;
			player.lastTickPosZ = player.posZ;
			player.fallDistance = 0;
			return true;
		}
		
		return false;
	}
	
	private boolean doPhysical(PlayerEntity player, ServerWorld world) {
		player.addPotionEffect(new EffectInstance(
				Effects.SPEED,
				20 * 30,
				1
				));
		
		NostrumMagicaSounds.DAMAGE_PHYSICAL.play(world, player.posX, player.posY, player.posZ);
		
		return true;
	}
	
	private boolean doLightning(PlayerEntity player, ServerWorld world) {
		if (world.isRaining()) {
			AxisAlignedBB bb = new AxisAlignedBB(
					player.posX - 5,
					player.posY - 2,
					player.posZ - 5,
					player.posX + 5,
					player.posY + 10,
					player.posZ + 5);
			List<Entity> entities = world.getEntitiesWithinAABB(LivingEntity.class, bb);
			if (entities != null && !entities.isEmpty())
				for (Entity e : entities) {
					world.addLightningBolt(new LightningBoltEntity(world, e.posX, e.posY, e.posZ, false)); // TODO nostrum lightning?
				}
		} else {
			world.getWorldInfo().setRaining(true);
			world.getWorldInfo().setThundering(true);
		}
		
		NostrumMagicaSounds.DAMAGE_LIGHTNING.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	public static int getMetaFromType(EMagicElement element) {
		if (element == null)
			return 0;
		
		return element.ordinal();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (element == EMagicElement.ENDER) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
			if (attr != null && attr.hasEnhancedTeleport()) {
				tooltip.add(new TranslationTextComponent("info.endercharm.enhanced"));
			}
		}
	}
	
}
