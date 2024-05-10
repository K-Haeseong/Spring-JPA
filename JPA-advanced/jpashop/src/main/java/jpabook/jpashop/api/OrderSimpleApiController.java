package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;


    /** 엔티티 직접 노출
    *   - 양방향 관계 문제 발생 - @JsonIgnore
    *   - Lazy 로딩 프록시 문제 발생 - Hibernate5Module 모듈 등록 (프록시 Null 처리)
    */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all) {
            order.getMember().getName();      // Lazy 로딩 강제 초기화
            order.getDelivery().getAddress();    // Lazy 로딩 강제 초기화
        }
        return all;
    }


    /**
     * 엔티티 -> DTO 변환(페치 조인 사용 X)
     *  - N + 1 문제 발생
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(Collectors.toList());

        return result;
    }


    /**
     * 엔티티 -> DTO 변환(페치 조인 사용 O)
     *  - 1번의 쿼리로 해결
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(order -> new SimpleOrderDto(order))
                .collect(Collectors.toList());

        return result;
    }


    /**
     * JPA에서 DTO 바로 조회
     *  - 쿼리 1번 호출
     *  - select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {

        private Long id;
        private String name;
        private OrderStatus orderStatus;
        private LocalDateTime orderDate;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.id = order.getId();
            this.name = order.getMember().getName();            // 지연 로딩 조회 발생
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();    // 지연 로딩 조회 발생
        }
    }


}
