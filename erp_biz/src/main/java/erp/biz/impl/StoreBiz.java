package erp.biz.impl;
import java.util.List;

import erp.biz.IStoreBiz;
import erp.dao.IStoreDao;
import erp.entity.Store;
/**
 * 仓库业务逻辑类
 * @author Administrator
 *
 */
public class StoreBiz extends BaseBiz<Store> implements IStoreBiz {

	private IStoreDao storeDao;
	
	public void setStoreDao(IStoreDao storeDao) {
		this.storeDao = storeDao;
		super.setBaseDao(this.storeDao);
	}

	@Override
	public List<Store> getList(Store s) {
		return storeDao.getList(s);
	}
	
	
}
