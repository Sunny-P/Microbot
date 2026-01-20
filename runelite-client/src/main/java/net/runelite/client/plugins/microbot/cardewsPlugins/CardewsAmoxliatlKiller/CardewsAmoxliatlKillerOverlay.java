package net.runelite.client.plugins.microbot.cardewsPlugins.CardewsAmoxliatlKiller;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class CardewsAmoxliatlKillerOverlay extends OverlayPanel {
    @Inject
    CardewsAmoxliatlKillerOverlay(CardewsAmoxliatlKillerPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            //panelComponent.setPreferredSize(new Dimension(200, 300));
            //panelComponent.getChildren().add(TitleComponent.builder()
            //        .text("Mini Tasks V0.0.1")
            //        .color(Color.GREEN)
            //        .build());
//
            //panelComponent.getChildren().add(LineComponent.builder().build());
//
            //panelComponent.getChildren().add(LineComponent.builder()
            //        .left(Microbot.status)
            //        .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
