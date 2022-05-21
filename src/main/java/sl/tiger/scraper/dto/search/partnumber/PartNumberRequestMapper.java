package sl.tiger.scraper.dto.search.partnumber;

import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.PartNumberCriteria;

public class PartNumberRequestMapper {
    private PartNumberRequestMapper() {
    }

    public static PartNumberCriteria getSearchCriteria(GetPartDetailsByPartNumberRequest request) {
        return PartNumberCriteria.builder()
                .site(request.getSite())
                .scrappers(getScrappersFromRequest(request))
                .partNumber(request.getPartNumber())
                .addToCart(request.getAddToCart())
                .customerName(request.getCustomerName())
                .customerContactNumber(request.getCustomerContactNumber())
                .build();
    }

    private static String[] getScrappersFromRequest(GetPartDetailsByPartNumberRequest request) {
        String id = ScraperId.valueOf(request.getSite()).id;
        return new String[]{id};
    }
}
