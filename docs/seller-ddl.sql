-- 판매자 테이블 정의 (PostgreSQL)
CREATE TABLE public."seller" (
    id uuid PRIMARY KEY,
    company_name varchar(100) NOT NULL,
    representative_name varchar(50) NOT NULL,
    email varchar(120) NOT NULL UNIQUE,
    phone varchar(20) NOT NULL UNIQUE,
    business_number varchar(20) NOT NULL UNIQUE,
    address varchar(200),
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);

COMMENT ON TABLE public."seller" IS '판매자 정보';
