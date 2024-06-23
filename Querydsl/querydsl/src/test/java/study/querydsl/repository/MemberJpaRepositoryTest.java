package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("basicTest")
    void basicTest() {
        //given
        Member member1 = new Member("member1");

        //when
        memberJpaRepository.save(member1);

        //then
        Member findMember = memberJpaRepository.findById(member1.getId()).get();
        assertThat(findMember).isEqualTo(member1);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member1);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member1);

        /* queryDsl 적용 */
        List<Member> result3 = memberJpaRepository.findAll_queryDsl();
        assertThat(result3).containsExactly(member1);

        List<Member> result4 = memberJpaRepository.findByUsername_queryDsl("member1");
        assertThat(result4).containsExactly(member1);
    }

    @Test
    @DisplayName("searchTest")
    void searchTest() {
        //given
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

        MemberSearchCond cond = new MemberSearchCond();
        cond.setTeamName("teamB");
        cond.setAgeGoe(20);
        cond.setAgeLoe(40);

        //when
        List<MemberTeamDto> result = memberJpaRepository.searchByCondition(cond);

        //then
        assertThat(result).extracting("username").containsExactly("member3", "member4");
    }

    

}