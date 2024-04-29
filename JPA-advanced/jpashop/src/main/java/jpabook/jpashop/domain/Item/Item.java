package jpabook.jpashop.domain.Item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@Getter @Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();


    /* 비지니스 로직 */
    // 필드에 대한 변경은 클래스 내에서 처리 하는 것이 객체 지향적
    
    /* 재고 수량 증가 */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /* 재고 수량 감소 */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if( restStock < 0 ) {
            throw new NotEnoughStockException("재고가 부족 합니다.");
        }
        this.stockQuantity = restStock;
    }


}
