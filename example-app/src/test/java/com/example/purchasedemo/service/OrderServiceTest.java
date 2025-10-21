package com.example.purchasedemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.purchasedemo.dto.OrderRequest;
import com.example.purchasedemo.exception.OrderNotFoundException;
import com.example.purchasedemo.exception.UnauthorizedAccessException;
import com.example.purchasedemo.repository.OrderRepository;
import com.example.purchasedemo.repository.UserRepository;
import com.example.purchasemanager.core.dto.OrderResponse;
import com.example.purchasemanager.core.dto.PaymentResponse;
import com.example.purchasemanager.core.entity.Order;
import com.example.purchasemanager.core.entity.Product;
import com.example.purchasemanager.core.entity.User;
import com.example.purchasemanager.core.enums.OrderStatus;
import com.example.purchasemanager.core.service.payment.PaymentGateway;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").password("password").roles(Set.of("ROLE_USER")).build();
        testProduct = Product.builder().name("Test Product").price(100.0).stock(10).build();

        // SecurityContext 설정
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(testUser.getUsername(), "password"));
        SecurityContextHolder.setContext(securityContext);

        // Mock 객체들의 기본 동작 설정 (필요에 따라 각 테스트에서 오버라이드)
        lenient().when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        try {
            lenient().when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("주문 생성 및 결제 요청 성공")
    void createOrderAndInitiatePayment_success() {
        // given
        OrderRequest orderRequest = new OrderRequest(1L, 2);
        Order savedOrder = Order.builder().id(1L).user(testUser).product(testProduct).quantity(2).totalPrice(200.0)
                .status(OrderStatus.PENDING).paymentKey(null).build();

        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId("test_payment_id")
                .status("READY")
                .checkoutUrl("https://payment.url")
                .orderId(savedOrder.getId().toString())
                .amount(savedOrder.getTotalPrice().longValue())
                .message("Payment initiated")
                .build();

        when(productService.getProductEntityById(anyLong())).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(paymentGateway.requestPayment(any(Order.class))).thenReturn(paymentResponse);

        // when
        OrderResponse response = orderService.createOrderAndInitiatePayment(orderRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(response.getCheckoutUrl()).isEqualTo("https://payment.url");
    }

    @Test
    @DisplayName("특정 주문 ID로 조회 성공")
    void getOrderById_success() {
        // given
        Order order = Order.builder().id(1L).user(testUser).product(testProduct).status(OrderStatus.COMPLETED)
                .orderDate(LocalDateTime.now()).paymentKey("test_key").build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.getOrderById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
    void getOrderById_fail_whenOrderNotFound() {
        // given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 주문 조회 시 예외 발생")
    void getOrderById_fail_whenUserNotAuthorized() {
        // given
        User otherUser = User.builder().id(2L).username("otheruser").build();
        Order order = Order.builder().id(1L).user(otherUser).product(testProduct).paymentKey("key").build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
