package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import sl.tiger.scraper.dto.PartNumberCriteria;

public interface PartNumberCriteriaRepository extends MongoRepository<PartNumberCriteria, String > {
}
