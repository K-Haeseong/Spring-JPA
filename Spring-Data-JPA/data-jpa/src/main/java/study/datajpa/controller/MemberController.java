package study.datajpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    /* 도메인 클래스 컨버터 사용 X */
//    @GetMapping("/members/{id}")
//    public String findMember(@PathVariable("id") Long id) {
//        Member findMember = memberRepository.findById(id).get();
//        return findMember.getUsername();
//    }


    /* 도메인 클래스 컨버터 사용 O */
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }


    /* 페이징과 정렬 - @PageableDefault 사용 X */
    @GetMapping("/members")
    public Page<MemberDto> list(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> pageDto = page.map(member -> new MemberDto(member));
        return pageDto;
    }

    /* 페이징과 정렬 - @PageableDefault 사용 O */
    @GetMapping("/members_page")
    public Page<Member> membersPage(@PageableDefault(page = 1, size = 15,
            sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }


    /* 회원 목록 데이터 삽입 */
    @PostConstruct
    public void init() {
        for (int i = 1; i <= 100; i++) {
            Member member = new Member("member" + i, i);
            memberRepository.save(member);
        }
    }


}
