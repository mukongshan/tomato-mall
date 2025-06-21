-- 1. 先创建 accounts 表（不带 shop_id 外键）

DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts
(
    id             INT AUTO_INCREMENT COMMENT '用户ID',
    PRIMARY KEY (id),
    username       VARCHAR(50)  NOT NULL COMMENT '用户名，不允许为空',
    password       VARCHAR(100) NOT NULL COMMENT '用户密码，仅参与插入操作',
    name           VARCHAR(50)  NOT NULL COMMENT '用户姓名，不允许为空',
    avatar         VARCHAR(255) NULL COMMENT '用户头像链接',
    role           VARCHAR(50)  NULL COMMENT '用户身份',
    telephone      VARCHAR(11)  NULL COMMENT '用户手机号，格式需符合1开头的11位数字',
    email          VARCHAR(100) NULL COMMENT '用户邮箱，格式需符合邮箱规范',
    location       VARCHAR(255) NULL COMMENT '用户所在地',
    shop_id        INT          NULL COMMENT '用户(员工)所属商店',
    is_valid_staff INT(1)       NULL COMMENT '用户申请成为员工是否通过'
) COMMENT '用户表';

DROP TABLE IF EXISTS shops;

-- 2. 再创建 shops 表（不带 owner_id 外键）
CREATE TABLE shops
(
    id          INT AUTO_INCREMENT COMMENT '商店ID',
    PRIMARY KEY (id),
    name        VARCHAR(100)  NOT NULL COMMENT '商店名称，不允许为空',
    owner_id    INT           NOT NULL COMMENT '店主ID，不允许为空',
    icon_url    VARCHAR(255)  NULL COMMENT '商店图标链接',
    description VARCHAR(255)  NULL COMMENT '商店描述',
    rate        DECIMAL(3, 2) NULL COMMENT '商店评分，保留两位小数 0-10',
    is_valid    INT(1)        NOT NULL DEFAULT 0 COMMENT '商店是否有效, 1表示有效，0表示无效，默认为0'
) COMMENT '商店表';

-- 3. 添加 shops.owner_id 外键（指向 accounts.id）
ALTER TABLE shops
    ADD FOREIGN KEY (owner_id) REFERENCES accounts (id);

-- 4. 添加 accounts.shop_id 外键（指向 shops.id）
ALTER TABLE accounts
    ADD FOREIGN KEY (shop_id) REFERENCES shops (id);

DROP TABLE IF EXISTS orders;

create table orders
(
    order_id       int auto_increment comment '订单ID'
        primary key,
    account_id     int                                   not null comment '用户ID',
    total_amount   decimal(10, 2)                        not null comment '订单总金额',
    payment_method varchar(50)                           not null comment '支付方式',
    status         varchar(20) default 'PENDING'         not null comment '订单支付状态（PENDING, SUCCESS, FAILED, TIMEOUT）',
    create_time    timestamp   default CURRENT_TIMESTAMP null comment '订单创建时间',
    foreign key (account_id) references accounts (id)
)
    comment '订单表';

create index account_id
    on orders (account_id);

DROP TABLE IF EXISTS products;

create table products
(
    id          int auto_increment comment '商品id'
        primary key,
    title       varchar(50)    not null comment '商品名称，不允许为空',
    price       decimal(10, 2) not null comment '商品价格，不允许为空，最低为0元',
    rate        double         not null comment '商品评分，最低0分，最高10分',
    description varchar(255)   null comment '商品描述',
    cover       varchar(500)   null comment '商品封面url',
    detail      varchar(500)   null comment '商品详细说明',
    shop_id     int            not null comment '商品所属商店id',
    foreign key (shop_id) references shops (id)
)
    comment '商品表';

DROP TABLE IF EXISTS advertisements;

create table advertisements
(
    id         int auto_increment comment '广告id'
        primary key,
    title      varchar(50)  not null comment '广告标题，不允许为空',
    content    varchar(500) not null comment '广告内容',
    image_url  varchar(500) not null comment '广告图片url',
    product_id int          not null comment '所属商品id，不允许为空',
    foreign key (product_id) references products (id)
)
    comment '广告表';

create index product_id
    on advertisements (product_id);

DROP TABLE IF EXISTS carts;

create table carts
(
    cartitem_id int auto_increment comment '购物车商品id'
        primary key,
    account_id  int           not null comment '用户id，关联用户表',
    product_id  int           not null comment '商品id，关联商品表',
    quantity    int default 1 not null comment '商品数量，默认为1',
    foreign key (account_id) references accounts (id)
        on delete cascade,
    foreign key (product_id) references products (id)
        on delete cascade
)
    comment '购物车商品表';

