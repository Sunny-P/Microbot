package net.runelite.client.plugins.microbot.cardewsPlugins.LineFiremaker;

import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigInformation;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.woodcutting.enums.WoodcuttingTree;

@ConfigGroup("LineFiremaker")
@ConfigInformation("Start the plugin where you would like to start making lines. <br />Will restock from the nearest bank. <br />Continually burns 27 logs in 1 straight line.")
public interface LineFiremakerConfig extends Config {

    enum LogType {
        LOGS("Logs", 1),
        OAK_LOGS("Oak logs", 15),
        WILLOW_LOGS("Willow logs", 30),
        MAPLE_LOGS("Maple logs", 45),
        YEW_LOGS("Yew logs", 60),
        MAGIC_LOGS("Magic logs", 75),
        REDWOOD_LOGS("Redwood logs", 90);

        private final String name;
        private final int firemakingLevel;

        LogType(String _name, int _fmLvl) {
            name = _name;
            firemakingLevel = _fmLvl;
        }

        public boolean HasRequiredLevel()
        {
            return Rs2Player.getSkillRequirement(Skill.FIREMAKING, firemakingLevel);
        }

        public String GetLogName()
        {
            return name;
        }
    }

    @ConfigItem(
            name = "Logs",
            keyName = "logs",
            description = "Type of logs to firemake",
            position = 0
    )
    default LogType GetSelectedLog() { return LogType.LOGS; }
}
