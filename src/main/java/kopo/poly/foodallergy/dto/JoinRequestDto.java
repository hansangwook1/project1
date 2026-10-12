package kopo.poly.foodallergy.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class JoinRequestDto {
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
    private List<Integer> allergyIds;
    private List<String> customAllergies;
}