create index account_id
    on carts (account_id);

create index product_id
    on carts (product_id);

DROP TABLE IF EXISTS carts_orders_relation;

create table carts_orders_relation
(
    carts_orders_relation_id int auto_increment comment '主键ID'
        primary key,
    cartitem_id              int not null comment '关联购物车商品ID',
    order_id                 int not null comment '关联订单ID',
    foreign key (cartitem_id) references carts (cartitem_id),
    foreign key (order_id) references orders (order_id)
)
    comment '购物车商品与订单关联表';

create index cartitem_id
    on carts_orders_relation (cartitem_id);

create index order_id
    on carts_orders_relation (order_id);

DROP TABLE IF EXISTS specifications;

create table specifications
(
    specification_id int auto_increment comment '规格id'
        primary key,
    item             varchar(50)  not null comment '规格名称，不允许为空',
    value            varchar(255) not null comment '规格内容，不允许为空',
    product_id       int          not null comment '所属商品id，不允许为空',
    foreign key (product_id) references products (id)
)
    comment '商品规格表';

create index product_id
    on specifications (product_id);

DROP TABLE IF EXISTS stockpiles;

create table stockpiles
(
    stockpile_id int auto_increment comment '商品库存id'
        primary key,
    product_id   int not null comment '所属商品id，不允许为空',
    amount       int not null comment '商品库存数，指可卖的商品数量，不允许为空',
    frozen       int not null comment '商品库存冻结数，指不可卖的商品数量，不允许为空',
    foreign key (product_id) references products (id)
)
    comment '商品库存表';

create index product_id
    on stockpiles (product_id);


DROP TABLE IF EXISTS messages;

CREATE TABLE messages
(
    id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
-- 1. 有新的员工申请 2. 有新的商店申请 3. 库存不足 4.您的申请已经通过/未通过 5. 解雇员工 6 您已被解雇
-- 消息正文内容就是这四个  前三个消息对应用户到对应页面检查  前端界面 这些数据单独显示
    content      TEXT       NOT NULL COMMENT '消息内容',
    -- type ENUM('SEND','REPLY') NOT NULL COMMENT '消息类型',
    is_read      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
    from_user    INT        NOT NULL COMMENT '发送方用户ID',
    to_user      INT        NOT NULL COMMENT '接收方用户ID',
    -- shop_id INT COMMENT '关联的商店ID（可选）',
    created_time DATETIME            DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
    FOREIGN KEY (from_user) REFERENCES accounts (id),
    FOREIGN KEY (to_user) REFERENCES accounts (id)
    -- FOREIGN KEY (shop_id) REFERENCES shops(id)
) COMMENT ='用户消息表';

DROP TABLE IF EXISTS reviews;

CREATE TABLE reviews
(
    id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    account_id INT                      NOT NULL COMMENT '评论用户ID',
    content    TEXT                     NOT NULL COMMENT '评论内容',
    rate       DECIMAL(3, 2)            NOT NULL COMMENT '评分（1.00-9.99）',
    -- 区分评论类型：商品评论（PRODUCT）或商店评论（SHOP）
    type       ENUM ('PRODUCT', 'SHOP') NOT NULL COMMENT '评论类型',
    -- 商品评论关联 product_id，商店评论关联 shop_id
    product_id INT                      NULL COMMENT '关联的商品ID（如果是商品评论）',
    shop_id    INT                      NULL COMMENT '关联的商店ID（如果是商店评论）',
    -- 评论时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    -- 外键约束
    FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (shop_id) REFERENCES shops (id) ON DELETE CASCADE,
    -- 确保评论要么关联商品，要么关联商店，但不能同时关联
    CONSTRAINT chk_review_type CHECK (
        (type = 'PRODUCT' AND product_id IS NOT NULL AND shop_id IS NULL) OR
        (type = 'SHOP' AND shop_id IS NOT NULL AND product_id IS NULL)
        )
) COMMENT ='评论表（商品评论/商店评论）';

DROP TABLE IF EXISTS coupon;

