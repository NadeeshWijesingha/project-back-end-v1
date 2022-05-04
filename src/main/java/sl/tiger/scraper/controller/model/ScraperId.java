package sl.tiger.scraper.controller.model;

public enum ScraperId {
    ALTROM("ALTROM"),
    CBK_INTERNATIONAL("CBK_INTERNATIONAL"),
    KEYSTONE_AUTOMOTIVE("KEYSTONE_AUTOMOTIVE"),
    MY_PLACE_FOR_PARTS("MY_PLACE_FOR_PARTS"),
    TIGER_AUTO_PARTS("TIGER_AUTO_PARTS"),
    TRANSBEC("TRANSBEC"),
    WORLD_AUTO("WORLD_AUTO");

    public final String id;

    ScraperId(String id) {
        this.id = id;
    }
}
