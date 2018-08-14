package erp.biz;
import java.io.OutputStream;
import java.util.List;

import erp.entity.Orders;
/**
 * 订单业务逻辑层接口
 * @author Administrator
 *
 */
public interface IOrdersBiz extends IBaseBiz<Orders>{
	//审核 oid订单ID，eid员工ID
	void doCheck(Long oid,Long eid);
	//确认
	void doStart(Long oid,Long eid);
	//退货订单查询
	List<Orders> listByReturn(Orders orders,Object params,int page,int rows);
	//导出
	void export(OutputStream os,Long uuid);
}

