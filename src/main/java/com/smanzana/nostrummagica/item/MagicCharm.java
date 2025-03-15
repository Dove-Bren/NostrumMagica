package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
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
	
	public static Item getCharmItem(EMagicElement element) {
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
		return item;
	}
	
	public static ItemStack getCharm(EMagicElement element, int count) {
		return new ItemStack(getCharmItem(element), count);
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
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		
		final ItemStack stack = playerIn.getItemInHand(hand);
		
		if (worldIn.isClientSide)
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		
		boolean used = false;
		
		switch (element) {
		case EARTH:
			used = doEarth(playerIn, (ServerWorld) worldIn);
			break;
		case ENDER:
			used = doEnder((ServerPlayerEntity) playerIn, (ServerWorld) worldIn);
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
		
		if (DimensionUtils.IsSorceryDim(world)) {
			return false;
		}
		
		for (int x = -4; x <= 4; x++)
		for (int y = 0; y < 5; y++)
		for (int z = -4; z <= 4; z++) {
			BlockPos pos = new BlockPos(player.getX() + x, player.getY() + y, player.getZ() + z);
			
			if (world.isEmptyBlock(pos))
				continue;

			BlockState state = world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				continue;
			
			float hardness = state.getDestroySpeed(world, pos);
			int harvestLevel = state.getBlock().getHarvestLevel(state);
			
			if (hardness > 10 || harvestLevel > 1 || hardness < 0)
				continue;
			
			List<ItemStack> drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos));
			if (!drops.isEmpty()) {
				for (ItemStack drop : drops) {
					Block.popResource(world, pos, drop);
				}
			}
			
			world.removeBlock(pos, false);
		}
		
		NostrumMagicaSounds.DAMAGE_EARTH.play(world, player.getX(), player.getY(), player.getZ());
		return true;
	}
	
	private boolean doFire(PlayerEntity player, ServerWorld world) {
		
		if (DimensionUtils.IsSorceryDim(world)) {
			return false;
		}
		
		for (int x = -5; x <= 5; x++)
		for (int z = -5; z <= 5; z++)
		for (int y = -3; y <= 2; y++) {
			// Y is last so we can break when we find a good spot but continue on xz plane
			
			// Leave small circle around player un-lit
			if (Math.abs(x) <= 1 && Math.abs(z) <= 1)
				break;
			
			BlockPos pos = new BlockPos(player.getX() + x, player.getY() + y, player.getZ() + z);
			
			if (y == -3) {
				// Bottom block has to be non-air or we can't place fire
				if (world.isEmptyBlock(pos))
					break; // Skip whole column
				
				if (!Block.canSupportRigidBlock(world, pos))
					break; // Same if it's not a solid block
				
				continue;
			}
			
			// Non-top block. Just need to find some air
			if (world.isEmptyBlock(pos)) {
				// Success!
				world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
				break;
			}
			
		}
		
		NostrumMagicaSounds.DAMAGE_FIRE.play(world, player.getX(), player.getY(), player.getZ());
		return true;
	}
	
	private boolean doIce(PlayerEntity player, ServerWorld world) {
		player.addEffect(new EffectInstance(NostrumEffects.magicShield, 20 * 60 * 2, 0));
		player.addEffect(new EffectInstance(NostrumEffects.physicalShield, 20 * 60 * 2, 0));
		
		NostrumMagicaSounds.DAMAGE_ICE.play(world, player.getX(), player.getY(), player.getZ());
		return true;
	}
	
	private boolean doWind(PlayerEntity player, ServerWorld world) {
		AxisAlignedBB bb = new AxisAlignedBB(
				player.getX() - 3,
				player.getY() - 1,
				player.getZ() - 3,
				player.getX() + 3,
				player.getY() + 2,
				player.getZ() + 3);
		List<Entity> entities = world.getEntities(player, bb);
		if (entities != null && !entities.isEmpty())
			for (Entity e : entities) {
				Vector3d vec = e.position().subtract(player.position().add(0, -1, 0));
				vec = vec.normalize();
				vec = vec.scale(2);
				e.lerpMotion(vec.x, vec.y, vec.z);
			}
		
		NostrumMagicaSounds.DAMAGE_WIND.play(world, player.getX(), player.getY(), player.getZ());
		return true;
	}
	
	private boolean doEnder(ServerPlayerEntity player, ServerWorld world) { 
		if (DimensionUtils.InDimension(player, player.getRespawnDimension())) {
			@Nullable BlockPos posOpt = player.getRespawnPosition();
			BlockPos pos;
			if (posOpt == null) {
				pos = world.getSharedSpawnPos();
				while (!world.isEmptyBlock(pos)) {
					pos = pos.offset(0, 2, 0);
				}
			} else {
				pos = posOpt;
			}
			
			if (NostrumMagica.attemptTeleport(new Location(world, pos), player, !player.isShiftKeyDown(), false, player)) {
				NostrumMagicaSounds.DAMAGE_ENDER.play(world, player.getX(), player.getY(), player.getZ());
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
		} else if (DimensionUtils.IsSorceryDim(world)) {
			// In  sorcery dimension. Return to beginning
			BlockPos spawn = NostrumMagica.getDimensionMapper(player.level).register(player.getUUID()).getCenterPos(NostrumSorceryDimension.SPAWN_Y);
			player.teleportTo(spawn.getX() + .5, spawn.getY() + 4, spawn.getZ() + .5);
			// Allow this type of teleportation by updating last coords...
			player.xOld = player.getX();
			player.yOld = player.getY();
			player.zOld = player.getZ();
			player.fallDistance = 0;
			return true;
		}
		
		return false;
	}
	
	private boolean doPhysical(PlayerEntity player, ServerWorld world) {
		player.addEffect(new EffectInstance(
				Effects.MOVEMENT_SPEED,
				20 * 30,
				1
				));
		
		NostrumMagicaSounds.DAMAGE_PHYSICAL.play(world, player.getX(), player.getY(), player.getZ());
		
		return true;
	}
	
	private boolean doLightning(PlayerEntity player, ServerWorld world) {
		if (world.isRaining()) {
			AxisAlignedBB bb = new AxisAlignedBB(
					player.getX() - 5,
					player.getY() - 2,
					player.getZ() - 5,
					player.getX() + 5,
					player.getY() + 10,
					player.getZ() + 5);
			List<Entity> entities = world.getEntitiesOfClass(LivingEntity.class, bb);
			if (entities != null && !entities.isEmpty())
				for (Entity e : entities) {
					LightningBoltEntity bolt = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, world);
					bolt.setPos(e.getX(), e.getY(), e.getZ());
					bolt.setVisualOnly(false);
					world.addFreshEntity(bolt); // TODO nostrum lightning?
				}
		} else {
			world.getLevelData().setRaining(true);
			((IServerWorldInfo) world.getLevelData()).setThundering(true);
		}
		
		NostrumMagicaSounds.DAMAGE_LIGHTNING.play(world, player.getX(), player.getY(), player.getZ());
		return true;
	}
	
	public static int getMetaFromType(EMagicElement element) {
		if (element == null)
			return 0;
		
		return element.ordinal();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (element == EMagicElement.ENDER) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
			if (attr != null && attr.hasEnhancedTeleport()) {
				tooltip.add(new TranslationTextComponent("info.endercharm.enhanced"));
			}
		}
	}
	
}
