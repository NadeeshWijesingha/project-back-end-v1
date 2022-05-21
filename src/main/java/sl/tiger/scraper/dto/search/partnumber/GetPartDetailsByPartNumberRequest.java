package sl.tiger.scraper.dto.search.partnumber;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPartDetailsByPartNumberRequest {
    private String site;
    private String partNumber;
    private Boolean addToCart;
    private String customerName;
    private String customerContactNumber;
}
