-- 养老资讯内容包：健康养生、政策解读、社区活动各新增 5 篇。
-- 可重复执行；相同标题不会再次插入。
SET @publisher_id = COALESCE((SELECT id FROM `user` WHERE role = 'ADMIN' ORDER BY id LIMIT 1), 1);

INSERT INTO news (title, content, summary, cover_image, news_type, publisher_id, status)
SELECT v.title, v.content, v.summary, v.cover_image, v.news_type, @publisher_id, 1
FROM (
  SELECT '老年人每天怎么吃更健康' AS `title`,
    '<p>老年人饮食要尽量做到食物多样、粗细搭配，并保证优质蛋白质和蔬果摄入。食欲较弱时可少量多餐，同时关注体重变化。</p><p>如患有糖尿病、肾病等慢性病，应听从医生或营养师的个体化建议。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202210/8a11962d784540d7a61ef883948f768e.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委《老年营养改善行动》</a></p><p>本文为便于阅读的摘要整理，具体内容以官方原文为准。</p>' AS `content`,
    '从食物多样、优质蛋白和体重管理三个方面，掌握老年营养要点。' AS `summary`,
    '/images/news/elderly-park-walk.jpg' AS `cover_image`, 'HEALTH' AS `news_type`
  UNION ALL SELECT '预防跌倒，先检查家里这几个地方',
    '<p>老年人跌倒与视力、用药、肌力和居家环境等多种因素有关。建议清理过道杂物，卫生间安装扶手和防滑垫，夜间保留照明，并选择合脚防滑的鞋。</p><p>如果近期反复头晕或跌倒，应及时就医评估。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/xcs/c100122/202604/e51230abcee348bea75d5ef456f26014.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委新闻发布会</a></p><p>本文为摘要整理，以官方原文为准。</p>',
    '从居家环境、鞋具、身体状况等方面降低跌倒风险。', '/images/news/elderly-community-fitness.jpg', 'HEALTH'
  UNION ALL SELECT '科学运动：从能坚持的强度开始',
    '<p>运动应量力而行，可把步行、太极拳等有氧活动与简单力量、平衡练习结合起来。开始前先热身，感觉胸痛、明显气短或眩晕时应停止并求助。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202201/96f260cf07684d90840484de01ca97da.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委《关于全面加强老年健康服务工作的通知》</a></p><p>本文为摘要整理，以官方原文为准。</p>',
    '适合老年人的运动原则：循序渐进、兼顾力量和平衡。', '/images/news/elderly-exercise.jpg', 'HEALTH'
  UNION ALL SELECT '听力下降不要硬扛，及时筛查和干预',
    '<p>听力下降可能影响交流、情绪和社会参与。出现听不清谈话、经常调高电视音量等情况，可先到正规医疗机构检查，再按专业意见选择治疗或助听设备。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202406/11ea15355b3c42d5b32362c66bf04eed.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委《老年听力健康促进行动》</a></p><p>本文为摘要整理，以官方原文为准。</p>',
    '识别听力下降信号，了解筛查、就医与助听干预。', '/images/news/elderly-park-walk.jpg', 'HEALTH'
  UNION ALL SELECT '改善睡眠，先从规律作息做起',
    '<p>建议每天尽量固定起床和入睡时间，白天适当活动，午睡不宜过长，晚上少饮浓茶和咖啡。若打鼾伴憋气、长期失眠或白天嗜睡，应就医排查。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/xcs/c100122/202604/e51230abcee348bea75d5ef456f26014.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委新闻发布会</a></p><p>本文为摘要整理，以官方原文为准。</p>',
    '用规律作息、日间活动和正确就医改善老年睡眠。', '/images/news/elderly-exercise.jpg', 'HEALTH'

  UNION ALL SELECT '养老服务改革：居家、社区、机构如何衔接',
    '<p>相关意见提出健全县、乡镇（街道）、村（社区）三级养老服务网络，形成居家为基础、社区为依托、机构为专业支撑、医养相结合的供给格局。</p><p><strong>资料来源：</strong><a href="https://www.beijing.gov.cn/zhengce/gwywj/202501/t20250108_3983528.html" target="_blank" rel="noopener noreferrer">《中共中央 国务院关于深化养老服务改革发展的意见》</a></p><p>本文为政策摘要，不替代当地部门解释。</p>',
    '读懂养老服务三级网络以及居家、社区、机构的分工。', '/images/news/elderly-community-fitness.jpg', 'POLICY'
  UNION ALL SELECT '普惠养老服务重点支持什么',
    '<p>政策强调扩大价格可负担、质量有保障、运营可持续的养老服务供给，并完善服务标准、评价体系和志愿服务。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202502/6fbe2df9bfa84d75ae27c52193a15555.shtml" target="_blank" rel="noopener noreferrer">国家发展改革委等部门《促进普惠养老服务高质量发展的若干措施》</a></p><p>本文为政策摘要，以官方原文和当地执行细则为准。</p>',
    '了解普惠养老在供给、价格、质量评价方面的政策方向。', '/images/news/elderly-park-walk.jpg', 'POLICY'
  UNION ALL SELECT '中度以上失能老年人养老服务消费补贴解读',
    '<p>国家部署向符合条件的中度以上失能老年人发放养老服务消费补贴，具体对象认定、服务范围和申领方式应以所在地民政部门最新通知为准。</p><p><strong>资料来源：</strong><a href="https://policy.mofcom.gov.cn/claw/clawContent.shtml?id=103439" target="_blank" rel="noopener noreferrer">民政部、财政部相关通知</a></p><p>本文为政策摘要，不构成申领资格认定。</p>',
    '快速了解失能老年人养老服务消费补贴的对象与查询渠道。', '/images/news/elderly-exercise.jpg', 'POLICY'
  UNION ALL SELECT '社区嵌入式服务设施能提供哪些帮助',
    '<p>社区嵌入式服务设施可在居民适宜步行范围内提供养老、助餐、家政、健康、健身和文化休闲等服务，让基本服务更靠近居民。</p><p><strong>资料来源：</strong><a href="https://app.www.gov.cn/govdata/gov/202311/26/509708/article.html" target="_blank" rel="noopener noreferrer">国务院办公厅相关实施方案</a></p><p>各地建设进度和开放项目请咨询所在社区。</p>',
    '了解家门口的助餐、健康、养老和文化休闲服务。', '/images/news/elderly-community-fitness.jpg', 'POLICY'
  UNION ALL SELECT '我国老龄事业发展数据在哪里查',
    '<p>国家发布的老龄事业发展公报汇总人口老龄化、社会保障、养老服务和健康服务等情况，可作为了解行业发展和撰写论文的数据来源。</p><p><strong>资料来源：</strong><a href="https://www.gov.cn/lianbo/bumen/202507/P020250725463987010460.pdf" target="_blank" rel="noopener noreferrer">《2024年度国家老龄事业发展公报》</a></p><p>本文为阅读提示，数据引用请以公报原表述为准。</p>',
    '一份了解老龄人口、保障和养老服务建设情况的官方资料。', '/images/news/elderly-park-walk.jpg', 'POLICY'

  UNION ALL SELECT '社区太极与平衡训练体验活动',
    '<p>活动建议安排热身、基础动作和平衡练习，并由志愿者协助高龄参与者。参加前请根据自身情况选择强度，携带水杯和常用药。</p><p><strong>活动依据：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202201/96f260cf07684d90840484de01ca97da.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委老年健康服务相关要求</a></p><p>演示活动的具体时间、地点以社区通知为准。</p>',
    '适合老人参与的低强度太极和平衡训练活动示例。', '/images/news/elderly-exercise.jpg', 'ACTIVITY'
  UNION ALL SELECT '银龄志愿服务：结伴探访与智能手机互助',
    '<p>社区可组织低龄健康老人参与结伴探访、智能手机教学和精神慰藉等志愿服务。活动应落实报名、培训、签到和紧急联系人机制。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202502/6fbe2df9bfa84d75ae27c52193a15555.shtml" target="_blank" rel="noopener noreferrer">促进普惠养老服务高质量发展的若干措施</a></p><p>本条为可落地的社区活动模板。</p>',
    '鼓励老人互助参与社区服务，同时建立安全保障机制。', '/images/news/elderly-community-fitness.jpg', 'ACTIVITY'
  UNION ALL SELECT '社区健康讲堂：防跌倒与居家安全',
    '<p>讲堂可带领老人识别卫生间湿滑、夜间照明不足、地毯卷边等风险，并现场演示扶手、防滑垫和紧急联系卡的用法。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/xcs/c100122/202604/e51230abcee348bea75d5ef456f26014.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委新闻发布会</a></p><p>本条为活动方案，实际安排以社区通知为准。</p>',
    '通过现场演示帮助老人排查居家跌倒隐患。', '/images/news/elderly-park-walk.jpg', 'ACTIVITY'
  UNION ALL SELECT '长者健康步行日：科学运动不攀比',
    '<p>活动路线宜平坦、设休息点并配备志愿者。参与者按自己的速度行走，不追求名次；有胸痛、眩晕等不适应立即停止并求助。</p><p><strong>资料来源：</strong><a href="https://www.nhc.gov.cn/lljks/c100158/202201/96f260cf07684d90840484de01ca97da.shtml" target="_blank" rel="noopener noreferrer">国家卫生健康委老年健康服务相关要求</a></p><p>本条为活动方案，报名以社区实际通知为准。</p>',
    '设置休息点和志愿者陪同的适老步行活动示例。', '/images/news/elderly-park-walk.jpg', 'ACTIVITY'
  UNION ALL SELECT '社区助餐开放日：看菜单、问价格、学预约',
    '<p>开放日可展示一周菜单、收费标准和特殊饮食提示，现场帮助老人学习电话或线上预约，并向独居、高龄和失能老人说明送餐服务。</p><p><strong>资料来源：</strong><a href="https://fgw.beijing.gov.cn/fgwzwgk/2024zcwj/bwgfxwj/202501/t20250121_3995487.htm" target="_blank" rel="noopener noreferrer">社区嵌入式服务设施项目建设运营管理办法</a></p><p>本条为活动模板，服务范围以当地社区为准。</p>',
    '帮助老人一次看懂社区助餐菜单、收费和预约方式。', '/images/news/elderly-community-fitness.jpg', 'ACTIVITY'
) v
WHERE NOT EXISTS (SELECT 1 FROM news n WHERE n.title = v.title);

