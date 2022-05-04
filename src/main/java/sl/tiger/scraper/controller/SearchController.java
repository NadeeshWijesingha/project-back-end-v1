package sl.tiger.scraper.controller;

import br.com.fluentvalidator.context.Error;
import br.com.fluentvalidator.context.ValidationResult;
import sl.tiger.scraper.controller.model.ScraperId;
import sl.tiger.scraper.dto.Criteria;
import sl.tiger.scraper.dto.CriteriaCategory;
import sl.tiger.scraper.dto.ResponseStatus;
import sl.tiger.scraper.dto.ResponseWrapper;
import sl.tiger.scraper.dto.search.advance.AdvanceSearchRequestMapper;
import sl.tiger.scraper.dto.search.advance.GetPartDetailsByAdvanceSearchRequest;
import sl.tiger.scraper.dto.search.advance.GetPartDetailsByAdvanceSearchRequestValidator;
import sl.tiger.scraper.dto.search.partnumber.GetPartDetailsByPartNumberRequest;
import sl.tiger.scraper.dto.search.partnumber.GetPartDetailsByPartNumberRequestValidator;
import sl.tiger.scraper.dto.search.partnumber.PartNumberRequestMapper;
import sl.tiger.scraper.exception.CriteriaException;
import sl.tiger.scraper.scraper.ScrapHandler;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchController {

    final GetPartDetailsByAdvanceSearchRequestValidator validator;
    private final GetPartDetailsByPartNumberRequestValidator partValidator;
    private final ScrapHandler handler;
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    public SearchController(GetPartDetailsByAdvanceSearchRequestValidator validator,
                            GetPartDetailsByPartNumberRequestValidator partValidator,
                            ScrapHandler handler) {
        this.validator = validator;
        this.partValidator = partValidator;
        this.handler = handler;
    }

    @GetMapping(value = "/search")
    public ResponseWrapper search(@RequestParam String text, String year, String make, String model, String engine, String category, String group) {

        Criteria criteria = new Criteria();

        String[] scrappers = {ScraperId.MY_PLACE_FOR_PARTS.id};
        criteria.setScrappers(scrappers);

        criteria.setText(text);
        criteria.setYear(year);
        criteria.setMake(make);
        criteria.setModel(model);
        criteria.setEngine(engine);
        CriteriaCategory[] categories = {new CriteriaCategory(category)};
        criteria.setCategories(categories);
        String[] groups = {group};
        categories[0].setGroups(groups);

        logger.trace("Get Request");

        try {
            return new ResponseWrapper(handler.search(criteria));
        } catch (CriteriaException exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setStatus(ResponseStatus.ERROR);
            responseWrapper.setMassage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping(value = "/search")
    public ResponseWrapper search(@RequestBody GetPartDetailsByAdvanceSearchRequest request) {

        //validate using fluent validation
        ValidationResult validate = validator.validate(request);
        //if request is not valid then return error
        if (!validate.isValid()) {
            return getInvalidResponse(validate);
        }
        //map request to search criteria
        Criteria searchCriteria = AdvanceSearchRequestMapper.getSearchCriteria(request);
        //perform search and return results
        try {
            return new ResponseWrapper(handler.search(searchCriteria));
        } catch (CriteriaException | WebDriverException exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setStatus(ResponseStatus.ERROR);
            responseWrapper.setMassage(exception.getMessage());
            return responseWrapper;
        }
    }

    @PostMapping(value = "/part-number-search")
    public ResponseWrapper searchByPartNumber(@RequestBody GetPartDetailsByPartNumberRequest request) {

        //validate using fluent validation
        ValidationResult validate = partValidator.validate(request);

        //if request is not valid then return error
        if (!validate.isValid()) {
            return getInvalidResponse(validate);
        }

        //map request to search criteria
        Criteria searchCriteria = PartNumberRequestMapper.getSearchCriteria(request);

        //perform search and return results
        try {
            return new ResponseWrapper(handler.searchByPartNumber(searchCriteria));
        } catch (CriteriaException exception) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.setStatus(ResponseStatus.ERROR);
            responseWrapper.setMassage(exception.getMessage());
            return responseWrapper;
        }
    }

    private ResponseWrapper getInvalidResponse(ValidationResult validate) {

        StringBuilder combinedErrorMassage = new StringBuilder();
        for (Error error : validate.getErrors()) {
            combinedErrorMassage.append(error.getMessage()).append("\n");
        }
        ResponseWrapper responseWrapper = new ResponseWrapper();
        responseWrapper.setStatus(ResponseStatus.ERROR);
        responseWrapper.setMassage(combinedErrorMassage.toString());
        return responseWrapper;
    }
}
