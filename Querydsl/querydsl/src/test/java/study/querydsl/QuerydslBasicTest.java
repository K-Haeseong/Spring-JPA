package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }


    @Test
    @DisplayName("JPQL")
    void startJPQL() {
        //member1 찾기

        //when
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("Querydsl")
    void startQuerydsl() {
        //member1 찾기

        //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("search")
    void search() {
        // name = member1, age = 10 찾기
        //when
        Member findMember = queryFactory
//                .selectFrom(member)
                .select(member)
                .from(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("and -> ,")
    void search2() {
        // name = member1, age = 10 찾기
        //when
        Member findMember = queryFactory
//                .selectFrom(member)
                .select(member)
                .from(member)
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetchOne();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    @DisplayName("정렬")
    void sort() {
        //given
        em.persist(new Member(null, 50));
        em.persist(new Member("member6", 50));
        em.persist(new Member("member7", 50));

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
//                .where(member.age.eq(50))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        
        for(Member member : result) {
            System.out.println("member = " + member);
        }
        
        //then
        Member member6 = result.get(0);
        Member member7 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member6.getAge()).isEqualTo(50);
        assertThat(member7.getUsername()).isEqualTo("member7");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    @DisplayName("페이징")
    void paging1() {
        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)
                .limit(2)
                .fetch();

        for(Member member : result) {
            System.out.println("member = " + member);
        }

        //then
        assertThat(result.get(0).getUsername()).isEqualTo("member4");
        assertThat(result.get(1).getUsername()).isEqualTo("member3");
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("페이징과 전체 조회 수")
    void paging2() {
        //when
        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)
                .limit(2)
                .fetchResults(); //deprecated 되었음 -> 따라서 count 쿼리가 필요하면 다음과 같이 별도로 작성

        List<Member> results = memberQueryResults.getResults();

        for (Member member : results) {
            System.out.println("member = " + member);
        }

        //then
        assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        assertThat(memberQueryResults.getLimit()).isEqualTo(2);
        assertThat(memberQueryResults.getOffset()).isEqualTo(0);
        assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * JPQL
     * // 회원수
     * // 나이 합
     * // 평균 나이
     * // 최대 나이
     * // 최소 나이
     */
    @Test
    @DisplayName("집합 함수")
    void aggregation() {
        //when
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        
        for(Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }


    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    @DisplayName("groupBy 사용")
    void groupBy() {
        //when
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15.0);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamA.get(member.age.avg())).isEqualTo(35.0);
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    @DisplayName("join")
    void join() {
        //when
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for(String username : result) {
            System.out.println("username = " + username);
        }

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsExactly("member1", "member2");
    }


    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL : select m from member m left join m.team t on t.name = "teamA"
     */
    @Test
    @DisplayName("on절 필터링")
    void on_filtering() throws Exception {
        //when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for(Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        //then
    }






}
