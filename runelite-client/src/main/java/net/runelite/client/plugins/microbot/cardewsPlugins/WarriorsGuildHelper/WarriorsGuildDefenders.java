package net.runelite.client.plugins.microbot.cardewsPlugins.WarriorsGuildHelper;

import lombok.Getter;
import net.runelite.api.gameval.ItemID;

@Getter
public enum WarriorsGuildDefenders {
    NONE(-1),
    BRONZE_DEFENDER(ItemID.BRONZE_PARRYINGDAGGER),
    IRON_DEFENDER(ItemID.IRON_PARRYINGDAGGER),
    STEEL_DEFENDER(ItemID.STEEL_PARRYINGDAGGER),
    BLACK_DEFENDER(ItemID.BLACK_PARRYINGDAGGER),
    MITHRIL_DEFENDER(ItemID.MITHRIL_PARRYINGDAGGER),
    ADAMANT_DEFENDER(ItemID.ADAMANT_PARRYINGDAGGER),
    RUNE_DEFENDER(ItemID.RUNE_PARRYINGDAGGER),
    DRAGON_DEFENDER(ItemID.DRAGON_PARRYINGDAGGER);

    final int id;

    WarriorsGuildDefenders(int id)
    {
        this.id = id;
    }
}
