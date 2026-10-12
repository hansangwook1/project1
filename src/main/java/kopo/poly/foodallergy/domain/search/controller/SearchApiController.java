package kopo.poly.foodallergy.domain.search.controller;

import kopo.poly.foodallergy.domain.search.service.SearchLogService;
import kopo.poly.foodallergy.dto.PopularKeywordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 인기 검색어 API
 * GET /api/search/popular?days=7&limit=10  → 200 OK / 400 (범위 벗어남)
 * 로그인하지 않은 사용자도 볼 수 있음 (SecurityConfig permitAll)
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
}
