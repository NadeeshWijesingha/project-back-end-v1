package sl.tiger.scraper.dto.search.partnumber;

import br.com.fluentvalidator.AbstractValidator;
import sl.tiger.scraper.controller.model.ScraperId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.fluentvalidator.predicate.LogicalPredicate.not;
import static br.com.fluentvalidator.predicate.ObjectPredicate.nullValue;
import static br.com.fluentvalidator.predicate.StringPredicate.stringEmptyOrNull;

@Component
public class GetPartDetailsByPartNumberRequestValidator extends AbstractValidator<GetPartDetailsByPartNumberRequest> {
    private static final List<String> sites = Arrays.stream(ScraperId.values()).map(Enum::name).collect(Collectors.toList());

    @Override
    public void rules() {
        ruleFor(GetPartDetailsByPartNumberRequest::getSite)
                .must(not(nullValue()))
                .withMessage("Site is missing")
                .withCode("1")
                .withFieldName("site")
                .must(sites::contains)
                .when(not(nullValue()))
                .withMessage("Site value should be one of " + String.join(",", sites))
                .withCode("1")
                .withFieldName("site");

        ruleFor(GetPartDetailsByPartNumberRequest::getPartNumber)
                .must(not(stringEmptyOrNull()))
                .withMessage("Part Number is missing")
                .withCode("1")
                .withFieldName("partNumber");
    }
}
