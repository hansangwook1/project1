package kopo.poly.foodallergy.dto;

/**
 * 인기 검색어 응답
 * 예: {"rank": 1, "keyword": "김치찌개", "count": 12}
 */
public record PopularKeywordDto(int rank, String keyword, long count) {
}
