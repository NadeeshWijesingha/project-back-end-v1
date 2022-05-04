package sl.tiger.scraper.dto;

import lombok.*;

import java.util.Arrays;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Criteria {
    private SearchType searchType;

    private String text;
    private String[] scrappers;
    private String year;
    private String make;
    private String model;
    private String trim;
    private String searchText;
    private String engine;
    private CriteriaCategory[] categories;
    private String[] partTypes;
    private String partNumber;
    private boolean addToCart;
    private boolean withAvailability;
    private int maxResultCount;
    public String[] getCategoryNames() {
        return Arrays.stream(categories).map(CriteriaCategory::getCategory).toArray(String[]::new);
    }

    public enum SearchType{
        PART_NUMBER, ADVANCE;
    }

    public String screenshotName() {
        return "[year-" + year + "] [make-" + make + "] [model-" + model + "] [category-" + Arrays.toString(categories) + "] [engine-" + engine + "]";
    }
}

