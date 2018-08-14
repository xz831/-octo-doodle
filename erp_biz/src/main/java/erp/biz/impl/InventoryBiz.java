package erp.biz.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import erp.biz.IInventoryBiz;
import erp.biz.exception.ERPException;
import erp.dao.IEmpDao;
import erp.dao.IGoodsDao;
import erp.dao.IInventoryDao;
import erp.dao.IStoreDao;
import erp.dao.IStoredetailDao;
import erp.dao.IStoreoperDao;
import erp.entity.Inventory;
import erp.entity.Storedetail;
import erp.entity.Storeoper;
/**
 * 盘盈盘亏业务逻辑类
 * @author Administrator
 *
 */
public class InventoryBiz extends BaseBiz<Inventory> implements IInventoryBiz {

	private IInventoryDao inventoryDao;
	private IEmpDao empDao;
	private IGoodsDao goodsDao;
	private IStoreDao storeDao;
	private IStoredetailDao storedetailDao;
	private IStoreoperDao storeoperDao;
	
	public void setInventoryDao(IInventoryDao inventoryDao) {
		this.inventoryDao = inventoryDao;
		super.setBaseDao(this.inventoryDao);
	}
	
	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}

	public void setGoodsDao(IGoodsDao goodsDao) {
		this.goodsDao = goodsDao;
	}

	public void setStoreDao(IStoreDao storeDao) {
		this.storeDao = storeDao;
	}

	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
	}
	
	public void setStoreoperDao(IStoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
	}

	/**
	 * 添加
	 */
	@Override
	public void add(List<Inventory> list, Long uuid) {
		Subject subject=SecurityUtils.getSubject();
		if(list.get(0).getType().equals(Inventory.TYPE_LESS)){
			if(!subject.isPermitted("盘亏申请")){
				throw new ERPException("权限不足");
			}
		}else if(list.get(0).getType().equals(Inventory.TYPE_MORE)){
			if(!subject.isPermitted("盘盈申请")){
				throw new ERPException("权限不足");
			}
		}else{
			throw new ERPException("非法参数");
		}
		Date d=new Date();
		for(Inventory i:list){			
			i.setState(Inventory.STATE_CREATE);
			i.setCreater(uuid);
			i.setCreatetime(d);
			inventoryDao.add(i);
		}
	}
	/**
	 * 重写查询
	 * @param t
	 * @param params
	 * @param page
	 * @param rows
	 * @return
	 */
	public List<Inventory> getList(Inventory inventory,Object params,int page,int rows){
		List<Inventory> list=super.getList(inventory, params, page, rows);
		//存姓名
		Map<Long,String> map1=new HashMap<Long, String>();
		//存商品名
		Map<Long,String> map2=new HashMap<Long, String>();
		//存仓库名
		Map<Long,String> map3=new HashMap<Long, String>();
		for(Inventory i: list){
			i.setCheckername(getEmpName(i.getChecker(), map1, empDao));
			i.setCreatername(getEmpName(i.getCreater(), map1, empDao));
			i.setGoodsname(getGoodsname(i.getGoodsuuid(), map2, goodsDao));
			i.setStorename(getStorename(i.getStoreuuid(), map3, storeDao));
		}
		return list;
	}
	/**
	 * 审核
	 */
	@Override
	public void doCheck(long id, Long eid) {
		/**盘盈盘亏*/
		Inventory i=inventoryDao.find(id);
		Subject subject =SecurityUtils.getSubject();
		if(i.getType().equals(Inventory.TYPE_LESS)){
			if(!subject.isPermitted("盘亏审核")){
				throw new ERPException("权限不足");
			}
		}else{
			if(!subject.isPermitted("盘盈审核")){
				throw new ERPException("权限不足");
			}
		}
		
		if(!i.getState().equals(Inventory.STATE_CREATE)){
			throw new ERPException("该项不是未审核状态");
		}
		i.setChecker(eid);
		i.setChecktime(new Date());
		i.setState(Inventory.STATE_CHECK);
		/**仓库*/
		Storedetail sd=new Storedetail();
		sd.setGoodsuuid(i.getGoodsuuid());
		sd.setStoreuuid(i.getStoreuuid());
		List<Storedetail> list=storedetailDao.getList(sd);
		//盘盈 加库存
		if(i.getType().equals(Inventory.TYPE_MORE)){
			//没有
			if(list.size()==0){
				sd.setNum(i.getNum());
				storedetailDao.add(sd);
			//有
			}else{
				list.get(0).setNum(i.getNum()+list.get(0).getNum());
			}	
		//盘亏 减库存
		}else{
			//没有
			if(list.size()==0){
				throw new ERPException("该仓库中无此商品");
			//有
			}else{
				Long num=list.get(0).getNum()-i.getNum();
				if(num<0){
					throw new ERPException("盈亏后库存量小于0");
				}else if(num==0){
					storedetailDao.delete(list.get(0).getUuid());
				}else{
					list.get(0).setNum(num);
				}
			}
		}
		/**库存操作*/
		Storeoper so=new Storeoper();
		so.setEmpuuid(i.getCreater());
		so.setGoodsuuid(i.getGoodsuuid());
		so.setNum(i.getNum());
		so.setOpertime(i.getChecktime());
		so.setStoreuuid(i.getStoreuuid());
		//盘盈
		if(i.getType().equals(Inventory.TYPE_MORE)){
			so.setType(Storeoper.TYPE_MORE);
		//盘亏
		}else{
			so.setType(Storeoper.TYPE_LESS);
		}
		storeoperDao.add(so);
	
	}
	
}