-- 为内容包中的 15 篇文章分配互不重复的本地配图；重复执行也会保持一致。
UPDATE news SET cover_image = CASE title
  WHEN '老年人每天怎么吃更健康' THEN '/images/news/healthy-meal.jpg'
  WHEN '预防跌倒，先检查家里这几个地方' THEN '/images/news/elderly-community-fitness.jpg'
  WHEN '科学运动：从能坚持的强度开始' THEN '/images/news/elderly-exercise.jpg'
  WHEN '听力下降不要硬扛，及时筛查和干预' THEN '/images/news/health-check.jpg'
  WHEN '改善睡眠，先从规律作息做起' THEN '/images/news/senior-learning.jpg'
  WHEN '养老服务改革：居家、社区、机构如何衔接' THEN '/images/news/policy-documents.jpg'
  WHEN '普惠养老服务重点支持什么' THEN '/images/news/policy-planning.jpg'
  WHEN '中度以上失能老年人养老服务消费补贴解读' THEN '/images/news/policy-reading.jpg'
  WHEN '社区嵌入式服务设施能提供哪些帮助' THEN '/images/news/digital-service.jpg'
  WHEN '我国老龄事业发展数据在哪里查' THEN '/images/news/news-reading.jpg'
  WHEN '社区太极与平衡训练体验活动' THEN '/images/news/group-exercise.jpg'
  WHEN '银龄志愿服务：结伴探访与智能手机互助' THEN '/images/news/service-consultation.jpg'
  WHEN '社区健康讲堂：防跌倒与居家安全' THEN '/images/news/chair-exercise.jpg'
  WHEN '长者健康步行日：科学运动不攀比' THEN '/images/news/elderly-park-walk.jpg'
  WHEN '社区助餐开放日：看菜单、问价格、学预约' THEN '/images/news/community-meal.jpg'
  ELSE cover_image
