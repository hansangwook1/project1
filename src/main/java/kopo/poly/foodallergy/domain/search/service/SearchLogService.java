package kopo.poly.foodallergy.domain.search.service;

import kopo.poly.foodallergy.domain.search.document.SearchLog;
import kopo.poly.foodallergy.domain.search.repository.SearchLogRepository;
import kopo.poly.foodallergy.dto.PopularKeywordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * [기능2] 인기 검색어 통계
     * 최근 days 일 동안의 검색 기록을 검색어별로 묶어 횟수를 세고, 많이 검색된 순서로 limit 개 반환
     */
    public List<PopularKeywordDto> getPopularKeywords(int days, int limit) {
        if (days < 1 || days > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 기간(days)은 1~30일 사이여야 합니다.");
        }
        if (limit < 1 || limit > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 개수(limit)는 1~20 사이여야 합니다.");
        }

        // 1. 기준 시각 이후의 검색 기록 조회
        LocalDateTime from = LocalDateTime.now().minusDays(days);
        List<SearchLog> logs = searchLogRepository.findBySearchedAtAfter(from);

        // 2. 검색어별 횟수 집계 (빈 검색어 제외, 앞뒤 공백 제거)
        Map<String, Long> countByKeyword = logs.stream()
                .map(SearchLog::getKeyword)
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .map(String::trim)
                .collect(Collectors.groupingBy(keyword -> keyword, Collectors.counting()));

        // 3. 횟수 내림차순 정렬 (횟수가 같으면 가나다순) 후 상위 limit 개
        List<Map.Entry<String, Long>> top = countByKeyword.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .toList();

        // 4. 순위를 붙여 DTO 로 변환
        List<PopularKeywordDto> result = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            result.add(new PopularKeywordDto(i + 1, top.get(i).getKey(), top.get(i).getValue()));
        }
        return result;
    }
}
