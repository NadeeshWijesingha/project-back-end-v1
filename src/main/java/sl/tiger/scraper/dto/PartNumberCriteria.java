package sl.tiger.scraper.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document
public class PartNumberCriteria {
    @Id
    private String id;
    private String site;
    private String[] scrappers;
    private String partNumber;
    private Boolean addToCart;
    private String customerName;
    private String customerContactNumber;
    private int maxResultCount;
}
