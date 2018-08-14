package erp.biz.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import erp.biz.IStoredetailBiz;
import erp.dao.IGoodsDao;
import erp.dao.IStoreDao;
import erp.dao.IStoredetailDao;
import erp.entity.Storedetail;
/**
 * 仓库库存业务逻辑类
 * @author Administrator
 *
 */
public class StoredetailBiz extends BaseBiz<Storedetail> implements IStoredetailBiz {

	private IStoredetailDao storedetailDao;
	private IGoodsDao goodsDao;
	private IStoreDao storeDao;
	
	public void setStoredetailDao(IStoredetailDao storedetailDao) {
		this.storedetailDao = storedetailDao;
		super.setBaseDao(this.storedetailDao);
	}
		
	public void setGoodsDao(IGoodsDao goodsDao) {
		this.goodsDao = goodsDao;
	}

	public void setStoreDao(IStoreDao storeDao) {
		this.storeDao = storeDao;
	}


	public List<Storedetail> getList(Storedetail storedetail,Object params,int page,int rows){
		List<Storedetail> list=super.getList(storedetail, params, page, rows);
		Map<Long,String> map1=new HashMap<Long, String>();
		Map<Long,String> map2=new HashMap<Long, String>();
		for(Storedetail s:list){	
			s.setGoodsname(getGoodsname(s.getGoodsuuid(), map1,goodsDao));
			s.setStorename(getStorename(s.getStoreuuid(), map2,storeDao));
		}	
		return list;
	};
	
	
}
