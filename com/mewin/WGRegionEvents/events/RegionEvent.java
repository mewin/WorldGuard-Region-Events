/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mewin.WGRegionEvents.events;

import com.mewin.WGRegionEvents.MovementWay;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author mewin
 */
public abstract class RegionEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    
    private ProtectedRegion region;
    private boolean cancelled, cancellable;
    private MovementWay movement;
    
    public RegionEvent(ProtectedRegion region, Player player, MovementWay movement)
    {
        super(player);
        this.region = region;
        cancelled = false;
        cancellable = true;
        this.movement = movement;
        
        if (movement == MovementWay.SPAWN)
        {
            this.setCancellable(false);
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
    
    public ProtectedRegion getRegion()
    {
        return region;
    }
    
    public static HandlerList getHandlerList()
    {
        return handlerList;
    }
    
    @Override
    public void setCancelled(boolean cancelled)
    {
        if (!this.cancellable)
        {
            return;
        }
        
        this.cancelled = cancelled;
    }
    
    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }
    
    public boolean isCancellable()
    {
        return this.cancellable;
    }
    
    protected void setCancellable(boolean cancellable)
    {
        this.cancellable = cancellable;
        
        if (!this.cancellable)
        {
            this.cancelled = false;
        }
    }
    
    public MovementWay getMovementWay()
    {
        return this.movement;
    }
}
