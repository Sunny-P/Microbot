package net.runelite.client.plugins.microbot.cardewsPlugins.SailingHelper;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class SailingHelperOverlay extends OverlayPanel {
    @Inject
    SailingHelperOverlay(SailingHelperPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Sailing Helper V0.0.1")
                    .color(Color.GREEN)
                    .build());

            switch (SailingHelperScript.currentAutoPortTaskState)
            {
                case DETERMINE_CURRENT_PORT:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Determine current port")
                            .build());
                    break;
                case GETTING_TASKS_FROM_BOARD:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Getting task from board")
                            .build());
                    break;
                case ACTIVE_TASK_BEGIN:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Active task begin")
                            .build());
                    break;
                case GO_TO_CARGO_PICKUP_DESTINATION:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Go to cargo pickup location")
                            .build());
                    break;
                case ACTIVE_TASK_SAIL_TO_DESTINATION:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Active task sail to destination")
                            .build());
                    break;
                case ACTIVE_TASK_FINISH:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Active task finish")
                            .build());
                    break;
                case FINISHED_TASK_DECIDING_NEXT_STATE:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Finished task deciding next state")
                            .build());
                    break;
            }

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
