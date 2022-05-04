package sl.tiger.scraper.dto.search.advance;

import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.dto.Criteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvanceSearchRequestMapperTest {

    @Test
    void when_noParams_given_should_returnEmptyObject() {
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(new GetPartDetailsByAdvanceSearchRequest());
        assertNotNull(searchCriteria);
    }

    @Test
    void when_correctSite_given_should_returnCriteriaWithCorrectScrapper() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setSite("ALTROM");
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);
        assertNotNull(searchCriteria);
        assertArrayEquals(searchCriteria.getScrappers(), new String[]{ScraperId.ALTROM.name()});
    }

    @Test
    void when_correctMake_given_should_returnCriteriaWithCorrectMake() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setMake("TOYOTA");
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);
        assertNotNull(searchCriteria);
        assertEquals("TOYOTA",searchCriteria.getMake());
    }

    @Test
    void when_correctModel_given_should_returnCriteriaWithCorrectModel() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setModel("PRIOUS");
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);
        assertNotNull(searchCriteria);
        assertEquals("PRIOUS", searchCriteria.getModel());
    }

    @Test
    void when_correctYear_given_should_returnCriteriaWithCorrectYear() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setYear(2000);
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);

        assertNotNull(searchCriteria);
        assertEquals("2000", searchCriteria.getYear());
    }

    @Test
    void when_correctCategory_given_should_returnCriteriaWithCorrectCategory() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setCategory("Brakes");
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);

        assertNotNull(searchCriteria);
        assertEquals("Brakes", searchCriteria.getCategories()[0].getCategory());
    }

    @Test
    void when_correctGroup_given_should_returnCriteriaWithCorrectGroup() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setCategory("Brakes");
        request.setGroup("Disc Brakes");
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);

        assertNotNull(searchCriteria);
        assertEquals("Disc Brakes", searchCriteria.getCategories()[0].getGroups()[0]);
    }
}