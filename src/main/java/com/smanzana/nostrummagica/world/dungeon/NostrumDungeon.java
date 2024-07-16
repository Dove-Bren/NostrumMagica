package com.smanzana.nostrummagica.world.dungeon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.JavaUtils;
import com.smanzana.nostrummagica.util.NetUtils;
import com.smanzana.nostrummagica.world.NostrumWorldKey;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry.DungeonRoomRecord;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonStartRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef;
import com.smanzana.nostrummagica.world.dungeon.room.IDungeonRoomRef.DungeonRoomRef;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
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
	
	private static final Random rand = new Random();
	
	private final String tag;
	private final int pathLen;
	private final int pathRand;
	//private final List<IDungeonRoomRef<?>> rooms;
	protected final IDungeonRoomRef<?> ending;
	protected final DungeonStartRoom starting;
	protected final NostrumDungeon self;
	
	protected int color;
	
	// Puzzle sets per spawn run
//	private List<Path> doorPoints;
//	private List<Path> keyPoints; // Potential keys, that is
	
	public NostrumDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending) {
		this(tag, starting, ending, 2, 3);
	}
	
	public NostrumDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending, int minPath, int randPath) {
		this.tag = tag;
		self = this;
		//rooms = new ArrayList<>();
		this.ending = ending;
		this.starting = starting;
		this.pathLen = minPath; // minimum length of paths
		this.pathRand = randPath; // add rand(0, (pathRand-1)) to the length of paths
		this.color = 0x80602080;
	}
	
	@Deprecated
	public NostrumDungeon add(IDungeonRoomRef<?> room) {
		//rooms.add(room);
		return this;
	}

	@Deprecated
	public void clearRooms() {
		//rooms.clear();
	}
	
	protected List<IDungeonRoomRef<?>> getRooms() {
		List<IDungeonRoomRef<?>> ret = new ArrayList<>();
		
		for (DungeonRoomRecord record : DungeonRoomRegistry.GetInstance().getAllRooms(tag)) {
			ret.add(new DungeonRoomRef(record.room.getRoomID()));
		}
		
		return ret;
	}
	
	public NostrumDungeon setColor(int color) {
		this.color = color;
		return this;
	}
	
	public List<DungeonRoomInstance> generate(IWorldHeightReader world, BlueprintLocation start) {
		return generate(world, start, DungeonInstance.Random());
	}
	
	// Generates a dungeon, and returns a list of all the instances that were generated.
	// These can be used to spawn the dungeon in the world.
	public List<DungeonRoomInstance> generate(IWorldHeightReader world, BlueprintLocation start, DungeonInstance instance) {
		DungeonGenerationContext context = new DungeonGenerationContext(this, rand, instance);
		
		// Calculate caches
		{
			for (IDungeonRoomRef<?> room : getRooms()) {
				if (!room.isValid()) {
					continue;
				}
				
				if (room.supportsKey())
					context.keyRooms.add(room);
				else if (room.supportsDoor())
					context.doorRooms.add(room);
				else if (room.getNumExits() == 0)
					context.endRooms.add(room);
				else
					context.contRooms.add(room);
			}
		}
		
		if (context.contRooms.isEmpty()) {
			NostrumMagica.logger.error("No continuation rooms found in dungeon. Aborting spawn...");
			return new ArrayList<>();
		}
		
		int unused; // This is still putting large key doors before the boss room even when supportsPuzzle is false
		final boolean supportsPuzzle = (!context.keyRooms.isEmpty() && !context.doorRooms.isEmpty());
		
		Path startPath = new Path(new DungeonRoomInstance(start, this.starting.getLobby(), false, false, instance, MakeNewRoomID(context))); // Note: false means starting won't ever have key
		
		startPath.generateChildren(context, pathLen + rand.nextInt(pathRand), ending, supportsPuzzle);
		
		addPuzzle(startPath, context);
		
		List<DungeonRoomInstance> ret = startPath.getInstances();
		ret.addAll(this.starting.generateExtraPieces(world, start, rand, instance));
		return ret;
	}
	
	private void makeSmallDoor(DungeonRoomInstance room, BlueprintLocation entry, DungeonGenerationContext context) {
		room.addSmallDoor(GetDoorAdjacent(entry, true));
	}
	
	private void addSmallKey(DungeonRoomInstance room, DungeonGenerationContext context) {
		room.addSmallKey();
	}
	
	private void addPuzzle(Path root, DungeonGenerationContext context) {
		// Puzzle algo is as follows:
		//   1) Before final ending room, put a LARGE door
		//   2) Somewhere else in dungeon (excluding ending or door rooms), put a LARGE key
		//   3) Let curKey be the room with LARGE key and curDoor be the same room.
		//   4) Let curPool be all rooms excluding the end room, LARGE door room, and curKey.
		//   5) Loop:
		//      6) Resolve common ancestor between curKey and curDoor
		//      7) IF ancestor is root, STOP
		//      8) Pick entry on path from common ancestor to root and create a SMALL door. Update curDoor to the room with this entry as an exit.
		//      9) Reduce curPool by removing all children of curDoor
		//     10) Pick random room from curPool and add a SMALL key. Update curKey to be this room.
		//     11) ENDLOOP (go back to 6)
		//
		// Note that steps 1 and 2 are done during initial generation but are validated here.
		
		// Find LARGE door and key rooms and verify both exist
		Path largeDoor = root.findChild(p -> p.hasLargeDoor);
		Path largeKey = root.findChild(p -> p.hasLargeKey);
		
		if (largeDoor == null || largeKey == null) {
			NostrumMagica.logger.warn("Could not make puzzle in dungeon, as door and key rooms are not both present");
			return;
		} else {
			// #3
			Path curKey = largeKey;
			Path curDoor = largeKey;
			
			// #4
			Set<Path> curPool = new HashSet<>();
			root.addSelfAndChildren(curPool, p -> p.myRoom.template.supportsTreasure());
			largeDoor.removeSelfAndChildren(curPool);
			largeKey.removeSelfAndChildren(curPool);
			if (curPool.isEmpty()) {
				NostrumMagica.logger.warn("Could not make puzzle in dungeon, as first-pass pool of rooms was empty");
				return;
			}
			
			// Stats
			int loops = 0;
			int addedDoors = 0;
			int addedKeys = 0;
			
			// #5
			while (true) {
				loops++;
				
				// #6
				Path commonParent = curKey.getCommonParent(curDoor);
				// #7
				if (commonParent.isRoot()) {
					break;
				}
				
				// #8
				// Want to only go back 1 or 2 nodes to try and make more complex puzzles by default.
				// Do so by peeking and seeing if what comes after us is the root already or not
				final Path roomBeforeDoor;
				final BlueprintLocation doorEntry;
				
				final Path roomAfterDoor;
				Deque<Path> rootPath = commonParent.getRootPath();
				rootPath.removeLast(); // will be commonParent
				if (loops == 1 || context.rand.nextBoolean() || rootPath.peekLast().isRoot()) {
					// Either we rolled a 1, this is the large key room, or only possible place is 1 back (which will be root)
					roomAfterDoor = commonParent;
				} else {
					roomAfterDoor = rootPath.peekLast();
				}
				doorEntry = roomAfterDoor.myRoom.entry;
				roomBeforeDoor = roomAfterDoor.parent;
				
				makeSmallDoor(roomBeforeDoor.myRoom, doorEntry, context);
				curDoor = roomBeforeDoor;
				addedDoors++;
				
				// #9
				// Reduce curpool
				roomAfterDoor.removeSelfAndChildren(curPool);
				
				// #10 Pick new key location
				final Path newKeyRoom = JavaUtils.GetRandom(curPool, context.rand).orElse(root);
				addSmallKey(newKeyRoom.myRoom, context);
				curKey = newKeyRoom;
				addedKeys++;
			}
			
			NostrumMagica.logger.debug("Generated dungeon puzzle in " + loops + " iterations with " + addedKeys + " keys and " + addedDoors + " doors");
		}
	}
	
	// Generates and then spawns a dungeon in the world immediately.
	// This doesn't do the normal structure spawning that works well on background threads
	// and instead does a blocking generate + block spawning.
	public void spawn(IWorld world, BlueprintLocation start) {
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
		public final List<IDungeonRoomRef<?>> endRooms;
		public final List<IDungeonRoomRef<?>> contRooms;
		public final List<IDungeonRoomRef<?>> keyRooms;
		public final List<IDungeonRoomRef<?>> doorRooms;
		
		public DungeonGenerationContext(NostrumDungeon dungeon, Random rand, DungeonInstance instance) {
			this.dungeon = dungeon;
			this.rand = rand;
			this.boundingBoxes = new ArrayList<>(32);
			this.instance = instance;
			this.endRooms = new ArrayList<>();
			this.contRooms = new ArrayList<>();
			this.keyRooms = new ArrayList<>();
			this.doorRooms = new ArrayList<>();
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
		
		public NostrumWorldKey getSmallKey() {
			return smallKey;
		}

		public NostrumWorldKey getLargeKey() {
			return largeKey;
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
		private final BlueprintLocation entry;
		private final IDungeonRoom template;
		private final boolean hasLargeKey; // whether the key should be in this room
		private final boolean hasLargeDoor; // Whether a large key door is in this room and should et stamped to be dungeon key
		private final DungeonInstance dungeonInstance;
		private final UUID roomID;
		
		// Puzzle mechanics that can be turned on after construction
		private final List<BlueprintLocation> smallDoors; // What (if any) exits should have small doors
		private boolean hasSmallKey;
		
		public DungeonRoomInstance(BlueprintLocation entry, IDungeonRoom template, boolean hasKey, boolean hasLargeDoor, DungeonInstance dungeonInstance, @Nonnull UUID roomID) {
			this.entry = entry;
			this.template = template;
			this.hasLargeKey = hasKey;
			this.hasLargeDoor = hasLargeDoor;
			this.dungeonInstance = dungeonInstance;
			this.roomID = roomID;
			this.smallDoors = new ArrayList<>(1);
			this.hasSmallKey = false;
		}
		
		protected void addSmallDoor(BlueprintLocation exit) {
			smallDoors.add(exit);
		}
		
		protected void addSmallKey() {
			this.hasSmallKey = true;
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
			if (this.hasLargeKey) {
				BlueprintLocation keyLoc = template.getKeyLocation(this.entry);
				if (bounds == null || bounds.isVecInside(keyLoc.getPos())) {
					spawnLargeKey(world, keyLoc);
				}
			}
			if (this.hasLargeDoor) {
				BlueprintLocation doorLoc = template.getDoorLocation(this.entry);
				if (bounds == null || bounds.isVecInside(doorLoc.getPos())) {
					spawnLargeDoor(world, doorLoc);
				}
			}
			if (this.hasSmallKey) {
				if (!this.template.supportsTreasure()) {
					NostrumMagica.logger.fatal("Room is meant to have a small key, but has no treasure locations");
				} else {
					// pick small key location based on something deterministic so that it'll be the same
					// even if we can't spawn it in this call
					Random rand = new Random(this.roomID.getLeastSignificantBits() ^ this.roomID.getMostSignificantBits());
					List<BlueprintLocation> treasureSpots = this.template.getTreasureLocations(this.entry);
					BlueprintLocation spot = treasureSpots.get((int) (rand.nextFloat() * treasureSpots.size()));
					if (bounds == null || bounds.isVecInside(spot.getPos())) {
						spawnSmallKey(world, spot);
					}
				}
			}
			for (BlueprintLocation smallDoor : this.smallDoors) {
				if (bounds == null || bounds.isVecInside(smallDoor.getPos())) {
					spawnSmallDoor(world, smallDoor, bounds);
				}
			}
		}

		private void spawnLargeKey(IWorld world, BlueprintLocation keyLocation) {
			// Technically, this spawns at two positions and could go out of bounds
			NostrumBlocks.largeDungeonKeyChest.makeDungeonChest(world, keyLocation.getPos(), keyLocation.getFacing(), this.dungeonInstance);
		}
		
		private void spawnLargeDoor(IWorld world, BlueprintLocation doorLocation) {
			// Relying on there already being a door... could make large chest do the same?
			NostrumBlocks.largeDungeonDoor.overrideDungeonKey(world, doorLocation.getPos(), this.dungeonInstance);
			
			// could if/else and use existing if it's th ere. same with large key?
		}
		
		private void spawnSmallKey(IWorld world, BlueprintLocation keyLocation) {
			NostrumBlocks.smallDungeonKeyChest.makeDungeonChest(world, keyLocation.getPos(), keyLocation.getFacing(), this.dungeonInstance);
		}
		
		private void spawnSmallDoor(IWorld world, BlueprintLocation smallDoor, @Nullable MutableBoundingBox bounds) {
			NostrumBlocks.smallDungeonDoor.spawnDungeonDoor(world, smallDoor.getPos(), smallDoor.getFacing(), bounds, this.dungeonInstance);
		}
		
		@Override
		public String toString() {
			return "[" + this.entry.getPos() + "] " + this.template.getRoomID() + ": " + this.getBounds();
		}
		
		private static final String NBT_ENTRY = "entry";
		private static final String NBT_TEMPLATE = "template";
		private static final String NBT_HASKEY = "hasKey";
		private static final String NBT_HASDOOR = "hasLargeDoor";
		private static final String NBT_DUNGEON_INSTANCE = "dungeonInstance";
		private static final String NBT_ROOM_ID = "roomID";
		private static final String NBT_HASSMALLKEY = "hasSmallKey";
		private static final String NBT_SMALL_DOORS = "smallDoors";
		
		public @Nonnull CompoundNBT toNBT(@Nullable CompoundNBT tag) {
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			tag.put(NBT_ENTRY, this.entry.toNBT());
			tag.putString(NBT_TEMPLATE, this.template.getRoomID().toString());
			tag.putBoolean(NBT_HASKEY, this.hasLargeKey);
			tag.put(NBT_DUNGEON_INSTANCE, this.dungeonInstance.toNBT());
			tag.putUniqueId(NBT_ROOM_ID, roomID);
			tag.putBoolean(NBT_HASSMALLKEY, this.hasSmallKey);
			tag.putBoolean(NBT_HASDOOR, this.hasLargeDoor);
			tag.put(NBT_SMALL_DOORS, NetUtils.ToNBT(this.smallDoors, e -> e.toNBT()));
			
			return tag;
		}
		
		public static DungeonRoomInstance fromNBT(CompoundNBT tag) {
			final BlueprintLocation entry = BlueprintLocation.fromNBT(tag.getCompound(NBT_ENTRY));
			final ResourceLocation templateID = new ResourceLocation(tag.getString(NBT_TEMPLATE));
			final DungeonRoomRecord record = DungeonRoomRegistry.GetInstance().getRegisteredRoom(templateID);
			final boolean hasKey = tag.getBoolean(NBT_HASKEY);
			final boolean hasLargeDoor = tag.getBoolean(NBT_HASDOOR);
			final DungeonInstance instance = DungeonInstance.FromNBT(tag.get(NBT_DUNGEON_INSTANCE));
			final UUID roomID = tag.getUniqueId(NBT_ROOM_ID);
			
			if (record == null) {
				NostrumMagica.logger.error("Failed to find dungeon room instance by id " + templateID);
			}
			
			final DungeonRoomInstance ret = new DungeonRoomInstance(entry, record.room, hasKey, hasLargeDoor, instance, roomID);
			
			ret.hasSmallKey = tag.getBoolean(NBT_HASSMALLKEY);
			ret.smallDoors.clear();
			if (tag.contains(NBT_SMALL_DOORS, NBT.TAG_LIST)) { // mostly just legacy support?
				NetUtils.FromNBT(ret.smallDoors, (ListNBT) tag.get(NBT_SMALL_DOORS), nbt -> BlueprintLocation.fromNBT((CompoundNBT) nbt));
			}
			
			return ret;
		}
	}
	
	// Checks if the provided room overlaps any existing bounds if it were to be spawned
	protected static boolean CheckRoomBounds(IDungeonRoom room, BlueprintLocation entry, DungeonGenerationContext context) {
		MutableBoundingBox bounds = room.getBounds(entry);
		for (MutableBoundingBox box : context.boundingBoxes) {
			if (bounds.intersectsWith(box)) {
				return false;
			}
		}
		return true;
	}
	
	private class Path {
		
		private final Path parent;
		private final List<Path> children;
		//private final Path parent;
		private boolean hasLargeKey; // whether this room itself has a large key
		private boolean hasLargeDoor; // Whether the room itself has an exit blocked by a large door
		
		private DungeonRoomInstance myRoom;
		
		public Path(Path parent) {
			this.parent = parent;
			this.children = new ArrayList<>();
		}
		
		public Path(DungeonRoomInstance startingRoom) {
			this((Path) null);
			this.myRoom = startingRoom;
		}
		
		public boolean isRoot() {
			return this.parent == null;
		}
		
		/**
		 * Gets all nodes on the way from this node to the root.
		 * Includes this node in the path.
		 * Path is a stack with the root at the top of the stack and itself at the base.
		 * @return
		 */
		protected Deque<Path> getRootPath() {
			Deque<Path> stack = new ArrayDeque<>(4);
			
			Path cur = this;
			while (true) {
				stack.push(cur);
				if (cur.isRoot()) {
					break;
				}
				cur = cur.parent;
			}
			return stack;
		}
		
		protected Path getCommonParent(Path other) {
			Deque<Path> myStack = this.getRootPath();
			Deque<Path> theirStack = other.getRootPath();
			
			// Find node before first divergence in the stacks
			Path lastCommon = myStack.pop();
			Validate.isTrue(lastCommon == theirStack.pop()); // Root should be first in both no matter what
			Validate.isTrue(lastCommon.isRoot());
			
			while (!myStack.isEmpty() && !theirStack.isEmpty()) {
				Path next = myStack.pop();
				if (next != theirStack.pop()) {
					break;
				}
				lastCommon = next;
			}
			
			return lastCommon;
		}
		
		protected void addSelfAndChildren(Collection<Path> paths, @Nullable Predicate<Path> filter) {
			if (filter == null || filter.test(this)) {
				paths.add(this);
			}
			for (Path child : this.children) {
				child.addSelfAndChildren(paths, filter);
			}
		}
		
		protected void removeSelfAndChildren(Collection<Path> paths) {
			paths.remove(this);
			for (Path child : this.children) {
				child.removeSelfAndChildren(paths);
			}
		}
		
		protected @Nullable Path findChild(Predicate<Path> filter) {
			if (filter.test(this)) {
				return this;
			}
			for (Path child : this.children) {
				Path foundChild = child.findChild(filter);
				if (foundChild != null) {
					return foundChild;
				}
			}
			return null;
		}
		
		protected @Nonnull IDungeonRoom pickRandomContRoom(DungeonGenerationContext context, BlueprintLocation entry, int remaining) {
			List<IDungeonRoom> eligibleRooms = context.contRooms.stream().filter(r -> r.getRoomCost() <= remaining).filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
			if (eligibleRooms.isEmpty()) {
				NostrumMagica.logger.warn("Failed to find a cont room that fit. Picking a random one for start " + entry);
				return context.contRooms.get(rand.nextInt(context.contRooms.size()));
			}
			return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
		}
		
		protected @Nonnull IDungeonRoom pickRandomEndRoom(DungeonGenerationContext context, BlueprintLocation entry) {
			if (context.endRooms.isEmpty()) {
				return getRooms().get(rand.nextInt(getRooms().size()));
			} else {
				List<IDungeonRoom> eligibleRooms = context.endRooms.stream().filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
				if (eligibleRooms.isEmpty()) {
					NostrumMagica.logger.warn("Failed to find an end room that fit. Picking a random one for start " + entry);
					return context.endRooms.get(rand.nextInt(context.endRooms.size()));
				}
				return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
			}
		}
		
		protected @Nonnull IDungeonRoom pickRandomKeyRoom(DungeonGenerationContext context, BlueprintLocation entry) {
			List<IDungeonRoom> eligibleRooms = context.keyRooms.stream().filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
			if (eligibleRooms.isEmpty()) {
				NostrumMagica.logger.warn("Failed to find a key room that fit. Picking a random one for start " + entry);
				return context.keyRooms.get(rand.nextInt(context.keyRooms.size()));
			}
			return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
		}
		
		protected @Nonnull IDungeonRoom pickRandomDoorRoom(DungeonGenerationContext context, BlueprintLocation entry, int remaining) {
			List<IDungeonRoom> eligibleRooms = context.doorRooms.stream().filter(r -> r.getRoomCost() <= remaining).filter(r -> NostrumDungeon.CheckRoomBounds(r, entry, context)).collect(Collectors.toList());
			if (eligibleRooms.isEmpty()) {
				NostrumMagica.logger.warn("Failed to find a door room that fit. Picking a random one for start " + entry);
				return context.doorRooms.get(rand.nextInt(context.doorRooms.size()));
			}
			return eligibleRooms.get(rand.nextInt(eligibleRooms.size()));
		}
		
		// Fill out this path, including a room for this node and spawning any children that are needed.
		protected void generate(DungeonGenerationContext context, int remaining, BlueprintLocation entry, @Nullable IDungeonRoom ending, boolean hasKey) {
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
					this.myRoom = new DungeonRoomInstance(entry, ending, false, false, context.instance, MakeNewRoomID(context));
				} else if (hasKey) {
					this.myRoom = new DungeonRoomInstance(entry, pickRandomKeyRoom(context, entry), true, false, context.instance, MakeNewRoomID(context));
					this.hasLargeKey = true;
				} else {
					this.myRoom = new DungeonRoomInstance(entry, pickRandomEndRoom(context, entry), false, false, context.instance, MakeNewRoomID(context));
				}
			} else {
				// If we have an ending and are about to terminate, get a door room.
				// Otherwise, do a regular cont room.
				
				// If we will need a key door, select that FIRST on every step to get a cost.
				// Then if there is still leftover remaining, generate a cont with that.
				if (ending != null) {
					IDungeonRoom doorRoom = pickRandomDoorRoom(context, entry, remaining);
					if (doorRoom.getRoomCost() >= remaining) {
						// Has to be door room
						Validate.isTrue(!hasKey); // Never want to get here. A room with a door AND a key would suck.
						myRoom = new DungeonRoomInstance(entry, doorRoom, false, true, context.instance, MakeNewRoomID(context));
						this.hasLargeDoor = true;
					} else {
						myRoom = new DungeonRoomInstance(entry, pickRandomContRoom(context, entry, remaining - doorRoom.getRoomCost()),
								false, false, context.instance, MakeNewRoomID(context));
					}
				} else {
					myRoom = new DungeonRoomInstance(entry, pickRandomContRoom(context, entry, remaining), false, false, context.instance, MakeNewRoomID(context));
				}
				
				this.generateChildren(context, remaining - (myRoom.template.getRoomCost()), ending, hasKey);
			}
		}
		
		// Fill out this path's children
		protected void generateChildren(DungeonGenerationContext context, int remaining, IDungeonRoom ending, boolean hasKey) {
			Validate.notNull(this.myRoom);
			// Add bounding box to context
			{
				final MutableBoundingBox innerBounds = myRoom.getBounds();
				// Shrink to not include walls, if walls are allowed to be shared
//				innerBounds.offset(1, 1, 1);
//				innerBounds.maxX -= 2;
//				innerBounds.maxY -= 2;
//				innerBounds.maxZ -= 2;
				context.boundingBoxes.add(innerBounds);
			}
			
			// Select a subpath to have the ending or key
			int keyI = -1;
			int endI = -1;
			
			if (hasKey) {
				keyI = rand.nextInt(myRoom.template.getNumExits());
			}
			if (ending != null) {
				endI = rand.nextInt(myRoom.template.getNumExits());
			}
			
			if (keyI != -1 && endI != -1 && keyI == endI) {
				// Note: can still be equal if there's one exit, but then a future child will do this same thing
				// until eventually there are multiple exits.
				endI = (endI + 1) % myRoom.template.getNumExits();
			}

			// Add subpaths based on doors
			for (BlueprintLocation door : myRoom.template.getExits(myRoom.entry)) {
				Path path = new Path(this);
				IDungeonRoom inEnd = null;
				boolean childHasKey = false;
				
				if (endI == 0) {
					inEnd = ending; // just set to null again if we don't have one 
				}
				if (keyI == 0) {
					childHasKey = true;
				}
				
				// TODO evaluate making 'remaining' be random to be like 1-remaining
				path.generate(context, remaining, GetDoorAdjacent(door, false), inEnd, childHasKey);
				keyI -= 1;
				endI -= 1;
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
	
	public static BlueprintLocation asRotated(BlueprintLocation start, BlockPos offset, Direction facing) {
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
		Direction out = start.getFacing();
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
			
		return new BlueprintLocation(pos, out);
	}
	
	/**
	 * Get the postion on the OTHER side of the provided door.
	 * For entries, this is the corresponding 'exit'. For exits, this is where the next 'entry' should be.
	 * @param door
	 * @param isEntry
	 * @return
	 */
	protected static final BlueprintLocation GetDoorAdjacent(BlueprintLocation door, boolean isEntry) {
		return new BlueprintLocation(
				door.getPos().offset(!isEntry ? door.getFacing().getOpposite() : door.getFacing()),
				door.getFacing()
				);
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
