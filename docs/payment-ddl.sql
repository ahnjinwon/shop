-- 결제 테이블 정의 (PostgreSQL)
CREATE TABLE public."payment" (
    id uuid PRIMARY KEY,
    payment_key varchar(200) UNIQUE NOT NULL,
    order_id varchar(100) NOT NULL,
    total_amount bigint NOT NULL,
    method varchar(50),
    status varchar(20) NOT NULL,
    requested_at timestamp,
    approved_at timestamp,
    fail_reason varchar(255),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);

COMMENT ON TABLE public."payment" IS '결제 정보';
COMMENT ON COLUMN public."payment".payment_key IS '토스 결제 키';
COMMENT ON COLUMN public."payment".order_id IS '가맹점 주문 번호';
COMMENT ON COLUMN public."payment".total_amount IS '결제 금액';

-- 결제 실패 로그 테이블
CREATE TABLE public."payment_failure" (
    id uuid PRIMARY KEY,
    order_id varchar(100) NOT NULL,
    payment_key varchar(200),
    error_code varchar(50),
    error_message varchar(255),
    amount bigint,
    raw_payload text,
    created_at timestamp NOT NULL DEFAULT now()
);

COMMENT ON TABLE public."payment_failure" IS '결제 실패 로그';

-- 셀러 정산 테이블
CREATE TABLE public."seller_settlement" (
    id uuid PRIMARY KEY,
    seller_id uuid NOT NULL,
    order_id uuid NOT NULL,
    amount numeric(15,2) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    settled_at timestamp
);
