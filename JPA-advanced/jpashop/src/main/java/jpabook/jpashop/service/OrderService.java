package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    
    /* 주문 생성 */
    @Transactional
    public Long Order(Long memberId, Long itemId, int count) {

        Member findMember = memberRepository.findOne(memberId);
        Item findItem = itemRepository.findOne(itemId);

        Delivery delivery = new Delivery();
        delivery.setStatus(DeliveryStatus.READY);
        delivery.setAddress(findMember.getAddress());

        OrderItem orderItem = OrderItem.createOrderItem(findItem, findItem.getPrice(), count);

        Order order = Order.createOrder(findMember, delivery, orderItem);

        orderRepository.save(order);

        return order.getId();
    }


    /* 주문 취소 */
    @Transactional
    public void cancel(Long itemId) {
        Order findOrder = orderRepository.findOne(itemId);
        findOrder.cancel();
    }
    
    /* 특정 주문 검색 */
}
