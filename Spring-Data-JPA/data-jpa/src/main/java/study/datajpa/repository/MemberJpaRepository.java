package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    /* 저장 */
    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    /* 변경 */
    // 변경감지를 사용함으로 메서드 필요 X

    /* 삭제 */
    public void delete(Member member) {
        em.remove(member);
    }

    /* 전체 조회 */
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /* 단건 조회 */
    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    /* 단건 조회(OPTIONAL) */
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    /* 카운트 */
    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }

    /* 메서드 이름으로 쿼리 생성 */
    /* 이름과 나이를 기준으로 회원을 조회 */
    public List<Member> findByUsernameAndAgeGreaterThan(String name, int age) {
        return em.createQuery("select m from Member m where m.username = :username and m.age > :age", Member.class)
                .setParameter("username", name)
                .setParameter("age", age)
                .getResultList();
    }

    /* Named 쿼리 호출 */
    public List<Member> findByUsername(String username) {
        return em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    /* 페이징과 정렬 */
    public List<Member> findByPage(int age, int offset, int limit) {
        return em.createQuery("select m from Member m where age = :age order by m.username desc", Member.class)
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age) {
        return em.createQuery("select count(m) from Member m where age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }


    /* 벌크성 수정 쿼리 */
    public int bulkAgePlus(int age) {
        return em.createQuery("update Member m set m.age = m.age+1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }



}
