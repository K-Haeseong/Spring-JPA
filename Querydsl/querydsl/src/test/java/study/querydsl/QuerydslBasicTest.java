package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @PersistenceUnit
    EntityManagerFactory emf;

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

    /**
     * 기본 문법
     */
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
        assertThat(teamB.get(member.age.avg())).isEqualTo(35.0);
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

    }

    /**
     * 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     */
    @Test
    @DisplayName("연관관계 없는 엔티티 외부조인")
    void join_on_no_relation() {
        //when
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    /**
     * 페치 조인 적용 X
     */
    @Test
    @DisplayName("페치조인 적용 X")
    void fetchJoinNo() {
        //given
        em.flush();
        em.clear();

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("findMember = " + findMember);

        //then
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용, 지연로딩").isFalse();
    }


    /**
     * 페치 조인 적용 O
     */
    @Test
    @DisplayName("페치조인 적용 O")
    void fetchJoinUse() {
        //given
        em.flush();
        em.clear();

        //when
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("findMember = " + findMember);

        //then
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }


    /**
     * 서브쿼리 - 나이가 가장 많은 회원 조회
     */
    @Test
    @DisplayName("subQuery")
    void subQuery() {
        //given
        QMember memberSub = new QMember("memberSub");
        //when
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
//                        JPAExpressions.select(memberSub.age.max())
//                                .from(memberSub)))
                // static import 적용
                select(memberSub.age.max())
                .from(memberSub)))
                .fetch();


        //then
        assertThat(result).extracting("age").containsExactly(40);
    }

    /**
     * 서브쿼리 - 나이가 평균 이상인 회원 조회
     */
    @Test
    @DisplayName("subQueryGoe")
    void subQueryGoe() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("username").containsExactly("member3", "member4");
    }

    /**
     * 서브쿼리 - 나이가 20, 30, 40인 회원 조회(in절 사용)
     */
    @Test
    @DisplayName("subQueryIn")
    void subQueryIn() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        //then
        assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    /**
     * 서브쿼리 - 회원이름, 평균 나이 조회 - select 절에 subquery
     */
    @Test
    @DisplayName("subQuery_select")
    void subQuery_select() {
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for(Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 10 -> 열살 
     * 20 -> 스무살
     * 나머지는  -> 기타
     * 이렇게 바꿔 출력하기
     */
    @Test
    @DisplayName("simpleCase")
    void simpleCase() throws Exception {
        //when
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String memberAge : result) {
            System.out.println("memberAge = " + memberAge);
        }

        //then
        assertThat(result.get(0)).isEqualTo("열살");
        assertThat(result.get(1)).isEqualTo("스무살");
    }

    @Test
    @DisplayName("CaseBuilder")
    void caseBuilder_use() throws Exception {
        //when
        StringExpression ageRange = new CaseBuilder()
                .when(member.age.between(0, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("21살~30살")
                .otherwise("기타");

        List<String> result = queryFactory
                .select(ageRange)
                .from(member)
                .fetch();

        //then
        assertThat(result.get(0)).isEqualTo("0~20살");
        assertThat(result.get(1)).isEqualTo("0~20살");
        assertThat(result.get(2)).isEqualTo("21살~30살");
        assertThat(result.get(3)).isEqualTo("기타");
    }


    /**
     * 중급 문법
     */


    /**
     *  순수 JPA에서 DTO로 결과 반환
     */
    @Test
    @DisplayName("JPA_DTO")
    void jpaDto() {
        //when
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) " +
                        "from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl 빈 생성을 통한 DTO 결과 반환
     */
    @Test
    @DisplayName("Querydsl 빈 생성 - 프로퍼티 접근")
    void querydsl_dto_property() {
        //when
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        
        for(MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("Querydsl 빈 생성 - 필드 접근")
    void querydsl_dto_field() {
        //when
//        List<MemberDto> result = queryFactory
//                .select(Projections.fields(MemberDto.class, member.username, member.age))
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class, member.username.as("name"), member.age.as("userAge")))
                .from(member)
                .fetch();

        for(UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    @DisplayName("Querydsl 빈 생성 - 생성자 접근")
    void querydsl_dto_constructor() {
        //when
//        List<MemberDto> result = queryFactory
//                .select(Projections.fields(MemberDto.class, member.username, member.age))
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for(UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * @QueryProjection을 활용한 DTO 반환
     */
    @Test
    @DisplayName("QueryProjection 사용하기")
    void queryProjection_use() {
        //when
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * distinct
     */
    @Test
    @DisplayName("distinct")
    void distinct() {
        //when
        List<Tuple> result = queryFactory
                .select(team, member)
                .from(team)
                .innerJoin(team.members, member)
                .fetch();

        System.out.println("=============================");
        for (Tuple tuple : result) {
            System.out.println("tuple1 = " + tuple);
        }
        System.out.println("=============================");



        List<Tuple> result2 = queryFactory
                .select(team, member).distinct()
                .from(team)
                .innerJoin(team.members, member)
                .fetch();

        System.out.println("=============================");
        for (Tuple tuple : result2) {
            System.out.println("tuple2 = " + tuple);
        }
        System.out.println("=============================");
    }


    @Test
    @DisplayName("builder 사용")
    void builder() throws Exception {
        //given
        String usernameParam = "member1";
        int ageParam = 10;

        //when
        List<Member> result = searchMember1(usernameParam, ageParam);

        //then
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember1(String usernameParam, int ageParam) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }
        if(ageParam > 0) {
            builder.and(member.age.eq(ageParam));
        }

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(builder)
                .fetch();

        return result;
    }

    @Test
    @DisplayName("where 다중처리")
    void where() throws Exception {
        //given
        String usernameParam = "member1";
        Integer ageParam = 10;

        //when
        List<Member> result = searchMember2(usernameParam, ageParam);

        //then
        assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }
    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // 조합으로 사용 가능
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    @Test
    @DisplayName("수정,삭제 벌크")
    void update_delete() throws Exception {
        // 대량 데이터 수정
        long count1 = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        // 더하기
        long count2 = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        // 삭제
        long count3 = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }


}
