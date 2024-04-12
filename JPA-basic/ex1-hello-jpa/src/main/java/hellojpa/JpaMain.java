package hellojpa;

import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();

        try {
//            Member member = new Member();
//            member.setId(3L);
//            member.setName("HelloC");
//
//            em.persist(member);

            Member findMember = em.find(Member.class, 1L);
            findMember.setName("JPA");

            em.detach(findMember);

            em.merge(findMember);

            tx.commit();
        } catch(Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();

    }
}
