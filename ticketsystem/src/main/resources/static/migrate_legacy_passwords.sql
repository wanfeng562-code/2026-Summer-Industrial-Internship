-- 将老师早期快照中的演示明文密码 123456 升级为 BCrypt。
-- 该脚本只更新密码仍为明文 123456 的指定演示账号，不覆盖用户自行修改的密码。
USE `ticket_system`;

UPDATE `sys_user`
SET `password` = '$2b$10$ZaSqjiuijP4UKPqbH/2T8.EJVJFPn7Qf1YwHESo5keCR30GQHmNXi'
WHERE `username` = 'admin' AND `password` = '123456';

UPDATE `sys_user`
SET `password` = '$2b$10$9LF.oPgIEvuCQPNzVJDQI.Nki0T6zX8CLagpgr.K8BjREtEbK53Na'
WHERE `username` = 'agent_zhang' AND `password` = '123456';

UPDATE `sys_user`
SET `password` = '$2b$10$PReAO8zbJtsag.ld8A8qNultYtUWBFyITDwkPE0z.Jh2ebShWnmu2'
WHERE `username` = 'agent_li' AND `password` = '123456';

UPDATE `sys_user`
SET `password` = '$2b$10$j3VTekfAr6/Aud/D7YtX7e.pypTMolHf86xu3RGs4p.SNDaZn0KxK'
WHERE `username` = 'user_wang' AND `password` = '123456';

UPDATE `sys_user`
SET `password` = '$2b$10$rMWF6TQfbsAyBISNkVujQenY2kz.ob5wK.u5/m9UCYJDbjLx8Fzoe'
WHERE `username` = 'user_liu' AND `password` = '123456';

UPDATE `sys_user`
SET `password` = '$2b$10$qavieAdPwTiSMYXWfa2Vm.NjIPWgzz1.s4QjCr4uFJBufjA4qB4XK'
WHERE `username` = 'user_chen' AND `password` = '123456';
