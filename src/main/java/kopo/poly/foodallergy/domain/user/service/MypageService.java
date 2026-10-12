package kopo.poly.foodallergy.domain.user.service;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.allergy.repository.AllergyItemRepository;
import kopo.poly.foodallergy.domain.allergy.repository.UserAllergyRepository;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final AllergyItemRepository allergyItemRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public List<UserAllergy> getUserAllergies(String email) {
        User user = getUser(email);
        return userAllergyRepository.findByUser(user);
    }

    public List<AllergyItem> getAllAllergyItems() {
        return allergyItemRepository.findAll();
    }

    // 알레르기 전체 저장 (기존 삭제 후 재저장)
    @Transactional
    public void saveAllergies(String email, List<Integer> allergyIds, List<String> customNames) {
        User user = getUser(email);
        userAllergyRepository.deleteByUser(user);

        if (allergyIds != null) {
            for (Integer id : allergyIds) {
                AllergyItem item = allergyItemRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("알레르기 항목을 찾을 수 없습니다."));
                userAllergyRepository.save(UserAllergy.builder()
                        .user(user).allergyItem(item).build());
            }
        }
        if (customNames != null) {
            for (String name : customNames) {
                if (!name.isBlank()) {
                    userAllergyRepository.save(UserAllergy.builder()
                            .user(user).customName(name).build());
                }
            }
        }
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, String currentPw, String newPw, String newPwConfirm) {
        User user = getUser(email);
        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (!newPw.equals(newPwConfirm)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
        userRepository.save(user.withNewPassword(passwordEncoder.encode(newPw)));
    }
}