package net.runelite.client.plugins.microbot.cardewsPlugins;

import net.runelite.api.Point;

import java.awt.*;

public class CUtil {
    public static Point GetRandomPointInRectangle(Rectangle rect)
    {
        int randX = (int)(Math.random() * rect.width);
        int randY = (int)(Math.random() * rect.height);
        return new Point(randX, randY);
    }
}
