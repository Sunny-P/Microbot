package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner.enums;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public enum SpinningLocation {
    LUMBRIDGE("Lumbridge", new WorldPoint(3209, 3213, 1)),
    SEERS_VILLAGE("Seers Village", new WorldPoint(2711, 3471, 1)),
    SPIN_FLAX_SPELL("Spin Flax Lunars", new WorldPoint(-1, -1, -1));

    final String name;
    @Getter
    final WorldPoint location;

    SpinningLocation(String name, WorldPoint location)
    {
        this.name = name;
        this.location = location;
    }
}
