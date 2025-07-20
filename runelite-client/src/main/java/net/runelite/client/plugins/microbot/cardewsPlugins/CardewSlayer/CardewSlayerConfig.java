package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;

@ConfigGroup("CardewSlayer")
public interface CardewSlayerConfig extends Config{
    @ConfigItem(
            name = "Slayer Master",
            keyName = "slayerMaster",
            position = 0,
            description = "Select your Slayer Master"
    )
    default String SlayerMaster() { return SlayerMaster.TURAEL.getName(); }

    @ConfigItem(
            name = "Eat Food %",
            keyName = "eatFoodPercent",
            position = 1,
            description = "At what percent of HP the Player will eat"
    )
    @Range(
            min = 0,
            max = 100
    )
    default int EatFoodPercent() { return 60; }

    @ConfigItem(
            name = "Only Loot My Drops?",
            keyName = "onlyLootMyDrops",
            position = 2,
            description = "Only picks up your own drops. Ironman?"
    )
    default boolean OnlyLootMyDrops() { return true; }

    @ConfigItem(
            name = "Get food",
            keyName = "getFood",
            description = "Gets food when banking?",
            position = 3
    )
    default boolean GetFood() { return true; }

    @ConfigItem(
            name = "Number of food",
            keyName = "numberOfFood",
            description = "How much food should be withdrawn",
            position = 4
    )
    default int NumberOfFood() { return 5; }

    @ConfigItem(
            name = "Pickup Untradeables",
            keyName = "pickupUntradeables",
            description = "Whether you loot untradeables.",
            position = 5
    )
    default boolean PickupUntradeables() { return true; }
}


