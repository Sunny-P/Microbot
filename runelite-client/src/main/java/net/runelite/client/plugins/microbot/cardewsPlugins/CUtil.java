package net.runelite.client.plugins.microbot.cardewsPlugins;

import lombok.Getter;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.slayer.enums.SlayerTaskMonster;

import java.awt.*;
import java.util.Map;

public class CUtil {
    public static Point GetRandomPointInRectangle(Rectangle rect)
    {
        int randX = (int)(Math.random() * rect.width);
        int randY = (int)(Math.random() * rect.height);
        return new Point(randX, randY);
    }

    public static String SingularisePluralName(String plural) {
        String word = plural.toLowerCase();
        Microbot.log("Tested plural name: " + word);

        // Handle known irregulars first
        Map<String, String> irregulars = Map.of(
                "wolves", "wolf",
                "dwarves", "dwarf",
                "vampyres", "vampyre",
                "men", "man",
                "women", "woman",
                "children", "child"
        );

        if (irregulars.containsKey(word)) {
            return irregulars.get(word);
        }

        // Handle regular plural patterns
        if (word.endsWith("ies"))
        {
            return word.substring(0, word.length() - 3) + "y"; // e.g., "bodies" → "body"
        }
        else if (word.endsWith("ves"))
        {
            return word.substring(0, word.length() - 3) + "f"; // e.g., "wolves" already handled, "shelves" → "shelf"
        }
        //else if (word.endsWith("es")) // IS THIS RELEVANT TO ANY TASKS? THIS WAS MAKING CAVE SLIMES CAVE SLIM
        //{
            // Handle "boxes", "witches", "bosses"
            //return word.substring(0, word.length() - 2);
        //}
        else if (word.endsWith("s") && word.length() > 1)
        {
            return word.substring(0, word.length() - 1);
        }

        // If none matched, return as is
        return word;
    }

    public static void SetMyAntiban(double _microbreakChance, int _mbreakDurationLow, int _mbreakDurationHigh, double _actionCooldownChance)
    {
        Rs2Antiban.resetAntibanSettings();

        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.randomIntervals = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.dynamicIntensity = false;
        Rs2AntibanSettings.dynamicActivity = false;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.1;
        Rs2AntibanSettings.takeMicroBreaks = true;
        Rs2AntibanSettings.microBreakDurationLow = _mbreakDurationLow;
        Rs2AntibanSettings.microBreakDurationHigh = _mbreakDurationHigh;
        Rs2AntibanSettings.microBreakChance = _microbreakChance;

        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.actionCooldownChance = _actionCooldownChance;

        Rs2AntibanSettings.devDebug = false;
    }

    @Getter
    public enum SlayerTarget {
        NONE(null, new WorldPoint(0,0,0)),
        BANSHEE(SlayerTaskMonster.BANSHEE, new WorldPoint(3436, 3558, 0)),
        BAT(SlayerTaskMonster.BAT, new WorldPoint(3370, 3485, 0)),
        BEAR(SlayerTaskMonster.BEAR, new WorldPoint(2705, 3340, 0)),
        BIRD(SlayerTaskMonster.BIRD, new WorldPoint(0, 0, 0)),  // BIRD uses alternative locations. Location is populated in script based on config.AlternativeBirdTask
        CATABLEPON(SlayerTaskMonster.CATABLEPON, new WorldPoint(2162, 5286, 0)),
        CAVE_BUG(SlayerTaskMonster.CAVE_BUG, new WorldPoint(3152, 9574, 0)),
        CAVE_CRAWLER(SlayerTaskMonster.CAVE_CRAWLER, new WorldPoint(2791, 9996, 0)),
        CAVE_SLIME(SlayerTaskMonster.CAVE_SLIME, new WorldPoint(3156, 9547, 0)),
        COCKATRICE(SlayerTaskMonster.COCKATRICE, new WorldPoint(2791, 10036, 0)),
        COW(SlayerTaskMonster.COW, new WorldPoint(2667, 3348, 0)),
        CRAB(SlayerTaskMonster.CRAB, new WorldPoint(0, 0, 0)),  // Alt crab locations
        CRAWLING_HAND(SlayerTaskMonster.CRAWLING_HAND, new WorldPoint(3411, 3538, 0)),
        DOG(SlayerTaskMonster.DOG, new WorldPoint(2669, 3495, 0)),   // Goes to McGrubors wood Guard Dogs. Can extend Alternative tasks if wanted, similar to BIRD.
        DWARF(SlayerTaskMonster.DWARF, new WorldPoint(0, 0, 0)),    // Alt dwarf locations
        FLESH_CRAWLER(SlayerTaskMonster.FLESH_CRAWLER, new WorldPoint(2040, 5187, 0)),
        GHOST(SlayerTaskMonster.GHOST, new WorldPoint(1690, 10062, 0)),
        GHOUL(SlayerTaskMonster.GHOUL, new WorldPoint(3417, 3512, 0)),
        GOBLIN(SlayerTaskMonster.GOBLIN, new WorldPoint(3259, 3228, 0)),
        HILL_GIANT(SlayerTaskMonster.HILL_GIANT, new WorldPoint(0, 0, 0)),  // Alt hill giant locations
        HOBGOBLIN(SlayerTaskMonster.HOBGOBLIN, new WorldPoint(2910, 3282, 0)),
        ICEFIEND(SlayerTaskMonster.ICEFIEND, new WorldPoint(3007, 3474, 0)),
        ICE_WARRIOR(SlayerTaskMonster.ICE_WARRIOR, new WorldPoint(3044, 9582, 0)),
        KALPHITE(SlayerTaskMonster.KALPHITE, new WorldPoint(0, 0, 0)),  // Alt kalphite locations
        KILLERWATT(SlayerTaskMonster.KILLERWATT, new WorldPoint(2674, 5214, 2)),
        LIZARD(SlayerTaskMonster.LIZARD, new WorldPoint(3441, 3065, 0)),
        MINOTAUR(SlayerTaskMonster.MINOTAUR, new WorldPoint(1881, 5217, 0)),
        MOGRE(SlayerTaskMonster.MOGRE, new WorldPoint(2990, 3114, 0)),
        MONKEY(SlayerTaskMonster.MONKEY, new WorldPoint(2877, 3154, 0)),
        PYREFIEND(SlayerTaskMonster.PYREFIEND, new WorldPoint(2762, 10002, 0)),
        RAT(SlayerTaskMonster.RAT, new WorldPoint(3199, 3209, 0)),
        ROCKSLUG(SlayerTaskMonster.ROCKSLUG, new WorldPoint(2797, 10017, 0)),
        SCORPION(SlayerTaskMonster.SCORPION, new WorldPoint(3043, 9789, 0)),
        SHADES(SlayerTaskMonster.SHADE, new WorldPoint(0, 0, 0)),   // Alt shade location
        SKELETON(SlayerTaskMonster.SKELETON, new WorldPoint(1641, 10047, 0)),
        SPIDER(SlayerTaskMonster.SPIDER, new WorldPoint(3167, 3245, 0)),
        WALL_BEAST(SlayerTaskMonster.WALL_BEAST, new WorldPoint(3207, 9555, 0)),
        WOLF(SlayerTaskMonster.WOLF, new WorldPoint(0, 0, 0)),  // Alt wolf locations
        VAMPYRE(SlayerTaskMonster.VAMPYRE, new WorldPoint(0, 0, 0)),
        ZOMBIE(SlayerTaskMonster.ZOMBIE, new WorldPoint(3146, 9900, 0));

