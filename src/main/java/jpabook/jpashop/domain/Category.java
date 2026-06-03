package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    /*
    한 회원은 여러 상품 주문 가능, 한 상품은 여러 회원이 주문 가능 겉으로 보면 “다대다”

    DB에 다대다는 존재하지 않음 중간 테이블 끼고 1:N + N:1으로 풀린다
    @ManyToMany는 JPA가 중간 테이블을 자동으로 숨겨주는것
    중간 테이블은 컬럼(주문 수량, 날짜 등)을 넣지 못하는 등 컨트롤 할 수 없어서 실제로 잘 사용되지는 않음

    일반적으로 중간 테이블을 엔티티(OrderItem)으로 승격해 풀어낸다
    Order 1 : N OrderItem N : 1 Item
     */

//    상품 <-> 카테고리는 단순 연결이기에 ManyToMany를 사용해도 무방한 경우
//    상품과 다대다 연관관계 설정 및 중간 테이블 생성
    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

//    자기자신과 다대일 연관관계 설정(하위 카테고리)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

//    자기자신과 일대다 연관관계 설정(상위 카테고리)
    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

//    연관관계 메서드
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
