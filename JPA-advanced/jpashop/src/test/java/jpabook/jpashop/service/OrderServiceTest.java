package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Item.Book;
import jpabook.jpashop.domain.Item.Item;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    private EntityManager em;


    @Test
    @DisplayName("상품주문_성공")
    void 상품주문_성공() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 99);
        int orderCount = 9;

        //when
        Long orderId = orderService.Order(member.getId(), item.getId(), orderCount);

        //then
        Order findOrder = orderRepository.findOne(orderId);

        Assertions.assertEquals(OrderStatus.ORDER, findOrder.getStatus(), "주문시 상태는 ORDER");
        Assertions.assertEquals(1, findOrder.getOrderItems().size(), "주문한 상품 종류 개수가 정확해야 한다.");
        Assertions.assertEquals(10000 * orderCount, findOrder.getTotalPrice(), "주문 가격 = 가격 * 수량");
        Assertions.assertEquals(99 - orderCount, item.getStockQuantity(), "주문 수량만큼 재고 줄어 들어야 한다.");
    }
    
    
    @Test
    @DisplayName("상품주문취소")
    void 상품주문_취소() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 99);
        int orderCount = 9;

        Long orderId = orderService.Order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancel(orderId);

        //then
        Order findOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, findOrder.getStatus(),"주문 취소시 상태는 CANCEL");
        assertEquals(99, item.getStockQuantity(), "주문 취소시 재고가 다시 증가 해야 한다.");
    }
    
    
    @Test
    @DisplayName("재고 수량 초과")
    void 재고_수량_초과() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("JPA 최신판", 10000, 50);

        int orderCount = 51;

        //when, then
        assertThatThrownBy(() -> orderService.Order(member.getId(), item.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }
    

    
    /* 상품 생성 */
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);

        return book;
    }

    /* 멤버 생성 */
    private Member createMember() {
        Member member = new Member();
        member.setName("멤버1");
        member.setAddress(new Address("서울", "거리", "123-123"));
        em.persist(member);
        return member;
    }


}