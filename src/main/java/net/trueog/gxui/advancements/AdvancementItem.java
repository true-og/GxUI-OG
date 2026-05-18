package net.trueog.gxui.advancements;

import java.util.List;
import java.util.Objects;

record AdvancementItem(String name, AdvancementState state, List<AdvancementAttribute> attributes,
        List<String> footer)
{

    AdvancementItem(String name, AdvancementState state, List<AdvancementAttribute> attributes, List<String> footer) {

        this.name = Objects.requireNonNull(name, "name");
        this.state = Objects.requireNonNull(state, "state");
        this.attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        this.footer = List.copyOf(Objects.requireNonNull(footer, "footer"));

    }

    String getName() {

        return name;

    }

    AdvancementState getState() {

        return state;

    }

    List<AdvancementAttribute> getAttributes() {

        return attributes;

    }

    List<String> getFooter() {

        return footer;

    }

}
