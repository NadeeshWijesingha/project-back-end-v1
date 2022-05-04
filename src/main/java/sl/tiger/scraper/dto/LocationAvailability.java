package sl.tiger.scraper.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LocationAvailability {
    private String location;
    private int quantity;
}


