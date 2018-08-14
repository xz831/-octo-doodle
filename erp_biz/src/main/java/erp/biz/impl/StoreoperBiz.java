package erp.biz.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import erp.biz.IStoreoperBiz;
import erp.dao.IEmpDao;
import erp.dao.IGoodsDao;
import erp.dao.IStoreDao;
import erp.dao.IStoreoperDao;
import erp.entity.Storeoper;
/**
 * 仓库操作记录业务逻辑类
 * @author Administrator
 *
 */
public class StoreoperBiz extends BaseBiz<Storeoper> implements IStoreoperBiz {

	private IStoreoperDao storeoperDao;
	private IEmpDao empDao;
	private IStoreDao storeDao;
	private IGoodsDao goodsDao;
	
	public void setStoreoperDao(IStoreoperDao storeoperDao) {
		this.storeoperDao = storeoperDao;
		super.setBaseDao(this.storeoperDao);
	}
	
	
	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}


	public void setStoreDao(IStoreDao storeDao) {
		this.storeDao = storeDao;
	}


	public void setGoodsDao(IGoodsDao goodsDao) {
		this.goodsDao = goodsDao;
	}


	public List<Storeoper> getList(Storeoper storeoper,Object params,int page,int rows){
		List<Storeoper> list=storeoperDao.getList(storeoper, params, page, rows);
		Map<Long,String> map1=new HashMap<Long, String>();
		Map<Long,String> map2=new HashMap<Long, String>();
		Map<Long,String> map3=new HashMap<Long, String>();
		for(Storeoper s:list){
			s.setEmpname(getEmpName(s.getEmpuuid(), map1, empDao));
			s.setGoodsname(getGoodsname(s.getGoodsuuid(), map2, goodsDao));
			s.setStorename(getStorename(s.getStoreuuid(), map3, storeDao));
		}
		return list;
	};
	
}
