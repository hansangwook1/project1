package kopo.poly.foodallergy.domain.favorite.service;

import kopo.poly.foodallergy.domain.favorite.entity.Favorite;
import kopo.poly.foodallergy.domain.favorite.repository.FavoriteRepository;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.repository.UserRepository;
import kopo.poly.foodallergy.dto.FavoriteRequestDto;
import kopo.poly.foodallergy.dto.FavoriteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    // 찜 추가
    @Transactional
    public FavoriteResponseDto addFavorite(String email, FavoriteRequestDto request) {
        if (isBlank(request.foodCode()) || isBlank(request.foodName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음식 정보가 올바르지 않습니다.");
        }

        User user = getUser(email);

        if (favoriteRepository.existsByUserAndFoodCode(user, request.foodCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 찜한 음식입니다.");
        }

        Favorite saved = favoriteRepository.save(Favorite.builder()
                .user(user)
                .foodCode(request.foodCode())
                .foodName(request.foodName())
                .build());

        return FavoriteResponseDto.from(saved);
    }

    // 찜 취소 - 로그인한 회원의 찜에서만 찾으므로 다른 회원의 찜은 삭제할 수 없음
    @Transactional
    public void removeFavorite(String email, String foodCode) {
        User user = getUser(email);

        Favorite favorite = favoriteRepository.findByUserAndFoodCode(user, foodCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "찜 목록에 없는 음식입니다."));

        favoriteRepository.delete(favorite);
    }

    // 내 찜 목록
    public List<FavoriteResponseDto> getFavorites(String email) {
        User user = getUser(email);
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(FavoriteResponseDto::from)
                .toList();
    }

    // 음식 상세 화면에서 찜 버튼 상태 표시용 (비로그인이면 false)
    public boolean isFavorite(String email, String foodCode) {
        if (email == null) {
            return false;
        }
        return userRepository.findByEmail(email)
                .map(user -> favoriteRepository.existsByUserAndFoodCode(user, foodCode))
                .orElse(false);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
