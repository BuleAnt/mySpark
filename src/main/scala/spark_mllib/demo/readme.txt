PV点击日志 t_ods_f_client_clieck							
序号	字段说明	    参考字段	 字段长度	 是否为必填	说明
1	入口编码	    TRANS_ID  	<=50	Y	1,2,3，	160161001	_bamboo_rep_transid
2	入口位置名称	ACT_CODE	<=50	Y	网厅首页-待定	网厅5.0门楣广告	_bamboo_rep_actcode
3	类型	        UP_TYPE	   <=200	Y	导航，广告，按钮功能；收藏；搜索；购物车；公告；	广告	_bamboo_rep_uptype
4	菜单ID	    MENUID	   <=100	N	指导航ID【参见客户端导航ID字典表】；广告ID；	xxxxxxxx	_bamboo_rep_menuid
5	title名称	TITLE_NAME	<=200	Y	导航菜单名称或title	_bamboo_rep_title	_bamboo_rep_title
6	用户号码   	MOBILE	   <=32 	N
7	省份编码	    PROV_ID	   <=5  	N	参见《省份字典表》
8	地市编码	    CITY_ID	   <=3  	Y	用户号码对应的地市编码
9	点击时间	    UP_TIME	   <=20 	Y	格式:yyyymmddhh24miss（记日志时间）24小时
10	客户端版本	VERSION	   <=20 	N	2.0;3.0;5.0
11	客户端类型	CLIENT_TYPE	<=100	N	Android ；IOS ；		
12	目标URL	    URL_APP	   <=500	N
13	备注1	    REMARK1	   <=50 	N	渠道编码（网厅5.0）
14	备注3	    REMARK3	   <=50 	N	N
15	备注4	    REMARK4	   <=50 	N	N
16	承载主机IP地址	BIZ_HOSTIP	<=100	Y	处理当前业务应用程序的主机IP地址		
17	承载节点名称	BIZ_PROECESS	<=100	Y	处理当前业务应用程序的节点名称		
