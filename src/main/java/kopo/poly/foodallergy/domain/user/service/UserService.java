package kopo.poly.foodallergy.domain.user.service;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.allergy.repository.AllergyItemRepository;
import kopo.poly.foodallergy.domain.allergy.repository.UserAllergyRepository;
import kopo.poly.foodallergy.domain.favorite.repository.FavoriteRepository;
import kopo.poly.foodallergy.domain.review.repository.ReviewRepository;
import kopo.poly.foodallergy.domain.search.repository.SearchLogRepository;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.repository.UserRepository;
import kopo.poly.foodallergy.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AllergyItemRepository allergyItemRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final PasswordEncoder passwordEncoder;
    private final FavoriteRepository favoriteRepository;     // [기능5]
    private final ReviewRepository reviewRepository;         // [기능5]
    private final SearchLogRepository searchLogRepository;   // [기능5]

    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void join(JoinRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .build();
        userRepository.save(user);

        if (dto.getAllergyIds() != null) {
            for (Integer allergyId : dto.getAllergyIds()) {
                AllergyItem item = allergyItemRepository.findById(allergyId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알레르기 항목입니다."));
                userAllergyRepository.save(UserAllergy.builder()
                        .user(user)
                        .allergyItem(item)
                        .build());
            }
        }

        if (dto.getCustomAllergies() != null) {
            for (String customName : dto.getCustomAllergies()) {
                userAllergyRepository.save(UserAllergy.builder()
                        .user(user)
                        .customName(customName)
                        .build());
            }
        }
    }

    // 아이디(이메일) 찾기 - 닉네임으로 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));
        userRepository.save(user.withNewPassword(passwordEncoder.encode(newPassword)));
    }

    /**
     * [기능5] 회원 탈퇴
     * 비밀번호 확인 후 회원과 연결된 데이터를 모두 삭제
     * - 외래키(USER_ID) 때문에 자식 데이터(찜, 리뷰, 알레르기)를 먼저 지우고 마지막에 회원 삭제
     * - @Transactional: 중간에 하나라도 실패하면 MariaDB 삭제 작업 전체가 취소(롤백)되어 일부만 지워지는 일이 없음
     * - MongoDB 검색 기록은 마지막에 삭제 → 여기서 실패해도 예외가 발생해 MariaDB 삭제가 함께 롤백됨
     */
    @Transactional
    public void withdraw(String email, String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        favoriteRepository.deleteByUser(user);       // 찜
        reviewRepository.deleteByUser(user);         // 리뷰
        userAllergyRepository.deleteByUser(user);    // 알레르기 설정
        userRepository.delete(user);                 // 회원
        searchLogRepository.deleteByEmail(email);    // 검색 기록 (MongoDB)
    }
}
