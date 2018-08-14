package erp.biz;
import erp.entity.Returnorders;
/**
 * 退货订单业务逻辑层接口
 * @author Administrator
 *
 */
public interface IReturnordersBiz extends IBaseBiz<Returnorders>{
	//审核
	void doCheck(Long id, Long uuid);

}

