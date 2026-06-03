package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   // 상속 구조 단일 테이블 전략
@DiscriminatorColumn(name = "dtype")                    // 엔티티 타입 식별
@Getter @Setter
/*
상품은 주문상품과 관련 없으므로 연관관계 매핑하지 않음
 */
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

//    비즈니스 로직
//    재고 증가
    public void addStock(int quantity) {

        this.stockQuantity += quantity;
    }

//    재고 감소
    public void removeStock(int quantity) {

        int restStock = this.stockQuantity - quantity;

        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }

        this.stockQuantity = restStock;
    }
}
