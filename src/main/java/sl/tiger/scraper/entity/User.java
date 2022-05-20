package sl.tiger.scraper.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
}
