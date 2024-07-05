package com.smanzana.nostrummagica.world.dungeon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.NetUtils;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonStartRoom;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.util.Constants.NBT;

public class NostrumDungeon {
	
	public static class DungeonExitPoint {
		private Direction facing;
		private BlockPos pos;
		
		public DungeonExitPoint(BlockPos pos, Direction facing) {
			this.pos = pos;
			this.facing = facing;
		}

		public Direction getFacing() {
			return facing;
		}

		public BlockPos getPos() {
			return pos;
		}
		
		@Override
		public String toString() {
			return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")[" + facing.name() + "]";
		}
		
		private static final String NBT_POS = "pos";
		private static final String NBT_DIR = "facing";
		
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.put(NBT_POS, NBTUtil.writeBlockPos(pos));
			tag.putByte(NBT_DIR, (byte) facing.getHorizontalIndex());
			return tag;
		}
		
		public static DungeonExitPoint fromNBT(CompoundNBT nbt) {
			final BlockPos pos;
			
			if (nbt.contains(NBT_POS, NBT.TAG_LONG)) {
				// Legacy
				// 1.13/1.14 changed BlockPos.fromLong, so have to use old version
				pos = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_POS));
			} else {
				pos = NBTUtil.readBlockPos(nbt.getCompound(NBT_POS));
			}
			
			Direction facing = Direction.byHorizontalIndex(nbt.getByte(NBT_DIR));
			return new DungeonExitPoint(pos, facing);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DungeonExitPoint) {
				DungeonExitPoint other = (DungeonExitPoint) o;
				return other.facing == this.facing && other.pos.equals(this.pos);
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.facing.hashCode() * 91 + this.pos.hashCode();
		}
	}

	private static Random rand = new Random();
	private int pathLen;
	private int pathRand;
	private List<IDungeonRoom> rooms;
	protected IDungeonRoom ending;
	protected IDungeonStartRoom starting;
	protected NostrumDungeon self;
	protected int color;
	
	// Cached subsets 
	private List<IDungeonRoom> endRooms;
	private List<IDungeonRoom> contRooms;
	private List<IDungeonRoom> keyRooms;
	private List<IDungeonRoom> doorRooms;
	
	// Puzzle sets per spawn run
