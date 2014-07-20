/*
 * Copyright 2014 Alex Bennett & Alexander Chauncey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demigodsrpg.stoa.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTeleportEvent;

public class VehicleUtil {
    /**
     * Teleport an entity and the vehicle it is inside of, or vice versa.
     *
     * @param entity The entity/vehicle to be teleported.
     * @param to     The destination.
     */
    public static void teleport(final Entity entity, final Location to) {
        EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        if (entity.isInsideVehicle()) {
            Entity vehicle = entity.getVehicle();
            vehicle.eject();
            vehicle.teleport(to);
            entity.teleport(to);
            vehicle.setPassenger(entity);
        } else if (entity.getPassenger() != null) {
            Entity passenger = entity.getPassenger();
            entity.eject();
            entity.teleport(to);
            passenger.teleport(to);
            entity.setPassenger(passenger);
        } else entity.teleport(to);
    }
}
