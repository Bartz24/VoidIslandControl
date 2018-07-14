package com.bartz24.voidislandcontrol.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class VICTeleporter extends Teleporter {
    private final WorldServer worldServer;

    private double x, y, z;


    public VICTeleporter(WorldServer server, double x, double y, double z) {
        super(server);
        worldServer = server;
        this.x = x;
        this.y = y;
        this.z = z;

    }

    @Override
    public void placeInPortal(Entity entity, float yaw) {
        worldServer.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));

        entity.setPosition(this.x, this.y, this.z);
        entity.motionX = 0.0f;
        entity.motionY = 0.0f;
        entity.motionZ = 0.0f;
    }

}