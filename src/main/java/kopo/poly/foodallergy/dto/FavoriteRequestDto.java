package kopo.poly.foodallergy.dto;

/**
 * 찜 추가 요청 (POST /api/favorites 의 JSON 본문)
 * 예: {"foodCode": "D000006", "foodName": "김치찌개"}
 */
public record FavoriteRequestDto(String foodCode, String foodName) {
}
