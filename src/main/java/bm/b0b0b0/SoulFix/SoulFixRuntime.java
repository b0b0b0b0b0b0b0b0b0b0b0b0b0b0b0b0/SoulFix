package bm.b0b0b0.SoulFix;

import bm.b0b0b0.SoulFix.gui.RepairGuiService;
import bm.b0b0b0.SoulFix.service.CooldownService;
import bm.b0b0b0.SoulFix.service.SlotService;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SoulFixRuntime {

    private final AtomicBoolean ready = new AtomicBoolean(false);
    private RepairGuiService repairGuiService;
    private SlotService slotService;
    private CooldownService cooldownService;
    private Runnable integrationReload;

    public boolean isReady() {
        return ready.get();
    }

    public RepairGuiService repairGuiService() {
        return repairGuiService;
    }

    public SlotService slotService() {
        return slotService;
    }

    public CooldownService cooldownService() {
        return cooldownService;
    }

    public Runnable integrationReload() {
        return integrationReload;
    }

    public void bind(
            RepairGuiService repairGuiService,
            SlotService slotService,
            CooldownService cooldownService,
            Runnable integrationReload
    ) {
        this.repairGuiService = repairGuiService;
        this.slotService = slotService;
        this.cooldownService = cooldownService;
        this.integrationReload = integrationReload;
        ready.set(true);
    }

    public void unbind() {
        ready.set(false);
        repairGuiService = null;
        slotService = null;
        cooldownService = null;
        integrationReload = null;
    }
}
