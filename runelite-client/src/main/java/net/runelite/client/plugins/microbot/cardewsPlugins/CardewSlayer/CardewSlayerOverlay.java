package net.runelite.client.plugins.microbot.cardewsPlugins.CardewSlayer;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CardewSlayerOverlay extends OverlayPanel {
    @Inject
    CardewSlayerOverlay(CardewSlayerPlugin plugin)
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
                    .text("Cardew Slayer V0.2")
                    .color(Color.GREEN)
                    .build());

            switch (CardewSlayerScript.currentState){
                case MOVING_TO_SLAYER_MASTER:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("MOVING_TO_SLAYER_MASTER")
                            .build());
                    break;
                case GETTING_SLAYER_TASK:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("GETTING_SLAYER_TASK")
                            .build());
                    break;
                case MOVING_TO_MONSTER_LOCATION:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("MOVING_TO_MONSTER_LOCATION")
                            .build());
                    break;
                case SLAYING_MONSTER:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("SLAYING_MONSTER")
                            .build());
                    break;
                case MOVING_TO_NEAREST_BANK:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("MOVING_TO_NEAREST_BANK")
                            .build());
                    break;
                case BANKING:
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("BANKING")
                            .build());
                    break;
            }

            panelComponent.getChildren().add(LineComponent.builder().build());

            if (CardewSlayerScript.slayerTarget.getMonsterData() == null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Target: Nothing | Kills Left: " + CardewSlayerScript.killsLeft)
                        .build());
            }
            else {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Target: " + CardewSlayerScript.slayerTarget.getMonsterData().getMonster()
                                + " | Kills Left: " + CardewSlayerScript.killsLeft)
                        .build());
            }


            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());


        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
