package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    /* 주문 저장 */
    public void save(Order order) {
        em.persist(order);
    }

    /* 주문 단건 조회 */
    public Order findOne(Long itemId) {
        return em.find(Order.class, itemId);
    }

    /* 특정 주문 조회 */

}
