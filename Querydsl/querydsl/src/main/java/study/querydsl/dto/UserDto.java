package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

    private String name;

    private int userAge;

    public UserDto(String name, int userAge) {
        this.name = name;
        this.userAge = userAge;
    }

}
