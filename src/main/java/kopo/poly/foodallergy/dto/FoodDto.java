package kopo.poly.foodallergy.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@JsonDeserialize(builder = FoodDto.FoodDtoBuilder.class)
public class FoodDto {
    private String foodName;
    private String foodCode;
    private Double energy;
    private Double protein;
    private Double fat;
    private Double carbohydrate;
    private Double sodium;
    private boolean safe;
    private List<String> containedAllergies;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FoodDtoBuilder {}
}