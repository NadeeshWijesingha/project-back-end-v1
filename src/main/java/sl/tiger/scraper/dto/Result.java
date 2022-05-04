package sl.tiger.scraper.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    private String partNumber;
    private String title;
    private String description;
    private String listPrice;
    private String yourPrice;
    private String corePrice;
    private String EhcPrice;
    private String extend;
    private String imageUrl;
    private String manufacturer;
    private String itemLocation;
    private String productLine;
    private String position;
    private String attributes;
    private String applicationNotes;
    private Availability availability;
}
