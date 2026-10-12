package kopo.poly.foodallergy.domain.food.controller;

import kopo.poly.foodallergy.domain.food.service.FoodService;
import kopo.poly.foodallergy.dto.FoodDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/food/loading")
    public String loading(@RequestParam String keyword, Model model) {
        try {
            String decoded = java.net.URLDecoder.decode(
                    new String(java.util.Base64.getDecoder().decode(keyword)), "UTF-8");
            model.addAttribute("keyword", decoded);
        } catch (Exception e) {
            model.addAttribute("keyword", keyword);
        }
        return "food-loading";
    }

    @GetMapping("/food")
    public String foodPage() {
        return "food";
    }

    @GetMapping("/food/search")
    public String search(@RequestParam String keyword,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {

        // Base64 디코딩
        String decodedKeyword;
        try {
            decodedKeyword = java.net.URLDecoder.decode(
                    new String(java.util.Base64.getDecoder().decode(keyword)),
                    "UTF-8"
            );
        } catch (Exception e) {
            decodedKeyword = keyword; // 디코딩 실패 시 원본 사용
        }

        List<String> userAllergies = List.of();
        if (userDetails != null) {
            userAllergies = foodService.getUserAllergyNames(userDetails.getUsername());
        }

        String email = (userDetails != null) ? userDetails.getUsername() : null;
        List<FoodDto> results = foodService.searchFood(decodedKeyword, userAllergies, email);
        long safeCount = results.stream().filter(FoodDto::isSafe).count();
        long dangerCount = results.stream().filter(f -> !f.isSafe()).count();

        model.addAttribute("keyword", decodedKeyword);
        model.addAttribute("results", results);
        model.addAttribute("safeCount", safeCount);
        model.addAttribute("dangerCount", dangerCount);
        model.addAttribute("totalCount", results.size());
        model.addAttribute("userAllergies", userAllergies);
        model.addAttribute("alternatives", foodService.getAlternatives(results));
        return "food-result";
    }

    @GetMapping("/food/detail")
    public String detail(@RequestParam String foodCode,
                         @RequestParam String keyword,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {

        String decodedFoodCode;
        String decodedKeyword;
        try {
            decodedFoodCode = java.net.URLDecoder.decode(
                    new String(java.util.Base64.getDecoder().decode(foodCode)), "UTF-8");
            decodedKeyword = java.net.URLDecoder.decode(
                    new String(java.util.Base64.getDecoder().decode(keyword)), "UTF-8");
        } catch (Exception e) {
            decodedFoodCode = foodCode;
            decodedKeyword = keyword;
        }

        // 로그 추가
        System.out.println("=== detail 디코딩 결과 ===");
        System.out.println("foodCode 원본: " + foodCode);
        System.out.println("foodCode 디코딩: " + decodedFoodCode);
        System.out.println("keyword 디코딩: " + decodedKeyword);

        List<String> userAllergies = List.of();
        if (userDetails != null) {
            userAllergies = foodService.getUserAllergyNames(userDetails.getUsername());
        }

        String email = (userDetails != null) ? userDetails.getUsername() : null;

        List<FoodDto> results = foodService.searchFood(decodedKeyword, userAllergies, email);

        System.out.println("검색 결과 수: " + results.size());
        results.forEach(f -> System.out.println("  - " + f.getFoodCode() + " | " + f.getFoodName()));

        final String finalFoodCode = decodedFoodCode;
        FoodDto food = results.stream()
                .filter(f -> finalFoodCode.equals(f.getFoodCode()))
                .findFirst()
                .orElse(null);

        System.out.println("매칭된 food: " + (food != null ? food.getFoodName() : "null"));

        if (food != null) {
            model.addAttribute("food", food);
            model.addAttribute("keyword", decodedKeyword);
            model.addAttribute("userAllergies", userAllergies);
            model.addAttribute("alternatives", foodService.getAlternatives(results));
        }

        return "food-detail";
    }
}