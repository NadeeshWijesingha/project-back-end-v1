package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sl.tiger.scraper.dto.Criteria;

import java.time.LocalDate;
import java.util.List;

public interface CriteriaRepository extends MongoRepository<Criteria, String> {
    List<Criteria> findByMakeIgnoreCase(String make);

    List<Criteria> findByModelIgnoreCase(String model);

    List<Criteria> findByDate(String  startDate);

    List<Criteria> findByDateAndMakeIgnoreCase(String date, String make);

    List<Criteria> findByDateAndModelIgnoreCase(String date, String model);

    @Aggregation(pipeline = {
            "{'$group': {'_id': {'make': '$make'}, 'make': {'$sum': 1}}}"
    })
    List<Criteria> findAll();
    @Aggregation(pipeline = {
            "{'$group': {'_id': {'model': '$model'}, 'model': {'$sum': 1}}}"
    })
    List<Criteria> findAllByModel();

    @Aggregation(pipeline = {
            "{'$group': {'_id': {'siteName': '$siteName'}, 'siteName': {'$sum': 1}}}"
    })
    List<Criteria> findAllBySiteName();
}
