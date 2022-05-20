package sl.tiger.scraper.business;

import org.springframework.data.mongodb.repository.MongoRepository;
import sl.tiger.scraper.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsernameAndPassword(String username, String password);
}
