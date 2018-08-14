package erp.biz.impl;

import erp.biz.IDepBiz;
import erp.dao.IDepDao;
import erp.entity.Dep;

/**
 * 部门业务层
 * @author Administrator
 *
 */
public class DepBiz extends BaseBiz<Dep> implements IDepBiz{
	
	private IDepDao depDao;
	
	public void setDepDao(IDepDao depDao) {
		this.depDao = depDao;
		super.setBaseDao(this.depDao);
	}
}
