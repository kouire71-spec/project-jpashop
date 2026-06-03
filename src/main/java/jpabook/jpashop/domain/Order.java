package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name="orders")                               // db 예약어 회피
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // jpa가 리플렉션을 통해 엔티티를 생성할 수 있도록 하고 의도없는 무분별한 객체 생성을 방지
public class Order {

    /*
    FK는 하나의 테이블에만 존재, 연관관계의 주인은 FK(외래키)를 가진 쪽

    객체 연관관계를 가지는 이유, 만약 order 클래스가 데이터(id값)을 참조한다면
    데이터 조회 수정시 모든 id값을 조회하고 객체로 가져오는 쿼리를 직접 만들어야함
    그러나 객체를 참조하게 된다면 데이터를 엔티티로 불러오고 관리하는것이 가능해짐

    엔티티에 연관관계 매핑 정보가 있기 때문에 JPA가 “연관 객체 존재”를 알고 프록시를 넣는다
     */

    /*
    영속성 컨텍스트는 엔티티 생명주기를 관리하는 시스템
    1차 캐시 -> 엔티티 보관
    변경 감지 -> 값 바뀌면 자동 UPDATE
    쓰기 지연 -> SQL 모아뒀다가 flush 때 실행
    동일성 보장 -> 같은 엔티티는 항상 같은 객체 반환
     */

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    /*
    @JoinColumn을 쓰지 않아도 @ManyToOne이 붙으면 기본적으로 기본 이름(member_id)으로 FK 컬럼을 만든다

    @OneToMany 단방향만 설정할 경우 FK위치를 확정할 수 없어(JPA가 마음대로 DB 구조를 바꾸는 식) 중간 테이블이 생성됨
    @JoinColumn을 사용해 강제로 FK를 관리하게 할 수는 있다 그러나 이 경우 마지막에 UPDATE 쿼리가 한번 더 나간다

    또 @OneToMany에서 mappedBy를 쓰지 않을때 마찬가지로 FK를 관리하려 하기 때문에 UPDATE 쿼리가 나간다(중복관리)
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /*
    mappedBy = "order" -> FK를 관리하지 않는 읽기 전용,
    orderItem 테이블에 order_id FK가 있음을 의미하고 조회 시 그 FK를 기준으로 가져온다
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)   // casecade는 연관관계에 있는 객체에 영속상태를 전파한다
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

//    연관관계 메서드
    public void setMember(Member member) {

        this.member = member;
        member.getOrders().add(this);
    }

    public void setDelivery(Delivery delivery) {

        this.delivery = delivery;
        delivery.setOrder(this);
    }

    public void addOrderItem(OrderItem orderItem) {

        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

//    생성 메서드
//    여러 상품을 주문받을 수 있도록 파라미터 설정
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {

        Order order = new Order();

        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

//    비즈니스 로직
    public void cancel() {

        if (delivery.getStatus() == DeliveryStatus.COMP) {

            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);

        for (OrderItem orderItem : orderItems) {

            orderItem.cancel();
        }
    }

//    조회 로직
//    주문 가격 조회
    public int getTotalPrice() {

        int totalPrice = 0;

        for (OrderItem orderItem : orderItems) {

            totalPrice += orderItem.getTotalPrice();
        }

        return totalPrice;
    }
}
