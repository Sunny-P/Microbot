package net.runelite.client.plugins.microbot.cardewsPlugins.WarriorsGuildHelper;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;

@ConfigGroup("WarriorsGuildHelper")
@ConfigInformation("Have gear equipped." +
            "   <br>Fights cyclopes, shows Kamfreena defenders to progress, and continues to collect the Dragon Defender." +
            "<br><br>Start at the Warriors Guild bank." +
            "   <br>Requires more than 100 tokens." +
            "<br><br>If using a Herb sack or Seed box, make sure you have them 'Open'" +
            "<br><br>Use QoL's Dialogue Auto Continue setting.")
public interface WarriorsGuildHelperConfig extends Config {

    // ********* BEGIN FOOD SECTION *****************
    @ConfigSection(
            name = "Food",
            description = "Options related to food.",
            position = 0,
            closedByDefault = true
    )
    String foodSection = "Config options related to food resources.";

    @ConfigItem(
            name = "Food to Use",
            keyName = "foodToUse",
            position = 0,
            description = "The type of food to use when fighting cyclopes.",
            section = foodSection
    )
    default Rs2Food FoodToUse() { return Rs2Food.KARAMBWAN; }

    @ConfigItem(
            name = "Food Amount",
            keyName = "foodAmount",
            position = 1,
            description = "The amount of food to use.",
            section = foodSection
    )
    default int FoodAmount() { return 0; }

    @ConfigItem(
            name = "Eat Percent",
            keyName = "eatPercent",
            position = 2,
            description = "How low to let HP drop before eating.",
            section = foodSection
    )
    @Range(min = 0, max = 100)
    default int EatPercent() { return 50; }
    // ********* END FOOD SECTION *****************

    // ********* BEGIN LOOT SECTION *****************
    @ConfigSection(
            name = "Loot",
            description = "Options related to loot.",
            position = 1,
            closedByDefault = true
    )
    String lootSection = "Config options related to looting.";

    @ConfigItem(
            name = "Loot Herbs",
            keyName = "lootHerbs",
            position = 0,
            description = "Whether to loot herbs or not",
            section = lootSection
    )
    default boolean LootHerbs() { return false; }

    @ConfigItem(
            name = "Use Herb Sack",
            keyName = "useHerbSack",
            position = 1,
            description = "Whether to use an open herb sack if looting herbs",
            section = lootSection
    )
    default boolean UseHerbSack() { return false; }

    @ConfigItem(
            name = "Loot Seeds",
            keyName = "lootSeeds",
            position = 2,
            description = "Whether to loot seeds or not",
            section = lootSection
    )
    default boolean LootSeeds() { return false; }

    @ConfigItem(
            name = "Use Seed Box",
            keyName = "useSeedBox",
            position = 3,
            description = "Whether to use an open seed box if looting seeds",
            section = lootSection
    )
    default boolean UseSeedBox() { return false; }

    @ConfigItem(
            name = "Loot Only My Items",
            keyName = "lootOnlyMyItems",
            position = 4,
            description = "Whether to only loot my own drops.",
            section = lootSection
    )
    default boolean LootOnlyMyItems() { return true; }

    @ConfigItem(
            name = "Min Loot Value",
            keyName = "minLootValue",
            position = 5,
            description = "Minimum value of drops to pickup, generally.",
            section = lootSection
    )
    default int MinLootValue() { return 2000; }
    // ********* END LOOT SECTION *****************
}
