package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import sl.tiger.scraper.dto.Criteria;

public interface CriteriaRepository extends MongoRepository<Criteria, String> {
}
