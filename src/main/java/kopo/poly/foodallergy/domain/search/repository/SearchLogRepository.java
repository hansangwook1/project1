package kopo.poly.foodallergy.domain.search.repository;

import kopo.poly.foodallergy.domain.search.document.SearchLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogRepository extends MongoRepository<SearchLog, String> {

    // [기능2] 기준 시각 이후의 검색 기록 조회 (MongoDB: { searchedAt: { $gt: from } })
    List<SearchLog> findBySearchedAtAfter(LocalDateTime from);
}
