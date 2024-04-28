//package jpabook.jpashop;
//
//import jpabook.jpashop.domain.Member;
//import jpabook.jpashop.repository.MemberRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//class MemberTest {
//
//    @Autowired
//    MemberRepository memberRepository;
//
//    @Test
//    @Transactional
//    @Rollback(false)
//    @DisplayName("Member Test")
//    void memberTest() throws Exception {
//        //given
//        Member member = new Member();
//        member.setName("memberA");
//
//        //when
//        Long id = memberRepository.save(member);
//        Member findMember = memberRepository.find(id);
//
//        //then
//        Assertions.assertThat(member.getId()).isEqualTo(findMember.getId());
//
//        Assertions.assertThat(member.getName()).isEqualTo(findMember.getName());
//
//        Assertions.assertThat(member).isEqualTo(findMember);
//
//    }
//}