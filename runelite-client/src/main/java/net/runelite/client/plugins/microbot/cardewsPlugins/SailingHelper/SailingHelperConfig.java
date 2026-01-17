package net.runelite.client.plugins.microbot.cardewsPlugins.SailingHelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("SailingHelper")
public interface SailingHelperConfig extends Config {
    @ConfigItem(
            keyName = "autoTrimSails",
            name = "Auto Trim Sails",
            description = "Whether to automatically trim the sails when sailing.",
            position = 0
    )
    default boolean ShouldAutoTrimSails() { return false; }


}