        private final SlayerTaskMonster monsterData;
        private WorldPoint location;

        SlayerTarget(SlayerTaskMonster _monsterData, WorldPoint _location) {
            this.monsterData = _monsterData;
            this.location = _location;
        }

        public void SetLocation(WorldPoint _newLocation)
        {
            location = _newLocation;
        }
    }

    @Getter
    public enum AlternativeBirdTask {
        SEAGULL("Seagull", new WorldPoint(3027, 3204, 0)),
        CHICKEN("Chicken", new WorldPoint(3177, 3299, 0)),
        TERRORBIRD("Terrorbird", new WorldPoint(2378, 3434, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeBirdTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeDwarfTask {
        DWARF_LVL7_11("Dwarf", new WorldPoint(3016, 3450, 0)),
        DWARF_LVL20("Dwarf", new WorldPoint(2864, 9877, 0)),
        CHAOS_DWARF_LVL49("Chaos dwarf", new WorldPoint(2923, 9760, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeDwarfTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeKalphiteTask {
        KALPHITE_WORKER_LVL28_EAST("Kalphite Worker", NpcID.KALPHITE_WORKER_STRONGHOLDCAVE, new WorldPoint(3322, 9502, 0)),
        KALPHITE_WORKER_LVL28_NORTHWEST("Kalphite Worker", NpcID.KALPHITE_WORKER_STRONGHOLDCAVE, new WorldPoint(3279, 9519, 0)),
        KALPHITE_SOLDIER_LVL85_NORTH("Kalphite Soldier", NpcID.KALPHITE_SOLDIER_STRONGHOLDCAVE, new WorldPoint(3309, 9522, 0)),
        KALPHITE_SOLDIER_LVL85_SOUTH("Kalphite Soldier", NpcID.KALPHITE_SOLDIER_STRONGHOLDCAVE, new WorldPoint(3314, 9481, 0)),
        KALPHITE_GUARDIAN_LVL141("Kalphite Guardian", NpcID.KALPHITE_LORD_STRONGHOLDCAVE, new WorldPoint(3280, 9498, 0));

        private final String monsterName;
        private final int id;
        private final WorldPoint location;

        AlternativeKalphiteTask(String _monsterName, int _id, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.id = _id;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeWolfTask {
        WOLF_LVL11_14("Wolf", new WorldPoint(2746, 3478, 0)),
        WHITE_WOLF_LVL25("White wolf", new WorldPoint(2848, 3481, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeWolfTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeCrabTask {
        SAND_CRAB_AVIUM_SAVANNAH("Sand crab", new WorldPoint(1612, 2892, 0)),
        SAND_CRAB_HOSIDIUS_SOUTH("Sand crab", new WorldPoint(1765, 3468, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeCrabTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeHillGiantTask {
        HILL_GIANT_EDGEVILLE_DUNGEON("Hill Giant", new WorldPoint(3117, 9845, 0)),
        HILL_GIANT_CATACOMBS_OF_KOUREND("Hill Giant", new WorldPoint(1664, 10072, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeHillGiantTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeShadeTask {
        // Uses the shadow variant of the name because if we are out of combat and need to search for a target, they are shadows
        LOAR_SHADE("Loar Shadow", new WorldPoint(3494, 3279, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeShadeTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }

    @Getter
    public enum AlternativeVampyreTask {
        FERAL_VAMPYRE("Feral Vampyre", new WorldPoint(3592, 3481, 0));

        private final String monsterName;
        private final WorldPoint location;

        AlternativeVampyreTask(String _monsterName, WorldPoint _location)
        {
            this.monsterName = _monsterName;
            this.location = _location;
        }
    }
}
