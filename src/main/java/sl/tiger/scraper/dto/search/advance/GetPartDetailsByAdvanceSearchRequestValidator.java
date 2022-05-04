package sl.tiger.scraper.dto.search.advance;

import br.com.fluentvalidator.AbstractValidator;
import sl.tiger.scraper.controller.model.ScraperId;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.fluentvalidator.predicate.ComparablePredicate.greaterThanOrEqual;
import static br.com.fluentvalidator.predicate.ComparablePredicate.lessThanOrEqual;
import static br.com.fluentvalidator.predicate.LogicalPredicate.not;
import static br.com.fluentvalidator.predicate.ObjectPredicate.nullValue;
import static br.com.fluentvalidator.predicate.StringPredicate.stringEmptyOrNull;

@Component
public class GetPartDetailsByAdvanceSearchRequestValidator extends AbstractValidator<GetPartDetailsByAdvanceSearchRequest> {
    private static final List<String> sites = Arrays.stream(ScraperId.values()).map(scraperId -> scraperId.id).collect(Collectors.toList());

    @Override
    public void rules() {
        ruleFor(GetPartDetailsByAdvanceSearchRequest::getSite)
                .must(not(nullValue()))
                    .withMessage("Site is missing")
                    .withCode("1")
                    .withFieldName("site")
                .must(sites::contains)
                    .when(not(nullValue()))
                    .withMessage("Site value should be one of " + String.join(",", sites))
                    .withCode("1")
                    .withFieldName("site");

        ruleFor(GetPartDetailsByAdvanceSearchRequest::getMake)
                .must(not(stringEmptyOrNull()))
                    .withMessage("Make is missing")
                    .withCode("1")
                    .withFieldName("make");

        ruleFor(GetPartDetailsByAdvanceSearchRequest::getModel)
                .must(not(stringEmptyOrNull()))
                    .withMessage("Model is missing")
                    .withCode("1")
                    .withFieldName("model");

        ruleFor(GetPartDetailsByAdvanceSearchRequest::getYear)
                .must(greaterThanOrEqual(1886))
                    .when(not(nullValue()))
                    .withMessage("Year must be greater than or equal to 1886")
                    .withCode("1")
                    .withFieldName("year")
                .must(lessThanOrEqual(Year.now().getValue() + 1))
                    .when(not(nullValue()))
                    .withMessage("Year must be less than or equal to " + (Year.now().getValue() + 1))
                    .withCode("1")
                    .withFieldName("year");

        ruleFor(GetPartDetailsByAdvanceSearchRequest::getCategory)
                .must(not(stringEmptyOrNull()))
                    .withMessage("Category is missing")
                    .withCode("1")
                    .withFieldName("category");
    }
}
