-- 주문 테이블 정의
CREATE TABLE public."purchase_order" (
    id uuid PRIMARY KEY,
    product_id uuid NOT NULL,
    seller_id uuid NOT NULL,
    member_id uuid NOT NULL,
    amount numeric(15,2) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);

COMMENT ON TABLE public."purchase_order" IS '구매 주문';
