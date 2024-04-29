package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입")
    void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("회원A");

        //when
        Long id = memberService.join(member);

        //then
        assertThat(member.getId()).isEqualTo(id);
        assertEquals(member, memberRepository.findOne(id));
    }
    
    @Test
    @DisplayName("중복회원예외")
    void 중복회원예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("회원B");


        Member member2 = new Member();
        member2.setName("회원B");

        //when
        memberService.join(member1);

        //then
        assertThatThrownBy(() -> memberService.join(member2)).isInstanceOf(IllegalStateException.class);
    }
}