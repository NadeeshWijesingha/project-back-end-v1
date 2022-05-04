package sl.tiger.scraper.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Availability {
    boolean isAvailable;
    String massage;
    List<LocationAvailability> locationAvailability = new ArrayList<>();

    public Availability(Availability availability) {
    }
}
