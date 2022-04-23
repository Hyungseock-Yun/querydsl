package study.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
  // 회원명, 팀명, 나이(ageGeo, ageLoe)

  private String username;

  private String teamName;

  private Integer ageGoe;

  private Integer ageLoe;

}
