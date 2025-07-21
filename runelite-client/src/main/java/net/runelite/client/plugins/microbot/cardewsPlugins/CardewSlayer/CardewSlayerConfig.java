package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;

@ConfigGroup("CardewSlayer")
@ConfigInformation("")
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
            position = 3,
            description = "Only picks up your own drops. Ironman?"
    )
    default boolean OnlyLootMyDrops() { return true; }

    @ConfigItem(
            name = "Number of food",
            keyName = "numberOfFood",
            description = "How much food should be withdrawn",
            position = 2
    )
    default int NumberOfFood() { return 5; }

    @ConfigItem(
            name = "Pickup Untradeables",
            keyName = "pickupUntradeables",
            description = "Whether you loot untradeables.",
            position = 4
    )
    default boolean PickupUntradeables() { return true; }

    @ConfigItem(
            name = "Minimum Loot Value",
            keyName = "minLootValue",
            description = "Loots items above this value",
            position = 5
    )
    @Range(
            min = 0,
            max = Integer.MAX_VALUE
    )
    default int MinLootValue() { return 800; }

    @ConfigItem(
            name = "Pickup and Bury bones?",
            keyName = "pickupAndBuryBones",
            description = "Whether to pickup and bury bones.",
            position = 6
    )
    default boolean PickupAndBuryBones() { return false; }

    @ConfigItem(
            name = "Pickup Runes",
            keyName = "pickupRunes",
            description = "Whether you loot any and all runes. Ironman?",
            position = 7
    )
    default boolean PickupRunes() { return true; }

    @ConfigItem(
            name = "Pickup Seeds",
            keyName = "pickupSeeds",
            description = "Whether you loot any and all seeds. Ironman?",
            position = 8
    )
    default boolean PickupSeeds() { return true; }

    @ConfigItem(
            name = "Pickup Grimy Herbs",
            keyName = "pickupGrimyHerbs",
            description = "Whether you loot any and all grimy herbs. Ironman?",
            position = 9
    )
    default boolean PickupGrimyHerbs() { return true; }
}


