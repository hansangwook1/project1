package kopo.poly.foodallergy.domain.review.service;

import kopo.poly.foodallergy.domain.review.entity.Review;
import kopo.poly.foodallergy.domain.review.repository.ReviewRepository;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.repository.UserRepository;
import kopo.poly.foodallergy.dto.ReviewRequestDto;
import kopo.poly.foodallergy.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private static final int MAX_LENGTH = 500;

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // 음식별 리뷰 목록 (비로그인이면 email == null → mine 은 모두 false)
    public List<ReviewResponseDto> getReviews(String foodCode, String email) {
        Long loginUserId = (email == null) ? null
                : userRepository.findByEmail(email).map(User::getUserId).orElse(null);

        return reviewRepository.findByFoodCodeOrderByCreatedAtDesc(foodCode).stream()
                .map(review -> ReviewResponseDto.of(review,
                        loginUserId != null && loginUserId.equals(review.getUser().getUserId())))
                .toList();
    }

    // 리뷰 등록
    @Transactional
    public ReviewResponseDto addReview(String email, String foodCode, ReviewRequestDto request) {
        if (foodCode == null || foodCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음식 정보가 올바르지 않습니다.");
        }
        String content = validateContent(request);
        User user = getUser(email);

        Review saved = reviewRepository.save(Review.builder()
                .user(user)
                .foodCode(foodCode)
                .content(content)
                .build());

        return ReviewResponseDto.of(saved, true);
    }

    // 리뷰 수정 - 작성자 본인만 가능
    @Transactional
    public ReviewResponseDto updateReview(String email, Long reviewId, ReviewRequestDto request) {
        String content = validateContent(request);
        User user = getUser(email);
        Review review = getOwnReview(reviewId, user, "수정");

        review.updateContent(content);   // save() 호출 없이 변경 감지로 UPDATE
        return ReviewResponseDto.of(review, true);
    }

    // 리뷰 삭제 - 작성자 본인만 가능
    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = getUser(email);
        Review review = getOwnReview(reviewId, user, "삭제");
        reviewRepository.delete(review);
    }

    // 리뷰 조회 + 작성자 확인 (없으면 404, 남의 리뷰면 403)
    private Review getOwnReview(Long reviewId, User user, String action) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!review.isWrittenBy(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 " + action + "할 수 있습니다.");
        }
        return review;
    }

    // 내용 검증: 비어 있으면 400, 500자 초과면 400
    private String validateContent(ReviewRequestDto request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "리뷰 내용을 입력해주세요.");
        }
        String content = request.content().trim();
        if (content.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "리뷰는 " + MAX_LENGTH + "자 이하로 입력해주세요.");
        }
        return content;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다."));
    }
}
