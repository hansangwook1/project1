package kopo.poly.foodallergy.domain.review.repository;

import kopo.poly.foodallergy.domain.review.entity.Review;
import kopo.poly.foodallergy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 음식별 리뷰 목록 (최신순)
    List<Review> findByFoodCodeOrderByCreatedAtDesc(String foodCode);

    // [기능5] 회원 탈퇴 시 해당 회원의 리뷰 전체 삭제
    void deleteByUser(User user);
}
