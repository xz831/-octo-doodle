package erp.biz;
import erp.entity.Orderdetail;
/**
 * 订单明细业务逻辑层接口
 * @author Administrator
 *
 */
public interface IOrderdetailBiz extends IBaseBiz<Orderdetail>{
	/**
	 * 入库
	 * @param orderitemId
	 * @param storeId
	 * @param empId
	 */
	void doInStore(Long orderitemId,Long storeId,Long empId);
	/**
	 * 出库
	 * @param orderitemId
	 * @param storeId
	 * @param empId
	 */
	void doOutStore(Long orderitemId, Long storeId, Long empId);
}

