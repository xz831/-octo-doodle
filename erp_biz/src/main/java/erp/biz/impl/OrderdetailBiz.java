package erp.biz.impl;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.redsun.bos.ws.impl.IWaybillWs;

import erp.biz.IOrderdetailBiz;
import erp.biz.exception.ERPException;
import erp.dao.IOrderdetailDao;
import erp.dao.IStoredetailDao;
import erp.dao.IStoreoperDao;
import erp.dao.ISupplierDao;
import erp.entity.Orderdetail;
import erp.entity.Orders;
import erp.entity.Storedetail;
import erp.entity.Storeoper;
import erp.entity.Supplier;
/**
 * 订单明细业务逻辑类
 * @author Administrator
 *
 */
public class OrderdetailBiz extends BaseBiz<Orderdetail> implements IOrderdetailBiz {

	private IOrderdetailDao orderdetailDao;
	private IStoredetailDao storedetailDao;
	private IStoreoperDao storeoperDao;
	private IWaybillWs waybillWs;
	private ISupplierDao supplierDao;
	
	
	public void setOrderdetailDao(IOrderdetailDao orderdetailDao) {
		this.orderdetailDao = orderdetailDao;
		super.setBaseDao(this.orderdetailDao);
	}
	
	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
	}
	
	public void setStoreoperDao(IStoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
	}

	public void setWaybillWs(IWaybillWs waybillWs) {
		this.waybillWs = waybillWs;
	}

	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}

	/**
	 * 入库
	 * orderitemId 	订单详情ID
	 * storeId		仓库ID
	 * empId		员工ID
	 */
	@Override
	@RequiresPermissions("采购订单入库")
	public void doInStore(Long orderitemId, Long storeId, Long empId) {
		//1.订单项
		//入库时间，入库人员，仓库编号，修改状态
		Orderdetail od=orderdetailDao.find(orderitemId);
		if(!od.getState().equals(Orderdetail.STATE_NOT_IN)){
			throw new ERPException("该订单项已入库");
		}
		od.setEndtime(new Date());
		od.setEnder(empId);
		od.setStoreuuid(storeId);
		od.setState(Orderdetail.STATE_IN);
		//2.仓库
		//添加或者修改
		Storedetail sd=new Storedetail();
		sd.setGoodsuuid(od.getGoodsuuid());
		sd.setStoreuuid(storeId);
		List<Storedetail> list=storedetailDao.getList(sd);
		if(list.size()>0){
			list.get(0).setNum(list.get(0).getNum()+od.getNum());
		}else{
			sd.setNum(od.getNum());
			storedetailDao.add(sd);
		}
		//3.操作记录
		//记录这条数据
		Storeoper s=new Storeoper();
		s.setEmpuuid(empId);
		s.setGoodsuuid(od.getGoodsuuid());
		s.setNum(od.getNum());
		s.setOpertime(od.getEndtime());
		s.setStoreuuid(storeId);
		s.setType(Storeoper.TYPE_IN);
		storeoperDao.add(s);
		//4.订单
		//判断订单内的订单项是否都已入库，如果都已入库要修改订单
		Orderdetail temp=new Orderdetail();
		temp.setOrders(od.getOrders());
		temp.setState(Orderdetail.STATE_NOT_IN);
		int total=getTotal(temp, null);
		if(total==0){
			Orders o=od.getOrders();
			o.setEndtime(od.getEndtime());
			o.setState(Orders.STATE_END);
			o.setEnder(empId);
		}
	}
	/**
	 * 出库
	 */
	@Override
	@RequiresPermissions("销售订单出库")
	public void doOutStore(Long orderitemId, Long storeId, Long empId) {
		//1.订单项
		//出库时间，出库人员，仓库编号，修改状态
		Orderdetail od=orderdetailDao.find(orderitemId);
		if(!od.getState().equals(Orderdetail.STATE_NOT_OUT)){
			throw new ERPException("该订单项已出库");
		}
		od.setEndtime(new Date());
		od.setEnder(empId);
		od.setStoreuuid(storeId);
		od.setState(Orderdetail.STATE_OUT);
		//2.仓库
		//修改库存
		Storedetail sd=new Storedetail();
		sd.setGoodsuuid(od.getGoodsuuid());
		sd.setStoreuuid(storeId);
		List<Storedetail> list=storedetailDao.getList(sd);
		if(list.size()>0){
			long num=list.get(0).getNum()-od.getNum();
			if(num<0){
				throw new ERPException("库存不足");
			}else if(num==0){
				storedetailDao.delete(list.get(0).getUuid());
			}else{
				list.get(0).setNum(num);
			}		
		}else{
			throw new ERPException("该仓库中无此商品");
		}
		//3.操作记录
		//记录这条数据
		Storeoper s=new Storeoper();
		s.setEmpuuid(empId);
		s.setGoodsuuid(od.getGoodsuuid());
		s.setNum(od.getNum());
		s.setOpertime(od.getEndtime());
		s.setStoreuuid(storeId);
		s.setType(Storeoper.TYPE_OUT);
		storeoperDao.add(s);
		//4.订单
		//判断订单内的订单项是否都已出库，如果都已入库要修改订单
		Orderdetail temp=new Orderdetail();
		temp.setOrders(od.getOrders());
		temp.setState(Orderdetail.STATE_NOT_OUT);
		int total=getTotal(temp, null);
		if(total==0){
			Orders o=od.getOrders();
			o.setEndtime(od.getEndtime());
			o.setState(Orders.STATE_OUT);
			o.setEnder(empId);
			Supplier supplier=supplierDao.find(o.getSupplieruuid());
			Long sn=waybillWs.addWaybill(1l,supplier.getAddress(), supplier.getName(), supplier.getTele(), "无");
			o.setWaybillsn(sn);
		}
	}
}
