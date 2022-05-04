package sl.tiger.scraper.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ResponseWrapper {
    private List<Result> data;
    private ResponseStatus status = ResponseStatus.SUCCESS;
    private String massage;

    public ResponseWrapper() {
        this.data = Collections.emptyList();
    }

    public ResponseWrapper(List<Result> data) {
        this.data = data;
    }
}

