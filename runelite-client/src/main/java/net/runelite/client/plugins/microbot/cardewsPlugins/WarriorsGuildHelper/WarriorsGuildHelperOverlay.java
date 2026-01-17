package net.runelite.client.plugins.microbot.cardewsPlugins.WarriorsGuildHelper;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.cardewsPlugins.SailingHelper.SailingHelperScript;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class WarriorsGuildHelperOverlay extends OverlayPanel {

    @Inject
    public WarriorsGuildHelperOverlay(WarriorsGuildHelperPlugin plugin) {
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        try {
            panelComponent.setPreferredSize(new Dimension(250, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Cardew's Warriors' Guild Helper V1.0.0")
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current Defender: " + WarriorsGuildHelperScript.GetCurrentDefender())
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Next Defender: " + WarriorsGuildHelperScript.GetNextDefender())
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current State: " + WarriorsGuildHelperScript.GetCurrentState())
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}

