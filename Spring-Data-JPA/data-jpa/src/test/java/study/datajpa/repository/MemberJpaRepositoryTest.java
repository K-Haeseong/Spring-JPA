package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.hibernate.dialect.TiDBDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(savedMember.getId());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);   //JPA 엔티티 동일성 보장
    }


    @Test
    @DisplayName("BasicCRUD")
    void basicCrud() throws Exception {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberJpaRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deleteCount = memberJpaRepository.count();
        assertThat(deleteCount).isEqualTo(0);

    }
    
    @Test
    @DisplayName("이름과 나이 조건 조회")
    void findByUsernameAndAgeGreaterThan() {
        //given
        Member member1 = new Member("memberA", 15);
        Member member2 = new Member("memberA", 30);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //when
        List<Member> findMembers = memberJpaRepository.findByUsernameAndAgeGreaterThan("memberA", 20);

        //then
        assertThat(findMembers.get(0).getUsername()).isEqualTo("memberA");
        assertThat(findMembers.get(0).getAge()).isEqualTo(30);
        assertThat(findMembers.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Named 쿼리 사용")
    void namedQuery() {
        //given
        Member member3 = new Member("memberB", 30);
        memberJpaRepository.save(member3);

        //when
        List<Member> findMember = memberJpaRepository.findByUsername("memberB");

        //then
        assertThat(findMember.get(0).getUsername()).isEqualTo("memberB");
    }
}