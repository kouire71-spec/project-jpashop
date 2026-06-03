package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        /*
            db에 쿼리를 보내는 em.flush()는 DB상태를 맞춰야하는 시점 = jpql 및 tx.commit() 시점에 발생한다
            em.find()는 캐시 기반이기에 em.flush()가 동작하지 않는다

            em.flush em.clear할 경우 commit하지 않았음에도 em.find()가 db에서 데이터를 불러올 수 있는 이유는
            em.flush()가 "같은 트랜잭션"안에서 쿼리를 전송했기 때문에 같은 트랜잭션 내부에서는 조회가 가능한 것이다
            만약 끝까지 commit하지 않을시 rollback 된다
         */

        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        Order order = Order.createOrder(member, delivery, orderItem);

        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findOne(orderId);

        order.cancel();
    }

    public List<Order> findOrders(OrderSearch orderSearch) {

        return orderRepository.findAll(orderSearch);
    }
}
