package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Team;
import study.datajpa.entity.Team;

import java.util.List;
import java.util.Optional;

@Repository
public class TeamJpaRepository {

    @PersistenceContext
    private EntityManager em;

    /* 저장 */
    public Team save(Team team) {
        em.persist(team);
        return team;
    }

    /* 변경 */
    // 변경감지를 사용함으로 메서드 필요 X

    /* 삭제 */
    public void delete(Team team) {
        em.remove(team);
    }

    /* 전체 조회 */
    public List<Team> findAll() {
        return em.createQuery("select t from Team t", Team.class)
                .getResultList();
    }


    /* 단건 조회(OPTIONAL) */
    public Optional<Team> findById(Long id) {
        Team team = em.find(Team.class, id);
        return Optional.ofNullable(team);
    }

    /* 카운트 */
    public long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult();
    }

}
