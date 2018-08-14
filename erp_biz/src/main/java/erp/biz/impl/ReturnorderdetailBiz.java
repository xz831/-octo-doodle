package erp.biz.impl;
import java.util.Date;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import erp.biz.IReturnorderdetailBiz;
import erp.biz.exception.ERPException;
import erp.dao.IOrdersDao;
import erp.dao.IReturnorderdetailDao;
import erp.dao.IStoreDao;
import erp.dao.IStoredetailDao;
import erp.dao.IStoreoperDao;
import erp.entity.Orderdetail;
import erp.entity.Orders;
import erp.entity.Returnorderdetail;
import erp.entity.Returnorders;
import erp.entity.Store;
import erp.entity.Storedetail;
import erp.entity.Storeoper;
/**
 * 退货订单明细业务逻辑类
 * @author Administrator
 *
 */
public class ReturnorderdetailBiz extends BaseBiz<Returnorderdetail> implements IReturnorderdetailBiz {

	private IReturnorderdetailDao returnorderdetailDao;
	private IOrdersDao ordersDao;
	private IStoredetailDao storedetailDao;
	private IStoreoperDao storeoperDao;
	private IStoreDao storeDao;
	
	public void setReturnorderdetailDao(IReturnorderdetailDao returnorderdetailDao) {
		this.returnorderdetailDao = returnorderdetailDao;
		super.setBaseDao(this.returnorderdetailDao);
	}
	
	public void setOrdersDao(IOrdersDao ordersDao) {
		this.ordersDao = ordersDao;
	}
	
	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
	}
	
	public void setStoreoperDao(IStoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
	}
	
	public void setStoreDao(IStoreDao storeDao) {
		this.storeDao = storeDao;
	}

	/**
	 * 出库
	 */
	@Override
	@RequiresPermissions("采购退货出库")
	public void doOutStore(Long rodid, Long eid) {
		/** 退单项的操作*/
		//退单项
		Returnorderdetail rod= returnorderdetailDao.find(rodid);
		//判断是否出库
		if(!rod.getState().equals(Returnorderdetail.STATE_NOT_OUT)){
			throw new ERPException("该商品已出库");
		}
		//退单
		Returnorders ro= rod.getReturnorders();
		//订单
		Orders o=ordersDao.find(ro.getOrdersuuid());
		//订单项
		List<Orderdetail> list=o.getOrderdetails();
		Long storeuuid=-1L;
		//从订单项中寻找对应退货项的
		for(Orderdetail od:list){
			if(od.getGoodsuuid()==rod.getGoodsuuid()){
				storeuuid=od.getStoreuuid();
				break;
			}
		}
		//没有找到
		if(storeuuid==-1L){
			throw new ERPException("该退货项没有对应订单项");
		}
		//判断出库权限
		Store store=storeDao.find(storeuuid);
		if(store.getEmpuuid()!=eid){
			throw new ERPException("没有出库权限");
		}
		
		rod.setEnder(eid);
		rod.setEndtime(new Date());
		rod.setStoreuuid(storeuuid);
		rod.setState(Returnorderdetail.STATE_OUT);
		/**仓库 */
		Storedetail sd=new Storedetail();
		sd.setGoodsuuid(rod.getGoodsuuid());
		sd.setStoreuuid(storeuuid);
		List<Storedetail> list2=storedetailDao.getList(sd);
		//没有
		if(list2.size()==0){
			throw new ERPException("仓库中没有该商品");
		}
		//查找得到的仓库详情
		Storedetail temp=list2.get(0);
		Long num=temp.getNum()-rod.getReturnnum();
		if(num<0){
			throw new ERPException("库存不足");
		}else if(num==0){
			storedetailDao.delete(temp.getUuid());
		}else{
			temp.setNum(num);
		}
		/**仓库操作*/
		Storeoper s=new Storeoper();
		s.setEmpuuid(eid);
		s.setGoodsuuid(rod.getGoodsuuid());
		s.setNum(rod.getReturnnum());
		s.setOpertime(rod.getEndtime());
		s.setStoreuuid(rod.getStoreuuid());
		s.setType(Storeoper.TYPE_OUT);
		storeoperDao.add(s);
		/**退货订单操作*/
		Returnorderdetail temp1=new Returnorderdetail();
		temp1.setReturnorders(rod.getReturnorders());
		temp1.setState(Returnorderdetail.STATE_NOT_OUT);
		int total=getTotal(temp1, null);
		if(total==0){
			ro.setEndtime(rod.getEndtime());
			ro.setState(Returnorders.STATE_END);
			ro.setEnder(eid);
		}
	}
	/**
	 * 入库
	 */
	@Override
	@RequiresPermissions("销售退货入库")
	public void doInStore(Long rodid, Long sid, Long eid) {
		/** 退单项的操作*/
		//退单项
		Returnorderdetail rod= returnorderdetailDao.find(rodid);
		if(!rod.getState().equals(Returnorderdetail.STATE_NOT_IN)){
			throw new ERPException("该商品已入库");
		}
		rod.setEnder(eid);
		rod.setEndtime(new Date());
		rod.setStoreuuid(sid);
		rod.setState(Returnorderdetail.STATE_OUT);
		/**仓库 */
		Storedetail sd=new Storedetail();
		sd.setGoodsuuid(rod.getGoodsuuid());
		sd.setStoreuuid(sid);
		List<Storedetail> list=storedetailDao.getList(sd);
		//仓库中没有
		if(list.size()==0){
			sd.setNum(rod.getReturnnum());
			storedetailDao.add(sd);		
		//仓库中有
		}else{
			list.get(0).setNum(rod.getReturnnum()+list.get(0).getNum());			
		}
		/**仓库操作*/
		Storeoper s=new Storeoper();
		s.setEmpuuid(eid);
		s.setGoodsuuid(rod.getGoodsuuid());
		s.setNum(rod.getReturnnum());
		s.setOpertime(rod.getEndtime());
		s.setStoreuuid(rod.getStoreuuid());
		s.setType(Storeoper.TYPE_IN);
		storeoperDao.add(s);
		/**退货订单操作*/
		Returnorderdetail temp=new Returnorderdetail();
		Returnorders ro=rod.getReturnorders();
		temp.setReturnorders(ro);
		temp.setState(Returnorderdetail.STATE_NOT_IN);
		int total=getTotal(temp, null);
		if(total==0){			
			ro.setEndtime(rod.getEndtime());
			ro.setState(Returnorders.STATE_END);
			ro.setEnder(eid);
		}		
	}
}
