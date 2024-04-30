package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Item.Item;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    private int orderPrice;

    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /* 주문 상품 생성 메서드 */
    public static OrderItem createOrderItem(Item item, int price ,int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(count);
        orderItem.setOrderPrice(price);
        item.removeStock(count);
        return orderItem;
    }

    /* 주문 상품 취소 */
    public void cancel() {
        // item 필드를 직접 사용하지 않고 get으로 가져오는 이유?
        //  ->  조회한 엔티티가 프록시 객체인 경우 필드에 직접 접근하면 원본 객체를 가져오지 못하고 프록시 객체의 필드에 직접 접근 해버리게 된다.
        //      이게 일반적인 상황에는 문제가 없는데, equals, hashcode를 JPA 프록시 객체로 구현할 때 문제가 될 수 있다.
        getItem().addStock(count);
    }

    /* 주문 상품 가격 조회 */
    public int getTotalPrice() {
         return getOrderPrice() * getCount();
    }
}
