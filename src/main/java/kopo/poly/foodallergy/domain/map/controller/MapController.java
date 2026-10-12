package kopo.poly.foodallergy.domain.map.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MapController {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @GetMapping("/map")
    public String mapPage(@RequestParam(required = false) String keyword, Model model) {
        String decodedKeyword = "";
        if (keyword != null && !keyword.isEmpty()) {
            try {
                decodedKeyword = java.net.URLDecoder.decode(
                        new String(java.util.Base64.getDecoder().decode(keyword)),
                        "UTF-8"
                );
            } catch (Exception e) {
                decodedKeyword = keyword;
            }
        }
        model.addAttribute("keyword", decodedKeyword);
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "map";
    }
}