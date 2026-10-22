insert into sys_user(username,password_hash,real_name,phone,role,status) values
('student','{noop}123456','张同学','13800000001','USER',1),('liutongxue','{noop}123456','刘同学','13800000006','USER',1),('admin','{noop}123456','系统管理员','13800000003','SYSTEM_ADMIN',1);
insert into sys_user(username,password_hash,real_name,phone,role,status) values
('labadmin1','{noop}123456','李老师','13800000011','LAB_ADMIN',1),
('labadmin2','{noop}123456','王老师','13800000012','LAB_ADMIN',1),
('labadmin3','{noop}123456','周老师','13800000013','LAB_ADMIN',1);
insert into lab(name,location,capacity,manager,phone,description,open_time,close_time,open_status) values
('软件工程实验室','科技楼A301',60,'李老师','13800000011','用于软件开发、课程实验与项目训练','08:00','21:30',1),
('人工智能实验室','科技楼B405',40,'李老师','13800000011','配备GPU工作站，支持机器学习实验','08:30','20:30',1),
('物联网创新实验室','实验楼C208',32,'王老师','13800000012','用于嵌入式、传感器和物联网实践','09:00','18:00',1);
insert into lab(name,location,capacity,manager,phone,description,open_time,close_time,open_status) values
('机器人与智能制造实验室','实验楼D212',36,'王老师','13800000012','用于机器人控制与智能制造实践','08:30','20:00',1),
('网络安全实验室','科技楼E503',30,'周老师','13800000013','用于网络安全教学和攻防实验','09:00','21:00',1);
insert into lab_admin_assignment(user_id,lab_id) values
(4,1),(4,2),(5,3),(5,4),(6,5);
insert into device(device_code,name,category,model,lab_id,purchase_date,status,remark) values
('PC-A301-01','开发工作站01','计算机','Dell Precision 3660',1,'2025-03-15','NORMAL','i7/32GB/1TB'),
('GPU-B405-01','深度学习服务器','服务器','Inspur NF5180M6',2,'2025-05-20','NORMAL','双GPU'),
('IOT-C208-01','物联网实验箱','开发板','ZigBee Pro',3,'2024-09-01','FAULT','传感器接口异常'),
('PJ-A301-01','激光投影仪','投影设备','Epson CB-L630U',1,'2024-06-12','NORMAL','');
insert into reservation(applicant_id,lab_id,reserve_date,start_time,end_time,purpose,participants,status,created_at) values
(1,1,dateadd('DAY',2,current_date),'14:00','16:00','软件工程课程小组讨论',12,'PENDING',current_timestamp),
(1,2,dateadd('DAY',5,current_date),'09:00','11:00','机器学习模型训练',8,'APPROVED',current_timestamp);
insert into repair_order(device_id,reporter_id,fault_desc,severity,status,created_at) values(3,1,'实验箱上电后传感器接口无响应','MEDIUM','PENDING',current_timestamp);
