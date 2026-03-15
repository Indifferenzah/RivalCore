package com.riluttante.rivalcore.models;

public enum GameTeam {
    RED("red", "&cROSSO", "§c"),
    BLUE("blue", "&bBLU", "§b");

    private final String id;
    private final String displayName;
    private final String colorCode;

    GameTeam(String id, String displayName, String colorCode) {
        this.id = id;
        this.displayName = displayName;
        this.colorCode = colorCode;
    }

    public static GameTeam fromId(String id) {
        for (GameTeam t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }
}
