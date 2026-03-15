package com.riluttante.rivalcore.services;

import com.riluttante.rivalcore.RivalCorePlugin;
import com.riluttante.rivalcore.config.ConfigManager;
import com.riluttante.rivalcore.models.GameTeam;
import com.riluttante.rivalcore.utils.ColorUtil;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SidebarService {

    // §0..§f — 16 unique invisible entry strings for sidebar slots
    private static final String SLOT_CHARS = "0123456789abcdef";
    private static final int SPACE_PX = 4;
    private final RivalCorePlugin plugin;
    private final ConfigManager configManager;
    private final KillTrackerService killTrackerService;
    private final TeamService teamService;
    private final GameService gameService;
    private BukkitTask task;

    public SidebarService(RivalCorePlugin plugin, ConfigManager configManager,
                          KillTrackerService killTrackerService,
                          TeamService teamService, GameService gameService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.killTrackerService = killTrackerService;
        this.teamService = teamService;
        this.gameService = gameService;
    }

    private static String slotEntry(int i) {
        return "§" + SLOT_CHARS.charAt(i % SLOT_CHARS.length());
    }

    /**
     * Approximates the pixel width of a player name using Minecraft's default font.
     * Only characters valid in player names ([a-zA-Z0-9_]) are considered.
     */
    private static int namePixelWidth(String name) {
        int w = 0;
        for (char c : name.toCharArray()) w += charPx(c);
        return w;
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private static int charPx(char c) {
        return switch (c) {
            case 'i', 'l', '|', '!' -> 3;
            case 'f', 'I', 'j', 't', '[', ']', '(', ')' -> 5;
            case 'm', 'w', 'M', 'W' -> 9;
            case ' ' -> SPACE_PX;
            default -> 6;
        };
    }

    // ── Update cycle ──────────────────────────────────────────────────────────

    /**
     * Removes Minecraft color/format codes (&X or §X) from a string.
     */
    private static String stripColors(String s) {
        return s.replaceAll("[&§][0-9a-fk-orA-FK-OR]", "");
    }

    public void start() {
        setupSidebar();
        int ticks = configManager.getSidebarUpdateTicks();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(plugin, 0L, ticks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        cleanupSidebar();
        resetTab();
    }

    // ── Name pixel-width alignment ────────────────────────────────────────────

    private void setupSidebar() {
        if (!configManager.isSidebarEnabled()) return;
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        Objective existing = sb.getObjective("rc_sidebar");
        if (existing != null) existing.unregister();

        List<String> lines = configManager.getSidebarLines();
        if (lines.isEmpty()) return;

        Objective obj = sb.registerNewObjective("rc_sidebar", Criteria.DUMMY,
                ColorUtil.colorize(configManager.getSidebarTitle()));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < lines.size(); i++) {
            Score score = obj.getScore(slotEntry(i));
            score.setScore(lines.size() - i); // top line = highest score
            score.numberFormat(NumberFormat.blank());
            score.customName(ColorUtil.colorize("&7-"));
        }
    }

    private void update() {
        List<Map.Entry<UUID, Integer>> top = killTrackerService.getTopKillers(3);
        if (configManager.isSidebarEnabled()) updateSidebar(top);
        updateTab(top);
    }

    private void updateSidebar(List<Map.Entry<UUID, Integer>> top) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = sb.getObjective("rc_sidebar");
        if (obj == null) return;

        List<String> lines = configManager.getSidebarLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = applyPlaceholders(lines.get(i), top);
            obj.getScore(slotEntry(i)).customName(ColorUtil.colorize(line));
        }
    }

    private void updateTab(List<Map.Entry<UUID, Integer>> top) {
        List<String> headerLines = configManager.getTabHeader();
        List<String> footerLines = configManager.getTabFooter();

        Component header = headerLines.isEmpty() ? null
                : ColorUtil.colorize(applyPlaceholders(String.join("\n", headerLines), top));
        Component footer = footerLines.isEmpty() ? null
                : ColorUtil.colorize(applyPlaceholders(String.join("\n", footerLines), top));

        boolean hpEnabled = configManager.isTabHpEnabled();
        String hpFormat = hpEnabled ? configManager.getTabHpFormat() : null;

        boolean teamsRevealed = teamService.isTeamsRevealed();
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        // Pre-compute team data per player (prefix string + name color)
        record TabEntry(String teamPrefix, String nameColor) {
        }
        java.util.Map<java.util.UUID, TabEntry> entries = new java.util.HashMap<>();
        for (Player p : online) {
            String teamPrefix = "";
            String nameColor = "";
            if (teamsRevealed && !gameService.isEliminated(p.getUniqueId())) {
                GameTeam team = teamService.getTeam(p.getUniqueId());
                if (team != null) {
                    boolean isRed = team == GameTeam.RED;
                    teamPrefix = isRed ? configManager.getChatPrefixRed() + " "
                            : configManager.getChatPrefixBlue() + " ";
                    nameColor = isRed ? "&c" : "&b";
                }
            }
            entries.put(p.getUniqueId(), new TabEntry(teamPrefix, nameColor));
        }

        // Max total visual width = prefixPx + namePx across all players
        int maxTotalPx = online.stream().mapToInt(p -> {
            TabEntry e = entries.get(p.getUniqueId());
            return namePixelWidth(stripColors(e.teamPrefix())) + namePixelWidth(p.getName());
        }).max().orElse(0);

        for (Player p : online) {
            if (header != null && footer != null) {
                p.sendPlayerListHeaderAndFooter(header, footer);
            } else if (header != null) {
                p.sendPlayerListHeader(header);
            } else if (footer != null) {
                p.sendPlayerListFooter(footer);
            }
            if (hpFormat != null) {
                TabEntry e = entries.get(p.getUniqueId());
                int prefixPx = namePixelWidth(stripColors(e.teamPrefix()));
                int usedPx = prefixPx + namePixelWidth(p.getName());
                int spaces = Math.max(2, (int) Math.ceil((double) (maxTotalPx - usedPx + SPACE_PX * 2) / SPACE_PX));
                String paddedName = e.nameColor() + p.getName() + " ".repeat(spaces);

                String text = hpFormat
                        .replace("%team%", e.teamPrefix())
                        .replace("%name%", paddedName)
                        .replace("%hp%", String.valueOf((int) p.getHealth()))
                        .replace("%online%", String.valueOf(online.size()));
                p.playerListName(ColorUtil.colorize(text));
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String applyPlaceholders(String text, List<Map.Entry<UUID, Integer>> top) {
        text = text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        for (int rank = 1; rank <= 3; rank++) {
            String playerName = (rank - 1) < top.size()
                    ? killTrackerService.getPlayerName(top.get(rank - 1).getKey()) : "-";
            int kills = (rank - 1) < top.size() ? top.get(rank - 1).getValue() : 0;
            text = text
                    .replace("%top" + rank + "_player%", playerName)
                    .replace("%top" + rank + "_kills%", String.valueOf(kills));
        }
        return text;
    }

    private void resetTab() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playerListName(null);
            p.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
        }
    }

    private void cleanupSidebar() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = sb.getObjective("rc_sidebar");
        if (obj != null) obj.unregister();
    }
}
