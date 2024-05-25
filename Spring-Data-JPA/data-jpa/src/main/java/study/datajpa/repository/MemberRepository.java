package study.datajpa.repository;

import jakarta.persistence.NamedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom { // 여기 선언한 도메인 클래스

    /* 메서드 이름으로 쿼리 생성 */
    /* 이름과 나이를 기준으로 회원을 조회 */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /* Named 쿼리 호출 */
    @Query(name = "Member.findByUsername")
    // 생략가능 - 생략시 도메인 클래스 이름 + . + 메서드이름으로 메서드를 찾고, 없으면 메서드 이름 분석해서 생성 호출
    List<Member> findByUsername(@Param("username") String username);

    /* @Query 사용하기 */
    @Query("select m from Member m where username = :username and age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    /*  페이징과 정렬 */
    Page<Member> findByAge(int age, Pageable pageable);

    /* 벌크성 수정 쿼리 */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age+1 where m.age >= :age")
    int bulkPlus (@Param("age") int age);

}
