package kopo.poly.foodallergy.domain.search.controller;

import kopo.poly.foodallergy.domain.search.service.SearchLogService;
import kopo.poly.foodallergy.dto.PopularKeywordDto;
import kopo.poly.foodallergy.dto.SearchHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 검색 API
 * [기능2] GET    /api/search/popular?days=7&limit=10  인기 검색어 (비로그인 가능) → 200 / 400
 * [기능4] GET    /api/search/history                  내 검색 기록 → 200
 * [기능4] DELETE /api/search/history/{id}             검색 기록 1개 삭제 → 204 / 403 / 404
 * [기능4] DELETE /api/search/history                  내 검색 기록 전체 삭제 → 200 {"deletedCount": n}
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchApiController {

    private final SearchLogService searchLogService;

    @GetMapping("/popular")
    public List<PopularKeywordDto> getPopularKeywords(@RequestParam(defaultValue = "7") int days,
                                                      @RequestParam(defaultValue = "10") int limit) {
        return searchLogService.getPopularKeywords(days, limit);
    }

    @GetMapping("/history")
    public List<SearchHistoryDto> getMyHistory(@AuthenticationPrincipal UserDetails userDetails) {
        return searchLogService.getMyHistory(userDetails.getUsername());
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteMyHistory(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable String id) {
        searchLogService.deleteMyHistory(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/history")
    public Map<String, Long> deleteAllMyHistory(@AuthenticationPrincipal UserDetails userDetails) {
        long deletedCount = searchLogService.deleteAllMyHistory(userDetails.getUsername());
        return Map.of("deletedCount", deletedCount);
    }
}
