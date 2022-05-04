package sl.tiger.scraper.dto.search.partnumber;

import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.dto.Criteria;

public class PartNumberRequestMapper {
    private PartNumberRequestMapper() {
    }

    public static Criteria getSearchCriteria(GetPartDetailsByPartNumberRequest request) {
        return Criteria.builder()
                .searchType(Criteria.SearchType.PART_NUMBER)
                .scrappers(getScrappersFromRequest(request))
                .partNumber(request.getPartNumber())
                .addToCart(request.getAddToCart())
                .build();
    }

    private static String[] getScrappersFromRequest(GetPartDetailsByPartNumberRequest request) {
        String id = ScraperId.valueOf(request.getSite()).id;
        return new String[]{id};
    }
}
