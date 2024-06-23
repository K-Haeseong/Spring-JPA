package study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCond {

    /**
     * 검색조건 - 회원명, 팀명, 나이(이상, 이하)
     */
    private String username;
    private String teamName;

    private Integer ageGoe;
    private Integer ageLoe;
}
