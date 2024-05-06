package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Item.Item;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /* 상품 저장 */
    public void save(Item item) {
        if(item.getId() == null) {
            em.persist(item);
        }
    }
    
    /* 상품 목록 조회 */
    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
            .getResultList();
}

    /* 상품 단일 조회 */
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

}
