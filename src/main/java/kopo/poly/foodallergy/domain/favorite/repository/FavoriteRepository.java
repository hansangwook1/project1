package kopo.poly.foodallergy.domain.favorite.repository;

import kopo.poly.foodallergy.domain.favorite.entity.Favorite;
import kopo.poly.foodallergy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 이미 찜한 음식인지 확인 (중복 찜 방지)
    boolean existsByUserAndFoodCode(User user, String foodCode);

    // 찜 취소할 대상 조회
    Optional<Favorite> findByUserAndFoodCode(User user, String foodCode);

    // 내 찜 목록 (최근 찜한 순)
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
}
