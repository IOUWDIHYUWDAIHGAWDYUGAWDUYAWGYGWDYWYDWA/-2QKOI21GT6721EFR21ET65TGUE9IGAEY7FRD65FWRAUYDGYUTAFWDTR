package com.reallifeearth.entity.npc;

public enum NpcProfession {
    UNEMPLOYED("unemployed", 0),
    FARMER("farmer", 40),
    MINER("miner", 55),
    DOCTOR("doctor", 120),
    POLICE("police", 80),
    TEACHER("teacher", 70),
    CHEF("chef", 50),
    CASHIER("cashier", 35),
    ENGINEER("engineer", 100),
    DRIVER("driver", 45),
    SHOPKEEPER("shopkeeper", 60),
    BANKER("banker", 90),
    BUILDER("builder", 65),
    ARTIST("artist", 38),
    SCIENTIST("scientist", 110),
    STUDENT("student", 0),
    RETIRED("retired", 30);

    public final String id;
    public final int dailyWage;

    NpcProfession(String id, int dailyWage) {
        this.id = id;
        this.dailyWage = dailyWage;
    }

    public static NpcProfession byId(String id) {
        for (var p : values()) if (p.id.equals(id)) return p;
        return UNEMPLOYED;
    }
}
