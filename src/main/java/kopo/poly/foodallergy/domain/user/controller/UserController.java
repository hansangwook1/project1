package kopo.poly.foodallergy.domain.user.controller;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.allergy.service.AllergyService;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.service.EmailService;
import kopo.poly.foodallergy.domain.user.service.MypageService;
import kopo.poly.foodallergy.domain.user.service.UserService;
import kopo.poly.foodallergy.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AllergyService allergyService;
    private final EmailService emailService;
    private final MypageService mypageService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/join")
    public String joinPage() {
        return "join";
    }

    @RequestMapping(value = "/join/allergy", method = {RequestMethod.GET, RequestMethod.POST})
    public String joinAllergyPage(Model model) {
        List<AllergyItem> allergyItems = allergyService.findAll();
        model.addAttribute("allergyItems", allergyItems);
        return "join-allergy";
    }

    @GetMapping("/join/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userService.isEmailDuplicated(email);
    }

    @PostMapping("/join")
    public String join(@ModelAttribute JoinRequestDto dto) {
        userService.join(dto);
        return "redirect:/join/complete";
    }

    @GetMapping("/find")
    public String findPage(@RequestParam(defaultValue = "id") String tab, Model model) {
        model.addAttribute("tab", tab);
        return "find";
    }

    @PostMapping("/find/id")
    public String findId(@RequestParam String email, Model model) {
        try {
            User user = userService.findByEmail(email);
            int atIdx = email.indexOf('@');
            String masked = email.substring(0, Math.min(4, atIdx)) + "***" + email.substring(atIdx);
            model.addAttribute("maskedEmail", masked);
            model.addAttribute("createdAt", user.getCreatedAt());
            return "find-id-result";
        } catch (IllegalArgumentException e) {
            model.addAttribute("tab", "id");
            model.addAttribute("errorMessage", e.getMessage());
            return "find";
        }
    }

    @PostMapping("/find/pw")
    public String changePw(@RequestParam String email,
                           @RequestParam String newPassword,
                           @RequestParam String newPasswordConfirm,
                           Model model) {
        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("tab", "pw");
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            return "find";
        }
        try {
            userService.changePassword(email, newPassword);
            model.addAttribute("tab", "pw");
            model.addAttribute("successMessage", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
            return "find";
        } catch (IllegalArgumentException e) {
            model.addAttribute("tab", "pw");
            model.addAttribute("errorMessage", e.getMessage());
            return "find";
        }
    }

    @PostMapping("/find/send-code")
    @ResponseBody
    public Map<String, Object> sendCode(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        try {
            emailService.sendCode(email);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "이메일 발송에 실패했습니다.");
        }
        return result;
    }

    @PostMapping("/find/verify-code")
    @ResponseBody
    public Map<String, Object> verifyCode(@RequestParam String email, @RequestParam String code) {
        Map<String, Object> result = new HashMap<>();
        boolean valid = emailService.verifyCode(email, code);
        result.put("success", valid);
        result.put("message", valid ? "인증이 완료되었습니다." : "인증번호가 올바르지 않습니다.");
        return result;
    }

    @GetMapping("/join/complete")
    public String joinComplete() {
        return "join-complete";
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            List<UserAllergy> userAllergies = mypageService.getUserAllergies(userDetails.getUsername());
            model.addAttribute("userName", user.getNickname());
            model.addAttribute("userAllergies", userAllergies);

            String cacheKey = "random:menu:" + user.getUserId();
            try {
                String cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    List<String> menus = objectMapper.readValue(cached,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    model.addAttribute("randomMenus", menus);
                } else {
                    List<String> menus = getRandomMenus(userAllergies);
                    redisTemplate.opsForValue().set(cacheKey,
                            objectMapper.writeValueAsString(menus), 300, TimeUnit.SECONDS);
                    model.addAttribute("randomMenus", menus);
                }
            } catch (Exception ignored) {}
        }
        return "main";
    }

    @GetMapping("/random/refresh")
    public String refreshMenu(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            String cacheKey = "random:menu:" + user.getUserId();
            redisTemplate.delete(cacheKey);
        }
        return "redirect:/";
    }

    private List<String> getRandomMenus(List<UserAllergy> userAllergies) {
        List<String> allMenus = new ArrayList<>(List.of(
                "비빔밥", "된장찌개", "제육볶음", "삼겹살", "냉면",
                "순두부찌개", "불고기", "잡채", "김치찌개", "갈비탕"
        ));
        Collections.shuffle(allMenus);
        return allMenus.stream().limit(3).toList();
    }
}