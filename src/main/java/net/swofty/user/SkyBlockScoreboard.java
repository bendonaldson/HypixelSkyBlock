package net.swofty.user;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.SkyBlock;
import net.swofty.event.SkyBlockEvent;
import net.swofty.event.custom.PlayerRegionChangeEvent;
import net.swofty.mission.MissionData;
import net.swofty.utility.StringUtility;
import net.swofty.data.DataHandler;
import net.swofty.data.datapoints.DatapointDouble;
import net.swofty.region.SkyBlockRegion;
import net.swofty.calendar.SkyBlockCalendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkyBlockScoreboard {
    private static Map<UUID, Sidebar> sidebarCache = new HashMap<>();
    private static Integer skyblockName = 0;

    public static void start() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();

        // Scoreboard Updater
        scheduler.submitTask(() -> {
            skyblockName++;
            if (skyblockName > 50) {
                skyblockName = 0;
            }

            for (SkyBlockPlayer player : SkyBlock.getLoadedPlayers()) {
                DataHandler dataHandler = player.getDataHandler();
                SkyBlockRegion region = player.getRegion();
                MissionData missionData = player.getMissionData();

                if (dataHandler == null) {
                    continue;
                }

                if (sidebarCache.containsKey(player.getUuid())) {
                    sidebarCache.get(player.getUuid()).removeViewer(player);
                }

                Sidebar sidebar = new Sidebar(getSidebarName(skyblockName, false));

                addLine("§7" + new SimpleDateFormat("MM/dd/yy").format(new Date()) + " §8???", sidebar);
                addLine("§7 ", sidebar);
                addLine("§f " + SkyBlockCalendar.getMonthName() + " " + StringUtility.ntify(SkyBlockCalendar.getDay()), sidebar);
                addLine("§7 " + SkyBlockCalendar.getDisplay(SkyBlockCalendar.getElapsed()), sidebar);
                try {
                    addLine("§7 ⏣ " + region.getType().getColor() + region.getType().getName(), sidebar);
                } catch (NullPointerException ignored) {
                    addLine(" §7Unknown", sidebar);
                }
                addLine("§7 ", sidebar);
                addLine("§fPurse: §6" + dataHandler.get(DataHandler.Data.COINS, DatapointDouble.class).getValue(), sidebar);
                addLine("§7 ", sidebar);
                if (region != null &&
                        !missionData.getActiveMissions(region.getType()).isEmpty()) {
                    addLine("§fObjective", sidebar);
                    addLine("§e" + missionData.getActiveMissions(region.getType()).get(0), sidebar);
                    addLine("§7 ", sidebar);
                }

                addLine("§ewww.hypixel.net", sidebar);

                sidebar.addViewer(player);

                sidebarCache.put(player.getUuid(), sidebar);
            }
            return TaskSchedule.tick(2);
        });
    }

    public static void removeCache(Player player) {
        sidebarCache.remove(player.getUuid());
    }

    private static void addLine(String text, Sidebar sidebar) {
        for (Sidebar.ScoreboardLine existingLine : sidebar.getLines()) {
            sidebar.updateLineScore(existingLine.getId(), existingLine.getLine() + 1);
        }

        sidebar.createLine(new Sidebar.ScoreboardLine(UUID.randomUUID().toString(), Component.text(text), 0));
    }
    private static String getSidebarName(int counter, boolean isGuest) {
        String baseText = "SKYBLOCK";
        String[] colors = {"§f§l", "§6§l", "§e§l"};
        String endColor = "§a§l";
        String endText = isGuest ? " GUEST" : "";

        if (counter > 0 && counter <= 8) {
            return colors[0] + baseText.substring(0, counter - 1) +
                    colors[1] + baseText.substring(counter - 1, counter) +
                    colors[2] + baseText.substring(counter) +
                    endColor + endText;
        } else if ((counter >= 9 && counter <= 19) ||
                (counter >= 25 && counter <= 29)) {
            return colors[0] + baseText + endColor + endText;
        } else {
            return colors[2] + baseText + endColor + endText;
        }
    }
}