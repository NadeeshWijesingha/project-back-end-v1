package sl.tiger.scraper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Result {
    @Id
    private String id;
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
    private LocalDateTime dateTime;
    private String siteName;
    private Availability availability;

    public Result(String partNumber, String title, String description, String listPrice,
                  String yourPrice, String corePrice, String ehcPrice, String extend,
                  String imageUrl, String manufacturer, String itemLocation, String productLine,
                  String position, String attributes, String applicationNotes, LocalDateTime dateTime,
                  String siteName, Availability availability) {
        this.partNumber = partNumber;
        this.title = title;
        this.description = description;
        this.listPrice = listPrice;
        this.yourPrice = yourPrice;
        this.corePrice = corePrice;
        EhcPrice = ehcPrice;
        this.extend = extend;
        this.imageUrl = imageUrl;
        this.manufacturer = manufacturer;
        this.itemLocation = itemLocation;
        this.productLine = productLine;
        this.position = position;
        this.attributes = attributes;
        this.applicationNotes = applicationNotes;
        this.dateTime = dateTime;
        this.siteName = siteName;
        this.availability = availability;
    }
}
