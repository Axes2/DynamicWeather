package com.dynamicweather.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CloudFieldManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random random = new Random();

    public static Vector2f globalWindDirection = new Vector2f(1f, 0f); // Eastward
    public static float globalWindSpeed = 0.02f;

    private static final List<CloudClusterInstance> clusters = new ArrayList<>();
    private static final List<CloudClusterInstance> pendingClusters = new ArrayList<>();


    public static int MAX_CLOUD_CUBES = 3000;
    public static int CUBES_PER_CLUSTER = 40;

    private static int tickCounter = 0;
    private static final int SPAWN_INTERVAL_TICKS = 40; // Every 2 seconds at 20 TPS

    private static CloudCover currentCover = CloudCover.PARTLY_CLOUDY;

    public static CloudCover getCloudCover() {
        return currentCover;
    }

    public static void clearAllClouds() {
        clusters.clear();
    }


    public static void setCloudCover(CloudCover cover) {
        if (cover.ordinal() > currentCover.ordinal()) {
            SkyCellSpawner.clear();  // allow denser fill
        } else if (cover == CloudCover.CLEAR) {
            clearAllClouds();  // 🔧 remove lingering clouds
        }
        currentCover = cover;
    }





    public static void clear() {
        clusters.clear();
        SkyCellSpawner.clear(); // ← reset world grid state
    }

    public static void setWindSpeed(float speed) {
        globalWindSpeed = speed;
    }

    private static float getRenderDistanceSq() {
        int chunks = Minecraft.getInstance().options.renderDistance().get();
        float blocks = chunks * 16f;
        return blocks * blocks;
    }

    public static void queueCluster(CloudClusterInstance cluster) {
        pendingClusters.add(cluster);
    }



    public static void addCluster(CloudClusterInstance cluster) {
        CloudFieldManager.queueCluster(cluster);
    }


    public static CloudType selectCloudTypeForCurrentCover(Random rand) {
        CloudCover cover = getCloudCover();
        CloudType type;

        switch (cover) {
            case OVERCAST:
                type = CloudType.STRATUS;
                break;
            case STORM:
                type = CloudType.CUMULONIMBUS;
                break;
            case PARTLY_CLOUDY:
                type = (rand.nextFloat() < 0.8f ? CloudType.CUMULUS : CloudType.STRATUS);
                break;
            case MOSTLY_CLOUDY:
                type = (rand.nextFloat() < 0.6f ? CloudType.STRATUS : CloudType.CUMULUS);
                break;
            case CLEAR:
            default:
                type = CloudType.CUMULUS;
                break;
        }

        return type;
    }


    public static void updateCloudsIfNeeded() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        tickCounter++;

        SkyCellSpawner.update();

        updateClusterOffsets();
        removeExpiredClusters();
        flushPendingClusters();


    }
    public static void flushPendingClusters() {
        clusters.addAll(pendingClusters);
        pendingClusters.clear();
    }
    public static int computeClusterLifetime() {
        float speedPerTick = globalWindSpeed + 0.01f; // prevents division by zero
        return (int)(600 / speedPerTick); // Clouds last ~600 ticks at speed 1.0f
    }



//    private static void trySpawnClusterNearPlayer() {
//        LocalPlayer player = mc.player;
//        if (player == null) return;
//
//        Vec3 playerPos = player.position();
//        float renderDistance = mc.options.renderDistance().get() * 16f;
//
//        float minDistance = 80f;
//        float maxDistance = renderDistance * 0.9f;
//
//        float windAngle = (float) Math.atan2(globalWindDirection.y, globalWindDirection.x);
//
//        float r = random.nextFloat();
//        float angle;
//        if (r < 0.7f) {
//            float spread = (float) Math.toRadians(180);
//            angle = windAngle + (float) Math.PI + (random.nextFloat() - 0.5f) * spread;
//        } else {
//            angle = (float) (random.nextFloat() * 2 * Math.PI);
//        }
//
//        float distance = minDistance + random.nextFloat() * (maxDistance - minDistance);
//        float baseX = (float) (playerPos.x + Math.cos(angle) * distance);
//        float baseZ = (float) (playerPos.z + Math.sin(angle) * distance);
//        float baseY = 130f + random.nextFloat() * 40f;
//
//        // Dynamically choose cloud type based on weather
//        CloudType type;
//        switch (currentCover) {
//            case OVERCAST -> type = CloudType.STRATUS;
//            case STORM -> type = CloudType.CUMULONIMBUS;
//            case PARTLY_CLOUDY -> type = (random.nextFloat() < 0.8f ? CloudType.CUMULUS : CloudType.STRATUS);
//            case MOSTLY_CLOUDY -> type = (random.nextFloat() < 0.6f ? CloudType.STRATUS : CloudType.CUMULUS);
//            case CLEAR, default -> type = CloudType.CUMULUS;
//        }
//
//        CloudType type = selectCloudTypeForCurrentCover(random);
//        List<Cloud> puff = CloudCluster.generateCluster(type, 0, 0, 0);
//        float angleOffset = random.nextFloat() * 10f - 5f;
//
//        CloudClusterInstance instance = new CloudClusterInstance(type, puff, angleOffset, baseX, baseY, baseZ);
//        instance.setLifetime(computeClusterLifetime());
//
//        clusters.add(instance);
//    }




    private static void removeExpiredClusters() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Iterator<CloudClusterInstance> iterator = clusters.iterator();
        while (iterator.hasNext()) {
            CloudClusterInstance cluster = iterator.next();
            cluster.tickLifetime();

            if (cluster.isExpired() || isOutOfRenderDistance(cluster, player)) {
                iterator.remove();

                // Reopen the cell this cluster came from so new clouds can form later
                int cx = Math.floorDiv((int) cluster.getBaseX(), SkyCellSpawner.CELL_SIZE);
                int cz = Math.floorDiv((int) cluster.getBaseZ(), SkyCellSpawner.CELL_SIZE);
                SkyCellSpawner.unvisitCell(cx, cz);
            }
        }



    // If near or over max capacity, prune oldest off-screen clusters
        if (countTotalClouds() >= MAX_CLOUD_CUBES * 0.9) {
            iterator = clusters.iterator();
            while (iterator.hasNext()) {
                CloudClusterInstance cluster = iterator.next();
                if (cluster.getAge() > 1000 && isOutOfRenderDistance(cluster, player)) {
                    iterator.remove();
                }
            }
        }
    }


    private static boolean isOutOfRenderDistance(CloudClusterInstance cluster, LocalPlayer player) {
        float dx = (float) (player.getX() - (cluster.getBaseX() + cluster.getOffsetX()));
        float dz = (float) (player.getZ() - (cluster.getBaseZ() + cluster.getOffsetZ()));
        return (dx * dx + dz * dz) > getRenderDistanceSq();
    }

    private static void updateClusterOffsets() {
        for (CloudClusterInstance cluster : clusters) {
            cluster.update(globalWindSpeed * 2.5f, 1f, globalWindDirection.x, globalWindDirection.y);

        }
    }

    private static int countTotalClouds() {
        int count = 0;
        for (CloudClusterInstance cluster : clusters) {
            count += cluster.getClouds().size();
        }
        return count;
    }

    public static List<CloudClusterInstance> getClusters() {
        return clusters;
    }
}
