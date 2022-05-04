package sl.tiger.scraper.dto.search.advance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPartDetailsByAdvanceSearchRequest {
    private String site;
    private String make;
    private String model;
    private String trim;
    private Integer year;
    private String searchText;
    private String category;
    private String group;
    private String engine;
    private boolean withAvailability;
    private int maxResultCount;
}
