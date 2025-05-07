package com.dynamicweather.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2f;

@Mod.EventBusSubscriber
public class CloudDebugCommands {

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("maxclouds")
                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int count = IntegerArgumentType.getInteger(ctx, "count");
                            CloudFieldManager.MAX_CLOUD_CUBES = count;
                            ctx.getSource().sendSuccess(() -> Component.literal("Set max cloud cubes to " + count), false);
                            return 1;
                        }))
        );

        dispatcher.register(Commands.literal("clearclouds")
                .executes(ctx -> {
                    CloudFieldManager.clear();
                    ctx.getSource().sendSuccess(() -> Component.literal("Cleared all cloud clusters."), false);
                    return 1;
                })
        );

        dispatcher.register(Commands.literal("globalwindspeed")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                        .executes(ctx -> {
                            float speed = FloatArgumentType.getFloat(ctx, "value");
                            CloudFieldManager.setWindSpeed(speed);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Set global wind speed to " + speed), false);
                            return 1;
                        }))
        );


        dispatcher.register(Commands.literal("globalwinddirection")
                .then(Commands.argument("degrees", FloatArgumentType.floatArg(0f, 360f))
                        .executes(ctx -> {
                            float deg = FloatArgumentType.getFloat(ctx, "degrees");
                            float rad = (float) Math.toRadians(deg);
                            CloudFieldManager.globalWindDirection.set((float)Math.cos(rad), (float)Math.sin(rad));
                            ctx.getSource().sendSuccess(() -> Component.literal("Set wind direction to " + deg + " degrees"), false);
                            return 1;
                        }))
        );

        dispatcher.register(Commands.literal("cubespercluster")
                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int count = IntegerArgumentType.getInteger(ctx, "count");
                            CloudCluster.CUBES_PER_CLUSTER = count;
                            ctx.getSource().sendSuccess(() -> Component.literal("Set cubes per cluster to " + count), false);
                            return 1;
                        }))
        );

        dispatcher.register(Commands.literal("clusterspread")
                .then(Commands.argument("x", FloatArgumentType.floatArg(0))
                        .then(Commands.argument("y", FloatArgumentType.floatArg(0))
                                .then(Commands.argument("z", FloatArgumentType.floatArg(0))
                                        .executes(ctx -> {
                                            float x = FloatArgumentType.getFloat(ctx, "x");
                                            float y = FloatArgumentType.getFloat(ctx, "y");
                                            float z = FloatArgumentType.getFloat(ctx, "z");
                                            CloudCluster.SPREAD_X = x;
                                            CloudCluster.SPREAD_Y = y;
                                            CloudCluster.SPREAD_Z = z;
                                            ctx.getSource().sendSuccess(() -> Component.literal("Set cluster spread to " + x + ", " + y + ", " + z), false);
                                            return 1;
                                        }))))
        );
        dispatcher.register(Commands.literal("clustercount")
                .executes(ctx -> {
                    int count = CloudFieldManager.getClusters().size();
                    ctx.getSource().sendSuccess(() ->
                            Component.literal("Current cloud cluster count: " + count), false);
                    return 1;
                })
        );
        dispatcher.register(Commands.literal("cloudcover")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (CloudCover cover : CloudCover.values()) {
                                builder.suggest(cover.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "type").toUpperCase();
                            try {
                                CloudCover cover = CloudCover.valueOf(input);
                                CloudFieldManager.setCloudCover(cover);
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal("Set cloud cover to " + cover.name().toLowerCase()), false);
                                return 1;
                            } catch (IllegalArgumentException e) {
                                ctx.getSource().sendFailure(Component.literal("Invalid cover type: " + input));
                                return 0;
                            }
                        }))
        );


    }
}
