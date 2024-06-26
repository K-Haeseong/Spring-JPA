package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import java.util.List;
import java.util.Optional;

import static io.micrometer.common.util.StringUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /* queryDsl 사용 */
    public List<Member> findAll_queryDsl() {
        return queryFactory
                .select(member)
                .from(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    /* queryDsl 사용 */
    public List<Member> findByUsername_queryDsl(String username) {
        return queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByCondition(MemberSearchCond memberSearchCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if ( hasText(memberSearchCond.getUsername()) ) {
            builder.and(member.username.eq(memberSearchCond.getUsername()));
        }
        if ( hasText(memberSearchCond.getTeamName()) ) {
            builder.and(team.name.eq(memberSearchCond.getTeamName()));
        }
        if ( memberSearchCond.getAgeGoe() != null ) {
            builder.and(member.age.goe(memberSearchCond.getAgeGoe()));
        }
        if ( memberSearchCond.getAgeLoe() != null ) {
            builder.and(member.age.loe(memberSearchCond.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }


    //회원명, 팀명, 나이(ageGoe, ageLoe)
    public List<MemberTeamDto> search(MemberSearchCond condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
//                .where( AllEq(
//                        condition.getUsername(),
//                        condition.getTeamName(),
//                        condition.getAgeGoe(),
//                        condition.getAgeLoe()) )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe !=null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe !=null ? member.age.loe(ageLoe) : null;
    }

    /* 조합해서 재활용 가능 */
    private Predicate AllEq(String username, String teamName, Integer ageGoe, Integer ageLoe) {
        return usernameEq(username)
                .and(teamNameEq(teamName))
                .and(ageGoe(ageGoe))
                .and(ageLoe(ageLoe));
    }


}
