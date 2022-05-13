package sl.tiger.scraper.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import sl.tiger.scraper.dto.Availability;

@Getter
@Setter
@Document
public class ResultEntity {
    @Id
    private String resultId;
    private String partNumber;
    private String title;
    private String description;
    private String listPrice;
    private String yourPrice;
    private String corePrice;
    private String ehcPrice;
    private String extend;
    private String imageUrl;
    private String manufacturer;
    private String itemLocation;
    private String productLine;
    private String position;
    private String attributes;
    private String applicationNotes;
    private Availability availability;

    public ResultEntity(String partNumber, String title, String description, String listPrice,
                        String yourPrice, String corePrice, String ehcPrice, String extend,
                        String imageUrl, String manufacturer, String itemLocation, String productLine,
                        String position, String attributes, String applicationNotes, Availability availability) {
        this.partNumber = partNumber;
        this.title = title;
        this.description = description;
        this.listPrice = listPrice;
        this.yourPrice = yourPrice;
        this.corePrice = corePrice;
        this.ehcPrice = ehcPrice;
        this.extend = extend;
        this.imageUrl = imageUrl;
        this.manufacturer = manufacturer;
        this.itemLocation = itemLocation;
        this.productLine = productLine;
        this.position = position;
        this.attributes = attributes;
        this.applicationNotes = applicationNotes;
        this.availability = availability;
    }
}
