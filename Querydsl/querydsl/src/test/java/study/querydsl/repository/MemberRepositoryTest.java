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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("basicTest")
    void basicTest() throws Exception {
        //given
        Member member1 = new Member("member1");

        //when
        memberRepository.save(member1);

        //then
        Member findMember = memberRepository.findById(member1.getId()).get();
        assertThat(findMember).isEqualTo(member1);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member1);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member1);
    }


    @Test
    @DisplayName("searchTest")
    void searchTest() throws Exception {
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
        List<MemberTeamDto> result = memberRepository.search(cond);

        //then
        assertThat(result).extracting("username").containsExactly("member3", "member4");
    }
    
}