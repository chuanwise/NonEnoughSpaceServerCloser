package cn.chuanwise.nessc.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NonEnoughSpaceEvent
    extends Event
    implements Cancellable {
    
    private static final HandlerList HANDLER_LIST = new HandlerList();
    
    private boolean cancelled;
    
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
