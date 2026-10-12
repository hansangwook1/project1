package kopo.poly.foodallergy.domain.favorite.entity;

import jakarta.persistence.*;
import kopo.poly.foodallergy.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 음식 찜 (FAVORITES 테이블)
 * - 회원 1명 : 찜 N개 (@ManyToOne)
 * - USER_ID + FOOD_CODE 유니크 → 같은 음식 중복 찜 방지 (DB 차원)
 */
@Entity
@Table(name = "FAVORITES",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_FAVORITES_USER_FOOD",
                columnNames = {"USER_ID", "FOOD_CODE"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "FOOD_CODE", nullable = false, length = 50)
    private String foodCode;

    @Column(name = "FOOD_NAME", nullable = false, length = 200)
    private String foodName;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
