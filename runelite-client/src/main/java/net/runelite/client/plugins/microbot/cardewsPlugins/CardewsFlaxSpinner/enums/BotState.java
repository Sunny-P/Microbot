package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsFlaxSpinner.enums;

import lombok.Getter;

@Getter
public enum BotState {
    MOVING_TO_WHEEL("Moving to Wheel"),
    SPIN_FLAX("Spinning Flax"),
    MOVING_TO_BANK("Moving to Bank"),
    BANKING("Banking");

    private final String name;

    BotState(String name)
    {
        this.name = name;
    }
}
