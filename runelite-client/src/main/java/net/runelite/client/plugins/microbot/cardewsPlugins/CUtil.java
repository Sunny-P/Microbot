package net.runelite.client.plugins.microbot.cardewsPlugins;

import net.runelite.api.Point;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;

import java.awt.*;

public class CUtil {
    public static Point GetRandomPointInRectangle(Rectangle rect)
    {
        int randX = (int)(Math.random() * rect.width);
        int randY = (int)(Math.random() * rect.height);
        return new Point(randX, randY);
    }

    public static void SetMyAntiban()
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
        Rs2AntibanSettings.microBreakDurationLow = 3;
        Rs2AntibanSettings.microBreakDurationHigh = 15;
        Rs2AntibanSettings.microBreakChance = 0.08;

        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.actionCooldownChance = 0.6;

        Rs2AntibanSettings.devDebug = false;
    }
}
