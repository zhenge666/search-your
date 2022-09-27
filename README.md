# search-your
演示ES自定义注解式使用方法，在API入参注解参数即可实现ES查询，省去ES的学习成本和语法错误。同样的思路也可以用于数据库查询，对于开发效率和运行稳定性都是一种很大的提升。
<br/>
接入数据库binlog实现数据库的实时同步，保障了ES数据的写入准确性。
<br/>
Job服务则提供了历史表的初始化任务。
<br/>
# 匹配查询
<br/>TERMS(1, "精确匹配"),
<br/>MATCH(2, "分词匹配"),
<br/>RANGE_GE(3, "大于等于"),
<br/>RANGE_LE(4, "小于等于"),
<br/>RANGE_GT(5, "大于"),
<br/>RANGE_LT(6, "小于"),
<br/>HAVE_OR_NO(7, "0表示没有:=0，1表示有:>0"),
<br/>IDS(8, "多id查询"),
<br/>TERMS_FILTER(9, "not in过滤"),
<br/>
# 聚合查询
<br/>MAX("esMax", "最大值"),
<br/>MIN("esMin", "最小值"),
<br/>AVG("esAvg", "平均值"),
<br/>SUM("esSum", "和"),
<br/>COUNT("esCount", "数量"),
<br/>STATS("esStats", "小于等于"),
<br/>GROUP("esGroup", "分组"),
<br/>Having("esGroupHaving", "分组后筛选数量"),
