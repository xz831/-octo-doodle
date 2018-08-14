package erp.biz;
import erp.entity.Returnorderdetail;
/**
 * 退货订单明细业务逻辑层接口
 * @author Administrator
 *
 */
public interface IReturnorderdetailBiz extends IBaseBiz<Returnorderdetail>{

	//出库
	void doOutStore(Long rodid, Long eid);
	//入库
	void doInStore(Long rodid, Long sid, Long eid);

}

