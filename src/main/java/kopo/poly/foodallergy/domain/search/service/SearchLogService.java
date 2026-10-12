package kopo.poly.foodallergy.domain.search.service;

import kopo.poly.foodallergy.domain.search.document.SearchLog;
import kopo.poly.foodallergy.domain.search.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    public void saveLog(String keyword, String email, int resultCount) {
        SearchLog log = SearchLog.builder()
                .keyword(keyword)
                .email(email != null ? email : "anonymous")
                .resultCount(resultCount)
                .searchedAt(LocalDateTime.now())
                .build();
        searchLogRepository.save(log);
    }
}