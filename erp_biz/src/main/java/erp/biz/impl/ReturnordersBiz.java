package erp.biz.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import erp.biz.IReturnordersBiz;
import erp.biz.exception.ERPException;
import erp.dao.IEmpDao;
import erp.dao.IReturnordersDao;
import erp.dao.ISupplierDao;
import erp.entity.Orders;
import erp.entity.Returnorderdetail;
import erp.entity.Returnorders;
/**
 * 退货订单业务逻辑类
 * @author Administrator
 *
 */
public class ReturnordersBiz extends BaseBiz<Returnorders> implements IReturnordersBiz {

	private IReturnordersDao returnordersDao;
	private IEmpDao empDao;
	private ISupplierDao supplierDao;
	
	public void setReturnordersDao(IReturnordersDao returnordersDao) {
		this.returnordersDao = returnordersDao;
		super.setBaseDao(this.returnordersDao);
	}
		
	public void setEmpDao(IEmpDao empDao) {
		this.empDao = empDao;
	}


	public void setSupplierDao(ISupplierDao supplierDao) {
		this.supplierDao = supplierDao;
	}


	/**
	 * 查询
	 */
	//条件查询
	public List<Returnorders> getList(Returnorders returnorders,Object params,int page,int rows){
		List<Returnorders> list=super.getList(returnorders, params, page, rows);
		Map<Long,String> empName=new HashMap<Long, String>();
		Map<Long,String> supplierName=new HashMap<Long, String>();
		for(Returnorders ro:list){
			ro.setCheckerName(getEmpName(ro.getChecker(), empName,empDao));
			ro.setCreaterName(getEmpName(ro.getCreater(), empName,empDao));
			ro.setEnderName(getEmpName(ro.getEnder(), empName,empDao));
			ro.setSupplierName(getSupplierName(ro.getSupplieruuid(), supplierName,supplierDao));
		}	
		return list;
	}
	/**
	 * 添加订单
	 */
	public void add(Returnorders ro){
		Subject subject=SecurityUtils.getSubject();
		if(ro.getType().equals(Returnorders.TYPE_IN)){
			if(!subject.isPermitted("采购退货申请")){
				throw new ERPException("权限不足");
			}
		}else if(ro.getType().equals(Returnorders.TYPE_OUT)){
			if(!subject.isPermitted("销售退货申请")){
				throw new ERPException("权限不足");
			}
		}else{
			throw new ERPException("权限不足");
		}
		
		ro.setState(Returnorders.STATE_CREATE);	
		ro.setCreatetime(new Date());
		double total=0d;
		String type=ro.getType();
		for(Returnorderdetail rod:ro.getReturnorderdetail()){
			total+=rod.getReturnmoney();
			if(type.equals(Returnorders.TYPE_IN)){
				rod.setState(Returnorderdetail.STATE_NOT_OUT);
			}else if(type.equals(Returnorders.TYPE_OUT)){
				rod.setState(Returnorderdetail.STATE_NOT_IN);
			}	
			rod.setReturnorders(ro);
		}	
		ro.setTotalmoney(total);
		returnordersDao.add(ro);
	}
	/**
	 * 审核
	 */
	@Override
	public void doCheck(Long roid, Long eid) {
		Returnorders ro=returnordersDao.find(roid);
		if(ro==null){
			throw new ERPException("订单信息错误");
		}
		if(!Returnorders.STATE_CREATE.equals(ro.getState())){
			throw new ERPException("订单不是未审核状态");
		}
		ro.setChecker(eid);
		ro.setChecktime(new Date());
		ro.setState(Orders.STATE_CHECK);		
	}
}
