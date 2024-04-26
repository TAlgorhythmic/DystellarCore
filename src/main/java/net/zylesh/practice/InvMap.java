package net.zylesh.practice;

import org.bukkit.inventory.Inventory;

public class InvMap {

    private final Inventory inv;
    private final PUser user;

    public InvMap(Inventory inv, PUser user) {
        this.inv = inv;
        this.user = user;
    }

    public Inventory getInv() {
        return inv;
    }

    public PUser getUser() {
        return user;
    }
}