//	private List<Path> doorPoints;
//	private List<Path> keyPoints; // Potential keys, that is
	
	public NostrumDungeon(IDungeonStartRoom starting, IDungeonRoom ending) {
		this(starting, ending, 2, 3);
	}
	
	public NostrumDungeon(IDungeonStartRoom starting, IDungeonRoom ending, int minPath, int randPath) {
		self = this;
		rooms = new LinkedList<>();
		endRooms = new LinkedList<>();
		contRooms = new LinkedList<>();
		keyRooms = new LinkedList<>();
		doorRooms = new LinkedList<>();
		this.ending = ending;
		this.starting = starting;
		this.pathLen = minPath; // minimum length of paths
		this.pathRand = randPath; // add rand(0, (pathRand-1)) to the length of paths
		this.color = 0x80602080;
		
		if (starting.getNumExits() <= 0)
			NostrumMagica.logger.warn("Dungeon created with 0-exit starting. This will not work.");
	}
	
	public NostrumDungeon add(IDungeonRoom room) {
		rooms.add(room);
		
		// invalidate cache
		endRooms.clear();
		contRooms.clear();
		keyRooms.clear();
		doorRooms.clear();
		return this;
	}
	
	public void clearRooms() {
		rooms.clear();
		// invalidate cache
		endRooms.clear();
		contRooms.clear();
		keyRooms.clear();
		doorRooms.clear();
	}
	
	public NostrumDungeon setColor(int color) {
		this.color = color;
		return this;
	}
	
	public List<DungeonRoomInstance> generate(IWorldHeightReader world, DungeonExitPoint start) {
		return generate(world, start, DungeonInstance.Random());
	}
	
	// Generates a dungeon, and returns a list of all the instances that were generated.
	// These can be used to spawn the dungeon in the world.
	public List<DungeonRoomInstance> generate(IWorldHeightReader world, DungeonExitPoint start, DungeonInstance instance) {
		// Calculate caches
		if (endRooms.isEmpty()) {
			for (IDungeonRoom room : rooms) {
				// any doors can be key room. door rooms cannot be key.
				
				if (room.getNumExits() == 0)
					endRooms.add(room);
				else
					contRooms.add(room);
				if (room.supportsKey())
					keyRooms.add(room);
				
				if (room.supportsDoor() && !room.supportsKey())
					doorRooms.add(room);
			}
		}
		
		if (contRooms.isEmpty()) {
			NostrumMagica.logger.error("No continuation rooms found in dungeon. Aborting spawn...");
			return new ArrayList<>();
		}
		
		DungeonGenerationContext context = new DungeonGenerationContext(this, rand, instance);
		Path startPath = new Path(new DungeonRoomInstance(start, this.starting, false, instance, MakeNewRoomID(context))); // Note: false means starting won't ever have key
		
		startPath.generateChildren(context, pathLen + rand.nextInt(pathRand), ending);
		
		List<DungeonRoomInstance> ret = startPath.getInstances();
				
		ret.addAll(this.starting.generateExtraPieces(world, start, rand, instance));
		return ret;
	}
	
	// Generates and then spawns a dungeon in the world immediately.
	// This doesn't do the normal structure spawning that works well on background threads
	// and instead does a blocking generate + block spawning.
	public void spawn(IWorld world, DungeonExitPoint start) {
		DungeonInstance dungeon = new DungeonInstance(UUID.randomUUID(), UUID.randomUUID());
		List<DungeonRoomInstance> dungeonInstances = generate((type, x, z) -> world.getHeight(type, x, z), start, dungeon);
		
		// Iterate and spawn instances
		// TODO I used to make sure to spawn the 'end room' last so it didn't get stomped.
		// Do that again? Or inforce bounds checking? Its not that expensive.
		for (DungeonRoomInstance instance : dungeonInstances) {
			instance.spawn(world);
		}
		
//		
//		starting.spawn(this, world, start);
//		
//		
//		
//		// Clear out puzzle info
////		doorPoints = new LinkedList<>();
////		keyPoints = new LinkedList<>();
//		
//		// Select a subpath to have the ending and another to have the key
//		int index = rand.nextInt(starting.getNumExits());
//		int key = rand.nextInt(starting.getNumExits());
//		if (index == key)
//			key = (key + 1) % starting.getNumExits();
//		IDungeonRoom inEnd;
//
//		int shrineroom = index;
//		List<DungeonExitPoint> exits = starting.getExits(start);
//		for (DungeonExitPoint exit : exits) {
//			inEnd = null;
//			
//
//			if (index == 0) {
//				; // Skip this one, so we can do it last outside the loop
//			} else {
//				
//				Path path = new Path(null, pathLen + rand.nextInt(pathRand));
//				if (key == 0) {
//					if (!keyRooms.isEmpty() && !doorRooms.isEmpty())
//						path.hasKey();
//				}
//				
//				path.spawn(world, exit, inEnd);
//			}
//			index -= 1;
//			key -= 1;
//		}
//		
//		{
//			DungeonExitPoint last = exits.get(shrineroom);
//			Path path = new Path(null, pathLen + rand.nextInt(pathRand));
//			inEnd = ending;
//			if (!keyRooms.isEmpty() && !doorRooms.isEmpty())
//				path.hasDoor();
//			
//			path.spawn(world, last, inEnd);
//		}
		
	}
	
	public void clientTick(World world, PlayerEntity player) {
		if (world.getLightFor(LightType.SKY, player.getPosition()) > 0) {
			return;
		}
		
		Random rand = player.world.rand;
		final float range = 15;
		for (int i = 0; i < 15; i++) {
			NostrumParticles.GLOW_ORB.spawn(player.world, new SpawnParams(
				1, player.getPosX() + (rand.nextGaussian() * range), player.getPosY() + (rand.nextGaussian() * 4), player.getPosZ() + (rand.nextGaussian() * range), .5,
				80, 30,
				new Vector3d(0, .025, 0), new Vector3d(.01, .0125, .01)
				).color(color));
		}
	}
	
	@SuppressWarnings("deprecation")
	public void setClientFogDensity(World world, PlayerEntity player, EntityViewRenderEvent.FogDensity event) {
		final int worldLight = world.getLightFor(LightType.SKY, player.getPosition());
		if (worldLight > 4) {
			return;
		}
		
		event.setCanceled(true);
		
		if (player.isPotionActive(Effects.BLINDNESS)) {
			//final Minecraft mc = Minecraft.getInstance();
			float farPlaneDistance = event.getRenderer().getFarPlaneDistance();
			//final Vector3d cameraPos = event.getInfo().getProjectedView();
			//boolean nearFog = ((ClientWorld) entity.world).func_239132_a_().func_230493_a_(MathHelper.floor(cameraPos.getX()), MathHelper.floor(cameraPos.getY())) || mc.ingameGUI.getBossOverlay().shouldCreateFog();
			
			int i = player.getActivePotionEffect(Effects.BLINDNESS).getDuration();
			float rangeMod = MathHelper.lerp(Math.min(1.0F, (float)i / 20.0F), farPlaneDistance, 5.0F);
			final float near;
			final float far;
			if (event.getType() == FogRenderer.FogType.FOG_SKY) {
				near = 0.0F;
				far = rangeMod * 0.8F;
			} else {
				near = rangeMod * 0.25F;
				far = rangeMod;
			}

			RenderSystem.fogStart(near);
			RenderSystem.fogEnd(far);
			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			RenderSystem.setupNvFogDistance();
			net.minecraftforge.client.ForgeHooksClient.onFogRender(event.getType(), event.getInfo(), (float) event.getRenderPartialTicks(), far);
		} else {
			if (worldLight <= 0) {
				event.setDensity(.03f);
			} else {
				final float prog = ((float) (4-worldLight) / 4f);
				event.setDensity(MathHelper.lerp(prog, .005f, .03f));
			}
		}
	}
	
	public void setClientFogColor(World world, PlayerEntity player, EntityViewRenderEvent.FogColors event) {
		final int worldLight = world.getLightFor(LightType.SKY, player.getPosition());
		if (worldLight > 4) {
			return;
		}
		
		final float prog = Math.max(0, ((float) (4-worldLight) / 4f));
		
		float[] color = ColorUtil.ARGBToColor(this.color);
		event.setRed(MathHelper.lerp(prog, event.getRed(), color[0]));
		event.setGreen(MathHelper.lerp(prog, event.getRed(), color[1]));
		event.setBlue(MathHelper.lerp(prog, event.getRed(), color[2]));
	}
	
	public static interface IWorldHeightReader {
		public int getHeight(Heightmap.Type type, int x, int z);
	}
	
	protected static class DungeonGenerationContext {
		public final NostrumDungeon dungeon;
		public final List<MutableBoundingBox> boundingBoxes;
		public final Random rand;
		public final DungeonInstance instance;
		
		public DungeonGenerationContext(NostrumDungeon dungeon, Random rand, DungeonInstance instance) {
			this.dungeon = dungeon;
			this.rand = rand;
			this.boundingBoxes = new ArrayList<>(32);
			this.instance = instance;
		}
	}
	
	public static class DungeonInstance {
		private final UUID dungeonID;
		private final NostrumWorldKey smallKey;
		private final NostrumWorldKey largeKey;
		
		private static final String NBT_DUNGEON_ID = "dungeonID";
		private static final String NBT_SMALL_KEY = "smallKey";
		private static final String NBT_LARGE_KEY = "largeKey";
		
		public DungeonInstance(UUID dungeonID, NostrumWorldKey smallKey, NostrumWorldKey largeKey) {
			this.dungeonID = dungeonID;
			this.smallKey = smallKey;
			this.largeKey = largeKey;
		}
		
		protected DungeonInstance(UUID dungeonID, UUID keyBaseID) {
			this(dungeonID,
					new NostrumWorldKey(dungeonID),
					new NostrumWorldKey(NetUtils.CombineUUIDs(dungeonID, keyBaseID)));
		}
		
		protected DungeonInstance(UUID dungeonID, Random rand) {
			this(dungeonID, NetUtils.CombineUUIDs(dungeonID, NetUtils.RandomUUID(rand)));
		}
		
		public UUID getDungeonID() {
			return this.dungeonID;
		}
		
		public static DungeonInstance Random() {
			return new DungeonInstance(UUID.randomUUID(), UUID.randomUUID());
		}
		
		public static DungeonInstance Random(Random rand) {
			return new DungeonInstance(NetUtils.RandomUUID(rand), rand);
		}
		
		public INBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putUniqueId(NBT_DUNGEON_ID, dungeonID);
			tag.put(NBT_SMALL_KEY, this.smallKey.asNBT());
			tag.put(NBT_LARGE_KEY, this.largeKey.asNBT());
			return tag;
		}
		
		public static DungeonInstance FromNBT(INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			UUID id = tag.getUniqueId(NBT_DUNGEON_ID);
			NostrumWorldKey smallKey = NostrumWorldKey.fromNBT(tag.getCompound(NBT_SMALL_KEY));
			NostrumWorldKey largeKey = NostrumWorldKey.fromNBT(tag.getCompound(NBT_LARGE_KEY));
			return new DungeonInstance(id, smallKey, largeKey);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(dungeonID, smallKey, largeKey);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof DungeonInstance) {
				DungeonInstance other = (DungeonInstance) o;
				return other.dungeonID.equals(this.dungeonID)
						&& other.smallKey.equals(this.smallKey)
						&& other.largeKey.equals(this.largeKey);
			}
			return false;
		}
	}
	
	public static class DungeonRoomInstance {
		private final DungeonExitPoint entry;
		private final IDungeonRoom template;
		private final boolean hasKey; // whether the key should be in this room
		private final DungeonInstance dungeonInstance;
		private final UUID roomID;
		
		public DungeonRoomInstance(DungeonExitPoint entry, IDungeonRoom template, boolean hasKey, DungeonInstance dungeonInstance, @Nonnull UUID roomID) {
			this.entry = entry;
			this.template = template;
			this.hasKey = hasKey;
			this.dungeonInstance = dungeonInstance;
			this.roomID = roomID;
		}

		public MutableBoundingBox getBounds() {
			if (this.template == null) {
				System.out.println("null");
			}
			return template.getBounds(this.entry);
		}
		
		public UUID getRoomID() {
			return roomID;
		}
		
		public DungeonInstance getDungeonInstance() {
			return this.dungeonInstance;
		}
		
		public void spawn(IWorld world) {
			spawn(world, null);
		}
		
		public void spawn(IWorld world, MutableBoundingBox bounds) {
			// Spawn room template
			template.spawn(world, this.entry, bounds, this.roomID);
			
			// If we have a key, do special key placement
			if (this.hasKey) {
				DungeonExitPoint keyLoc = template.getKeyLocation(this.entry);
				if (bounds == null || bounds.isVecInside(keyLoc.pos)) {
					spawnKey(world, keyLoc);
				}
			}
		}

		private void spawnKey(IWorld world, DungeonExitPoint keyLocation) {
			NonNullList<ItemStack> loot = NonNullList.withSize(27, ItemStack.EMPTY);
			for (int i = 0; i < 27; i++) {
				if (rand.nextFloat() < .2) {
					loot.set(i, new ItemStack(Items.ARROW, rand.nextInt(3) + 1));
				} else if (rand.nextFloat() < .5) {
					loot.set(i, ReagentItem.CreateStack(
							ReagentType.values()[rand.nextInt(ReagentType.values().length)],
							rand.nextInt(10) + 1));
				} else if (rand.nextFloat() < .5) {
					loot.set(i, ReagentItem.CreateStack(
							ReagentType.values()[rand.nextInt(ReagentType.values().length)],
							rand.nextInt(20) + 1));
				}
			}
			
			loot.set(rand.nextInt(27), new ItemStack(Items.GOLDEN_APPLE)); // FIXME should be key
			LootUtil.createLoot(world, keyLocation.getPos(), keyLocation.getFacing(),
					loot);
		}
		
		@Override
		public String toString() {
			return "[" + this.entry.pos + "] " + this.template.getRoomID() + ": " + this.getBounds();
		}
		
		private static final String NBT_ENTRY = "entry";
		private static final String NBT_TEMPLATE = "template";
		private static final String NBT_HASKEY = "hasKey";
		private static final String NBT_DUNGEON_INSTANCE = "dungeonInstance";
		private static final String NBT_ROOM_ID = "roomID";
		
		public @Nonnull CompoundNBT toNBT(@Nullable CompoundNBT tag) {
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			tag.put(NBT_ENTRY, this.entry.toNBT());
			tag.putString(NBT_TEMPLATE, this.template.getRoomID());
			tag.putBoolean(NBT_HASKEY, this.hasKey);
			tag.put(NBT_DUNGEON_INSTANCE, this.dungeonInstance.toNBT());
			tag.putUniqueId(NBT_ROOM_ID, roomID);
			
			return tag;
		}
		
		public static DungeonRoomInstance fromNBT(CompoundNBT tag) {
			final DungeonExitPoint entry = DungeonExitPoint.fromNBT(tag.getCompound(NBT_ENTRY));
			final IDungeonRoom template = IDungeonRoom.GetRegisteredRoom(tag.getString(NBT_TEMPLATE));
			final boolean hasKey = tag.getBoolean(NBT_HASKEY);
			final DungeonInstance instance = DungeonInstance.FromNBT(tag.get(NBT_DUNGEON_INSTANCE));
			final UUID roomID = tag.getUniqueId(NBT_ROOM_ID);
			
			return new DungeonRoomInstance(entry, template, hasKey, instance, roomID);
		}
	}
	
	// Checks if the provided room overlaps any existing bounds if it were to be spawned
	protected static boolean CheckRoomBounds(IDungeonRoom room, DungeonExitPoint entry, DungeonGenerationContext context) {
		MutableBoundingBox bounds = room.getBounds(entry);
		for (MutableBoundingBox box : context.boundingBoxes) {
			if (bounds.intersectsWith(box)) {
				return false;
			}
		}
		return true;
	}
	
	private class Path {
		
		private List<Path> children;
		//private final Path parent;
		private boolean hasKey; // whether this path will have a key when spawned
		private boolean hasDoor; // Whether the door should be spawned on this path
		
		private DungeonRoomInstance myRoom;
		
//		private int doorKey; // if is a door room, set to the key that's needed to unlock
//		// If is a key supporting room, set to the key that we have. -1 is none
//		private int numKeys; // Number of key-supporting rooms we have to have
		
		public Path(Path parent) {
//			this.remaining = remaining;
//			this.parent = parent;
//			this.firstRoom = room;
			this.hasKey = false;
			this.hasDoor = false;
			this.children = new ArrayList<>();
		}
		
		public Path(DungeonRoomInstance startingRoom) {
			this((Path) null);
			this.myRoom = startingRoom;
		}
		
		public void hasKey() {
			this.hasKey = true;
		}
		
		public void hasDoor() {
			this.hasDoor = true;
		}
		
//		public int getDoorKey() {
//			return doorKey;
//		}
		
//		/**
//		 * Return keys behind this room
//		 * @return
//		 */
//		public List<Integer> getHiddenKeys() {
//			List<Integer> keys = null;
//			if (children.isEmpty()) {
//				keys = new LinkedList<>();
//			} else {
//				for (Path child : children) {
//					if (keys == null)
//						keys = child.getHiddenKeys();
//					else
//						keys.addAll(child.getHiddenKeys());
//				}
//			}
//			
//			if (this.doorKey != -1)
//				keys.add(doorKey);
//			
//			return keys;
//		}
		
//		public Path getDoor(int key) {
//			for (Path path : doorPoints) {
//				if (path.getDoorKey() == key)
//					return path;
//			}
//			
//			return null;
//		}
		
		protected @Nonnull IDungeonRoom pickRandomContRoom(DungeonGenerationContext context, DungeonExitPoint entry, int remaining) {
			List<IDungeonRoom> eligibleRooms = contRooms.stream().filter(r -> r.getRoomCost() <= remaining).filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
			if (eligibleRooms.isEmpty()) {
				NostrumMagica.logger.warn("Failed to find a cont room that fit. Picking a random one for start " + entry);
				return contRooms.get(rand.nextInt(contRooms.size()));
			}
			return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
		}
		
		protected @Nonnull IDungeonRoom pickRandomEndRoom(DungeonGenerationContext context, DungeonExitPoint entry) {
			if (endRooms.isEmpty()) {
				return rooms.get(rand.nextInt(rooms.size()));
			} else {
				List<IDungeonRoom> eligibleRooms = endRooms.stream().filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
				if (eligibleRooms.isEmpty()) {
					NostrumMagica.logger.warn("Failed to find an end room that fit. Picking a random one for start " + entry);
					return endRooms.get(rand.nextInt(endRooms.size()));
				}
				return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
			}
		}
		
		protected @Nonnull IDungeonRoom pickRandomKeyRoom(DungeonGenerationContext context, DungeonExitPoint entry) {
			List<IDungeonRoom> eligibleRooms = keyRooms.stream().filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
			if (eligibleRooms.isEmpty()) {
				NostrumMagica.logger.warn("Failed to find a key room that fit. Picking a random one for start " + entry);
				return keyRooms.get(rand.nextInt(keyRooms.size()));
			}
			return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
		}
		
		protected @Nonnull IDungeonRoom pickRandomDoorRoom(DungeonGenerationContext context, DungeonExitPoint entry) {
			List<IDungeonRoom> eligibleRooms = doorRooms.stream().filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
			if (eligibleRooms.isEmpty()) {
				NostrumMagica.logger.warn("Failed to find a door room that fit. Picking a random one for start " + entry);
				return doorRooms.get(rand.nextInt(doorRooms.size()));
			}
			return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
		}
		
		// Fill out this path, including a room for this node and spawning any children that are needed.
		protected void generate(DungeonGenerationContext context, int remaining, DungeonExitPoint entry, IDungeonRoom ending) {
			Validate.isTrue(this.myRoom == null); // If room is already set, only generate children!
			
			/*
			 * 0) If remaining is 0, spawn end if we have it. Otherwise, if we
			 *    somehow still have the key, spawn a keyroom. Otherwise, try to
			 *    find a room that has 0 exists and spawn taht. Otherwise, spawn
			 *    whatever.
			 * 2) If we have a door or key, roll to spawn that. If we have both,
			 *    roll for key first. Otherwise place regular >0 door room.
			 */
			if (remaining <= 0) {
				// Terminal
				if (ending != null) {
					this.myRoom = new DungeonRoomInstance(entry, ending, false, context.instance, MakeNewRoomID(context));
				} else if (this.hasKey) {
					this.myRoom = new DungeonRoomInstance(entry, pickRandomKeyRoom(context, entry), true, context.instance, MakeNewRoomID(context));
				} else {
					this.myRoom = new DungeonRoomInstance(entry, pickRandomEndRoom(context, entry), false, context.instance, MakeNewRoomID(context));
				}
				
				if (myRoom != null) {
					final MutableBoundingBox innerBounds = myRoom.getBounds().func_215127_b(1, 1, 1);
					innerBounds.maxX -= 2;
					innerBounds.maxY -= 2;
					innerBounds.maxZ -= 2;
					context.boundingBoxes.add(innerBounds);
				}
			} else {
				// If we have door or key, try those first
				// If we have both, roll for key first
				// Do not do door if we have both and key didn'tt succeed
				myRoom = null;
				if (hasKey) {
					if (rand.nextFloat() < 1.0f / ((float) remaining + 1)) {
						myRoom = new DungeonRoomInstance(entry, pickRandomKeyRoom(context, entry), true, context.instance, MakeNewRoomID(context));
						hasKey = false;
					}
				} else if (hasDoor) {
					if (rand.nextFloat() < 1.0f / ((float) remaining + 1)) {
						myRoom = new DungeonRoomInstance(entry, pickRandomDoorRoom(context, entry), false, context.instance, MakeNewRoomID(context));
						hasDoor = false;
					}
				}
				
				if (myRoom == null) {
					myRoom = new DungeonRoomInstance(entry, pickRandomContRoom(context, entry, remaining), false, context.instance, MakeNewRoomID(context));
				}

				if (myRoom != null) {
					final MutableBoundingBox innerBounds = myRoom.getBounds().func_215127_b(1, 1, 1);
					innerBounds.maxX -= 2;
					innerBounds.maxY -= 2;
					innerBounds.maxZ -= 2;
					context.boundingBoxes.add(innerBounds);
				}
				
				this.generateChildren(context, remaining - (myRoom.template.getRoomCost()), ending);
			}
		}
		
		// Fill out this path's children
		protected void generateChildren(DungeonGenerationContext context, int remaining, IDungeonRoom ending) {
			// Select a subpath to have the ending
			int keyI = -1;
			int doorI = -1;
			IDungeonRoom inEnd;
			
			if (hasKey) {
				keyI = rand.nextInt(myRoom.template.getNumExits());
			}
			if (hasDoor || ending != null) {
				doorI = rand.nextInt(myRoom.template.getNumExits());
			}
			
			if (keyI != -1 && doorI != -1 && keyI == doorI) {
				doorI = (doorI + 1) % myRoom.template.getNumExits();
			}

			// Add subpaths based on doors
			for (DungeonExitPoint door : myRoom.template.getExits(myRoom.entry)) {
				Path path = new Path(this);
				inEnd = null;
				if (doorI == 0) {
					if (hasDoor)
						path.hasDoor();
					inEnd = ending; // just set to null again if we don't have one 
				}
				if (keyI == 0) {
					path.hasKey();
				}
				
				// TODO evaluate making 'remaining' be random to be like 1-remaining
				path.generate(context, remaining, door, inEnd);
				keyI -= 1;
				doorI -= 1;
				this.children.add(path);
			}
		}
		
		public List<DungeonRoomInstance> getInstances() {
			return getInstances(new ArrayList<>());
		}
		
		public List<DungeonRoomInstance> getInstances(@Nonnull List<DungeonRoomInstance> list) {
			list.add(this.myRoom);
			
			for (Path child : this.children) {
				child.getInstances(list);
			}
			
			return list;
		}
	}
	
	public static DungeonExitPoint asRotated(DungeonExitPoint start, BlockPos offset, Direction facing) {
		int modX = 1;
		int modZ = 1;
		boolean swap = false;
		switch (start.getFacing()) {
		case EAST:
			swap = true;
			modX = -1;
			break;
		case SOUTH:
			modX = -1;
			modZ = -1;
			break;
		case NORTH: // -z
		default:
			break;
		case WEST: // -x
			swap = true;
			modZ = -1;
			break;
		}
		

		BlockPos pos = start.getPos();
		int x = offset.getX();
		int z = offset.getZ();
		if (swap) {
			int t = x;
			x = z;
			z = t;
		}
		x *= modX;
		z *= modZ;
		
		pos = new BlockPos(pos.getX() + x, pos.getY() + offset.getY(), pos.getZ() + z);
		
		int rot;
		Direction out = start.facing;
		switch (facing) {
		case NORTH:
		default:
			rot = 0;
			break;
		case EAST:
			rot = 1;
			break;
		case SOUTH:
			rot = 2;
			break;
		case WEST:
			rot = 3;
			break;
		}
		
		while (rot-- > 0)
			out = out.rotateY();
			
		return new DungeonExitPoint(pos, out);
	}
	
	/**
	 * Generate a new room ID based on the current generation context.
	 * Uses the context's random to provide a consistent ID that is also based on the dungeon ID.
	 * @param context
	 * @return
	 */
	protected static final UUID MakeNewRoomID(DungeonGenerationContext context) {
		return NetUtils.CombineUUIDs(context.instance.getDungeonID(), NetUtils.RandomUUID(context.rand));
	}
}
