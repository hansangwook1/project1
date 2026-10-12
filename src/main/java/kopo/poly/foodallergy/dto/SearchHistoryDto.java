package kopo.poly.foodallergy.dto;

import kopo.poly.foodallergy.domain.search.document.SearchLog;

import java.time.LocalDateTime;

/**
 * 내 검색 기록 응답
 * 예: {"id": "6708...", "keyword": "김치찌개", "resultCount": 12, "searchedAt": "2026-10-12T16:00:00"}
 */
public record SearchHistoryDto(String id, String keyword, int resultCount, LocalDateTime searchedAt) {

    public static SearchHistoryDto from(SearchLog log) {
        return new SearchHistoryDto(log.getId(), log.getKeyword(), log.getResultCount(), log.getSearchedAt());
    }
}
