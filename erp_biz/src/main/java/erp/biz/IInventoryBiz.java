package erp.biz;
import java.util.List;

import erp.entity.Inventory;
/**
 * 盘盈盘亏业务逻辑层接口
 * @author Administrator
 *
 */
public interface IInventoryBiz extends IBaseBiz<Inventory>{
	//添加
	void add(List<Inventory> list, Long uuid);
	//审核
	void doCheck(long id, Long uuid);

}

