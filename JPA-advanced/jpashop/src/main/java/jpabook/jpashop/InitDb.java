package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static jpabook.jpashop.domain.DeliveryStatus.*;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }


    @Component
    @Transactional
    @RequiredArgsConstructor
    /* 샘플 데이터 추가 */
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {

            Member member = createMember("userA", "서울", "한강변", "123123");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = createBook("JPA1 BOOK", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 10);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 20);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {

            Member member = createMember("userB", "충북", "거리", "456456");
            em.persist(member);

            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);

            Book book2 = createBook("SPRING2 BOOK", 40000, 200);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 50);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 100);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }


        /* 배달 정보 생성 */
        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setStatus(READY);
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        /*  물품 생성 */
        private Book createBook(String name, int price, int stockQuantity) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);
            return book;
        }

        /* 멤버 생성 */
        private Member createMember(String name, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(name);
            Address address = new Address(city, street, zipcode);
            member.setAddress(address);
            return member;
        }

    }
}
