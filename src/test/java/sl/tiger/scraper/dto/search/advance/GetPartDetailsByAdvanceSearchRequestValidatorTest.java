package sl.tiger.scraper.dto.search.advance;

import br.com.fluentvalidator.context.ValidationResult;
import sl.tiger.scraper.controller.model.ScraperId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;


@SpringBootTest(classes = {GetPartDetailsByAdvanceSearchRequestValidator.class})
@EnableConfigurationProperties
class GetPartDetailsByAdvanceSearchRequestValidatorTest {

    @Autowired
    GetPartDetailsByAdvanceSearchRequestValidator validatorParent;

    @Test
    void when_noPramsGiven_given_Error() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        final ValidationResult result = validatorParent.validate(request);

        assertFalse(result.isValid());
        assertThat(result.getErrors(), not(empty()));
    }

    @Test
    void when_noSiteGiven_given_Error_withCorrectMessage() {
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();

        final ValidationResult result = validatorParent.validate(request);

        assertFalse(result.isValid());
        assertThat(result.getErrors(), not(empty()));

        assertThat(result.getErrors(), hasItem(hasProperty("field", containsString("site"))));
        assertThat(result.getErrors(), hasItem(hasProperty("message", containsString("Site is missing"))));
    }

    @Test
    void when_invalidSiteGiven_given_Error_withCorrectMessage() {
        List<String> sites = Arrays.stream(ScraperId.values()).map(Enum::name).collect(Collectors.toList());
        GetPartDetailsByAdvanceSearchRequest request = new GetPartDetailsByAdvanceSearchRequest();
        request.setSite("XX");
        final ValidationResult result = validatorParent.validate(request);

        assertFalse(result.isValid());
        assertThat(result.getErrors(), not(empty()));

        assertThat(result.getErrors(), hasItem(hasProperty("field", containsString("site"))));
        assertThat(result.getErrors(), hasItem(hasProperty("message", containsString("Site value should be one of " + String.join(",", sites)))));
    }
}