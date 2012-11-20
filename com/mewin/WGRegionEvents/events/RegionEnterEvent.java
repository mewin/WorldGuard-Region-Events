package com.mewin.WGRegionEvents.events;

import com.mewin.WGRegionEvents.MovementWay;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 *
 * @author mewin
 */
public class RegionEnterEvent extends RegionEvent implements Cancellable {
    private boolean cancelled, cancellable;
    public RegionEnterEvent(ProtectedRegion region, Player player, MovementWay movement)
    {
        super(region, player, movement);
        cancelled = false;
        cancellable = true;
        
        if (movement == MovementWay.SPAWN
            || movement == MovementWay.DISCONNECT)
        {
            cancellable = false;
        }
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
}
