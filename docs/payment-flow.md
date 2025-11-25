# Toss Payment Flow Guide

이 문서는 토스 결제 연동 시 `orderId`, `paymentKey`를 어떻게 얻고 `/api/v1/payments/confirm` API를 호출해야 하는지 설명한다.

## 전체 흐름

1. **서버에서 `orderId` 생성**  
   - `/api/v1/orders`로 상품 ID와 구매자 정보를 보내 주문을 생성한다.  
   - 응답으로 돌아온 주문 ID(UUID)가 Toss `orderId`로 사용된다.
2. **프런트에서 Toss 위젯 초기화**  
   - 사용자가 결제 버튼을 누르면 Toss Payments SDK/위젯을 띄운다.  
   - 이때 서버가 발급한 `orderId`, 결제 금액(`amount`) 등을 Toss에 전달한다.
3. **사용자 결제 완료 → Toss 응답**  
   - 사용자가 결제를 성공적으로 마치면 Toss가 프런트에 `paymentKey`, `orderId`, `amount` 등을 반환한다.  
   - 실패 시에는 실패 코드/메시지를 돌려준다.
4. **프런트 → 서버 Confirm 요청**  
   - 프런트는 받은 `paymentKey`, `orderId`, `amount`를 JSON으로 만들어 서버의 `/api/v1/payments/confirm` 엔드포인트에 전송한다.  
   - 예시 (값은 실제 받은 값으로 대체해야 한다):
     ```bash
     curl -X POST http://localhost:8080/api/v1/payments/confirm \
       -H "Content-Type: application/json" \
       -d '{"paymentKey":"{REAL_PAYMENT_KEY}","orderId":"{ORDER_ID}","amount":1000}'
     ```
5. **서버에서 Toss Confirm API 호출**  
   - 서버는 `paymentKey`, `orderId`, `amount`를 사용해 토스의 `/v1/payments/confirm` API를 호출한다.  
   - 성공 시 결제 정보가 `payment` 테이블에 저장되고 주문 상태를 `PAID`로 변경한 뒤 `seller_settlement`에 정산 레코드를 만든다. 응답으로 `PaymentInfo`가 반환된다.

## 값 설명

- **orderId**: 가맹점(우리 서비스)이 직접 생성하는 주문 번호. 결제마다 고유해야 하며 Toss SDK에 그대로 넘긴다.
- **paymentKey**: 사용자가 결제를 성공적으로 마친 후 Toss가 콜백으로 반환하는 키. Confirm API 호출 시 반드시 필요하다.
- **amount**: 결제 금액. 프런트에서 Toss 위젯을 띄울 때 전달한 값과 동일해야 하며, Confirm 단계에서도 동일하게 사용한다.

## 테스트 팁

- Toss 개발자 센터의 “테스트 결제 시나리오”를 활용하면 `paymentKey`를 쉽고 안전하게 얻을 수 있다.
- 서버 실행 전 `.env`의 `TOSS_SECRET_KEY`를 테스트/운영 키로 채우고 `source .env`로 환경 변수 등록을 잊지 말자.
- DB에 `docs/payment-ddl.sql`로 정의된 `public.payment` 테이블을 미리 생성해야 한다.
- `src/main/resources/static/toss-payment.html`을 브라우저에서 열면 간단한 테스트 위젯을 확인할 수 있다. 위젯이 리다이렉트한 success URL에서 `paymentKey` 등을 확인한 뒤 `/api/v1/payments/confirm`에 전달하면 된다.
- 결제 성공/실패 리다이렉트는 `src/main/resources/static/payments/success.html`, `fail.html`로 제공된다. 성공 페이지는 쿼리 파라미터를 자동으로 `/api/v1/payments/confirm`에 전달하고, 실패 페이지는 `/api/v1/payments/fail`로 실패 정보를 전송한 뒤 응답을 화면에 출력한다.
- 정산 배치는 `/api/v1/settlements/run`을 호출해 수동으로 실행할 수 있으며, 자세한 내용은 `docs/settlement-batch.md`를 참고한다.
