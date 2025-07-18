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
}


