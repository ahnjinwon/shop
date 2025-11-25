-- 제품 테이블 정의 (PostgreSQL)
CREATE TABLE public."product" (
    id uuid PRIMARY KEY,
    seller_id uuid NOT NULL,
    name varchar(100) NOT NULL,
    description text,
    price numeric(15,2) NOT NULL,
    stock integer NOT NULL DEFAULT 0,
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',
    reg_id uuid NOT NULL,
    reg_dt timestamp with time zone NOT NULL,
    modify_id uuid NOT NULL,
    modify_dt timestamp with time zone NOT NULL
);

COMMENT ON TABLE public."product" IS '상품 정보';
COMMENT ON COLUMN public."product".seller_id IS '판매자 ID';
COMMENT ON COLUMN public."product".id IS '상품 ID';
COMMENT ON COLUMN public."product".name IS '상품명';
COMMENT ON COLUMN public."product".description IS '상품 설명';
COMMENT ON COLUMN public."product".price IS '판매가';
COMMENT ON COLUMN public."product".stock IS '재고 수량';
COMMENT ON COLUMN public."product".status IS '상품 상태';
COMMENT ON COLUMN public."product".reg_id IS '등록자';
COMMENT ON COLUMN public."product".reg_dt IS '등록일시';
COMMENT ON COLUMN public."product".modify_id IS '수정자';
COMMENT ON COLUMN public."product".modify_dt IS '수정일시';
