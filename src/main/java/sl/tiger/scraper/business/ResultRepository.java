package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import sl.tiger.scraper.dto.Result;
import sl.tiger.scraper.entity.ResultEntity;

public interface ResultRepository extends MongoRepository<Result, String> {
}
