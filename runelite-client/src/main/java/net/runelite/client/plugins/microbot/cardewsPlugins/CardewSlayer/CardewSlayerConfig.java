package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.cardewsPlugins.CUtil;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerMaster;

@ConfigGroup("CardewSlayer")
@ConfigInformation("Please have any required items in your bank. <br>Superiors not yet supported.")
public interface CardewSlayerConfig extends Config{
    @ConfigItem(
            name = "Slayer Master",
            keyName = "slayerMaster",
            position = 0,
            description = "Select your Slayer Master"
    )
    default SlayerMaster SlayerMaster() { return SlayerMaster.TURAEL; }

    @ConfigSection(
            name = "Task Alternatives",
            description = "When a slayer task with multiple options is received, it will take this choice.",
            position = 1,
            closedByDefault = true
    )
    String taskAlternativeSection = "When a slayer task with multiple options is received, it will take this choice.";

    @ConfigItem(
            name = "Bird Alternative Task",
            keyName = "birdAlternativeTask",
            position = 0,
            description = "The monster you will slay for when on task for Birds",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeBirdTask AlternativeBirdTask() { return CUtil.AlternativeBirdTask.CHICKEN; }

    @ConfigItem(
            name = "Dwarf Alternative Task",
            keyName = "dwarfAleternativeTask",
            position = 1,
            description = "The monster you will slay for when on task for Dwarves",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeDwarfTask AlternativeDwarfTask() { return CUtil.AlternativeDwarfTask.DWARF_LVL20; }

    @ConfigItem(
            name = "Kalphite Alternative Task",
            keyName = "kalphiteAlternativeTask",
            position = 2,
            description = "The monster you will slay for when on task for Kalphite",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeKalphiteTask AlternativeKalphiteTask() { return CUtil.AlternativeKalphiteTask.KALPHITE_WORKER_LVL28_NORTHWEST; }

    @ConfigItem(
            name = "Wolf Alternative Task",
            keyName = "wolfAlternativeTask",
            position = 3,
            description = "The monster you will slay for when on task for Wolves",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeWolfTask AlternativeWolfTask() { return CUtil.AlternativeWolfTask.WHITE_WOLF_LVL25; }

    @ConfigItem(
            name = "Crab Alternative Task",
            keyName = "crabAlternativeTask",
            position = 4,
            description = "The location you will slay Crabs",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeCrabTask AlternativeCrabTask() { return CUtil.AlternativeCrabTask.SAND_CRAB_HOSIDIUS_SOUTH; }

    @ConfigItem(
            name = "Hill Giant Alternative Task",
            keyName = "hillgiantAlternativeTask",
            position = 5,
            description = "The location you will slay Hill Giants",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeHillGiantTask AlternativeHillGiantTask() { return CUtil.AlternativeHillGiantTask.HILL_GIANT_EDGEVILLE_DUNGEON; }

    @ConfigItem(
            name = "Shades Alternative Task",
            keyName = "shadesAlternativeTask",
            position = 6,
            description = "The variant of shade you will slay",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeShadeTask AlternativeShadeTask() { return CUtil.AlternativeShadeTask.LOAR_SHADE; }

    @ConfigItem(
            name = "Vampyre Alternative Task",
            keyName = "vampyreAlternativeTask",
            position = 7,
            description = "The vampyre you will go to slay",
            section = taskAlternativeSection
    )
    default CUtil.AlternativeVampyreTask AlternativeVampyreTask() { return CUtil.AlternativeVampyreTask.FERAL_VAMPYRE; }

    @ConfigItem(
            name = "Inventory Setup to Use",
            keyName = "inventorySetupToUse",
            position = 2,
            description = "The InventorySetup to load equipment/inventory from"
    )
    default InventorySetup InventorySetup() { return null; }

    @ConfigItem(
            name = "Eat Food %",
            keyName = "eatFoodPercent",
            position = 2,
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

    @ConfigItem(
            name = "Enable Prayer Flicking",
            keyName = "enablePrayerFlicking",
            description = "Whether or not to prayer flick.<br>This does 1 tick flicking.",
            position = 10
    )
    default boolean EnablePrayerFlicking() { return false; }
}


