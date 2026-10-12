package kopo.poly.foodallergy.domain.user.controller;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.favorite.service.FavoriteService;
import kopo.poly.foodallergy.domain.search.service.SearchLogService;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;
    private final FavoriteService favoriteService;
    private final SearchLogService searchLogService;

    // 알레르기 관리 페이지
    @GetMapping("/allergy")
    public String allergyPage(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = mypageService.getUser(userDetails.getUsername());
        List<UserAllergy> userAllergies = mypageService.getUserAllergies(userDetails.getUsername());
        List<AllergyItem> allItems = mypageService.getAllAllergyItems();

        // 선택된 기본 항목 ID 목록
        List<Integer> selectedIds = userAllergies.stream()
                .filter(a -> a.getAllergyItem() != null)
                .map(a -> a.getAllergyItem().getAllergyId())
                .toList();

        // 직접 입력 항목
        List<UserAllergy> customAllergies = userAllergies.stream()
                .filter(a -> a.getAllergyItem() == null)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("allItems", allItems);
        model.addAttribute("selectedIds", selectedIds);
        model.addAttribute("customAllergies", customAllergies);
        return "mypage-allergy";
    }

    // 알레르기 저장
    @PostMapping("/allergy")
    public String saveAllergy(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) List<Integer> allergyIds,
                              @RequestParam(required = false) List<String> customNames,
                              Model model) {
        mypageService.saveAllergies(userDetails.getUsername(), allergyIds, customNames);
        model.addAttribute("successMessage", "알레르기 정보가 저장되었습니다.");
        return allergyPage(userDetails, model);
    }

    // [기능1] 찜한 음식 페이지
    @GetMapping("/favorites")
    public String favoritesPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = mypageService.getUser(userDetails.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("favorites", favoriteService.getFavorites(userDetails.getUsername()));
        return "mypage-favorites";
    }

    // [기능4] 내 검색 기록 페이지
    @GetMapping("/history")
    public String historyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = mypageService.getUser(userDetails.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("histories", searchLogService.getMyHistory(userDetails.getUsername()));
        return "mypage-history";
    }

    // 회원정보 수정 페이지
    @GetMapping("/info")
    public String infoPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = mypageService.getUser(userDetails.getUsername());
        model.addAttribute("user", user);
        return "mypage-info";
    }

    // 비밀번호 변경
    // 비밀번호 변경
    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String currentPw,
                                 @RequestParam String newPw,
                                 @RequestParam String newPwConfirm,
                                 Model model) {
        try {
            mypageService.changePassword(userDetails.getUsername(), currentPw, newPw, newPwConfirm);
            model.addAttribute("successMessage", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        User user = mypageService.getUser(userDetails.getUsername());
        model.addAttribute("user", user);
        return "mypage-info";
    }
}