package kopo.poly.foodallergy.domain.search.repository;

import kopo.poly.foodallergy.domain.search.document.SearchLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SearchLogRepository extends MongoRepository<SearchLog, String> {
}