package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.world.dimension.NostrumEmptyDimension;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MagicCharm extends Item implements ILoreTagged {

	public static final String ID = "charm";
	
	private static MagicCharm instance = null;
	public static MagicCharm instance() {
		if (instance == null)
			instance = new MagicCharm();
		
		return instance;
	}
	
	public MagicCharm() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(16);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = getNameFromMeta(i);
		
		return this.getUnlocalizedName() + "." + suffix;
	}
	
	public static String getNameFromMeta(int meta) {
    	String suffix = "unknown";
		
    	EMagicElement type = getTypeFromMeta(meta);
    	if (type != null)
    		suffix = type.name().toLowerCase();
    	
		return suffix;
    }
	
	public static EMagicElement getTypeFromMeta(int meta) {
    	EMagicElement ret = null;
    	for (EMagicElement type : EMagicElement.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (EMagicElement element: EMagicElement.values()) {
    		subItems.add(new ItemStack(itemIn, 1, element.ordinal()));
    	}
	}
	
	public static ItemStack getCharm(EMagicElement element, int count) {
		int meta = element.ordinal();
		return new ItemStack(instance(), count, meta);
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
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote)
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		
		EMagicElement element = MagicCharm.getTypeFromMeta(stack.getMetadata());
		boolean used = false;
		
		switch (element) {
		case EARTH:
			used = doEarth(playerIn, worldIn);
			break;
		case ENDER:
			used = doEnder(playerIn, worldIn);
			break;
		case FIRE:
			used = doFire(playerIn, worldIn);
			break;
		case ICE:
			used = doIce(playerIn, worldIn);
			break;
		case LIGHTNING:
			used = doLightning(playerIn, worldIn);
			break;
		case PHYSICAL:
			used = doPhysical(playerIn, worldIn);
			break;
		case WIND:
			used = doWind(playerIn, worldIn);
			break;
		}
		
		if (used) {
			stack.stackSize--;			
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
	
	private boolean doEarth(EntityPlayer player, World world) {
		
		if (world.provider.getDimension() == ModConfig.config.sorceryDimensionIndex()) {
			return false;
		}
		
		for (int x = -4; x <= 4; x++)
		for (int y = 0; y < 5; y++)
		for (int z = -4; z <= 4; z++) {
			BlockPos pos = new BlockPos(player.posX + x, player.posY + y, player.posZ + z);
			
			if (world.isAirBlock(pos))
				continue;

			IBlockState state = world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				continue;
			
			float hardness = state.getBlockHardness(world, pos);
			int harvestLevel = state.getBlock().getHarvestLevel(state);
			
			if (hardness > 10 || harvestLevel > 1 || hardness < 0)
				continue;
			
			List<ItemStack> drops = state.getBlock().getDrops(world, pos, state, 0);
			if (drops != null && !drops.isEmpty())
				for (ItemStack drop : drops) {
					EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), drop);
					world.spawnEntityInWorld(item);
				}
			
			world.setBlockToAir(pos);
		}
		
		NostrumMagicaSounds.DAMAGE_EARTH.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doFire(EntityPlayer player, World world) {
		
		if (world.provider.getDimension() == ModConfig.config.sorceryDimensionIndex()) {
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
				
				IBlockState state = world.getBlockState(pos);
				if (!state.isSideSolid(world, pos, EnumFacing.UP))
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
	
	private boolean doIce(EntityPlayer player, World world) {
		player.addPotionEffect(new PotionEffect(MagicShieldPotion.instance(), 20 * 60 * 2, 0));
		player.addPotionEffect(new PotionEffect(PhysicalShieldPotion.instance(), 20 * 60 * 2, 0));
		
		NostrumMagicaSounds.DAMAGE_ICE.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doWind(EntityPlayer player, World world) {
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
				Vec3d vec = e.getPositionVector().subtract(player.getPositionVector().addVector(0, -1, 0));
				vec = vec.normalize();
				vec = vec.scale(2);
				e.setVelocity(vec.xCoord, vec.yCoord, vec.zCoord);
			}
		
		NostrumMagicaSounds.DAMAGE_WIND.play(world, player.posX, player.posY, player.posZ);
		return true;
	}
	
	private boolean doEnder(EntityPlayer player, World world) { 
		if (player.dimension == 0) {
			BlockPos pos = player.getBedLocation(player.dimension);
			if (pos == null) {
				pos = world.getSpawnPoint();
				while (!world.isAirBlock(pos)) {
					pos = pos.add(0, 2, 0);
				}
			}
			
			player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(world, player.posX, player.posY, player.posZ);
			return true;
		} else if (player.dimension == ModConfig.config.sorceryDimensionIndex()) {
			// In  sorcery dimension. Return to beginning
			BlockPos spawn = NostrumMagica.getDimensionMapper(player.worldObj).register(player.getUniqueID()).getCenterPos(NostrumEmptyDimension.SPAWN_Y);
			player.setPositionAndUpdate(spawn.getX() + .5, spawn.getY() + 1, spawn.getZ() + .5);
			// Allow this type of teleportation by updating last coords...
			player.lastTickPosX = player.posX;
			player.lastTickPosY = player.posY;
			player.lastTickPosZ = player.posZ;
			return true;
		}
		
		return false;
	}
	
	private boolean doPhysical(EntityPlayer player, World world) {
		player.addPotionEffect(new PotionEffect(
				Potion.getPotionFromResourceLocation("speed"),
				20 * 30,
				1
				));
		
		NostrumMagicaSounds.DAMAGE_PHYSICAL.play(world, player.posX, player.posY, player.posZ);
		
		return true;
	}
	
	private boolean doLightning(EntityPlayer player, World world) {
		if (world.isRaining()) {
			AxisAlignedBB bb = new AxisAlignedBB(
					player.posX - 5,
					player.posY - 2,
					player.posZ - 5,
					player.posX + 5,
					player.posY + 10,
					player.posZ + 5);
			List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
			if (entities != null && !entities.isEmpty())
				for (Entity e : entities) {
					world.spawnEntityInWorld(new EntityLightningBolt(world, e.posX, e.posY, e.posZ, false));
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
	
}
