package net.runelite.client.plugins.microbot.cardewsPlugins.CrabTrapping;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("CrabTrapping")
public interface CrabTrappingConfig extends Config {
    @ConfigItem(
            keyName = "areCrabPeoplePeople",
            name = "Are Crab People, People?",
            description = "Crabs are people.., crabs are people...",
            position = 0
    )
    default boolean AreCrabPeoplePeople() { return false; }

}
