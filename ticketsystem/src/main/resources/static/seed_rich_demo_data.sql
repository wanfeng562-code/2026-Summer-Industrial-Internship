-- Rich incremental demo data for ticket_system.
-- Safe properties:
--   1. Does not DROP or truncate business tables.
--   2. Uses dedicated demo_* usernames and DEMO* business numbers.
--   3. Re-running the script does not duplicate the seeded records.
--   4. Rolls back the current run when any SQL statement fails.
--
-- Prerequisite:
--   source ticketsystem/src/main/resources/static/migrate_full_requirements.sql
--
-- Recommended client:
--   mysql --default-character-set=utf8mb4 -u root -p

SET NAMES utf8mb4;
USE `ticket_system`;

DELIMITER $$

DROP PROCEDURE IF EXISTS `seed_rich_demo_data`$$
CREATE PROCEDURE `seed_rich_demo_data`()
BEGIN
    DECLARE demo_password_hash VARCHAR(200)
        DEFAULT '$2b$10$ZaSqjiuijP4UKPqbH/2T8.EJVJFPn7Qf1YwHESo5keCR30GQHmNXi';

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'agent_group'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'ticket_category'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'ticket_satisfaction'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name = 'ai_chat_session'
    ) OR NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'ticket' AND column_name = 'group_id'
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Please run migrate_full_requirements.sql before seed_rich_demo_data.sql';
    END IF;

    START TRANSACTION;

    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_users`;
    CREATE TEMPORARY TABLE `tmp_demo_users` (
        `username` VARCHAR(50) NOT NULL PRIMARY KEY,
        `nickname` VARCHAR(50) NOT NULL,
        `email` VARCHAR(100) NOT NULL,
        `phone` VARCHAR(20) NOT NULL,
        `role` VARCHAR(20) NOT NULL,
        `reputation_score` INT NOT NULL
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    INSERT INTO `tmp_demo_users`
        (`username`, `nickname`, `email`, `phone`, `role`, `reputation_score`)
    VALUES
        ('demo_2026_chenxi', '陈曦', 'chenxi.demo@ticket.local', '13710000001', 'USER', 98),
        ('demo_2026_linan', '李楠', 'linan.demo@ticket.local', '13710000002', 'USER', 92),
        ('demo_2026_zhouyu', '周宇', 'zhouyu.demo@ticket.local', '13710000003', 'USER', 88),
        ('demo_2026_sunyue', '孙悦', 'sunyue.demo@ticket.local', '13710000004', 'USER', 96),
        ('demo_2026_zhaolei', '赵磊', 'zhaolei.demo@ticket.local', '13710000005', 'USER', 75),
        ('demo_2026_qianjing', '钱静', 'qianjing.demo@ticket.local', '13710000006', 'USER', 84),
        ('demo_2026_wutong', '吴桐', 'wutong.demo@ticket.local', '13710000007', 'USER', 90),
        ('demo_2026_zhenghao', '郑浩', 'zhenghao.demo@ticket.local', '13710000008', 'USER', 68),
        ('demo_2026_fengyu', '冯雨', 'fengyu.demo@ticket.local', '13710000009', 'USER', 81),
        ('demo_2026_heyuan', '何远', 'heyuan.demo@ticket.local', '13710000010', 'USER', 94),
        ('demo_2026_luoxin', '罗欣', 'luoxin.demo@ticket.local', '13710000011', 'USER', 87),
        ('demo_2026_gaoyang', '高阳', 'gaoyang.demo@ticket.local', '13710000012', 'USER', 79),
        ('demo_2026_agent_refund', '周客服', 'refund.agent@ticket.local', '13610000001', 'AGENT', 100),
        ('demo_2026_agent_logistics', '吴客服', 'logistics.agent@ticket.local', '13610000002', 'AGENT', 100),
        ('demo_2026_agent_quality', '孙客服', 'quality.agent@ticket.local', '13610000003', 'AGENT', 100),
        ('demo_2026_agent_general', '赵客服', 'general.agent@ticket.local', '13610000004', 'AGENT', 100);

    INSERT INTO `sys_user`
        (`username`, `password`, `nickname`, `email`, `phone`, `role`,
         `reputation_score`, `deleted`, `create_time`, `update_time`)
    SELECT seed.username, demo_password_hash, seed.nickname, seed.email, seed.phone,
           seed.role, seed.reputation_score, 0,
           DATE_SUB(NOW(), INTERVAL 120 DAY), NOW()
    FROM `tmp_demo_users` seed
    LEFT JOIN `sys_user` existing ON existing.username = seed.username
    WHERE existing.id IS NULL;

    INSERT INTO `agent_group`
        (`group_name`, `leader_id`, `description`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT '演示数据-退款售后组', NULL, '负责退款、退货和支付争议类工单', 1, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `agent_group` WHERE `group_name` = '演示数据-退款售后组'
    );
    INSERT INTO `agent_group`
        (`group_name`, `leader_id`, `description`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT '演示数据-物流服务组', NULL, '负责发货、配送、签收和物流异常类工单', 1, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `agent_group` WHERE `group_name` = '演示数据-物流服务组'
    );
    INSERT INTO `agent_group`
        (`group_name`, `leader_id`, `description`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT '演示数据-商品质量组', NULL, '负责破损、质量、安装和使用问题', 1, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `agent_group` WHERE `group_name` = '演示数据-商品质量组'
    );
    INSERT INTO `agent_group`
        (`group_name`, `leader_id`, `description`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT '演示数据-综合服务组', NULL, '负责发票、账号和其他综合售后问题', 1, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `agent_group` WHERE `group_name` = '演示数据-综合服务组'
    );

    UPDATE `agent_group`
    SET `leader_id` = (SELECT id FROM `sys_user` WHERE username = 'demo_2026_agent_refund'),
        `update_time` = NOW()
    WHERE `group_name` = '演示数据-退款售后组';
    UPDATE `agent_group`
    SET `leader_id` = (SELECT id FROM `sys_user` WHERE username = 'demo_2026_agent_logistics'),
        `update_time` = NOW()
    WHERE `group_name` = '演示数据-物流服务组';
    UPDATE `agent_group`
    SET `leader_id` = (SELECT id FROM `sys_user` WHERE username = 'demo_2026_agent_quality'),
        `update_time` = NOW()
    WHERE `group_name` = '演示数据-商品质量组';
    UPDATE `agent_group`
    SET `leader_id` = (SELECT id FROM `sys_user` WHERE username = 'demo_2026_agent_general'),
        `update_time` = NOW()
    WHERE `group_name` = '演示数据-综合服务组';

    UPDATE `sys_user`
    SET `agent_group_id` = (
            SELECT id FROM `agent_group` WHERE group_name = '演示数据-退款售后组'
        ),
        `update_time` = NOW()
    WHERE `username` = 'demo_2026_agent_refund';
    UPDATE `sys_user`
    SET `agent_group_id` = (
            SELECT id FROM `agent_group` WHERE group_name = '演示数据-物流服务组'
        ),
        `update_time` = NOW()
    WHERE `username` = 'demo_2026_agent_logistics';
    UPDATE `sys_user`
    SET `agent_group_id` = (
            SELECT id FROM `agent_group` WHERE group_name = '演示数据-商品质量组'
        ),
        `update_time` = NOW()
    WHERE `username` = 'demo_2026_agent_quality';
    UPDATE `sys_user`
    SET `agent_group_id` = (
            SELECT id FROM `agent_group` WHERE group_name = '演示数据-综合服务组'
        ),
        `update_time` = NOW()
    WHERE `username` = 'demo_2026_agent_general';

    INSERT INTO `ticket_category`
        (`category_code`, `category_name`, `group_id`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT 'QUALITY', '商品质量', group_data.id, 1, 0, NOW(), NOW()
    FROM `agent_group` group_data
    WHERE group_data.group_name = '演示数据-商品质量组'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_category` WHERE category_code = 'QUALITY'
      );
    INSERT INTO `ticket_category`
        (`category_code`, `category_name`, `group_id`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT 'INSTALLATION', '安装使用', group_data.id, 1, 0, NOW(), NOW()
    FROM `agent_group` group_data
    WHERE group_data.group_name = '演示数据-商品质量组'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_category` WHERE category_code = 'INSTALLATION'
      );

    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_orders`;
    CREATE TEMPORARY TABLE `tmp_demo_orders` (
        `order_no` VARCHAR(50) NOT NULL PRIMARY KEY,
        `username` VARCHAR(50) NOT NULL,
        `product_name` VARCHAR(200) NOT NULL,
        `quantity` INT NOT NULL,
        `unit_price` DECIMAL(10,2) NOT NULL,
        `order_status` VARCHAR(30) NOT NULL,
        `payment_status` VARCHAR(20) NOT NULL,
        `logistics_status` VARCHAR(20) DEFAULT NULL,
        `logistics_no` VARCHAR(50) DEFAULT NULL,
        `days_ago` INT NOT NULL
    ) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    INSERT INTO `tmp_demo_orders`
        (`order_no`, `username`, `product_name`, `quantity`, `unit_price`,
         `order_status`, `payment_status`, `logistics_status`, `logistics_no`, `days_ago`)
    VALUES
        ('DEMO202607001', 'demo_2026_chenxi', '智能降噪蓝牙耳机', 1, 1299.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607001', 92),
        ('DEMO202607002', 'demo_2026_chenxi', '便携式机械键盘', 1, 499.00, 'SHIPPED', 'PAID', 'SHIPPED', 'YTDEMO2607002', 18),
        ('DEMO202607003', 'demo_2026_chenxi', '办公显示器支架', 2, 189.00, 'PAID', 'PAID', 'PENDING', NULL, 4),
        ('DEMO202607004', 'demo_2026_linan', '轻薄商务笔记本电脑', 1, 6599.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607004', 88),
        ('DEMO202607005', 'demo_2026_linan', '65W氮化镓充电器', 2, 159.00, 'COMPLETED', 'REFUNDED', 'RECEIVED', 'ZTDEMO2607005', 52),
        ('DEMO202607006', 'demo_2026_linan', '人体工学办公椅', 1, 1399.00, 'DELIVERED', 'PAID', 'DELIVERED', 'JDDEMO2607006', 12),
        ('DEMO202607007', 'demo_2026_zhouyu', '4K运动相机套装', 1, 2699.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607007', 75),
        ('DEMO202607008', 'demo_2026_zhouyu', '高速存储卡256GB', 2, 229.00, 'CANCELLED', 'UNPAID', NULL, NULL, 30),
        ('DEMO202607009', 'demo_2026_zhouyu', '旅行摄影双肩包', 1, 599.00, 'SHIPPED', 'PAID', 'SHIPPED', 'YDDEMO2607009', 7),
        ('DEMO202607010', 'demo_2026_sunyue', '扫拖一体机器人', 1, 3299.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607010', 70),
        ('DEMO202607011', 'demo_2026_sunyue', '空气净化器滤芯', 3, 199.00, 'COMPLETED', 'PAID', 'RECEIVED', 'ZTDEMO2607011', 41),
        ('DEMO202607012', 'demo_2026_sunyue', '智能门锁Pro版', 1, 1899.00, 'PAID', 'PAID', 'PENDING', NULL, 3),
        ('DEMO202607013', 'demo_2026_zhaolei', '全自动咖啡机', 1, 4599.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607013', 66),
        ('DEMO202607014', 'demo_2026_zhaolei', '精品咖啡豆礼盒', 2, 168.00, 'COMPLETED', 'PAID', 'RECEIVED', 'ZTDEMO2607014', 28),
        ('DEMO202607015', 'demo_2026_zhaolei', '手冲咖啡电子秤', 1, 239.00, 'SHIPPED', 'PAID', 'SHIPPED', 'YDDEMO2607015', 6),
        ('DEMO202607016', 'demo_2026_qianjing', '护眼阅读台灯', 1, 699.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607016', 63),
        ('DEMO202607017', 'demo_2026_qianjing', '电子墨水阅读器', 1, 2399.00, 'DELIVERED', 'PAID', 'DELIVERED', 'JDDEMO2607017', 11),
        ('DEMO202607018', 'demo_2026_qianjing', '阅读器保护套', 1, 129.00, 'COMPLETED', 'REFUNDED', 'RECEIVED', 'ZTDEMO2607018', 35),
        ('DEMO202607019', 'demo_2026_wutong', '智能运动手表', 1, 2199.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607019', 59),
        ('DEMO202607020', 'demo_2026_wutong', '户外运动水壶', 2, 99.00, 'COMPLETED', 'PAID', 'RECEIVED', 'ZTDEMO2607020', 23),
        ('DEMO202607021', 'demo_2026_wutong', '家用跑步机', 1, 3899.00, 'PAID', 'PAID', 'PENDING', NULL, 2),
        ('DEMO202607022', 'demo_2026_zhenghao', '专业游戏手柄', 1, 499.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607022', 55),
        ('DEMO202607023', 'demo_2026_zhenghao', '27英寸电竞显示器', 1, 1999.00, 'COMPLETED', 'PAID', 'RECEIVED', 'JDDEMO2607023', 31),
        ('DEMO202607024', 'demo_2026_zhenghao', '桌面音响套装', 1, 899.00, 'SHIPPED', 'PAID', 'SHIPPED', 'YDDEMO2607024', 5),
        ('DEMO202607025', 'demo_2026_fengyu', '婴儿监护摄像头', 1, 799.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607025', 48),
        ('DEMO202607026', 'demo_2026_fengyu', '恒温调奶器', 1, 399.00, 'COMPLETED', 'PAID', 'RECEIVED', 'ZTDEMO2607026', 26),
        ('DEMO202607027', 'demo_2026_heyuan', '智能投影仪', 1, 5299.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607027', 44),
        ('DEMO202607028', 'demo_2026_heyuan', '投影幕布', 1, 499.00, 'DELIVERED', 'PAID', 'DELIVERED', 'JDDEMO2607028', 9),
        ('DEMO202607029', 'demo_2026_luoxin', '大容量双开门冰箱', 1, 6999.00, 'COMPLETED', 'PAID', 'RECEIVED', 'SFDEMO2607029', 40),
        ('DEMO202607030', 'demo_2026_gaoyang', '洗烘一体机', 1, 5499.00, 'PAID', 'PAID', 'PENDING', NULL, 1);

    INSERT INTO `orders`
        (`order_no`, `user_id`, `product_name`, `quantity`, `unit_price`, `total_amount`,
         `order_status`, `payment_status`, `logistics_status`, `logistics_no`,
         `order_time`, `pay_time`, `deliver_time`, `receive_time`,
         `deleted`, `create_time`, `update_time`)
    SELECT seed.order_no, user_data.id, seed.product_name, seed.quantity, seed.unit_price,
           seed.quantity * seed.unit_price,
           seed.order_status, seed.payment_status, seed.logistics_status, seed.logistics_no,
           DATE_SUB(NOW(), INTERVAL seed.days_ago DAY),
           CASE WHEN seed.payment_status IN ('PAID', 'REFUNDED')
                THEN DATE_ADD(DATE_SUB(NOW(), INTERVAL seed.days_ago DAY), INTERVAL 5 MINUTE) END,
           CASE WHEN seed.logistics_status IN ('SHIPPED', 'DELIVERED', 'RECEIVED')
                THEN DATE_ADD(DATE_SUB(NOW(), INTERVAL seed.days_ago DAY), INTERVAL 1 DAY) END,
           CASE WHEN seed.logistics_status = 'RECEIVED'
                THEN DATE_ADD(DATE_SUB(NOW(), INTERVAL seed.days_ago DAY), INTERVAL 3 DAY) END,
           0, DATE_SUB(NOW(), INTERVAL seed.days_ago DAY), NOW()
    FROM `tmp_demo_orders` seed
    JOIN `sys_user` user_data ON user_data.username = seed.username
    LEFT JOIN `orders` existing ON existing.order_no = seed.order_no
    WHERE existing.id IS NULL;

    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_tickets`;
    CREATE TEMPORARY TABLE `tmp_demo_tickets` (
        `ticket_no` VARCHAR(50) NOT NULL PRIMARY KEY,
        `username` VARCHAR(50) NOT NULL,
        `agent_username` VARCHAR(50) DEFAULT NULL,
        `order_no` VARCHAR(50) NOT NULL,
        `group_name` VARCHAR(80) DEFAULT NULL,
        `title` VARCHAR(200) NOT NULL,
        `description` TEXT NOT NULL,
        `category` VARCHAR(30) NOT NULL,
        `status` VARCHAR(30) NOT NULL,
        `priority` VARCHAR(10) NOT NULL,
        `days_ago` INT NOT NULL,
        `sla_warning` TINYINT NOT NULL,
        `sla_escalated` TINYINT NOT NULL,
        `sla_offset_hours` INT NOT NULL,
        `archived` TINYINT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    INSERT INTO `tmp_demo_tickets`
        (`ticket_no`, `username`, `agent_username`, `order_no`, `group_name`,
         `title`, `description`, `category`, `status`, `priority`, `days_ago`,
         `sla_warning`, `sla_escalated`, `sla_offset_hours`, `archived`)
    VALUES
        ('DEMO-TK-001', 'demo_2026_chenxi', 'demo_2026_agent_refund', 'DEMO202607001', '演示数据-退款售后组',
         '耳机降噪效果不符合预期', '地铁环境下仍能听到明显噪声，希望退货退款。', 'REFUND', 'CLOSED', 'MEDIUM', 80, 0, 0, -1800, 0),
        ('DEMO-TK-002', 'demo_2026_chenxi', 'demo_2026_agent_logistics', 'DEMO202607002', '演示数据-物流服务组',
         '键盘物流连续三天未更新', '物流停留在中转站三天，请帮忙核查。', 'LOGISTICS', 'MANUAL_REVIEW', 'HIGH', 3, 1, 0, 3, 0),
        ('DEMO-TK-003', 'demo_2026_chenxi', NULL, 'DEMO202607003', '演示数据-综合服务组',
         '需要开具企业电子发票', '请按订单金额开具增值税电子普通发票。', 'INVOICE', 'AI_PROCESSING', 'LOW', 1, 0, 0, 36, 0),
        ('DEMO-TK-004', 'demo_2026_linan', 'demo_2026_agent_quality', 'DEMO202607004', '演示数据-商品质量组',
         '笔记本触控板偶发失灵', '触控板使用一段时间后无响应，重启后暂时恢复。', 'QUALITY', 'RESOLVED', 'HIGH', 32, 0, 0, -700, 0),
        ('DEMO-TK-005', 'demo_2026_linan', 'demo_2026_agent_general', 'DEMO202607005', '演示数据-综合服务组',
         '充电器接口型号购买错误', '商品已经拆封但未使用，咨询是否支持退换。', 'OTHER', 'REJECTED', 'LOW', 45, 0, 0, -1000, 1),
        ('DEMO-TK-006', 'demo_2026_linan', 'demo_2026_agent_logistics', 'DEMO202607006', '演示数据-物流服务组',
         '办公椅送达时外包装破损', '外包装有明显挤压，检查后商品暂时没有损坏。', 'LOGISTICS', 'CLOSED', 'MEDIUM', 10, 0, 0, -200, 0),
        ('DEMO-TK-007', 'demo_2026_zhouyu', NULL, 'DEMO202607007', '演示数据-商品质量组',
         '运动相机电池续航偏短', '满电拍摄约四十分钟就没电，希望检测电池。', 'QUALITY', 'MANUAL_REVIEW', 'MEDIUM', 4, 0, 0, 16, 0),
        ('DEMO-TK-008', 'demo_2026_zhouyu', 'demo_2026_agent_general', 'DEMO202607008', '演示数据-综合服务组',
         '取消订单后优惠券未退回', '订单已取消，但下单使用的优惠券没有回到账户。', 'OTHER', 'CLOSED', 'LOW', 26, 0, 0, -600, 0),
        ('DEMO-TK-009', 'demo_2026_zhouyu', NULL, 'DEMO202607009', '演示数据-商品质量组',
         '摄影包肩带安装方式咨询', '包装里有两条固定带，不清楚正确的安装位置。', 'INSTALLATION', 'AI_PROCESSING', 'LOW', 2, 0, 0, 40, 0),
        ('DEMO-TK-010', 'demo_2026_sunyue', 'demo_2026_agent_refund', 'DEMO202607010', '演示数据-退款售后组',
         '扫地机器人重复漏扫', '多次建图后仍漏扫卧室区域，申请退换。', 'REFUND', 'RESOLVED', 'HIGH', 18, 0, 0, -400, 0),
        ('DEMO-TK-011', 'demo_2026_sunyue', 'demo_2026_agent_quality', 'DEMO202607011', '演示数据-商品质量组',
         '滤芯包装密封不完整', '其中一个滤芯的塑封已经开口，担心影响使用。', 'DAMAGE', 'MANUAL_REVIEW', 'HIGH', 2, 1, 0, 2, 0),
        ('DEMO-TK-012', 'demo_2026_sunyue', 'demo_2026_agent_general', 'DEMO202607012', '演示数据-综合服务组',
         '智能门锁预约安装', '商品已经付款，希望预约本周末上门安装。', 'INSTALLATION', 'CLOSED', 'MEDIUM', 20, 0, 0, -450, 0),
        ('DEMO-TK-013', 'demo_2026_zhaolei', 'demo_2026_agent_logistics', 'DEMO202607013', '演示数据-物流服务组',
         '咖啡机配送时配件遗失', '收到主机但包装内没有奶管和清洁刷。', 'LOGISTICS', 'REJECTED', 'MEDIUM', 14, 0, 0, -300, 0),
        ('DEMO-TK-014', 'demo_2026_zhaolei', NULL, 'DEMO202607014', '演示数据-退款售后组',
         '咖啡豆口味与页面描述不符', '实际烘焙程度较深，希望退还未拆封的一盒。', 'REFUND', 'MANUAL_REVIEW', 'MEDIUM', 1, 0, 0, 22, 0),
        ('DEMO-TK-015', 'demo_2026_zhaolei', 'demo_2026_agent_quality', 'DEMO202607015', '演示数据-商品质量组',
         '电子秤显示数值漂移', '静置状态下读数不断变化，无法正常称量。', 'QUALITY', 'CLOSED', 'MEDIUM', 22, 0, 0, -500, 0),
        ('DEMO-TK-016', 'demo_2026_qianjing', NULL, 'DEMO202607016', '演示数据-物流服务组',
         '台灯物流显示签收但未收到', '快递状态显示本人签收，实际没有收到包裹。', 'LOGISTICS', 'AI_PROCESSING', 'URGENT', 1, 0, 0, 4, 0),
        ('DEMO-TK-017', 'demo_2026_qianjing', 'demo_2026_agent_general', 'DEMO202607017', '演示数据-综合服务组',
         '阅读器发票抬头需要修改', '电子发票中的公司名称少了两个字，需要重开。', 'INVOICE', 'RESOLVED', 'MEDIUM', 8, 0, 0, -160, 0),
        ('DEMO-TK-018', 'demo_2026_qianjing', NULL, 'DEMO202607018', NULL,
         '保护套退款金额有疑问', '退款已到账，但金额比实际支付金额少十元。', 'OTHER', 'MANUAL_REVIEW', 'HIGH', 2, 0, 0, 8, 0),
        ('DEMO-TK-019', 'demo_2026_wutong', 'demo_2026_agent_quality', 'DEMO202607019', '演示数据-商品质量组',
         '运动手表无法绑定手机', '蓝牙可以发现设备，但应用一直提示绑定失败。', 'INSTALLATION', 'CLOSED', 'MEDIUM', 16, 0, 0, -350, 0),
        ('DEMO-TK-020', 'demo_2026_wutong', 'demo_2026_agent_quality', 'DEMO202607020', '演示数据-商品质量组',
         '运动水壶瓶盖轻微漏水', '倒置时瓶盖边缘会渗水，希望补发瓶盖。', 'QUALITY', 'MANUAL_REVIEW', 'LOW', 3, 0, 0, 30, 0),
        ('DEMO-TK-021', 'demo_2026_zhenghao', 'demo_2026_agent_logistics', 'DEMO202607024', '演示数据-物流服务组',
         '音响预计送达时间查询', '订单已经发货，想确认是否能在周五前送达。', 'LOGISTICS', 'RESOLVED', 'LOW', 4, 0, 0, -60, 0),
        ('DEMO-TK-022', 'demo_2026_fengyu', 'demo_2026_agent_refund', 'DEMO202607025', '演示数据-退款售后组',
         '监护摄像头夜视画面异常', '夜间画面频繁出现条纹，检测后申请退款。', 'REFUND', 'CLOSED', 'HIGH', 36, 0, 0, -820, 1),
        ('DEMO-TK-023', 'demo_2026_heyuan', NULL, 'DEMO202607027', '演示数据-商品质量组',
         '投影仪画面边缘模糊', '调整对焦后中心清晰，但四周仍然模糊。', 'DAMAGE', 'AI_PROCESSING', 'HIGH', 1, 0, 0, 12, 0),
        ('DEMO-TK-024', 'demo_2026_luoxin', 'demo_2026_agent_general', 'DEMO202607029', '演示数据-综合服务组',
         '冰箱电子发票下载失败', '订单页点击下载发票后提示文件不存在。', 'INVOICE', 'MANUAL_REVIEW', 'MEDIUM', 2, 0, 0, 20, 0);

    INSERT INTO `ticket`
        (`ticket_no`, `user_id`, `agent_id`, `group_id`, `order_id`,
         `title`, `description`, `category`, `status`, `priority`,
         `sla_warning`, `sla_escalated`, `sla_deadline`,
         `resolve_time`, `close_time`, `archived`, `archive_time`,
         `deleted`, `create_time`, `update_time`)
    SELECT seed.ticket_no, owner_data.id, agent_data.id, group_data.id, order_data.id,
           seed.title, seed.description, seed.category, seed.status, seed.priority,
           seed.sla_warning, seed.sla_escalated,
           TIMESTAMPADD(HOUR, seed.sla_offset_hours, NOW()),
           CASE WHEN seed.status IN ('RESOLVED', 'CLOSED')
                THEN TIMESTAMPADD(DAY, -(seed.days_ago - 1), NOW()) END,
           CASE WHEN seed.status = 'CLOSED'
                THEN TIMESTAMPADD(DAY, -(seed.days_ago - 2), NOW()) END,
           seed.archived,
           CASE WHEN seed.archived = 1
                THEN TIMESTAMPADD(DAY, -(seed.days_ago - 3), NOW()) END,
           0, DATE_SUB(NOW(), INTERVAL seed.days_ago DAY), NOW()
    FROM `tmp_demo_tickets` seed
    JOIN `sys_user` owner_data ON owner_data.username = seed.username
    JOIN `orders` order_data ON order_data.order_no = seed.order_no
    LEFT JOIN `sys_user` agent_data ON agent_data.username = seed.agent_username
    LEFT JOIN `agent_group` group_data ON group_data.group_name = seed.group_name
    LEFT JOIN `ticket` existing ON existing.ticket_no = seed.ticket_no
    WHERE existing.id IS NULL;

    INSERT INTO `ticket_message`
        (`ticket_id`, `user_id`, `sender_type`, `message_type`, `content`, `deleted`, `create_time`)
    SELECT ticket_data.id, ticket_data.user_id, 'USER', 'TEXT', ticket_data.description, 0,
           DATE_ADD(ticket_data.create_time, INTERVAL 1 MINUTE)
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_message` message_data
          WHERE message_data.ticket_id = ticket_data.id
            AND message_data.sender_type = 'USER'
            AND message_data.message_type = 'TEXT'
      );

    INSERT INTO `ticket_message`
        (`ticket_id`, `user_id`, `sender_type`, `message_type`, `content`, `deleted`, `create_time`)
    SELECT ticket_data.id, ticket_data.user_id, 'AI', 'AI_REPLY',
           CONCAT('您好，已收到您关于“', ticket_data.title,
                  '”的问题。系统已核对关联订单，建议保留相关凭证；如需业务处理可转人工客服。'),
           0, DATE_ADD(ticket_data.create_time, INTERVAL 3 MINUTE)
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_message` message_data
          WHERE message_data.ticket_id = ticket_data.id
            AND message_data.message_type = 'AI_REPLY'
      );

    INSERT INTO `ticket_message`
        (`ticket_id`, `user_id`, `sender_type`, `message_type`, `content`, `deleted`, `create_time`)
    SELECT ticket_data.id, ticket_data.agent_id, 'AGENT', 'TEXT',
           CONCAT('您好，我是本工单处理客服。已查看“', ticket_data.title,
                  '”的情况，正在按照售后规则为您核实处理。'),
           0, DATE_ADD(ticket_data.create_time, INTERVAL 20 MINUTE)
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.agent_id IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_message` message_data
          WHERE message_data.ticket_id = ticket_data.id
            AND message_data.sender_type = 'AGENT'
      );

    INSERT INTO `ticket_message`
        (`ticket_id`, `user_id`, `sender_type`, `message_type`, `content`, `deleted`, `create_time`)
    SELECT ticket_data.id,
           COALESCE(ticket_data.agent_id,
                    (SELECT id FROM `sys_user` WHERE username = 'admin' LIMIT 1),
                    ticket_data.user_id),
           'SYSTEM', 'SYSTEM',
           CASE ticket_data.status
               WHEN 'RESOLVED' THEN '工单已处理完成，等待确认关闭。'
               WHEN 'CLOSED' THEN '用户已确认处理结果，工单已关闭。'
               WHEN 'REJECTED' THEN '当前材料不满足处理条件，工单已驳回，可补充材料后继续跟进。'
           END,
           0, ticket_data.update_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.status IN ('RESOLVED', 'CLOSED', 'REJECTED')
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_message` message_data
          WHERE message_data.ticket_id = ticket_data.id
            AND message_data.message_type = 'SYSTEM'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'CREATE', ticket_data.user_id, 'USER',
           NULL, 'AI_PROCESSING', '用户创建演示工单', ticket_data.create_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'CREATE'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'AI_REPLY', NULL, 'SYSTEM',
           'AI_PROCESSING', 'AI_PROCESSING', 'AI生成受控建议回复',
           DATE_ADD(ticket_data.create_time, INTERVAL 3 MINUTE)
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'AI_REPLY'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'CLAIM', ticket_data.agent_id, 'AGENT',
           'MANUAL_REVIEW', 'MANUAL_REVIEW', '客服接取演示工单',
           DATE_ADD(ticket_data.create_time, INTERVAL 15 MINUTE)
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.agent_id IS NOT NULL
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'CLAIM'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'RESOLVE', ticket_data.agent_id, 'AGENT',
           'MANUAL_REVIEW', 'RESOLVED', '客服完成问题处理',
           ticket_data.resolve_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.status IN ('RESOLVED', 'CLOSED')
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'RESOLVE'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'CLOSE', ticket_data.agent_id, 'AGENT',
           'RESOLVED', 'CLOSED', '用户确认后关闭工单',
           ticket_data.close_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.status = 'CLOSED'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'CLOSE'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'REJECT', ticket_data.agent_id, 'AGENT',
           'MANUAL_REVIEW', 'REJECTED', '材料不足或不满足当前售后条件',
           ticket_data.update_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.status = 'REJECTED'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'REJECT'
      );

    INSERT INTO `ticket_operation_log`
        (`ticket_id`, `action`, `operator_id`, `operator_role`,
         `before_status`, `after_status`, `detail`, `create_time`)
    SELECT ticket_data.id, 'ARCHIVE',
           (SELECT id FROM `sys_user` WHERE username = 'admin' LIMIT 1),
           'ADMIN', ticket_data.status, ticket_data.status, '管理员归档历史工单',
           ticket_data.archive_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.archived = 1
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_operation_log` log_data
          WHERE log_data.ticket_id = ticket_data.id AND log_data.action = 'ARCHIVE'
      );

    INSERT INTO `ai_process_log`
        (`ticket_id`, `message_id`, `intent_result`, `policy_matched`,
         `ai_action`, `ai_reply`, `process_detail`, `execution_time`, `create_time`)
    SELECT ticket_data.id,
           (SELECT message_data.id FROM `ticket_message` message_data
            WHERE message_data.ticket_id = ticket_data.id
              AND message_data.message_type = 'AI_REPLY'
            ORDER BY message_data.id LIMIT 1),
           ticket_data.category, '演示数据-分类基础策略',
           'AUTO_REPLY', '已生成只读建议，未直接修改业务数据。',
           '演示数据：模型完成分类与建议回复', 680,
           DATE_ADD(ticket_data.create_time, INTERVAL 3 MINUTE)
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND NOT EXISTS (
          SELECT 1 FROM `ai_process_log` log_data
          WHERE log_data.ticket_id = ticket_data.id
            AND log_data.ai_action = 'AUTO_REPLY'
      );

    INSERT INTO `ticket_satisfaction`
        (`ticket_id`, `user_id`, `score`, `comment`, `create_time`, `update_time`)
    SELECT ticket_data.id, ticket_data.user_id,
           CASE ticket_data.ticket_no
               WHEN 'DEMO-TK-001' THEN 5
               WHEN 'DEMO-TK-006' THEN 4
               WHEN 'DEMO-TK-008' THEN 4
               WHEN 'DEMO-TK-012' THEN 5
               WHEN 'DEMO-TK-015' THEN 3
               WHEN 'DEMO-TK-019' THEN 5
               WHEN 'DEMO-TK-022' THEN 4
               ELSE 4
           END,
           CASE ticket_data.ticket_no
               WHEN 'DEMO-TK-015' THEN '问题最终解决，但等待配件的时间较长。'
               WHEN 'DEMO-TK-006' THEN '客服核查及时，解释清楚。'
               ELSE '处理过程清晰，客服回复及时。'
           END,
           ticket_data.close_time, ticket_data.close_time
    FROM `ticket` ticket_data
    WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
      AND ticket_data.status = 'CLOSED'
      AND NOT EXISTS (
          SELECT 1 FROM `ticket_satisfaction` satisfaction_data
          WHERE satisfaction_data.ticket_id = ticket_data.id
            AND satisfaction_data.user_id = ticket_data.user_id
      );

    INSERT INTO `after_sale_policy`
        (`policy_name`, `category`, `condition_type`, `min_amount`, `max_amount`,
         `min_reputation`, `action`, `reply_template`, `priority`, `enabled`,
         `sla_hours`, `deleted`, `create_time`, `update_time`)
    SELECT '演示-高信誉用户小额退款', 'REFUND', 'AMOUNT_REPUTATION', 0.00, 300.00,
           85, 'AUTO_REPLY', '已记录退款诉求，系统将优先核验订单和支付状态。', 20, 1,
           12, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `after_sale_policy` WHERE policy_name = '演示-高信誉用户小额退款'
    );
    INSERT INTO `after_sale_policy`
        (`policy_name`, `category`, `condition_type`, `min_amount`, `max_amount`,
         `min_reputation`, `action`, `reply_template`, `priority`, `enabled`,
         `sla_hours`, `deleted`, `create_time`, `update_time`)
    SELECT '演示-物流签收争议转人工', 'LOGISTICS', 'ALWAYS', NULL, NULL,
           NULL, 'MANUAL', '签收争议需要核验配送凭证，已建议转人工。', 20, 1,
           4, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `after_sale_policy` WHERE policy_name = '演示-物流签收争议转人工'
    );
    INSERT INTO `after_sale_policy`
        (`policy_name`, `category`, `condition_type`, `min_amount`, `max_amount`,
         `min_reputation`, `action`, `reply_template`, `priority`, `enabled`,
         `sla_hours`, `deleted`, `create_time`, `update_time`)
    SELECT '演示-商品质量检测', 'QUALITY', 'ALWAYS', NULL, NULL,
           NULL, 'AUTO_REPLY', '请提供故障视频、商品序列号和外包装照片。', 20, 1,
           24, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `after_sale_policy` WHERE policy_name = '演示-商品质量检测'
    );
    INSERT INTO `after_sale_policy`
        (`policy_name`, `category`, `condition_type`, `min_amount`, `max_amount`,
         `min_reputation`, `action`, `reply_template`, `priority`, `enabled`,
         `sla_hours`, `deleted`, `create_time`, `update_time`)
    SELECT '演示-安装服务预约', 'INSTALLATION', 'ALWAYS', NULL, NULL,
           NULL, 'AUTO_REPLY', '请提供可上门时间和安装地址所在区县。', 20, 1,
           48, 0, NOW(), NOW()
    WHERE NOT EXISTS (
        SELECT 1 FROM `after_sale_policy` WHERE policy_name = '演示-安装服务预约'
    );

    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_faq`;
    CREATE TEMPORARY TABLE `tmp_demo_faq` (
        `category` VARCHAR(30) NOT NULL,
        `question` VARCHAR(500) NOT NULL PRIMARY KEY,
        `answer` TEXT NOT NULL,
        `keywords` VARCHAR(500) DEFAULT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    INSERT INTO `tmp_demo_faq` (`category`, `question`, `answer`, `keywords`) VALUES
        ('REFUND', '演示：退款金额为什么与商品标价不同？', '退款以订单实际支付金额为准，优惠券、满减和运费会按平台规则分摊。', '退款金额,优惠券,满减,运费'),
        ('REFUND', '演示：已发货订单还能申请退款吗？', '可以提交售后申请；是否需要拒收或寄回商品，以审核结果为准。', '已发货,退款,拒收,退货'),
        ('LOGISTICS', '演示：物流显示签收但本人没有收到怎么办？', '请先核对家人、前台和驿站；仍未找到时，应转人工核查签收凭证。', '签收,未收到,驿站,配送凭证'),
        ('LOGISTICS', '演示：可以修改已经发货的收货地址吗？', '已发货订单通常无法直接修改地址，可联系承运商尝试改派。', '修改地址,改派,已发货'),
        ('DAMAGE', '演示：商品外包装破损但商品正常需要退货吗？', '建议先完整拍照留存；商品正常时可继续使用，如有隐患可提交人工核验。', '包装破损,拍照,退货'),
        ('INVOICE', '演示：电子发票抬头填写错误如何重开？', '请提供订单号、原发票号码和正确抬头，客服核验后发起红冲重开。', '发票,抬头,重开,红冲'),
        ('QUALITY', '演示：电子产品出现间歇性故障如何举证？', '建议录制包含故障现象和设备序列号的视频，并说明复现步骤。', '质量,间歇故障,视频,序列号'),
        ('QUALITY', '演示：商品检测无故障是否需要承担运费？', '以订单售后政策和检测结论为准，提交前请保留寄回凭证。', '检测,无故障,运费,寄回'),
        ('INSTALLATION', '演示：上门安装前需要准备什么？', '请确认安装环境、电源、水路或墙体条件，并保持电话畅通。', '安装,上门,环境,预约'),
        ('OTHER', '演示：如何转接人工客服？', '可在 AI 客服页面选择“转人工 / 创建工单”，补充订单和问题信息后提交。', '人工客服,创建工单,转人工');

    INSERT INTO `faq`
        (`category`, `question`, `answer`, `keywords`, `enabled`, `deleted`, `create_time`, `update_time`)
    SELECT seed.category, seed.question, seed.answer, seed.keywords, 1, 0, NOW(), NOW()
    FROM `tmp_demo_faq` seed
    WHERE NOT EXISTS (
        SELECT 1 FROM `faq` existing WHERE existing.question = seed.question
    );

    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_chat`;
    CREATE TEMPORARY TABLE `tmp_demo_chat` (
        `session_no` VARCHAR(64) NOT NULL PRIMARY KEY,
        `username` VARCHAR(50) NOT NULL,
        `title` VARCHAR(200) NOT NULL,
        `question` TEXT NOT NULL,
        `answer` TEXT NOT NULL,
        `days_ago` INT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

    INSERT INTO `tmp_demo_chat`
        (`session_no`, `username`, `title`, `question`, `answer`, `days_ago`)
    VALUES
        ('DEMOCHAT202607001', 'demo_2026_chenxi', '查询我的订单', '请列出我最近的订单。', '我可以通过只读工具查询当前登录账号有权访问的订单。', 8),
        ('DEMOCHAT202607002', 'demo_2026_linan', '充电器退换咨询', '买错接口型号可以退换吗？', '是否支持退换需要结合订单状态、商品使用情况和售后策略判断。', 7),
        ('DEMOCHAT202607003', 'demo_2026_zhouyu', '物流预计到达时间', '我的摄影包什么时候能到？', '我会先读取该账号下对应订单的真实物流状态，再提供建议。', 6),
        ('DEMOCHAT202607004', 'demo_2026_sunyue', '预约上门安装', '智能门锁怎样预约安装？', '可以创建安装使用类工单，并提供可上门时间和所在区县。', 5),
        ('DEMOCHAT202607005', 'demo_2026_zhaolei', '退款进度查询', '帮我看看退款是否到账。', '请提供订单号，系统只会查询当前账号有权访问的退款信息。', 4),
        ('DEMOCHAT202607006', 'demo_2026_qianjing', '修改发票抬头', '电子发票抬头错了怎么办？', '可提交发票类工单，提供原发票号码和正确抬头后申请重开。', 3);

    INSERT INTO `ai_chat_session`
        (`session_no`, `user_id`, `title`, `create_time`, `update_time`)
    SELECT seed.session_no, user_data.id, seed.title,
           DATE_SUB(NOW(), INTERVAL seed.days_ago DAY),
           DATE_SUB(NOW(), INTERVAL seed.days_ago DAY)
    FROM `tmp_demo_chat` seed
    JOIN `sys_user` user_data ON user_data.username = seed.username
    WHERE NOT EXISTS (
        SELECT 1 FROM `ai_chat_session` existing WHERE existing.session_no = seed.session_no
    );

    INSERT INTO `ai_chat_message`
        (`session_id`, `sender_type`, `content`, `create_time`)
    SELECT session_data.id, 'USER', seed.question,
           DATE_SUB(NOW(), INTERVAL seed.days_ago DAY)
    FROM `tmp_demo_chat` seed
    JOIN `ai_chat_session` session_data ON session_data.session_no = seed.session_no
    WHERE NOT EXISTS (
        SELECT 1 FROM `ai_chat_message` existing
        WHERE existing.session_id = session_data.id
          AND existing.sender_type = 'USER'
          AND existing.content = seed.question
    );

    INSERT INTO `ai_chat_message`
        (`session_id`, `sender_type`, `content`, `create_time`)
    SELECT session_data.id, 'AI', seed.answer,
           DATE_ADD(DATE_SUB(NOW(), INTERVAL seed.days_ago DAY), INTERVAL 1 MINUTE)
    FROM `tmp_demo_chat` seed
    JOIN `ai_chat_session` session_data ON session_data.session_no = seed.session_no
    WHERE NOT EXISTS (
        SELECT 1 FROM `ai_chat_message` existing
        WHERE existing.session_id = session_data.id
          AND existing.sender_type = 'AI'
          AND existing.content = seed.answer
    );

    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_chat`;
    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_faq`;
    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_tickets`;
    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_orders`;
    DROP TEMPORARY TABLE IF EXISTS `tmp_demo_users`;

    COMMIT;
END$$

CALL `seed_rich_demo_data`()$$
DROP PROCEDURE `seed_rich_demo_data`$$

DELIMITER ;

SELECT 'demo users' AS `dataset`, COUNT(*) AS `row_count`
FROM `sys_user` WHERE `username` LIKE 'demo_2026_%'
UNION ALL
SELECT 'demo orders', COUNT(*) FROM `orders` WHERE `order_no` LIKE 'DEMO2026%'
UNION ALL
SELECT 'demo tickets', COUNT(*) FROM `ticket` WHERE `ticket_no` LIKE 'DEMO-TK-%'
UNION ALL
SELECT 'demo ticket messages', COUNT(*)
FROM `ticket_message` message_data
JOIN `ticket` ticket_data ON ticket_data.id = message_data.ticket_id
WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
UNION ALL
SELECT 'demo operation logs', COUNT(*)
FROM `ticket_operation_log` log_data
JOIN `ticket` ticket_data ON ticket_data.id = log_data.ticket_id
WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
UNION ALL
SELECT 'demo satisfactions', COUNT(*)
FROM `ticket_satisfaction` satisfaction_data
JOIN `ticket` ticket_data ON ticket_data.id = satisfaction_data.ticket_id
WHERE ticket_data.ticket_no LIKE 'DEMO-TK-%'
UNION ALL
SELECT 'demo chat sessions', COUNT(*) FROM `ai_chat_session`
WHERE `session_no` LIKE 'DEMOCHAT2026%'
UNION ALL
SELECT 'demo faq', COUNT(*) FROM `faq` WHERE `question` LIKE '演示：%'
UNION ALL
SELECT 'demo policies', COUNT(*) FROM `after_sale_policy`
WHERE `policy_name` LIKE '演示-%';
