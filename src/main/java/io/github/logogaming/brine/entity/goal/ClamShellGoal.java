package io.github.logogaming.brine.entity.goal;

import io.github.logogaming.brine.entity.custom.ClamEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class ClamShellGoal extends Goal {
    private final ClamEntity clam;

    public ClamShellGoal(ClamEntity clam) {
        this.clam = clam;
    }

    @Override
    public boolean canUse() {
        return (this.clam.isOpen() ? clam.getRandom().nextInt(100) == 0 : clam.getRandom().nextInt(500) == 0);
    }

    @Override
    public void start() {
        clam.setOpen(!clam.isOpen());
        super.start();
    }

}
