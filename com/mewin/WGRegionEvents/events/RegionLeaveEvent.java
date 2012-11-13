/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGRegionEvents.events;

import com.mewin.WGRegionEvents.MovementWay;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

/**
 *
 * @author mewin
 */
public class RegionLeaveEvent extends RegionEvent {
    public RegionLeaveEvent(ProtectedRegion region, Player player, MovementWay movement)
    {
        super(region, player, movement);
    }
}
