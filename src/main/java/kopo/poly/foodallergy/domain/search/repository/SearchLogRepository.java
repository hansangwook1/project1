package kopo.poly.foodallergy.domain.search.repository;

import kopo.poly.foodallergy.domain.search.document.SearchLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogRepository extends MongoRepository<SearchLog, String> {

    // [기능2] 기준 시각 이후의 검색 기록 조회 (MongoDB: { searchedAt: { $gt: from } })
    List<SearchLog> findBySearchedAtAfter(LocalDateTime from);

    // [기능4] 내 최근 검색 기록 20개 (최신순)
    List<SearchLog> findTop20ByEmailOrderBySearchedAtDesc(String email);

    // [기능4][기능5] 내 검색 기록 전체 삭제 → 삭제된 개수 반환
    long deleteByEmail(String email);
}
