package erp.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import erp.biz.IReportBiz;
import erp.dao.IReportDao;

public class ReportBiz implements IReportBiz {

	private IReportDao reportDao;
	
	public void setReportDao(IReportDao reportDao) {
		this.reportDao = reportDao;
	}
	/**
	 * 销售统计
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getReptor(Date start, Date end) {
		return reportDao.getReport(start, end);
	}
	/**
	 * 销售趋势
	 */
	@Override
	public List<Map<String, Object>> getTrend(int year) {
		//查询出的
		List<Map<String, Object>> list = reportDao.getTrend(year);
		//存储查询出的容器，用于判断
		List<Object> temp=new ArrayList<Object>();
		//数据处理后存储的容器
		List<Map<String, Object>> list1=new ArrayList<Map<String,Object>>();
		//存储查询出的月份值，用于判断
		for(Map<String, Object> m: list){
			temp.add(m.get("name"));
		}
		//遍历12个月
		for(int i=1,j=0;i<=12;i++){
			//如果当月没结果
			if(!temp.contains(i+"月份")){
				Map<String,Object> map=new HashMap<String, Object>();
				map.put("name", i+"月份");
				map.put("y", 0);
				list1.add(map);
			//当月有结果
			}else{
				list1.add(list.get(j++));
			}
		}	
		return list1;
	}
}