CREATE TABLE coupon
(
    id             INT AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    name           VARCHAR(255) NOT NULL COMMENT '优惠券名称',
    description    VARCHAR(255) COMMENT '优惠券描述',
    discount_type  INT          NOT NULL COMMENT '折扣类型：1-百分比折扣，2-固定金额折扣',
    discount_value DOUBLE       NOT NULL COMMENT '折扣值：百分比折扣时为折扣率，固定金额折扣时为折扣金额',
    start_time     DATETIME     NOT NULL COMMENT '优惠券开始时间',
    end_time       DATETIME     NOT NULL COMMENT '优惠券结束时间',
    quantity       INT          NOT NULL COMMENT '优惠券总量',
    used_quantity  INT          NOT NULL DEFAULT 0 COMMENT '已使用数量',
    is_valid        INT          NOT NULL DEFAULT 1 COMMENT '优惠券是否有效，1表示有效，0表示无效',
    -- 确保开始时间早于结束时间
    CONSTRAINT chk_coupon_time CHECK (start_time < end_time),
    -- 确保折扣类型有效
    CONSTRAINT chk_coupon_discount_type CHECK (discount_type IN (1, 2)),
    -- 确保折扣值有效
    CONSTRAINT chk_coupon_discount_value CHECK (
        (discount_type = 1 AND discount_value > 0 AND discount_value <= 1) OR
        (discount_type = 2 AND discount_value > 0)
        )
) COMMENT ='优惠券表';

DROP TABLE IF EXISTS account_coupons_relation;

CREATE TABLE account_coupons_relation
(
    id                 INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    account_id         INT NOT NULL COMMENT '用户ID',
    coupon_id          INT NOT NULL COMMENT '优惠券ID',
    quantity INT NOT NULL COMMENT '剩余数量',
    -- 外键约束
    FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupon (id) ON DELETE CASCADE
) COMMENT ='用户优惠券池';

DELIMITER //

-- 新增商品评论时，更新商品 rate
CREATE TRIGGER after_review_insert_product
    AFTER INSERT
    ON reviews
    FOR EACH ROW
BEGIN
    IF NEW.type = 'PRODUCT' THEN
        UPDATE products p
        SET p.rate = (SELECT IFNULL(AVG(r.rate), 0.00)
                      FROM reviews r
                      WHERE r.type = 'PRODUCT'
                        AND r.product_id = NEW.product_id)
        WHERE p.id = NEW.product_id;
    END IF;

    -- 新增商店评论时，更新商店 rate
    IF NEW.type = 'SHOP' THEN
        UPDATE shops s
        SET s.rate = (SELECT IFNULL(AVG(r.rate), 0.00)
                      FROM reviews r
                      WHERE r.type = 'SHOP'
                        AND r.shop_id = NEW.shop_id)
        WHERE s.id = NEW.shop_id;
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE TRIGGER after_review_delete
    AFTER DELETE
    ON reviews
    FOR EACH ROW
BEGIN
    -- 删除商品评论后，更新商品 rate
    IF OLD.type = 'PRODUCT' THEN
        UPDATE products p
        SET p.rate = (SELECT IFNULL(AVG(r.rate), 0.00)
                      FROM reviews r
                      WHERE r.type = 'PRODUCT'
                        AND r.product_id = OLD.product_id)
        WHERE p.id = OLD.product_id;
    END IF;

    -- 删除商店评论后，更新商店 rate
    IF OLD.type = 'SHOP' THEN
        UPDATE shops s
        SET s.rate = (SELECT IFNULL(AVG(r.rate), 0.00)
                      FROM reviews r
                      WHERE r.type = 'SHOP'
                        AND r.shop_id = OLD.shop_id)
        WHERE s.id = OLD.shop_id;
    END IF;
END //

DELIMITER ;


-- 或者查看所有触发器
SELECT *
FROM INFORMATION_SCHEMA.TRIGGERS
WHERE TRIGGER_SCHEMA = DATABASE();

DELIMITER //
CREATE TRIGGER trg_delete_product
    BEFORE DELETE
    ON products
    FOR EACH ROW
BEGIN
    -- 删除相关广告
    DELETE FROM advertisements WHERE product_id = OLD.id;

    -- 删除相关购物车记录
    DELETE FROM carts WHERE product_id = OLD.id;

    -- 删除相关评论
    DELETE FROM reviews WHERE product_id = OLD.id;

    -- 删除相关规格
    DELETE FROM specifications WHERE product_id = OLD.id;

    -- 删除相关库存记录
    DELETE FROM stockpiles WHERE product_id = OLD.id;
END //

DELIMITER ;

DELIMITER //

CREATE TRIGGER trg_delete_coupon
    BEFORE DELETE
    ON coupon
    FOR EACH ROW
BEGIN
    -- 删除所有 count_id 等于当前 coupon id 的关联记录
    DELETE FROM account_coupons_relation
    WHERE coupon_id = OLD.id;
END //

DELIMITER ;
