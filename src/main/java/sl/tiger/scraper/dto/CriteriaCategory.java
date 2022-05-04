package sl.tiger.scraper.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CriteriaCategory {
    private String category;
    private String[] groups;

    public CriteriaCategory(String name) {
        category = name;
    }
}