END
WHERE title IN (
  '老年人每天怎么吃更健康','预防跌倒，先检查家里这几个地方','科学运动：从能坚持的强度开始','听力下降不要硬扛，及时筛查和干预','改善睡眠，先从规律作息做起',
  '养老服务改革：居家、社区、机构如何衔接','普惠养老服务重点支持什么','中度以上失能老年人养老服务消费补贴解读','社区嵌入式服务设施能提供哪些帮助','我国老龄事业发展数据在哪里查',
  '社区太极与平衡训练体验活动','银龄志愿服务：结伴探访与智能手机互助','社区健康讲堂：防跌倒与居家安全','长者健康步行日：科学运动不攀比','社区助餐开放日：看菜单、问价格、学预约'
);

SELECT news_type, COUNT(*) AS article_count
FROM news
WHERE title IN (
  '老年人每天怎么吃更健康','预防跌倒，先检查家里这几个地方','科学运动：从能坚持的强度开始','听力下降不要硬扛，及时筛查和干预','改善睡眠，先从规律作息做起',
  '养老服务改革：居家、社区、机构如何衔接','普惠养老服务重点支持什么','中度以上失能老年人养老服务消费补贴解读','社区嵌入式服务设施能提供哪些帮助','我国老龄事业发展数据在哪里查',
  '社区太极与平衡训练体验活动','银龄志愿服务：结伴探访与智能手机互助','社区健康讲堂：防跌倒与居家安全','长者健康步行日：科学运动不攀比','社区助餐开放日：看菜单、问价格、学预约'
)
GROUP BY news_type;
