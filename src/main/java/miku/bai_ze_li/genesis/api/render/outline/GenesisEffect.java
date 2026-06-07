package miku.bai_ze_li.genesis.api.render.outline;

public enum GenesisEffect {
    // 瀹氫箟椤哄簭=鍐呴儴绱㈠紩椤哄簭锛屾€ц兘鏈€浼?
    BLACK_RED("black_red"),      // ordinal=0
    BLUE_WHITE("blue_white"),    // ordinal=1
    RAINBOW("rainbow");          // ordinal=2

    private static final int CACHE_SIZE = 3;
    private static final GenesisEffect[] ORDINAL_CACHE = values();
    private static final String[] ID_CACHE = new String[CACHE_SIZE];

    private final String id;

    GenesisEffect(String id) {
        this.id = id.intern();
    }

    public String getId() {
        return id;
    }
    public static GenesisEffect fromOrdinal(int ordinal) {
        return (ordinal >= 0 && ordinal < CACHE_SIZE) ? ORDINAL_CACHE[ordinal] : null;
    }

    public static GenesisEffect byId(String id) {
        if (id == null) {
            return null;
        }
        for (GenesisEffect effect : ORDINAL_CACHE) {
            if (effect.isId(id)) {
                return effect;
            }
        }
        return null;
    }

    public static int getEffectCount() {
        return CACHE_SIZE;
    }

    public static GenesisEffect[] getCachedValues() {
        return ORDINAL_CACHE;
    }

    public boolean isId(String testId) {
        return this.id == testId || this.id.equals(testId);
    }
}
