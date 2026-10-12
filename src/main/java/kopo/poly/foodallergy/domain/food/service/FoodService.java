package kopo.poly.foodallergy.domain.food.service;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.allergy.repository.AllergyItemRepository;
import kopo.poly.foodallergy.domain.allergy.repository.UserAllergyRepository;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.repository.UserRepository;
import kopo.poly.foodallergy.dto.FoodDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.foodallergy.domain.search.service.SearchLogService;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final UserRepository userRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final AllergyItemRepository allergyItemRepository;
    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SearchLogService searchLogService;

    @Value("${food.api.key}")
    private String apiKey;

    private static final String API_URL =
            "https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02";

    // 사용자 알레르기 조회
    public List<String> getUserAllergyNames(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return List.of();

        List<UserAllergy> allergies = userAllergyRepository.findByUser(userOpt.get());
        List<String> names = new ArrayList<>();
        for (UserAllergy a : allergies) {
            if (a.getAllergyItem() != null) {
                names.add(a.getAllergyItem().getName());
            } else if (a.getCustomName() != null) {
                names.add(a.getCustomName());
            }
        }
        return names;
    }

    // 알레르기 검사 - DB의 KEYWORDS 컬럼 기반
    private List<String> detectAllergies(String searchTarget, List<String> userAllergies) {
        List<AllergyItem> allAllergyItems = allergyItemRepository.findAll();

        Map<String, List<String>> keywordMap = new HashMap<>();
        for (AllergyItem item : allAllergyItems) {
            if (item.getKeywords() != null && !item.getKeywords().isBlank()) {
                keywordMap.put(item.getName(), Arrays.asList(item.getKeywords().split(",")));
            } else {
                keywordMap.put(item.getName(), List.of(item.getName()));
            }
        }

        List<String> contained = new ArrayList<>();
        for (String userAllergy : userAllergies) {
            List<String> keywords = keywordMap.get(userAllergy);
            if (keywords != null) {
                for (String kw : keywords) {
                    if (searchTarget.contains(kw.trim())) {
                        contained.add(userAllergy);
                        break;
                    }
                }
            }
        }
        return contained;
    }

    // 음식 검색
    public List<FoodDto> searchFood(String keyword, List<String> userAllergies, String email) {

        String userId = (email != null) ? email : "anonymous";
        String cacheKey = "search:cache:" + keyword + ":" + userId;

        // Redis 캐시 조회
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(
                        cached,
                        objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, FoodDto.class)
                );
            }
        } catch (Exception ignored) {}

        // API URL 생성
        String url = UriComponentsBuilder
                .fromUriString(API_URL)
                .queryParam("serviceKey", apiKey)
                .queryParam("type", "json")
                .queryParam("FOOD_NM_KR", keyword)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 30)
                .build(false)
                .toUriString();

        List<FoodDto> result = new ArrayList<>();

        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return result;

            Map<String, Object> body = (Map<String, Object>) response.get("body");
            if (body == null) return result;

            Object itemsObj = body.get("items");
            if (!(itemsObj instanceof List)) return result;

            List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;

            for (Map<String, Object> item : items) {
                String foodName = str(item.get("FOOD_NM_KR")).replace("_", " ");
                String[] parts = foodName.split(" ");
                if (parts.length >= 2) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        sb.append(parts[i]);
                        if (i < parts.length - 1) sb.append(" ");
                    }
                    foodName = sb + " " + parts[0];
                }

                String searchTarget = foodName
                        + " " + str(item.get("FOOD_CAT2_NM"))
                        + " " + str(item.get("FOOD_CAT3_NM"))
                        + " " + str(item.get("FOOD_REF_NM"));

                List<String> contained = detectAllergies(searchTarget, userAllergies);

                result.add(FoodDto.builder()
                        .foodName(foodName)
                        .foodCode(str(item.get("FOOD_CD")))
                        .energy(toDouble(item.get("AMT_NUM1")))
                        .protein(toDouble(item.get("AMT_NUM3")))
                        .fat(toDouble(item.get("AMT_NUM4")))
                        .carbohydrate(toDouble(item.get("AMT_NUM6")))
                        .sodium(toDouble(item.get("AMT_NUM8")))
                        .safe(contained.isEmpty())
                        .containedAllergies(contained)
                        .build()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redis 캐시 저장
        try {
            if (!result.isEmpty()) {
                redisTemplate.opsForValue().set(
                        cacheKey,
                        objectMapper.writeValueAsString(result),
                        3600,
                        TimeUnit.SECONDS
                );
            }
        } catch (Exception ignored) {}

        // MongoDB 검색 로그 저장
        try {
            searchLogService.saveLog(keyword, email, result.size());
        } catch (Exception ignored) {}

        return result;
    }

    // 대체 식품 추천
    public List<FoodDto> getAlternatives(List<FoodDto> allFoods) {
        return allFoods.stream()
                .filter(FoodDto::isSafe)
                .limit(3)
                .toList();
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private Double toDouble(Object o) {
        if (o == null) return 0.0;
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
}