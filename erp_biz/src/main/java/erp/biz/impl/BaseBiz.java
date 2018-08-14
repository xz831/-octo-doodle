package erp.biz.impl;

import java.util.List;
import java.util.Map;

import erp.biz.IBaseBiz;
import erp.dao.IBaseDao;
import erp.dao.IEmpDao;
import erp.dao.IGoodsDao;
import erp.dao.IStoreDao;
import erp.dao.ISupplierDao;
import erp.entity.Emp;
import erp.entity.Goods;
import erp.entity.Store;
import erp.entity.Supplier;
/**
 * 公共业务层
 * @author Administrator
 *
 * @param <T>
 */
public class BaseBiz<T> implements IBaseBiz<T>{
	
	private IBaseDao<T> baseDao;
	
	public void setBaseDao(IBaseDao<T> baseDao) {
		this.baseDao = baseDao;
	}
	/**
	 * 获取所有部门
	 */
	@Override
	public List<T> getList() {
		return baseDao.getList();
	}
	/**
	 * 条件查询部门
	 */
	@Override
	public List<T> getList(T t,Object params,int page,int rows) {
		return baseDao.getList(t,params,page,rows);
	}
	/**
	 * 查询部门总数
	 */
	@Override
	public int getTotal(T t,Object params) {
		return baseDao.getTotal(t,params);
	}
	/**
	 * 添加部门
	 */
	@Override
	public void add(T t) {
		baseDao.add(t);
	}
	/**
	 * 删除部门
	 */
	@Override
	public void delete(long id) {
		baseDao.delete(id);
	}
	/**
	 * ID查找部门
	 * @return 
	 * @throws Exception 
	 */
	@Override
	public T find(Long id){
		return baseDao.find(id);
	}
	@Override
	public T find(String id){
		return baseDao.find(id);
	}
	/**
	 * 更新
	 * @param dep
	 */
	@Override
	public void update(T t) {
		baseDao.update(t);
	}
	/**
	 * 获取员工姓名
	 * @param id
	 * @param map
	 * @return
	 */
	public String getEmpName(Long id,Map<Long,String> map,IEmpDao empDao){
		if(id==null){
			return null;
		}
		String name=map.get(id);
		if(name==null){
			Emp emp=empDao.find(id);
			if(emp==null){
				return null;
			}
			name=emp.getName();
			map.put(id, name);
		}
		return name;
	}
	/**
	 * 获取供应或客户名称
	 * @param id
	 * @param map
	 * @return
	 */
	public String getSupplierName(Long id,Map<Long,String> map,ISupplierDao supplierDao){
		if(id==null){
			return null;
		}
		String name=map.get(id);
		if(name==null){
			Supplier s=supplierDao.find(id);
			if(s==null){
				return null;
			}
			name=s.getName();
			map.put(id, name);
		}
		return name;
	}
	/**
	 * 获取商品名称
	 * @param uuid
	 * @param map
	 * @return
	 */
	public String getGoodsname(Long uuid,Map<Long,String> map,IGoodsDao goodsDao){
		if(uuid==null){
			return null;
		}
		String name=map.get(uuid);
		if(name==null){
			Goods goods=goodsDao.find(uuid);
			if(goods==null){
				return null;
			}
			name=goods.getName();
			map.put(uuid, name);
		}	
		return name;
	}
	/**
	 * 获取仓库名称
	 * @param uuid
	 * @param map
	 * @return
	 */
	public String getStorename(Long uuid,Map<Long,String> map,IStoreDao storeDao){
		if(uuid==null){
			return null;
		}
		String name=map.get(uuid);
		if(name==null){
			Store store=storeDao.find(uuid);
			if(store==null){
				return null;
			}
			name=store.getName();
			map.put(uuid, name);
		}	
		return name;
	}
}
