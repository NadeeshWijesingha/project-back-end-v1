package sl.tiger.scraper.dto.search.advance;

import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.CriteriaCategory;

public class AdvanceSearchRequestMapper {

    private AdvanceSearchRequestMapper() {
    }

    public static Criteria getSearchCriteria(GetPartDetailsByAdvanceSearchRequest request) {
        return Criteria.builder()
                .searchType(Criteria.SearchType.ADVANCE)
                .scrappers(getScrappersFromRequest(request))
                .make(request.getMake())
                .model(request.getModel())
                .trim(request.getTrim())
                .year(String.valueOf(request.getYear()))
                .searchText(request.getSearchText())
                .engine(request.getEngine())
                .withAvailability(request.isWithAvailability())
                .maxResultCount(request.getMaxResultCount())
                .categories(
                        new CriteriaCategory[]{
                                CriteriaCategory.builder()
                                        .category(request.getCategory())
                                        .groups(new String[]{request.getGroup()})
                                        .build()})
                .build();
    }

    private static String[] getScrappersFromRequest(GetPartDetailsByAdvanceSearchRequest request) {
        String id = ScraperId.valueOf(request.getSite()).id;
        return new String[]{id};
    }
}
