package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {
    
    @Autowired
    MemberRepository memberRepository;
    
    @Test
    @DisplayName("Data JPA 테스트")
    void testMember() {
        //given
        Member member = new Member("memberA");
        Member saveMember = memberRepository.save(member);

        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(saveMember.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(saveMember.getUsername());
        Assertions.assertThat(findMember).isEqualTo(saveMember);
    }

    @Test
    @DisplayName("BasicCRUD")
    void basicCrud() throws Exception {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        Assertions.assertThat(findMember1).isEqualTo(member1);
        Assertions.assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberRepository.findAll();
        Assertions.assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        Assertions.assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        Assertions.assertThat(deleteCount).isEqualTo(0);
    }


    @Test
    @DisplayName("이름과 나이 조건 조회")
    void findByUsernameAndAgeGreaterThan() {
        //given
        Member member1 = new Member("memberA", 15);
        Member member2 = new Member("memberA", 30);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<Member> findMembers = memberRepository.findByUsernameAndAgeGreaterThan("memberA", 20);

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
        memberRepository.save(member3);

        //when
        List<Member> findMember = memberRepository.findByUsername("memberB");

        //then
        assertThat(findMember.get(0).getUsername()).isEqualTo("memberB");
    }

    /* @Query 사용 */
    @Test
    @DisplayName("@Query 사용")
    void findUser()  {
        //given
        Member member3 = new Member("memberB", 30);
        memberRepository.save(member3);

        //when
        List<Member> findMember = memberRepository.findUser("memberB", 30);

        //then
        assertThat(findMember.size()).isEqualTo(1);
        assertThat(findMember.get(0).getUsername()).isEqualTo("memberB");
        assertThat(findMember.get(0).getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Paging")
    void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //then

        List<Member> content = page.getContent();                   // 조회된 데이터
        assertThat(content.size()).isEqualTo(3);            // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5);   // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0);          // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);      // 전체 페이지 번호
        assertThat(page.isFirst()).isTrue();                        // 첫번째 항목인가?
        assertThat(page.hasNext()).isTrue();                        // 다음 페이지가 있는가?
    }

    @Test
    @DisplayName("bulkUpdate")
    void bulkUpdate() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 15));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 25));
        memberRepository.save(new Member("member5", 45));

        //when
        int resultCount = memberRepository.bulkPlus(20);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

}