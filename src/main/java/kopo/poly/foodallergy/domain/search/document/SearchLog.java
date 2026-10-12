package kopo.poly.foodallergy.domain.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "search_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog {

    @Id
    private String id;

    private String keyword;
    private String email;
    private int resultCount;
    private LocalDateTime searchedAt;
}