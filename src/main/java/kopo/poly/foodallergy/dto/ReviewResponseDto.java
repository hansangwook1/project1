package kopo.poly.foodallergy.dto;

import kopo.poly.foodallergy.domain.review.entity.Review;

import java.time.LocalDateTime;

/**
 * 리뷰 응답
 * - 작성자는 닉네임만 내보냄 (이메일, 비밀번호 등 회원 정보는 제외)
 * - mine: 로그인한 사용자가 쓴 리뷰인지 (화면에서 수정/삭제 버튼 표시 여부)
 */
public record ReviewResponseDto(Long reviewId,
                                String nickname,
                                String content,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt,
                                boolean mine) {

    public static ReviewResponseDto of(Review review, boolean mine) {
        return new ReviewResponseDto(
                review.getId(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                mine);
    }
}
