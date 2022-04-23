package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


/**
 * 특정 화면에 종속 또는 너무 복잡한 쿼리라
 * 재사용성이 없는 경우에는 추상화를 따로 하지 않고
 * 구현체를 Repository로 만들어 사용하는 것도 좋음
 * */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<MemberTeamDto> search(MemberSearchCondition condition) {
    return queryFactory
      .select(new QMemberTeamDto(
        member.id.as("memberId"),
        member.username,
        member.age,
        team.id.as("teamId"),
        team.name.as("teamName")
      ))
      .from(member)
      .leftJoin(member.team, team)
      .where(
        usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe())
      )
      .fetch();
  }

  @Override
  public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> result = queryFactory
      .select(new QMemberTeamDto(
        member.id.as("memberId"),
        member.username,
        member.age,
        team.id.as("teamId"),
        team.name.as("teamName")
      ))
      .from(member)
      .leftJoin(member.team, team)
      .where(
        usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe())
      )
      .offset(pageable.getOffset())
      .limit(pageable.getPageSize())
      .fetch();
//      .fetchResults();    // fetchResults가 deprecated 됨.
    return new PageImpl<>(result, pageable, result.size());
  }

  @Override
  public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> result = queryFactory
      .select(new QMemberTeamDto(
        member.id.as("memberId"),
        member.username,
        member.age,
        team.id.as("teamId"),
        team.name.as("teamName")
      ))
      .from(member)
      .leftJoin(member.team, team)
      .where(
        usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe())
      )
      .offset(pageable.getOffset())
      .limit(pageable.getPageSize())
      .fetch();
//      .fetchResults();    // fetchResults가 deprecated 됨.

    JPAQuery<Member> countQuery = queryFactory
      .select(member)
      .from(member)
      .leftJoin(member.team, team)
      .where(
        usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
        ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe())
      );

    // fetchCount가 deprecated되어 Wildcard로 교체
    Long count = queryFactory
      .select(Wildcard.count)
      .from(member)
      .fetch().get(0);

    // fetchCount 사용. but 해당 메소드가 deprecated 됨.
    return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchCount);
//    return new PageImpl<>(result, pageable, count);
  }

  private BooleanExpression usernameEq(String username) {
    return hasText(username) ? member.username.eq(username) : null;
  }

  private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName) ? team.name.eq(teamName) : null;
  }

  private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe != null ? member.age.goe(ageGoe) : null;
  }

  private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe != null ? member.age.loe(ageLoe) : null;
  }

}
