package kopo.poly.foodallergy.domain.review.controller;

import kopo.poly.foodallergy.domain.review.service.ReviewService;
import kopo.poly.foodallergy.dto.ReviewRequestDto;
import kopo.poly.foodallergy.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 음식 리뷰 API
 * GET    /api/foods/{foodCode}/reviews  리뷰 목록 (비로그인 가능) → 200
 * POST   /api/foods/{foodCode}/reviews  리뷰 등록 → 201 / 400
 * PUT    /api/reviews/{reviewId}        리뷰 수정 → 200 / 400 / 403 / 404
 * DELETE /api/reviews/{reviewId}        리뷰 삭제 → 204 / 403 / 404
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping("/foods/{foodCode}/reviews")
    public List<ReviewResponseDto> getReviews(@PathVariable String foodCode,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        return reviewService.getReviews(foodCode, email);
    }

    @PostMapping("/foods/{foodCode}/reviews")
    public ResponseEntity<ReviewResponseDto> addReview(@PathVariable String foodCode,
                                                       @AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestBody ReviewRequestDto request) {
        ReviewResponseDto response = reviewService.addReview(userDetails.getUsername(), foodCode, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/reviews/{reviewId}")
    public ReviewResponseDto updateReview(@PathVariable Long reviewId,
                                          @AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody ReviewRequestDto request) {
        return reviewService.updateReview(userDetails.getUsername(), reviewId, request);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
