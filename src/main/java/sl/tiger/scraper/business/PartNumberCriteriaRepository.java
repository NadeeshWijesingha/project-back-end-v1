package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import sl.tiger.scraper.dto.PartNumberCriteria;

import java.util.List;

public interface PartNumberCriteriaRepository extends MongoRepository<PartNumberCriteria, String > {
    List<PartNumberCriteria> findAll();
}
