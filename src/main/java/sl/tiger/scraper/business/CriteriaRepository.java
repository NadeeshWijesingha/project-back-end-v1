package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import sl.tiger.scraper.dto.Criteria;

import java.util.List;

public interface CriteriaRepository extends MongoRepository<Criteria, String> {
    List<Criteria> findByMakeIgnoreCase(String make);

    List<Criteria> findByModelIgnoreCase(String model);
}
