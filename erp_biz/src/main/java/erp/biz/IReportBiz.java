package erp.biz;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IReportBiz {
	//销售统计
	@SuppressWarnings("rawtypes")
	List getReptor(Date start,Date end);
	//销售趋势
	List<Map<String,Object>> getTrend(int year);
}
