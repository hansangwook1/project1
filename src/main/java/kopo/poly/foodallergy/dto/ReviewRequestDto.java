package kopo.poly.foodallergy.dto;

/**
 * 리뷰 등록/수정 요청 (JSON 본문)
 * 예: {"content": "우유 알레르기 있는데 먹어도 괜찮았어요"}
 */
public record ReviewRequestDto(String content) {
}
