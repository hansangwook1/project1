package kopo.poly.foodallergy.domain.review.entity;

import jakarta.persistence.*;
import kopo.poly.foodallergy.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 음식 리뷰 (REVIEWS 테이블)
 * - 회원 1명 : 리뷰 N개 (@ManyToOne)
 * - 음식은 별도 테이블 없이 식약처 API 의 음식 코드(FOOD_CODE)로 연결
 */
@Entity
@Table(name = "REVIEWS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "FOOD_CODE", nullable = false, length = 50)
    private String foodCode;

    @Column(name = "CONTENT", nullable = false, length = 500)
    private String content;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 리뷰 내용 수정 - 트랜잭션 안에서 값만 바꾸면 JPA 변경 감지(dirty checking)로 UPDATE 실행
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // 작성자 본인인지 확인
    public boolean isWrittenBy(User user) {
        return this.user.getUserId().equals(user.getUserId());
    }
}
