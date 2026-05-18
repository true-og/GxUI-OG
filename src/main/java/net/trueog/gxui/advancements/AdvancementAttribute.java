package net.trueog.gxui.advancements;

import java.util.Objects;

// One requirement entry beneath an advancement. Built by AdvancementMenu.Builder.
final class AdvancementAttribute {

    private final String name;
    private final boolean done;
    private final long current;
    private final long target;
    private final boolean hasProgress;

    AdvancementAttribute(String name, boolean done) {

        this.name = Objects.requireNonNull(name, "name");
        this.done = done;
        this.current = 0L;
        this.target = 0L;
        this.hasProgress = false;

    }

    AdvancementAttribute(String name, long current, long target) {

        this.name = Objects.requireNonNull(name, "name");
        this.current = current;
        this.target = target;
        this.done = current >= target;
        this.hasProgress = true;

    }

    String getName() {

        return name;

    }

    boolean isDone() {

        return done;

    }

    boolean hasProgress() {

        return hasProgress;

    }

    long getCurrent() {

        return current;

    }

    long getTarget() {

        return target;

    }

}
