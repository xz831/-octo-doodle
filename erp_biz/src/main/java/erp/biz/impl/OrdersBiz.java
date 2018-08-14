package erp.biz.impl;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;

import erp.biz.IOrdersBiz;
import erp.biz.exception.ERPException;
import erp.dao.IEmpDao;
import erp.dao.IOrdersDao;
import erp.dao.IReturnordersDao;
import erp.dao.ISupplierDao;
import erp.entity.Orderdetail;
import erp.entity.Orders;
import erp.entity.Returnorderdetail;
import erp.entity.Returnorders;
/**
 * 订单业务逻辑类
 * @author Administrator
 *
 */
public class OrdersBiz extends BaseBiz<Orders> implements IOrdersBiz {

	private IOrdersDao ordersDao;
	private IEmpDao empDao;
	private ISupplierDao supplierDao;
	private IReturnordersDao returnordersDao;

	
	public void setOrdersDao(IOrdersDao ordersDao) {
		this.ordersDao = ordersDao;
		super.setBaseDao(this.ordersDao);
	}
	
	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}

	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}
	
	public void setReturnordersDao(IReturnordersDao returnordersDao) {
		this.returnordersDao = returnordersDao;
	}

	/**
	 * 添加订单
	 */
	public void add(Orders orders){
		Subject subject=SecurityUtils.getSubject();
		if(orders.getType().equals(Orders.TYPE_IN)){
			if(!subject.isPermitted("我的采购订单")){
				throw new ERPException("权限不足");
			}
		}else if(orders.getType().equals(Orders.TYPE_OUT)){
			if(!subject.isPermitted("我的销售订单")){
				throw new ERPException("权限不足");
			}
		}else{
			throw new ERPException("非法参数");
		}		
		if(orders.getType().equals(Orders.TYPE_IN)){
			orders.setState(Orders.STATE_CREATE);
		}else if(orders.getType().equals(Orders.TYPE_OUT)){
			orders.setState(Orders.STATE_NOT_OUT);
		}		
		orders.setType(orders.getType());
		orders.setCreatetime(new Date());
		double total=0d;
		for(Orderdetail od:orders.getOrderdetails()){
			total+=od.getMoney();
			od.setState(Orderdetail.STATE_NOT_IN);
			od.setOrders(orders);
		}
		orders.setTotalmoney(total);
		ordersDao.add(orders);
	}
	/**
	 * 查询方法
	 */
	public List<Orders> getList(Orders o,Object params,int page,int rows){
		List<Orders> list=super.getList(o, params, page, rows);
		Map<Long,String> empName=new HashMap<Long, String>();
		Map<Long,String> supplierName=new HashMap<Long, String>();
		for(Orders order:list){
			order.setCheckerName(getEmpName(order.getChecker(), empName,empDao));
			order.setCreaterName(getEmpName(order.getCreater(), empName,empDao));
			order.setEnderName(getEmpName(order.getEnder(), empName,empDao));
			order.setStarterName(getEmpName(order.getStarter(), empName,empDao));
			order.setSupplierName(getSupplierName(order.getSupplieruuid(), supplierName,supplierDao));
		}	
		return list;
	}
	
	/**
	 * 审核
	 * oid 订单ID
	 * eid 员工ID
	 */
	@Override
	@RequiresPermissions("采购订单审核")
	public void doCheck(Long oid, Long eid) {
		Orders orders=ordersDao.find(oid);
		if(orders==null){
			throw new ERPException("订单信息错误");
		}
		if(!Orders.STATE_CREATE.equals(orders.getState())){
			throw new ERPException("订单不是未审核状态");
		}
		orders.setChecker(eid);
		orders.setChecktime(new Date());
		orders.setState(Orders.STATE_CHECK);
	}
	/**
	 * 确认
	 * oid 订单ID
	 * eid 员工ID
	 */
	@Override
	@RequiresPermissions("采购订单确认")
	public void doStart(Long oid, Long eid) {
		Orders orders=ordersDao.find(oid);
		if(orders==null){
			throw new ERPException("订单信息错误");
		}
		if(!Orders.STATE_CHECK.equals(orders.getState())){
			throw new ERPException("订单不是已审核状态");
		}
		orders.setStarter(eid);
		orders.setStarttime(new Date());
		orders.setState(Orders.STATE_START);
	}
	/**
	 * 退货订单查询
	 */
	@Override
	public List<Orders> listByReturn(Orders orders,Object params,int page,int rows) {
		List<Orders> list=getList(orders, params, page, rows);
		if(list!=null){
			for(Orders o:list){
				//每个订单对应的多个退货单
				Returnorders ro=new Returnorders();
				ro.setOrdersuuid(o.getUuid());
				List<Returnorders> list2=returnordersDao.getList(ro, params, page, rows);
				//如果有退货单
				if(list2!=null){
					//创建容器 第一个参数为商品ID，第二个参数为商品数量
					Map<Long,Long> map=new HashMap<Long, Long>();
					for(Returnorders r:list2){
						for(Returnorderdetail rod:r.getReturnorderdetail()){
							if(map.get(rod.getGoodsuuid())==null){
								map.put(rod.getGoodsuuid(), rod.getReturnnum());
							}else{
								map.put(rod.getGoodsuuid(), map.get(rod.getGoodsuuid())+rod.getReturnnum());
							}				
						}
					}
					for(Orderdetail od:o.getOrderdetails()){
						//如果map中有退货,就设置退货数量
						if(map.containsKey(od.getGoodsuuid())){
							od.setNum2(map.get(od.getGoodsuuid()));
						}else{
							od.setNum2((long) 0);
						}
					}
					
				}
			}
		}
		return list;
	}
	
	/**
	 * 导出
	 */
	@Override
	public void export(OutputStream os, Long uuid) {
		//查找订单
		Orders orders=ordersDao.find(uuid);
		if(orders==null){
			throw new ERPException("订单不存在");
		}
		//创建工作簿
		HSSFWorkbook wb=new HSSFWorkbook();
		//创建表
		HSSFSheet sheet=null;
		if(orders.getType().equals(Orders.TYPE_IN)){
			sheet=wb.createSheet("采购订单");
		}else{
			sheet=wb.createSheet("销售订单");
		}	
		//创建内容字体
		HSSFFont font_content=wb.createFont();
		font_content.setFontName("宋体");
		font_content.setFontHeightInPoints((short) 11);
		//创建标题字体
		HSSFFont font_title=wb.createFont();
		font_title.setFontName("黑体");
		font_title.setFontHeightInPoints((short) 18);
		font_title.setColor(HSSFColor.GREEN.index);
		
		
		//定义一般单元格样式
		HSSFCellStyle style_content=wb.createCellStyle();
		style_content.setBorderBottom(BorderStyle.THIN);
		style_content.setBorderLeft(BorderStyle.THIN);
		style_content.setBorderTop(BorderStyle.THIN);
		style_content.setBorderRight(BorderStyle.THIN);
		//水平居中
		style_content.setAlignment(HorizontalAlignment.CENTER);
		//垂直居中
		style_content.setVerticalAlignment(VerticalAlignment.CENTER);
		//字体
		style_content.setFont(font_content);
		
		//定义标题样式
		HSSFCellStyle style_title=wb.createCellStyle();
		//水平居中
		style_title.setAlignment(HorizontalAlignment.CENTER);
		//垂直居中
		style_title.setVerticalAlignment(VerticalAlignment.CENTER);	
		//字体
		style_title.setFont(font_title);
		
		//设置日期样式
		HSSFCellStyle style_date=wb.createCellStyle();
		style_date.cloneStyleFrom(style_content);
		HSSFDataFormat df=wb.createDataFormat();
		style_date.setDataFormat(df.getFormat("yyyy-MM-dd HH:mm"));
		
		//创建姓名缓存
		Map<Long,String> map=new HashMap<Long, String>();
		
		int rowcount=-1;
		//采购单
		if(orders.getType().equals(Orders.TYPE_IN)){
			//初始化row
			HSSFRow row=null;		
			//第二至八行
			for(int i=1;i<8;i++){
				row = sheet.createRow(i);
				row.setHeight((short) 500);
				for(int j=0;j<4;j++){
					row.createCell(j).setCellStyle(style_content);
					sheet.setColumnWidth(j, 5000);				
				}
			}
			//1
			row=sheet.createRow(0);
			row.setHeight((short) 1000);
			row.createCell(0).setCellStyle(style_title);
			//设置表单固定的内容
					
			row.getCell(0).setCellValue("采购单");
			//2
			sheet.getRow(1).getCell(0).setCellValue("供应商");
			sheet.getRow(1).getCell(1).setCellValue(supplierDao.find(orders.getSupplieruuid()).getName());
			
			//3
			sheet.getRow(2).getCell(0).setCellValue("下单日期");
			if(orders.getCreatetime()!=null){
				sheet.getRow(2).getCell(1).setCellValue(orders.getCreatetime());
			}			
			sheet.getRow(2).getCell(2).setCellValue("经办人");
			sheet.getRow(2).getCell(3).setCellValue(getEmpName(orders.getCreater(), map, empDao));
			
			//4
			sheet.getRow(3).getCell(0).setCellValue("审核日期");
			if(orders.getChecktime()!=null){
				sheet.getRow(3).getCell(1).setCellValue(orders.getChecktime());
			}
			sheet.getRow(3).getCell(2).setCellValue("经办人");
			sheet.getRow(3).getCell(3).setCellValue(getEmpName(orders.getChecker(), map, empDao));
			
			//5
			sheet.getRow(4).getCell(0).setCellValue("确认日期");
			if(orders.getStarttime()!=null){			
				sheet.getRow(4).getCell(1).setCellValue(orders.getStarttime());
			}
			sheet.getRow(4).getCell(2).setCellValue("经办人");
			sheet.getRow(4).getCell(3).setCellValue(getEmpName(orders.getStarter(), map, empDao));
			
			//6
			sheet.getRow(5).getCell(0).setCellValue("入库日期");
			if(orders.getEndtime()!=null){
				sheet.getRow(5).getCell(1).setCellValue(orders.getEndtime());
			}
			sheet.getRow(5).getCell(2).setCellValue("经办人");
			sheet.getRow(5).getCell(3).setCellValue(getEmpName(orders.getEnder(), map, empDao));
			
			//7
			sheet.getRow(6).getCell(0).setCellValue("订单详情");
			
			//8
			sheet.getRow(7).getCell(0).setCellValue("商品名称");
			sheet.getRow(7).getCell(1).setCellValue("数量");
			sheet.getRow(7).getCell(2).setCellValue("价格");
			sheet.getRow(7).getCell(3).setCellValue("金额");
			rowcount=8;
			//设置日期样式
			sheet.getRow(2).getCell(1).setCellStyle(style_date);
			sheet.getRow(3).getCell(1).setCellStyle(style_date);
			sheet.getRow(4).getCell(1).setCellStyle(style_date);
			sheet.getRow(5).getCell(1).setCellStyle(style_date);
			
			//合并单元格
			//合并 标题
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
			//合并 供应商名称
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));
			//合并  订单详情
			sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 3));
		
		
		//销售单
		}else{
			//初始化row
			HSSFRow row=null;		
			//第二至六行
			for(int i=1;i<6;i++){
				row = sheet.createRow(i);
				row.setHeight((short) 500);
				for(int j=0;j<4;j++){
					row.createCell(j).setCellStyle(style_content);
					sheet.setColumnWidth(j, 5000);				
				}
			}
			//1
			row=sheet.createRow(0);
			row.setHeight((short) 1000);
			row.createCell(0).setCellStyle(style_title);
			//设置表单固定的内容
			row.getCell(0).setCellValue("销售单");
			
			//2
			sheet.getRow(1).getCell(0).setCellValue("客户");
			sheet.getRow(1).getCell(1).setCellValue(supplierDao.find(orders.getSupplieruuid()).getName());
			
			//3
			sheet.getRow(2).getCell(0).setCellValue("下单日期");
			if(orders.getCreatetime()!=null){
				sheet.getRow(2).getCell(1).setCellValue(orders.getCreatetime());
			}
			sheet.getRow(2).getCell(2).setCellValue("经办人");
			sheet.getRow(2).getCell(3).setCellValue(getEmpName(orders.getCreater(), map, empDao));
			
			//4
			sheet.getRow(3).getCell(0).setCellValue("出库日期");
			if(orders.getEndtime()!=null){
				sheet.getRow(3).getCell(1).setCellValue(orders.getEndtime());
			}
			sheet.getRow(3).getCell(2).setCellValue("经办人");
			sheet.getRow(3).getCell(3).setCellValue(getEmpName(orders.getEnder(), map, empDao));
			
			//5
			sheet.getRow(4).getCell(0).setCellValue("订单详情");
			
			//6
			sheet.getRow(5).getCell(0).setCellValue("商品名称");
			sheet.getRow(5).getCell(1).setCellValue("数量");
			sheet.getRow(5).getCell(2).setCellValue("价格");
			sheet.getRow(5).getCell(3).setCellValue("金额");
			rowcount=6;
			//设置日期样式
			sheet.getRow(2).getCell(1).setCellStyle(style_date);
			sheet.getRow(3).getCell(1).setCellStyle(style_date);
			
			//合并单元格
			//合并 标题
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
			//合并 客户名称
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));
			//合并  订单详情
			sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 3));
		}
		
		for(Orderdetail od:orders.getOrderdetails()){
			sheet.createRow(rowcount).createCell(0).setCellValue(od.getGoodsname());
			sheet.getRow(rowcount).createCell(1).setCellValue(od.getNum());
			sheet.getRow(rowcount).createCell(2).setCellValue(od.getPrice());
			sheet.getRow(rowcount).createCell(3).setCellValue(od.getMoney());
			sheet.getRow(rowcount).setHeight((short) 500);
			for(int i=0;i<4;i++){
				sheet.getRow(rowcount).getCell(i).setCellStyle(style_content);
			}
			rowcount++;
		}
		sheet.createRow(rowcount).createCell(0).setCellValue("合计");
		sheet.getRow(rowcount).setHeight((short) 500);
		sheet.getRow(rowcount).getCell(0).setCellStyle(style_content);
		sheet.getRow(rowcount).createCell(1).setCellValue(orders.getTotalmoney());
		sheet.getRow(rowcount).getCell(1).setCellStyle(style_content);
		sheet.getRow(rowcount).createCell(2).setCellStyle(style_content);
		sheet.getRow(rowcount).createCell(3).setCellStyle(style_content);
		//合并 合计
		sheet.addMergedRegion(new CellRangeAddress(rowcount, rowcount, 1, 3));
				
		try {
			wb.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
