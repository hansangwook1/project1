package kopo.poly.foodallergy.dto;

import kopo.poly.foodallergy.domain.favorite.entity.Favorite;

import java.time.LocalDateTime;

/**
 * 찜 응답
 * Entity(Favorite)를 그대로 반환하지 않는 이유:
 * Favorite 안에 User(비밀번호 포함)가 연결되어 있어 그대로 내보내면 회원 정보가 노출될 수 있음
 */
public record FavoriteResponseDto(String foodCode, String foodName, LocalDateTime createdAt) {

    public static FavoriteResponseDto from(Favorite favorite) {
        return new FavoriteResponseDto(
                favorite.getFoodCode(),
                favorite.getFoodName(),
                favorite.getCreatedAt());
    }
}
