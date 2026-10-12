package kopo.poly.foodallergy.domain.allergy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ALLERGY_ITEMS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AllergyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ALLERGY_ID")
    private Integer allergyId;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "CODE", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "KEYWORDS", length = 500)
    private String keywords;
}