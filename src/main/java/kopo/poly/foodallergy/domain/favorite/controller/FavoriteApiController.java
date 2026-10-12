package kopo.poly.foodallergy.domain.favorite.controller;

import kopo.poly.foodallergy.domain.favorite.service.FavoriteService;
import kopo.poly.foodallergy.dto.FavoriteRequestDto;
import kopo.poly.foodallergy.dto.FavoriteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 음식 찜 API
 * POST   /api/favorites            찜 추가   → 201 Created / 400 / 409(중복)
 * DELETE /api/favorites/{foodCode} 찜 취소   → 204 No Content / 404
 * GET    /api/favorites            내 찜 목록 → 200 OK
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteApiController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<FavoriteResponseDto> addFavorite(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody FavoriteRequestDto request) {
        FavoriteResponseDto response = favoriteService.addFavorite(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{foodCode}")
    public ResponseEntity<Void> removeFavorite(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable String foodCode) {
        favoriteService.removeFavorite(userDetails.getUsername(), foodCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<FavoriteResponseDto> getFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        return favoriteService.getFavorites(userDetails.getUsername());
    }
}
