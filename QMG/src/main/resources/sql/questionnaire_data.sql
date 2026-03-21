-- ============================================
-- 问卷配置数据初始化脚本
-- 包含13项QMG评分的所有项目和选项数据
-- ============================================

-- 清空现有数据（可选，首次初始化时使用）
-- TRUNCATE TABLE `questionnaire_option`;
-- TRUNCATE TABLE `questionnaire_item`;

-- ============================================
-- 眼肌类 (3项)
-- ============================================

-- 1. 左右侧视出现复视
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('左右侧视出现复视', 'diplopia', 'eyes', 1);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('diplopia', '≥61秒', '>=61', 0, 1),
('diplopia', '11~60秒', '11-60', 1, 2),
('diplopia', '1~10秒', '1-10', 2, 3),
('diplopia', '自发', '0', 3, 4);

-- 2. 上视出现眼睑下垂
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('上视出现眼睑下垂', 'ptosis', 'eyes', 2);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('ptosis', '≥61秒', '>=61', 0, 1),
('ptosis', '11~60秒', '11-60', 1, 2),
('ptosis', '1~10秒', '1-10', 2, 3),
('ptosis', '自发', '0', 3, 4);

-- 3. 眼睑闭合
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('眼睑闭合', 'eyelidClosure', 'eyes', 3);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('eyelidClosure', '正常', 'normal', 0, 1),
('eyelidClosure', '闭合时可抵抗部分阻力', 'partial', 1, 2),
('eyelidClosure', '闭合时不能抵抗阻力', 'no-resistance', 2, 3),
('eyelidClosure', '不能闭合', 'unable', 3, 4);

-- ============================================
-- 球部肌类 (2项)
-- ============================================

-- 4. 吞咽100mL水
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('吞咽100mL水', 'swallowing', 'bulbar', 4);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('swallowing', '正常', 'normal', 0, 1),
('swallowing', '轻度呛咳', 'mild', 1, 2),
('swallowing', '严重呛咳或鼻腔反流', 'severe', 2, 3),
('swallowing', '不能完成', 'unable', 3, 4);

-- 5. 数数1~50（观察构音障碍）
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('数数1~50（观察构音障碍）', 'counting', 'bulbar', 5);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('counting', '无构音障碍', '50', 0, 1),
('counting', '30~49', '30-49', 1, 2),
('counting', '10~29', '10-29', 2, 3),
('counting', '0~9', '0-9', 3, 4);

-- ============================================
-- 呼吸类 (1项)
-- ============================================

-- 6. 肺活量预计值
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('肺活量预计值', 'vitalCapacity', 'respiratory', 6);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('vitalCapacity', '≥80%', '>=80', 0, 1),
('vitalCapacity', '65-79%', '65-79', 1, 2),
('vitalCapacity', '50-64%', '50-64', 2, 3),
('vitalCapacity', '<50%', '<50', 3, 4);

-- ============================================
-- 肢体肌类 (7项)
-- ============================================

-- 7. 坐位右上肢抬起90°时间
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('坐位右上肢抬起90°时间', 'armRaiseRight', 'limbs', 7);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('armRaiseRight', '≥240秒', '>=240', 0, 1),
('armRaiseRight', '90~239秒', '90-239', 1, 2),
('armRaiseRight', '10~89秒', '10-89', 2, 3),
('armRaiseRight', '0~9秒', '0-9', 3, 4);

-- 8. 坐位左上肢抬起90°时间
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('坐位左上肢抬起90°时间', 'armRaiseLeft', 'limbs', 8);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('armRaiseLeft', '≥240秒', '>=240', 0, 1),
('armRaiseLeft', '90~239秒', '90-239', 1, 2),
('armRaiseLeft', '10~89秒', '10-89', 2, 3),
('armRaiseLeft', '0~9秒', '0-9', 3, 4);

-- 9. 右手握力
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('右手握力', 'gripStrengthRight', 'limbs', 9);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('gripStrengthRight', '≥45kg(男)/≥30kg(女)', 'high', 0, 1),
('gripStrengthRight', '15~44kg(男)/10~29kg(女)', 'medium', 1, 2),
('gripStrengthRight', '5~14kg(男)/5~9kg(女)', 'low', 2, 3),
('gripStrengthRight', '0~4kg', 'very-low', 3, 4);

-- 10. 左手握力
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('左手握力', 'gripStrengthLeft', 'limbs', 10);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('gripStrengthLeft', '≥35kg(男)/≥25kg(女)', 'high', 0, 1),
('gripStrengthLeft', '15~34kg(男)/10~24kg(女)', 'medium', 1, 2),
('gripStrengthLeft', '5~14kg(男)/5~9kg(女)', 'low', 2, 3),
('gripStrengthLeft', '0~4kg', 'very-low', 3, 4);

-- 11. 平卧位抬头45°
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('平卧位抬头45°', 'headLift', 'limbs', 11);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('headLift', '≥120秒', '>=120', 0, 1),
('headLift', '30~119秒', '30-119', 1, 2),
('headLift', '1~29秒', '1-29', 2, 3),
('headLift', '0秒', '0', 3, 4);

-- 12. 平卧位右下肢抬起45°
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('平卧位右下肢抬起45°', 'legRaiseRight', 'limbs', 12);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('legRaiseRight', '≥100秒', '>=100', 0, 1),
('legRaiseRight', '31~99秒', '31-99', 1, 2),
('legRaiseRight', '1~30秒', '1-30', 2, 3),
('legRaiseRight', '0秒', '0', 3, 4);

-- 13. 平卧位左下肢抬起45°
INSERT INTO `questionnaire_item` (`name`, `key`, `category`, `display_order`) VALUES
('平卧位左下肢抬起45°', 'legRaiseLeft', 'limbs', 13);

INSERT INTO `questionnaire_option` (`item_key`, `label`, `value`, `score`, `display_order`) VALUES
('legRaiseLeft', '≥100秒', '>=100', 0, 1),
('legRaiseLeft', '31~99秒', '31-99', 1, 2),
('legRaiseLeft', '1~30秒', '1-30', 2, 3),
('legRaiseLeft', '0秒', '0', 3, 4);
